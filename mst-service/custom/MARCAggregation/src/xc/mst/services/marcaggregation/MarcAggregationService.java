/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */
package xc.mst.services.marcaggregation;

import gnu.trove.TLongLongHashMap;
import gnu.trove.TLongObjectHashMap;

import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import xc.mst.bo.provider.Format;
import xc.mst.bo.record.InputRecord;
import xc.mst.bo.record.OutputRecord;
import xc.mst.bo.record.Record;
import xc.mst.bo.record.RecordMessage;
import xc.mst.bo.record.SaxMarcXmlRecord;
import xc.mst.bo.record.marc.Field;
import xc.mst.constants.Status;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.repo.Repository;
import xc.mst.services.ServiceValidationException;
import xc.mst.services.impl.service.GenericMetadataService;
import xc.mst.services.marcaggregation.dao.MarcAggregationServiceDAO;
import xc.mst.services.marcaggregation.matcher.FieldMatcher;
import xc.mst.services.marcaggregation.matcher.MatchSet;
import xc.mst.services.marcaggregation.matchrules.MatchRuleIfc;
import xc.mst.utils.LogWriter;
import xc.mst.utils.MSTConfiguration;
import xc.mst.utils.TimingLogger;

/**
 * @author Benjamin D. Anderson
 * @author John Brand
 *
 */
public class MarcAggregationService extends GenericMetadataService {

    /**
     * The output format (marcxml) for records processed from this service
     */
    protected Format                                 marc21 = null;
    protected Map<String, FieldMatcher>              matcherMap = null;
    protected Map<String, MatchRuleIfc>              matchRuleMap = null;
    protected MarcAggregationServiceDAO              masDAO = null;
    protected TLongObjectHashMap<RecordOfSourceData> scores = null;
    protected List<TreeSet<Long>>                    masMatchSetList = null;

    // map output records to corresponding input records map
    //   not only tracked merged records, 1 to many, but track unmerged 1 to 1
    protected Map<Long, TreeSet<Long>>               mergedRecordsO2Imap = null;

    // map input records to corresponding output map,
    //   not only tracked merged records, many to 1, but track unmerged 1 to 1
    protected TLongLongHashMap                       mergedRecordsI2Omap = null;

//    protected final XmlHelper xmlHelper = new XmlHelper();

    private List<Character> leaderVals = null;
    private Repository      inputRepo  = null;

    private boolean leader_byte17_weighting_enabled;
    private boolean bigger_record_weighting_enabled;

    private Transformer staticTransformer;
    private Transformer holdingTransformer;

    private static final String STATIC_TRANSFORM  = "createStatic.xsl";
    private static final String HOLDING_TRANSFORM = "stripHolding.xsl";
    private static final Logger LOG               = Logger.getLogger(MarcAggregationService.class);


    public void setup() {
        LOG.debug("MAS:  setup()");

        try {
            marc21 = getFormatService().getFormatByName("marc21");
        } catch (DatabaseConfigException e) {
            LOG.error("Could not connect to the database with the parameters in the configuration file.", e);
        } catch (Exception e2) {
            LOG.error("Problem with init.", e2);
        }

        setupRecordOfSource();
        staticTransformer = setupTransformer(getTransformForStaticFilename());

        final boolean transformHolding = false;
        if (transformHolding) {
            holdingTransformer= setupTransformer(getTransformForHoldingFilename());
        }
        try {
            validateService();
        } catch (ServiceValidationException e) {

            // Update database with status of service
            service.setStatus(Status.ERROR);
            LOG.error("Error validating service:", e);
            LogWriter.addInfo(service.getServicesLogFileName(), "** Error validating service - service will not run " + e.getMessage() + " **");
            sendReportEmail("Error validating service: " + e.getMessage());

            // in case the WorkerThread code addition causes issues, simply uncomment the below:
            // throw new RuntimeException(e);
        }
        setupMatchers();
        setupMatchRules();
        mergedRecordsI2Omap = loadMasMergedRecords();
        mergedRecordsO2Imap = createMergedRecordsO2Imap(mergedRecordsI2Omap);
    }

    // map output records to corresponding input records map
    //   there is probably a lot slicker way to do this.
    private Map<Long, TreeSet<Long>> createMergedRecordsO2Imap(TLongLongHashMap i_to_o_map) {
        TreeMap<Long,TreeSet<Long>> results = new TreeMap<Long, TreeSet<Long>>();
        for (Long out: i_to_o_map.getValues()) {
            if (!results.containsKey(out)) {
                List<Long> vals = masDAO.getInputRecordsMergedToOutputRecord(out);
                TreeSet<Long> set = new TreeSet<Long>();
                for (Long val: vals) {
                    set.add(val);
                }
                results.put(out, set);
            }
        }
        return results;
    }

    private TLongLongHashMap loadMasMergedRecords() {
        return masDAO.getMergedRecords();
    }

    protected Transformer setupTransformer(String xslFileName) throws TransformerFactoryConfigurationError {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        xslFileName = MSTConfiguration.getInstance().getServicePath() + service.getName() + "/xsl/" + xslFileName;
        try {
            return transformerFactory.newTransformer(new StreamSource(new FileInputStream(xslFileName)));
        } catch (Throwable t) {
            LOG.error("", t);
            throw new RuntimeException(t);
        }
    }

    protected void setupMatchRules() {
        this.matchRuleMap = new HashMap<String, MatchRuleIfc>();
        List<String> mrs = getConfigFileValues("match.rules.value");
        for (String mrStr : mrs) {
            MatchRuleIfc mr = (MatchRuleIfc) config.getBean(mrStr + "MatchRule");
            matchRuleMap.put(mrStr, mr);
        }
        if (this.masDAO == null) {  // this was really an initial unit test
            LOG.error("***  ERROR, DAO did not get initialized by Spring!");
        }
        masMatchSetList = new ArrayList<TreeSet<Long>>();
        scores = new TLongObjectHashMap<RecordOfSourceData>();
    }

    protected void setupMatchers() {
        this.matcherMap = new HashMap<String, FieldMatcher>();
        List<String> mps = getConfigFileValues("matchers.value");
        for (String mp : mps) {
            final String n = mp + "Matcher";
            FieldMatcher m = (FieldMatcher) config.getBean(n);
            m.setName(n);
            m.setMarcAggregationService(this);
            matcherMap.put(mp, m);
            m.load();
        }
    }

    protected void setupRecordOfSource() {
        // determine record of source leader character priority, byte 17
        leaderVals = new ArrayList<Character>();
        List<String> _leaderVals = getConfigFileValues("leader.order");
        for (String val: _leaderVals) {
            LOG.debug("Leader val==>"+val+"<== val length="+val.length());
            if (val.length() == 3) {
                leaderVals.add(val.charAt(1));  // char between quotes
            }
            else {
                leaderVals.add(val.charAt(0));
            }
        }
        leader_byte17_weighting_enabled= config.getPropertyAsBoolean("leader_byte17_weighting_enabled", false);
        bigger_record_weighting_enabled= config.getPropertyAsBoolean("bigger_record_weighting_enabled", false);
    }

    @Override
    protected void validateService() throws ServiceValidationException {
        if (!leader_byte17_weighting_enabled && !bigger_record_weighting_enabled) {
            throw new ServiceValidationException("Service configuration file invalid: leader_byte17_weighting_enabled & bigger_record_weighting_enabled cannot both be disabled!");
        }
    }

    // http://stackoverflow.com/questions/367626/how-do-i-fix-the-expression-of-type-list-needs-unchecked-conversion
    // TODO move this UP to common utilities.
    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
        List<T> r = new ArrayList<T>(c.size());
        for (Object o : c)
            r.add(clazz.cast(o));
        return r;
    }

    // this is just for what we have so far, not meant to always be up to date, i.e. it doesn't get
    // started off from looking at existing merged stuff in the database.  Based on the current record
    // that comes in, see what it matches, and go from there.
    public List<TreeSet<Long>> getCurrentMatchSetList() {
        return masMatchSetList;
    }

    // need to look to see if the given match set impacts existing sets.  i.e if this set  is {1,47,50}
    // and we have existing sets {1,3} and {4,47} then we need a superset: {1,3,4,47,50} and need to
    // remove the existing sets {1,3}, {4,47}
    //
    // disjoint-set data structure?
    //
    private List<TreeSet<Long>> addToMatchSetList(TreeSet<Long> matchset,  final List<TreeSet<Long>> origMasMatchSetList) {
        if (matchset==null) {
            return origMasMatchSetList;
        }
        if (matchset.size() < 1) {
            return origMasMatchSetList;
        }

        LOG.debug("** addToMatchSetList, matchset length="+matchset.size()+" TOTAL matchset size ="+origMasMatchSetList.size());
        List<TreeSet<Long>> newMasMatchSetList = new ArrayList<TreeSet<Long>>();
        newMasMatchSetList.addAll(origMasMatchSetList);

        boolean added = false;
        for (TreeSet<Long> set: origMasMatchSetList) {
            for (Long number: matchset) {
                if (set.contains(number)) {
                    if (!set.containsAll(matchset)) {
                        newMasMatchSetList.remove(set);
                        set.addAll(matchset);
                        newMasMatchSetList.add(set);
                        LOG.debug("addToMatchSetList, post-merge!  set.contains("+number+") merged newMasMatchSetList set="+set);
                    }
                    else {
                        LOG.debug("addToMatchSetList, will not add in: "+matchset);
                    }
                    added = true;  // this flag means that we don't want this set added to the big list below
                    break;   // get you out of THIS set, but still must check the others.
                }
            }
        }
        //
        // the list of sets has to start somewhere, and if you don't find a set including some part
        // of your set, you must add your set explicitly.
        //
        if (!added) {
            LOG.debug("must add in: "+matchset);
            newMasMatchSetList.add(matchset);
        }
        LOG.debug("** addToMatchSetList, NEW TOTAL matchset size ="+newMasMatchSetList.size());
        return newMasMatchSetList;

    }

    private List<String> getConfigFileValues(String name) {
        try {
            // there is probably a more righteous way to grab the service name.
            final PropertiesConfiguration props = new PropertiesConfiguration(MSTConfiguration.getUrlPath() + "/services/" + getUtil().normalizeName("MARCAggregation") +
                    "/META-INF/classes/xc/mst/services/custom.properties");

            final List<String> values = castList(String.class, props.getList(name));
            return values;
        } catch (Exception e) {
            LOG.error("Error loading custom.properties for service: " + this.getServiceName(), e);
            return null;
        }
    }

    // for spring to inject.
    public void setMarcAggregationServiceDAO(MarcAggregationServiceDAO masDAO) {
        this.masDAO = masDAO;
    }

    public MarcAggregationServiceDAO getMarcAggregationServiceDAO() {
        return this.masDAO;
    }

    // wrap it.
    //  (to increase accessibility - classes like Matcher/MatchRules that aren't subclasses may need it.)
    public void addMessage(InputRecord record, int code, char level) {
        try {
            // originally had to grab service as below but that was a workaround that didn't help ultimately.
            //
            //final Service service= getServicesService().getServiceByName("MARCAggregation");
            super.addMessage(record, code, level, null, getService());
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    // in case we use the model of merging AFTER all records initially seen.
    private void mergeAll(List<TreeSet<Long>> matches, Repository repo) {
        // merge each match set, 1 winning record used to pull static content,
        // all in the set used to pull dynamic content

        for (Set<Long> set: matches) {
            mergeBibSet(set, repo);
        }
    }


    //createStatic => strip 001/003/035,  create 035, save 035 (as dynamic)
    //   returns static xml + saved dynamic content (included or not?)
    //
    private List<OutputRecord> mergeBibSet(/*InputRecord inRecord, */ Set<Long> set, Repository repo) {
        Long recordOfSource = determineRecordOfSource(set);
        LOG.info("**** Record of Source == "+recordOfSource);

        String oaiXml = repo.getRecord(recordOfSource).getOaiXml();
        //SaxMarcXmlRecord smr = new SaxMarcXmlRecord(oaiXml);

        Map<Integer, Set<MarcDatafieldHolder>> dynamic = getDynamicContent(recordOfSource, repo, set);

        oaiXml = getStaticBase(oaiXml);
        // this would be a lot of data in the log.
        //
        //LOG.debug("STATIC-"+recordOfSource);
        //LOG.debug(oaiXml);

        oaiXml = updateDynamicRecordWithStaticContent(oaiXml, dynamic);
        //LOG.info("STATIC-"+recordOfSource);
        //LOG.info(oaiXml);

        // TODO q and a:
        // do I need to reconstitute all the records of the match set to setup pred/succ correctly?
        // do I need to provide a  list of these full records to the createNewRecord method?
        List<OutputRecord> list = createNewRecord(repo.getRecord(recordOfSource), "b", oaiXml);
        return list;
        return list;
    }

    /**
     * we KNOW what data fields we will be getting,
     * so specifically look for those in the desired order, and build the block of dynamic data.
     *
     * @param oaiXml
     * @param dynamic
     * @return
     */
    private String updateDynamicRecordWithStaticContent(String oaiXml, Map<Integer, Set<MarcDatafieldHolder>> dynamic) {
        String dynData = getDynamicDataBlock(dynamic);
        //  LOG.info("DYNAMIC DATA:");
        //  LOG.info(dynData);

        // now insert the dynamic block into the correct spot in oaiXml,
        // it goes after the 008!
        final String regex = "controlfield tag=\"008\".*/marc:controlfield>";
        oaiXml=insertDynamic(oaiXml, dynData, regex);
        //LOG.info("DATA WITH DYNAMIC DATA:");
        //LOG.info(oaiXml);
        return oaiXml;
    }

    /**
     * Use a regular expression to search for a pattern.  Insert 'dynamic' right after the end
     * of the found match. If match not found insert at end.
     */
    private String insertDynamic(String inputXml, String dynamic, String regex) {
        //
        // Create a Pattern instance
        //
        Pattern pattern = Pattern.compile(regex);

        //
        // Create matcher object
        //
        Matcher matcher = pattern.matcher(inputXml);
        StringBuffer sb = new StringBuffer(inputXml);

        //
        // Find where to place the text that match the pattern (at end of pattern matching text)
        //
        int end = -1;
        if (matcher.find()) {
            end = matcher.end();
            sb.insert(end, dynamic);
        }
        else {
            LOG.error("*** Could not find controlfield tag=\"008\".  Placed dynamic data at end of record!");
            sb.append(dynamic);
        }
        return sb.toString();
    }

    /**
     * we KNOW what data fields we will be getting, 10,20,22,24,35.
     * so specifically look for those in the desired order, and build the block of dynamic data.
     *
     */
    private String getDynamicDataBlock(Map<Integer, Set<MarcDatafieldHolder>> dynamic) {

        StringBuilder results = new StringBuilder(System.getProperty("line.separator"));
        results.append(getDynamicPiece(dynamic.get(10)));
        results.append(getDynamicPiece(dynamic.get(20)));
        results.append(getDynamicPiece(dynamic.get(22)));
        results.append(getDynamicPiece(dynamic.get(24)));
        results.append(getDynamicPiece(dynamic.get(35)));

        return results.toString();
    }

    /**
     * iterate over the set of found fields.  For each field in the set, append the xml string
     * constructed to represent it.
     *
     * @param dynamic the set of encapsulated data field objects
     * @return
     */
    private String getDynamicPiece(Set<MarcDatafieldHolder> dynamic) {

        Iterator<MarcDatafieldHolder> _i = dynamic.iterator();
        StringBuilder results = new StringBuilder();

        while (_i.hasNext()) {
            results.append(_i.next().toString());
        }

        return results.toString();
    }

    /**
     * transform the given xml by stripping 001's,003's, 010,020's,022's,024's & 035's
     * using an xsl to do the transformation.
     * The 035 fields are stripped then added back in because we only want well-formed 035's with prefix.
     * Contrast this to other keep fields that we may leave in the record of source.
     *
     * @param oaiXml
     * @return
     */
    private String getStaticBase(String oaiXml) {
        return getTransformedXml(oaiXml, staticTransformer);
    }

    /**
     * transform the given xml by stripping 004's & 014's
     * using an xsl to do the transformation.
     *
     *  This is in prep for adding in fields based on 004/014 later
     *  (so need to saved that info before now! )
     *
     * Turns out we DO NOT strip 004/014's.  Leaving this as a placeholder
     * in case it turns out later we do need to transform the holding record
     * in some way.
     *
     * @param oaiXml
     * @return
     */
    private String getHoldingBase(String oaiXml) {
        return getTransformedXml(oaiXml, holdingTransformer);
    }

    /**
     * run a sax transformation
     * @param oaiXml
     * @param transformer
     * @return
     */
    private String getTransformedXml(String oaiXml, Transformer transformer) {
        try {
            // Use the parser as a SAX source for input
            MASSaxMarcXmlRecord record = new MASSaxMarcXmlRecord(oaiXml);
            InputSource inputSource = new InputSource(new StringReader(oaiXml));

            SAXSource source = new SAXSource(record.getXmlReader(), inputSource);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);

            oaiXml = sw.getBuffer().toString();
            return  oaiXml;
        } catch (Throwable t) {
            LOG.error("", t);
        }
        return  oaiXml;
    }

    protected String getTransformForStaticFilename() {
        return STATIC_TRANSFORM;
    }


    protected String getTransformForHoldingFilename() {
        return HOLDING_TRANSFORM;
    }

    /*
    //getDynamic => create 035 from 001/003, save existing 035's, save existing 010,020,022,024
    //   returns dynamic content
    //
    // dynamic:
    // record_id ->  {{035$a list}, {010 list}, etc.}
    //
    // need to pass to a method that gets static content and dynamic content and builds a list of it.
      Grab the 5 fields below for all of the records that match,
        then dedup  fields with identical content.

        035- just $a, and only when the field is properly formatted with a prefix in parens followed by the number
        010 � just $a  (1 and only 1 allowed, prefer the record of source version, else grab any other found in the match set)
        020 � just $a (which may contain more than just the ISBN � it may contain additional text, like � (v. 1)� or �paperback�)
        022 � In addition to $a, also $ l [lowercase L], $m, and $z.
              The rationale here is that rather than lumping �invalid� and �incorrect� into the $z,
              in this case the �invalid� ones have their own subfield codes.
              These may be useful in some cases.  Also, a new kind of ISSN, called the �Linking ISSN� has just been defined in $l,
              and we may want to do something more with that someday so we need to keep it in the output record.
        024 � just $a.

        Copy all of this to the output record even when not from the record of source.

        By using TreeSet to hold the data, and with an appropriate 'Comparable' for each MarcDatafieldHolder 'subtype', can easily
        and effectively dedup the data.
     */
    protected Map<Integer, Set<MarcDatafieldHolder>> getDynamicContent(Long recordOfSource, Repository repo, Set<Long> set) {

        Map<Integer, Set<MarcDatafieldHolder>> dynamic = new HashMap<Integer, Set<MarcDatafieldHolder>>();
        TreeSet<MarcDatafieldHolder> fields35 = new TreeSet<MarcDatafieldHolder>();
        TreeSet<MarcDatafieldHolder> fields10 = new TreeSet<MarcDatafieldHolder>();
        TreeSet<MarcDatafieldHolder> fields20 = new TreeSet<MarcDatafieldHolder>();
        TreeSet<MarcDatafieldHolder> fields22 = new TreeSet<MarcDatafieldHolder>();
        TreeSet<MarcDatafieldHolder> fields24 = new TreeSet<MarcDatafieldHolder>();

        ArrayList<Character> charListA  = new ArrayList<Character>();
        ArrayList<Character> charList22 = new ArrayList<Character>();
        ArrayList<Character> charList24 = new ArrayList<Character>();

        // for most we just want the values from the $a subfield
        charListA.add(new Character('a'));

        // but for datafield 022 we want the values from more subfields:
        charList22.add(new Character('a'));
        charList22.add(new Character('l'));
        charList22.add(new Character('m'));
        charList22.add(new Character('z'));

        // & for datafield 024 we want the values from 1 more subfield:
        charList24.add(new Character('a'));
        charList24.add(new Character('2'));

        for (Long num: set) {
            String oai = repo.getRecord(num).getOaiXml();

            // make an 035 out of 001/003
            String _035 = create035(oai);
            if (_035 != null) {
                MarcSubfieldHolder subfield = new MarcSubfieldHolder('a', _035);
                List<MarcSubfieldHolder> subfields = new ArrayList<MarcSubfieldHolder>();
                subfields.add(subfield);
                fields35.add(new MarcDatafieldHolder("035",subfields," ", " "));
            }
            // add 035 data already in the documents, but it must be well-formed.
            // The getDynamicField method makes sure it is.
            fields35.addAll(getDynamicDatafield(oai, 35, "035", charListA));  // add 035$a data
        }
        dynamic.put(35, fields35);

        // can have 1 and only 1 010, and prefer it to be from record of source.
        String _oai = repo.getRecord(recordOfSource).getOaiXml();
        fields10.addAll(getDynamicDatafield(_oai,10, "010",charListA));
        if (fields10.size() < 1) {

            for (Long num: set) {
                // now get first 010$a you can find from all non-record of source in the match set
                if (num != recordOfSource) {
                    String oai = repo.getRecord(num).getOaiXml();
                    fields10.addAll(getDynamicDatafield(oai,10, "010",charListA));
                    if (fields10.size() > 0) {
                        continue;
                    }
                }
            }
        }
        dynamic.put(10, fields10);

        for (Long num: set) {
            // now get 020$a from all in the match set
            String oai = repo.getRecord(num).getOaiXml();
            fields20.addAll(getDynamicDatafield(oai,20, "020", charListA));
        }
        dynamic.put(20, fields20);

        for (Long num: set) {
            // now get 024$a2 from all in the match set
            String oai = repo.getRecord(num).getOaiXml();
            fields24.addAll(getDynamicDatafield(oai,24, "024", charList24));
        }
        dynamic.put(24, fields24);

        for (Long num: set) {
            // now get 022$almz from all in the match set
            String oai = repo.getRecord(num).getOaiXml();
            fields22.addAll(getDynamicDatafield(oai,22, "022", charList22));
        }
        dynamic.put(22, fields22);

        return dynamic;
    }

    //getDynamicHoldingContent => create 904 from 004/014, don't worry about existing 904,
    // we are not trimming anything from existing record.
    //   returns content to dynamically insert into holding records
    //
    protected String getDynamicHoldingContent(Repository repo, Long num) {
        String oai = repo.getRecord(num).getOaiXml();
        String _904 = create904(oai);   // the block of xml

        return _904;
    }

    /**
     * package the found Marc datafields into a list of MarcDatafieldHolder, an encapsulating class that collects
     * ind1, ind2, datafield name, and subfields into one class.
     *
     * @param oaiXml
     * @param fieldNum used to retrieve the datafields from the oaiXml
     * @param fieldName string representation - need it to pass it to MarcDatafieldHolder
     * @param subfieldC - list of character subfields of interest
     * @return
     */
    private List<MarcDatafieldHolder> getDynamicDatafield(String oaiXml, int fieldNum, String fieldName, List<Character> subfieldC) {
        SaxMarcXmlRecord smr = new SaxMarcXmlRecord(oaiXml);
        List<Field> fields = smr.getDataFields(fieldNum);
        List<MarcDatafieldHolder> marcFields = new ArrayList<MarcDatafieldHolder>();

        for (Field field : fields) {
            ArrayList<MarcSubfieldHolder> _flds = new ArrayList<MarcSubfieldHolder>();
            char ind1 = field.getInd1();
            char ind2 = field.getInd2();
            for (Character c: subfieldC) {
                List<String> subfields = SaxMarcXmlRecord.getSubfieldOfField(field, c);
                for (String subfield : subfields) {
                    //
                    // ignore all 035's with no prefix.
                    //
                     if (subfield != null) {
                        if (fieldNum == 35) {
                            if (subfield.trim().contains("(")) {
                                _flds.add(new MarcSubfieldHolder(new Character(c),subfield));
                                LOG.debug("add 035:"+subfield+"<=");
                            }
                            else {
                                LOG.debug("do not add 035:"+subfield+"<=");
                            }
                        }
                        else {
                            _flds.add(new MarcSubfieldHolder(new Character(c),subfield));
                        }
                    }
                }
            }
            marcFields.add(new MarcDatafieldHolder(fieldName,_flds,
                    new Character(ind1).toString(),new Character(ind2).toString()));
        }
        return marcFields;
    }

    /**
     * The 001 and 003 from BOTH the Selected Record and the Non-Selected Record will be used to create separate,
     * new 035 fields in the Output Record,
     * with the value of the 003 (institution code) set as the prefix for the control number in $a, enclosed in parens.
     * The number from the 001 will follow the parens without a space.
     * @param oaiXml
     * @return can return null, so check for it!
     *         if it returns a string, it will be subfield contents of the form:
     *         (OCoLC)1788884
     */
    private String create035(String oaiXml) {
        SaxMarcXmlRecord smr = new SaxMarcXmlRecord(oaiXml);
        String org = smr.getOrgCode();
        if (org != null) {
            org = org.trim();
        }
        else {
            LOG.debug("no org code found in "+smr.recordId);
            return null;
        }
        String _001 = smr.getControlField(1);
        if (_001 != null) {
            _001 = _001.trim();
        }
        else {
            LOG.debug("no _001 code found in "+smr.recordId);
            return null;
        }
        StringBuilder sb = new StringBuilder("(").append(org).append(")").append(_001);
        return sb.toString();
    }

    /**
        No matching or merging is done on MARC Holdings records. Therefore, the content of an Output
        holdings record will be identical to the content of the Input record, with the following exceptions:

        For every holdings record, whether or not its parent bibliographic record matches on and is merged with
        another record, the Service generates one or more new 904 �XC Uplink� fields in each Output Holdings
        record. This 904 field contains, in $a, the OAI ID for the Output parent record; that is, for the successor
        to the record represented in the input Holdings record�s 004 field. In addition, the 904 field contains $1
        NyRoXCO to identify it as a field created by an XC service.

        The Service generates additional 904 fields to contain links to the successors to additional linked
        bibliographic records linked through 014 fields in the input records (�bound withs�) if the following
        conditions are met:

        014 first indicator value=1
        If an 014 $b is present, the code matches that in the 003 of the incoming holdings record

        The Transformation Service will use these 904 fields as �uplinks� instead of using 004 or 014 fields
        in the MARC record to create the �uplinks� between XC Holdings and XC Manifestation records. These
        additional 904 fields will contain the same subfields ($a, $1) as 904 fields created from 004 fields.
     * @param oaiXml
     * @return can return null, so check for it!
     */
    private String create904(String oaiXml) {
        Marc904Generator _904generator = null;
        SaxMarcXmlRecord smr = new SaxMarcXmlRecord(oaiXml);
        String _003 = smr.getControlField(3);
        if (_003 != null) {
            _003 = _003.trim();
        }
        else {
            LOG.error("no _003 code found in "+smr.recordId);
            throw new RuntimeException("no _003 code found in "+smr.recordId);
        }
        String _004 = smr.getControlField(4);
        if (_004 != null) {
            _004 = _004.trim();
        }
        else {
            LOG.error("no _004 code found in "+smr.recordId);
        }
        StringBuilder sb = new StringBuilder("").append(_004);
        return sb.toString();
        //TODO fix this.  Really need to create or use an encapsulating class like MarcDatafieldHolder.
    }

    /**
     # Record of source criteria:
     #
     # 1) leader_byte17_weighting_enabled = true/false
     # 2) bigger_record_weighting_enabled = true/false
     #
     # And four cases:
     #
     # 1-true, 2-false
     # In this case we first compare Leader/byte17, pick the earliest (in String leader.order above),
     #   if they are the same, pick the record that is being processed.
     #
     # 1-true, 2-true
     # In this case we first compare Leader/byte17, pick the earliest (in String leader.order above),
     #   if they are the same, pick the record that is largest in bytes.
     #
     # 1-false, 2-true
     # Pick the record that is largest in bytes.
     #
     # 1-false, 2-false
     # This is a not-allowed state and the service will throw an error message.
     #
     * @param set
     * @return
     */
    private Long determineRecordOfSource(Set<Long> set) {

        TreeMap<SortableRecordOfSourceData, RecordOfSourceData> sortedMap = new TreeMap<SortableRecordOfSourceData, RecordOfSourceData>();
        Repository repo = getInputRepo();  //for date tie-breaker
        for (Long num: set) {

            // grab leader byte 17 value and size
            RecordOfSourceData source;
            if (!scores.containsKey(num)) {
                source = masDAO.getScoreData(num);
            }
            else {
                //use the data already in memory.
                source = scores.get(num);
            }
            LOG.debug("Source data for id: "+num+" char:"+source.leaderByte17+": "+" size="+source.size);

            // use leaderVals:
            // List<Character> leaderVals
            // leader_byte17_weighting_enabled;
            // bigger_record_weighting_enabled;
            sortedMap.put(new SortableRecordOfSourceData(repo,leaderVals,num,source, leader_byte17_weighting_enabled ,bigger_record_weighting_enabled ), source);
        }
        return sortedMap.firstKey().recordId;
    }

    public void processComplete(Repository repo) {
        //
        // for performance may want to do this:
        //        .createIndiciesIfNecessary(name);
        //
        // evaluate match sets here?
        LOG.info("** START processComplete!");

        // start to do the real work of the service.  Probably belongs
        // up higher than processCompleted method!!
        //
        List<TreeSet<Long>> matches = getCurrentMatchSetList();
        if (matches != null) {
            //TODO maybe change this to 'debug' vs. 'info' at some point.
            LOG.info("** processComplete, matchset length="+matches.size());

            for (Set<Long> set: matches) {
                StringBuilder sb = new StringBuilder("*** Matchset: {");
                for (Long num: set) {
                    sb.append(num+", ");
                }
                sb.append("}");
                //TODO change this to 'debug' vs. 'info' at some point.
                LOG.info(sb.toString());
            }

            // TODO
            // important - this is not going to totally nail it for the long term
            // need to consider records received during THIS run of the service, and
            // there status, i.e. if if goes to deleted state and is part of a merge
            // set.  Future solution still in the works  - could be customProcessQueue
            // and if that is not enough save more to the current match set list?
            //
            // TODO
            // Do you need to build lists of records to create (part of merge set & not)
            // and records that will
            // not being created because they are being merged?
            //
            //mergeAll(matches, repo);
        }
        //end real work of the service (getting matches and merging)
    }

    // note the 'well' named class Set collides with java.util.Set
    //
    // overriding this so you can save the repo/start over?
    public void process(Repository repo, Format inputFormat, xc.mst.bo.provider.Set inputSet, xc.mst.bo.provider.Set outputSet) {
        this.inputRepo = repo;
        LOG.info("MarcAggregationService, processing repo "+ repo.getName()+" started.");
        try {
            super.process(repo, inputFormat, inputSet, outputSet);
            processComplete(repo);
        } catch (Exception e) {
            LOG.error("MarcAggregationService, processing repo "+ repo.getName()+" failed.", e);
        }
    }

    protected Repository getInputRepo() {
        return this.inputRepo;
    }

    /**
     *
     * @param record -the record of source
     * @return
     */
    private List<OutputRecord> createNewRecord(InputRecord record, String type, String newXml) {

        // should this check be here or before we even get here?  notice the method name I chose...
        //
        // If there was already a processed record for the record we just processed, update it
        if (record.getSuccessors() != null && record.getSuccessors().size() > 0) {

        }
        else {
            // new
        }

        TimingLogger.start("new");

        // The list of records resulting from processing the incoming record
        //    actually maybe we want to return list of InputRecord as we are collapsing,
        //    not expanding.
        ArrayList<OutputRecord> results = new ArrayList<OutputRecord>();

        // Create the aggregated record
        OutputRecord aggRecord = getRecordService().createRecord();

        aggRecord.setMode(Record.STRING_MODE);
        aggRecord.setOaiXml(newXml); /* use the merged content */
        aggRecord.setFormat(marc21);

        // Insert the new (possibly) aggregated record

        // The setSpec and set Description of the "type" set we should add the normalized record to
        String setSpec = null;
        String setDescription = null;
        String setName = null;

        // Setup the setSpec and description based on the leader 06
        // TODO is this right?
        if (type.equals("b")) {
            setSpec = "MARCXMLbibliographic";
            setName = "MARCXML Bibliographic Records";
            setDescription = "A set of all MARCXML Bibliographic records in the repository.";
        } else if (type.equals("h")) {
            setSpec = "MARCXMLholding";
            setName = "MARCXML Holding Records";
            setDescription = "A set of all MARCXML Holding records in the repository.";

        } // TODO what to do in the case of records not of about types?  Create a 1:1 output record?

        if (setSpec != null) {
            try {
                // Get the set for the provider
                TimingLogger.start("getSetBySetSpec");
                xc.mst.bo.provider.Set recordTypeSet = getSetService().getSetBySetSpec(setSpec);
                TimingLogger.stop("getSetBySetSpec");

                // Add the set if it doesn't already exist
                if (recordTypeSet == null)
                    recordTypeSet = addSet(setSpec, setName, setDescription);

                // Add the set to the record
                aggRecord.addSet(recordTypeSet);
            } catch (DatabaseConfigException e) {
                LOG.error("Could not connect to the database with the parameters in the configuration file.", e);
                e.printStackTrace();
            } catch (DataException e) {
                LOG.error("Error.", e);
            }
        }

        // Add the record to the list of records resulting from processing the
        // incoming record
        aggRecord.setType(type);
        results.add(aggRecord);
        if (LOG.isDebugEnabled())
            LOG.debug("Created aggregated record from record with ID " + record.getId());

        TimingLogger.stop("new");
        return results;
    }

    // search to see if there are multiple in records for this given out record.
    //
    protected void addPredecessor(Record in, Record out) {
        TreeSet<Long> set = mergedRecordsO2Imap.get(out.getId());
        if (set==null || set.isEmpty()) {
          out.addPredecessor(in);
        }
        else {
            for (Long in_rec: set) {
                Record r = inputRepo.getRecord(in_rec);
                if (r != null) {
                    out.addPredecessor(r);
                }
            }
        }
    }

    public List<OutputRecord> process(InputRecord r) {
        String type = null;
        List<OutputRecord> results = null;
        try {
            LOG.debug("MAS:  process record+"+r.getId());

            if (r.getStatus() != Record.DELETED) {
                SaxMarcXmlRecord smr = new SaxMarcXmlRecord(r.getOaiXml());
                smr.setRecordId(r.getId());

                final char leaderByte17 = smr.getLeader().charAt(17);
                final int rSize = r.getOaiXml().getBytes().length;
                scores.put(r.getId(), new RecordOfSourceData(leaderByte17, rSize));

                // Get the Leader 06. This will allow us to determine the record's type
                char leader06 = smr.getLeader().charAt(6);

                // check if the record is a bibliographic record
                if ("abcdefghijkmnoprt".contains("" + leader06)) {
//                    TimingLogger.start("bib steps");
                    type = "b";
                    results = processBib(r, smr, inputRepo);
                }
                // check if the record is a holding record
                else if ("uvxy".contains("" + leader06)) {
//                    TimingLogger.start("hold steps");
                    type = "h";
                    results = processHolding(r, smr, inputRepo);
//                    ((Record) record).setType(type);
                }
                //TODO what about authority and any other type?  pass it through or not?
                if (leader06 == 'z') {
                    // authority
                }

                // was here....
//                processBib(r, smr);

                // will we want to do this on a 2nd loop around all the records?
                //
                if (r.getSuccessors().size() == 0) {
                    // NEW-ACTIVE

                } else {
                    // UPDATE-ACTIVE
                        // unmerge
                        /*
                        for (inputBibId : inputBibIds) {
                            customProcessQueue.push(inputBibId)
                        }
                        for (inputHoldingId : inputHoldingIds) {
                            customProcessQueue.push(inputHoldingId)
                        }
                        */
                }
            } else {// Record.DELETED
                if (r.getSuccessors().size() == 0) {
                    // NEW-DELETED
                    //
                    // nothing to do?
                } else {
                    // UPDATE-DELETED
                    //
                    // TODO - unmerge ramifications, figure out merge set you may be in, remerge?
                }
            }

            if (results != null && results.size() != 1) {
                // TODO incr records counts no output
                addMessage(r, 103, RecordMessage.ERROR);
            }
            return results;


        } catch (Throwable t) {
            util.throwIt(t);
        }
        return null;
    }

    protected List<OutputRecord> processHolding(InputRecord r, SaxMarcXmlRecord smr, Repository repo) {
        // oaiXml = getHoldingBase(oaiXml);
        List<OutputRecord> list = null;
        list = createNewRecord(r, "h", oaiXml);
        return list;
    }

        Map<Integer, HashSet<String>> dynamic = getDynamicHoldingContent(repo, r.getId());
        HashSet<String> dyn904 = dynamic.get(904);
        for (String _904 : dyn904) {
            LOG.debug("created 904: "+_904);
        }
        // originally I thought we were stripping 004/014 from holding.  We are not.
        //
        // String oaiXml = repo.getRecord(r.getId()).getOaiXml();
        // oaiXml = getHoldingBase(oaiXml);
        List<OutputRecord> list = null;
        //TODO make sure 904 block is tacked onto the end!
        list = createNewRecord(r, "h", r.getOaiXml());
        return list;
    }

    protected List<OutputRecord> processBib(InputRecord r, SaxMarcXmlRecord smr, Repository repo) {
        MatchSet ms = new MatchSet(smr);
        for (Map.Entry<String, FieldMatcher> me : this.matcherMap.entrySet()) {
            String matchPointKey = me.getKey();
            FieldMatcher matcher = me.getValue();
            matcher.addRecordToMatcher(smr, r);  // is this the place to do this?  (was originally missing)
            // possibly need/want to add all match points 1st, then look for matches.
            ms.addMatcher(matchPointKey, matcher);
        }

        // maybe this will come into play with rules that have parts that are alike...
        Set<Long> previouslyMatchedRecordIds = null;

        TreeSet<Long> matchedRecordIds = new TreeSet<Long>();
        for (Map.Entry<String, MatchRuleIfc> me : this.matchRuleMap.entrySet()) {
            String matchRuleKey = me.getKey();
            MatchRuleIfc matchRule = me.getValue();
            Set<Long> set = matchRule.determineMatches(ms);
            if (set !=null && !set.isEmpty()) {
                matchedRecordIds.addAll(set);
            }
        }

        List<OutputRecord> results = new ArrayList<OutputRecord>();

        // make sure to get all the disjoint merge sets in the total set, i.e. if this given input record
        // does not match something that another record it did match did, it needs to be in the total.
        matchedRecordIds = expandMatchedRecords(matchedRecordIds);

        // this is the merge as you go along spot,
        // does not seem like it is most efficient but if fits our paradigm of running through all records 1x.
        // TODO change to merge at end, looping a 2nd time through the records, if need be.
        masMatchSetList = addToMatchSetList(matchedRecordIds, masMatchSetList);

        List<OutputRecord> list = null;
        // may not have any matches!
        final boolean hasMatches = matchedRecordIds.size() > 0;
        if (hasMatches) {
            list = mergeBibSet(/* r, */ matchedRecordIds, repo);
            // was merge(masMatchSetList,repo);  // that went through whole list every time.
        }
        else {
             list = createNewRecord(r, "b", r.getOaiXml());
        }
        return list;
    }

    /**
     * given sets {62,160} and {160,201} where 160 individually matches the other 2, but the other 2 don't directly match
     * each other, indirectly they do, so the output record needs to combine {62,160,201}
     *
     * @param matchedRecordIds
     * @return
     */
    private TreeSet<Long> expandMatchedRecords(TreeSet<Long> matchedRecordIds) {
        TreeSet<Long> results = new TreeSet<Long>();
        results.addAll(matchedRecordIds);

        for (Long input: matchedRecordIds) {
            Long output = mergedRecordsI2Omap.get(input);
            if (output != null) {
                TreeSet<Long> temp = mergedRecordsO2Imap.get(output);
                if (temp != null) {
                    results.addAll(mergedRecordsO2Imap.get(output));
                }
            }
        }

        return results;
    }

    @Override
    protected boolean commitIfNecessary(boolean force, long processedRecordsCount) {
        try {
            LOG.debug("***FORCE: masDAO.commitIfNecessary");
            TimingLogger.start("masDAO.commitIfNecessary");

            // break down timing logger more later if necessary.
            for (Map.Entry<String, FieldMatcher> me : this.matcherMap.entrySet()) {
                final FieldMatcher matcher = me.getValue();
                matcher.flush(force);
                LOG.debug("flush matcher: "+matcher.getName());
            }
            // this should not need to done in must do, must do frequently section.
            masDAO.persistScores(scores);
            masDAO.persistLongMatchpointMaps(mergedRecordsI2Omap, MarcAggregationServiceDAO.merged_records_table, false);

            TimingLogger.stop("masDAO.commitIfNecessary");
        } catch (Throwable t) {
            getUtil().throwIt(t);
        }
        if (!force) {
            TimingLogger.reset();
            return super.commitIfNecessary(force, 0);
        }
        try {
            TimingLogger.start("MarcAggregationServiceDAO.non-generic");
            super.commitIfNecessary(true, 0);
            TimingLogger.stop("MarcAggregationServiceDAO.non-generic");
            // as part of the flush call matcher must clear its memory data structures

            TimingLogger.stop("MarcAggregationServiceDAO.endBatch");

//            getRepository().setPersistentProperty("inputBibs", inputBibs);
//            getRepository().setPersistentProperty("inputHoldings", inputHoldings);
            TimingLogger.reset();
        } catch (Throwable t) {
            getUtil().throwIt(t);
        }
        return true;
    }
}
