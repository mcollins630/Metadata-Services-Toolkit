/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */
package xc.mst.services.marcaggregation.dao;


import gnu.trove.TLongLongHashMap;
import gnu.trove.TLongLongProcedure;
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongObjectProcedure;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import xc.mst.services.impl.dao.GenericMetadataServiceDAO;
import xc.mst.services.marcaggregation.RecordOfSourceData;
import xc.mst.services.marcaggregation.matcher.SCNData;
import xc.mst.utils.MSTConfiguration;
import xc.mst.utils.TimingLogger;
/**
*
* @author John Brand
*
*/
public class MarcAggregationServiceDAO extends GenericMetadataServiceDAO {

    private final static Logger LOG = Logger.getLogger(MarcAggregationServiceDAO.class);
    private final int RECORDS_AT_ONCE = 100000;
    
    // can't load into mysql table if the field data exceeds its type size
    private final static int MAX_STRING_LENGTH = 255;

    // not yet used starts
    public final static String matchpoints_028a_table   = "matchpoints_028a";
    public final static String matchpoints_130a_table   = "matchpoints_130a";
    public final static String matchpoints_240a_table   = "matchpoints_240a";
    public final static String matchpoints_245a_table   = "matchpoints_245a";
    public final static String matchpoints_260abc_table = "matchpoints_260abc";
    // not yet used stops

    public final static String matchpoints_010a_table   = "matchpoints_010a";
    public final static String matchpoints_020a_table   = "matchpoints_020a";
    public final static String matchpoints_022a_table   = "matchpoints_022a";
    public final static String matchpoints_024a_table   = "matchpoints_024a";
    public final static String matchpoints_035a_table   = "matchpoints_035a";
    public final static String prefixes_035a_table      = "prefixes_035a";

    public final static String merge_scores_table       = "merge_scores";
    public final static String merged_records_table     = "merged_records";
    public final static String bib_records_table        = "bib_records";
    public final static String record_of_source_table   = "record_of_source";

    public final static String input_record_id_field    = "input_record_id";
    public final static String string_id_field          = "string_id";
    public final static String numeric_id_field         = "numeric_id";
    public final static String prefix_id_field          = "prefix_id";
    public final static String prefix_field             = "prefix";
    public final static String leaderByte17_field       = "leaderByte17";
    public final static String size_field               = "size";

    /**
     * there is a constraint on prefix_id, so have to place that into the db 1st, before trying to write 035 data.
     * @param inputId2matcherMap
     * @param tableName
     */
    @SuppressWarnings("unchecked")
    public void persistSCNMatchpointMaps(Map<Long, List<SCNData>> inputId2matcherMap, String tableName) {

        TimingLogger.start("MarcAggregationServiceDAO.persistSCNMaps");

        TimingLogger.start("prepare to write");
        String dbLoadFileStr = getDbLoadFileStr();

        final byte[] tabBytes = getTabBytes();
        final byte[] newLineBytes = getNewLineBytes();

        try {
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));
            final MutableInt j2 = new MutableInt(0);

            for (Object keyObj : inputId2matcherMap.keySet()) {
                Long id = (Long) keyObj;
                final byte[] idBytes = String.valueOf(id).getBytes();
                Object list = inputId2matcherMap.get(id);

                try {
                    if (list == null) {
                        continue;
                    }
                    List<SCNData> scnList = (List<SCNData>) list;
                    LOG.debug("insert: " + tableName + ".size(): " + scnList.size());
                    if (scnList != null && scnList.size() > 0) {
                        for (SCNData _scn: scnList) {
                            try {   // need to loop through all strings associated with id!
	                                if (_scn.full.length() > MAX_STRING_LENGTH || _scn.scn.length() > MAX_STRING_LENGTH) {
	                                	LOG.error("*** problem with data (TOO LONG > " + MAX_STRING_LENGTH + ") readying id="+id);
	                                	continue;
	                                }

                                    if (j2.intValue() > 0) {
                                        os.write(newLineBytes);
                                    } else {
                                        j2.increment();
                                    }
                                    // ends up 'quoting' the string, was needed for ISBN 020$a but this method called by other matchers.
                                    os.write(getBytes(_scn.full));
                                    os.write(tabBytes);
                                    os.write((String.valueOf(_scn.prefixNum).getBytes()));
                                    os.write(tabBytes);
                                    os.write(getBytesFixTabs(_scn.scn));
                                    os.write(tabBytes);
                                    os.write(idBytes);
                            } catch (Exception e) {
                                LOG.error("problem with data - id="+id,e);
                                getUtil().throwIt(e);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.error("*** problem with data readying id="+id,t);
                    getUtil().throwIt(t);
                }
            }
            os.close();
            TimingLogger.stop("prepare to write");

            TimingLogger.start("will replace");
            replaceIntoTable(tableName, dbLoadFileStr);
            TimingLogger.stop("will replace");

        } catch (Exception e4) {
            LOG.error("*** problem with replaceIntoTable data",e4);
            getUtil().throwIt(e4);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persistSCNMaps");
        }
    }

    @SuppressWarnings("unchecked")
    public void persist2StrMatchpointMaps(Map<Long, List<String[]>> inputId2matcherMap, String tableName) {

        TimingLogger.start("MarcAggregationServiceDAO.persist2StrMaps");

        TimingLogger.start("prepare to write");
        String dbLoadFileStr = getDbLoadFileStr();

        final byte[] tabBytes = getTabBytes();
        final byte[] newLineBytes = getNewLineBytes();

        try {
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));
            final MutableInt j2 = new MutableInt(0);

            for (Object keyObj : inputId2matcherMap.keySet()) {
                Long id = (Long) keyObj;
                final byte[] idBytes = String.valueOf(id).getBytes();
                Object list = inputId2matcherMap.get(id);

                try {
                    if (list == null) {
                        continue;
                    }
                    List<String[]> strList = (List<String[]>) list;
                    LOG.debug("insert: " + tableName + ".size(): " + strList.size());
                    if (strList != null && strList.size() > 0) {
                        for (String[] _s: strList) {
                            try {   // need to loop through all strings associated with id!
	                                if (_s.length > MAX_STRING_LENGTH) {
	                                	LOG.error("*** problem with data (TOO LONG > " + MAX_STRING_LENGTH + ") readying id="+id);
	                                	continue;
	                                }

                                    if (j2.intValue() > 0) {
                                        os.write(newLineBytes);
                                    } else {
                                        j2.increment();
                                    }
                                    // ends up 'quoting' the string, was needed for ISBN 020$a but this method called by other matchers.
                                    os.write(getBytes(_s[1]));
                                    os.write(tabBytes);
                                    os.write(getBytes(_s[0]));
                                    os.write(tabBytes);
                                    os.write(idBytes);
                            } catch (Exception e) {
                                LOG.error("problem with data - id="+id,e);
                                getUtil().throwIt(e);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.error("*** problem with data readying id="+id,t);
                    getUtil().throwIt(t);
                }
            }
            os.close();
            TimingLogger.stop("prepare to write");

            TimingLogger.start("will replace");
            replaceIntoTable(tableName, dbLoadFileStr);
            TimingLogger.stop("will replace");

        } catch (Exception e4) {
            LOG.error("*** problem with replaceIntoTable data",e4);
            getUtil().throwIt(e4);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persist2StrMaps");
        }
    }

    @SuppressWarnings("unchecked")
    public void persist1StrMatchpointMaps(Map<Long, List<String>> inputId2matcherMap, String tableName) {
        TimingLogger.start("MarcAggregationServiceDAO.persist1StrMatchpointMaps");
        TimingLogger.start("prepare to write");

        String dbLoadFileStr = getDbLoadFileStr();
        final byte[] tabBytes = getTabBytes();
        final byte[] newLineBytes = getNewLineBytes();

        try {
            final MutableInt j = new MutableInt(0);
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));
            for (Object keyObj : inputId2matcherMap.keySet()) {
                Long id = (Long) keyObj;
                Object list = inputId2matcherMap.get(id);

                try {
                    if (list == null) {
                        continue;
                    }
                    final byte[] idBytes = String.valueOf(id).getBytes();

                    List<String> strList = (List<String>) list;
                    LOG.debug("insert: " + tableName + ".size(): " + strList.size());
                    if (strList != null && strList.size() > 0) {
                        for (String _s: strList) {
                            if (StringUtils.isEmpty(_s)) {
                            	LOG.error("*** problem with data (EMPTY) readying id="+id);
                            	continue;
                            }
                            if (_s.length() > MAX_STRING_LENGTH) {
                            	LOG.error("*** problem with data (TOO LONG > " + MAX_STRING_LENGTH + ") readying id="+id);
                                continue;
                            }
                            try {
                            	
                                if (j.intValue() > 0) {
                                    os.write(newLineBytes);
                                } else {
                                    j.increment();
                                }

                                // need to loop through all strings associated with id!
                                //
                                // write the newline after we have written a line, but not at the end of the last line
                                os.write(getBytes(_s));
                                os.write(tabBytes);
                                os.write(idBytes);
                            } catch (Exception e) {
                                LOG.error("problem with data - id="+id,e);
                                getUtil().throwIt(e);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.error("problem with replaceIntoTable data - id="+id,t);
                    getUtil().throwIt(t);
                }
            }
            os.close();
            TimingLogger.stop("prepare to write");

            TimingLogger.start("will replace");
            replaceIntoTable(tableName, dbLoadFileStr);
            TimingLogger.stop("will replace");

        } catch (Throwable t4) {
            LOG.error("*** problem with replaceIntoTable data",t4);
            getUtil().throwIt(t4);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persist1StrMatchpointMaps");
        }
    }

    @SuppressWarnings("unchecked")
    public void persistPrefixList(Map<Integer, String> prefixList, String tableName) {
        TimingLogger.start("MarcAggregationServiceDAO.persistPrefixMap");
        TimingLogger.start("prepare to write");

        String dbLoadFileStr = getDbLoadFileStr();
        final byte[] tabBytes = getTabBytes();
        final byte[] newLineBytes = getNewLineBytes();

        try {
            final MutableInt j = new MutableInt(0);
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));            
            for (Integer id : prefixList.keySet()) {
                Object prefixO = prefixList.get(id);

                try {
                    if (prefixO == null) {
                        continue;
                    }
                    String prefix = (String) prefixO;
                    if (StringUtils.isEmpty(prefix)) {
                    	LOG.error("*** problem with data (EMPTY) readying id="+id);
                    	continue;
                    }
                    if (prefix.length() > MAX_STRING_LENGTH) {
                    	LOG.error("*** problem with data (TOO LONG > " + MAX_STRING_LENGTH + ") readying id="+id);
                        continue;
                    }

                    if (j.intValue() > 0) {
                        os.write(newLineBytes);
                    } else {
                        j.increment();
                    }

                    final byte[] idBytes = String.valueOf(id).getBytes();

                    try {
                        os.write(getBytes(prefix));
                        os.write(tabBytes);
                        os.write(idBytes);
                    } catch (Exception e) {
                        LOG.error("problem with data - id="+id,e);
                        getUtil().throwIt(e);
                    }
                } catch (Throwable t) {
                    LOG.error("problem with replaceIntoTable data - id="+id,t);
                    getUtil().throwIt(t);
                }
            }
            os.close();
            TimingLogger.stop("prepare to write");

            TimingLogger.start("will replace");
            replaceIntoTable(tableName, dbLoadFileStr);
            TimingLogger.stop("will replace");

        } catch (Throwable t4) {
            LOG.error("*** problem with replaceIntoTable data",t4);
            getUtil().throwIt(t4);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persistPrefixMap");
        }
    }

    public void persistLongOnly(List<Long> values, String tableName)  {

        TimingLogger.start("MarcAggregationServiceDAO.persistLongOnly");
        try {

            String dbLoadFileStr = getDbLoadFileStr();
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));
            final MutableInt j = new MutableInt(0);

            final byte[] newLineBytes = getNewLineBytes();
            for (Long value: values) {
                try {
                    if (j.intValue() > 0) {
                        LOG.debug("line break!!! j:" + j.intValue());
                        os.write(newLineBytes);
                    } else {
                        j.increment();
                    }
                    os.write(String.valueOf(value).getBytes());
                } catch (Throwable t) {
                    getUtil().throwIt(t);
                }
            }
            os.close();
            replaceIntoTable(tableName, dbLoadFileStr);
        } catch (Throwable t) {
            LOG.error("problem with replaceIntoTable data ",t);
            getUtil().throwIt(t);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persistLongOnly");
        }
    }

    /**
     * this one if for persisting those that do not repeat (1 set of entries per record id) and has a TLongLong only for each record id
     * ,also using it to persist bib_records, input_record->output_record
     *
     * @param inputId2numMap
     * @param tableName
     * @param swap - if true, then write the key / value as value / key into the db
     */
    public void persistLongMatchpointMaps(TLongLongHashMap inputId2numMap, String tableName, final boolean swap) {

        TimingLogger.start("MarcAggregationServiceDAO.persistLongMaps");
        try {

            String dbLoadFileStr = getDbLoadFileStr();
            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));
            final MutableInt j = new MutableInt(0);

            final byte[] tabBytes = getTabBytes();
            final byte[] newLineBytes = getNewLineBytes();

            if (inputId2numMap instanceof TLongLongHashMap) {
                LOG.debug("insert: " + tableName + ".size(): " + inputId2numMap.size());
                if (inputId2numMap != null && inputId2numMap.size() > 0) {
                    inputId2numMap.forEachEntry(new TLongLongProcedure() {
                        public boolean execute(long id, long num) {
                            try {
                                if (j.intValue() > 0) {
                                    os.write(newLineBytes);
                                } else {
                                    j.increment();
                                }
                                if (swap) {        // write value then key
                                    os.write(String.valueOf(num).getBytes());
                                    os.write(tabBytes);
                                    os.write(String.valueOf(id).getBytes());
                                }
                                else {             // write key then value
                                    os.write(String.valueOf(id).getBytes());
                                    os.write(tabBytes);
                                    os.write(String.valueOf(num).getBytes());
                                }
                            } catch (Throwable t) {
                                LOG.error("problem with data - id="+id,t);
                                getUtil().throwIt(t);
                            }
                            return true;
                        }
                    });
                }
            }
            os.close();
            replaceIntoTable(tableName, dbLoadFileStr);
        } catch (Throwable t) {
            LOG.error("problem with replaceIntoTable data - ",t);
            getUtil().throwIt(t);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persistLongMaps");
        }
    }

    public void persistScores(TLongObjectHashMap<xc.mst.services.marcaggregation.RecordOfSourceData> scores) {

        final String tableName = merge_scores_table;
        TimingLogger.start("MarcAggregationServiceDAO.persistScores");
        try {
            String dbLoadFileStr = getDbLoadFileStr();

            final byte[] tabBytes = getTabBytes();
            final byte[] newLineBytes = getNewLineBytes();

            final OutputStream os = new BufferedOutputStream(new FileOutputStream(dbLoadFileStr));
            final MutableInt j = new MutableInt(0);

            if (scores instanceof TLongObjectHashMap) {
                LOG.debug("insert: " + tableName + ".size(): " + scores.size());
                if (scores != null && scores.size() > 0) {
                    scores.forEachEntry(new TLongObjectProcedure<xc.mst.services.marcaggregation.RecordOfSourceData>() {
                        public boolean execute(long id, xc.mst.services.marcaggregation.RecordOfSourceData source) {
                            try {
                                if (j.intValue() > 0) {
                                    os.write(newLineBytes);
                                } else {
                                    j.increment();
                                }
                                os.write(String.valueOf(id).getBytes());
                                os.write(tabBytes);
                                os.write(String.valueOf(source.leaderByte17).getBytes());
                                os.write(tabBytes);
                                os.write(String.valueOf(source.size).getBytes());
                            } catch (Throwable t) {
                                getUtil().throwIt(t);
                            }
                            return true;
                        }
                    });
                }
            }
            os.close();
            replaceIntoTable(tableName, dbLoadFileStr);
        } catch (Throwable t) {
            getUtil().throwIt(t);
        } finally {
            TimingLogger.stop("MarcAggregationServiceDAO.persistScores");
        }
    }

    /**
     * quote the string as otherwise mysql insert fails when inserting '123344\'
     *
     *   But STILL, a problem with the trailing backslash.  So replace backslashes by double backslashes at the time of the quoting.  Ideally, you
     *   only replace single backslashes with doubles but that is a complicated regular expression lets see if we need it first.
     *
     *   also requires special syntax on insert @see this.replaceIntoTable method.
     *
     * @param s
     * @return
     */
    protected static byte[] getBytes(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        final String s3 = getQuoted(s);
        return s3.getBytes();
    }

    protected static byte[] getBytesFixTabs(String s) {
    	final String s3 = s.replaceAll("\t", " ").replaceAll("\\\\", "\\\\\\\\");
        return s3.getBytes();
    }

    protected static byte[] getTabBytes() {
        return "\t".getBytes();
    }

    protected static byte[] getNewLineBytes() {
        return "\n".getBytes();
    }

    protected static String getQuoted(String s) {
        final String s3 = "'"+ s + "'" ;
        return s3;
    }

    // not only does it create the string but it has a side effect - it creates a file from the string,
    // checks for its existence and deletes it if it finds it.
    protected String getDbLoadFileStr() {
        String dbLoadFileStr =
        (MSTConfiguration.getUrlPath() + "/db_load.in").replace('\\', '/');

        File dbLoadFile = new File(dbLoadFileStr);
        if (dbLoadFile.exists()) {
            dbLoadFile.delete();
        }
        return dbLoadFileStr;
    }

    /**
     * had an issue inserting a file that looked like this:
     *   0120546507\     30232779
     *   Adding quotes around it worked for the insert, but don't want the string altered in the db, and don't want to store all
     *   those unnecessary chars either.  So before it gets here, string could have '\' around it.  Use:
     *   optionally enclosed by '\''
     *   as an additional field parameter to cover that possibility.  Then db on insert won't place the quotes into the db.

     *   '0120546507\'     30232779
     *
     *   But STILL, a problem with the trailing backslash.  So replace backslashes by double backslashes at the time of the quoting.  Ideally, you
     *   only replace single backslashes with doubles but that is a complicated regular expression lets see if we need it first.
     *
     * @param tableName
     * @param dbLoadFileStr
     */
    protected void replaceIntoTable(String tableName, String dbLoadFileStr) {

        TimingLogger.start(tableName + ".insert.create_infile");
        TimingLogger.start(tableName + ".insert.load_infile");
        this.jdbcTemplate.execute(
                "load data infile '" + dbLoadFileStr + "' REPLACE into table " +
                        tableName +
                        " character set utf8 fields terminated by '\\t' optionally enclosed by '\\'' lines terminated by '\\n'"
                );
        TimingLogger.stop(tableName + ".insert.load_infile");
        TimingLogger.stop(tableName + ".insert.create_infile");
    }

    /**
     *   bib_records
     *   purpose: provides a mapping of input records to output records. This allows for 2 paths:
     *
     *     -------------------------------------------------------------------
     *     | given               | can be determined                           |
     *     |-------------------------------------------------------------------|
     *     | an output_record_id | all the input_records that have been merged |
     *     |                     | together to create this output_record       |
     *     |-------------------------------------------------------------------|
     *     | an input_record_id  | all the other input_records that have been  |
     *     |                     | merged with this input_record and the       |
     *     |                     | corresponding output_record                 |
     *      -------------------------------------------------------------------
     *
     * @param output_record_id
     * @return all the input_records that have been merged together to create this output_record, possibly on 1 record.
     */
    public List<Long> getInputRecordsMappedToOutputRecord(Long output_record_id) {
        TimingLogger.start("MarcAggregationServiceDAO.getInputRecordsMappedToOutputRecord");
        String sql = "select input_record_id from " + bib_records_table +
                            " where output_record_id = ? ";
        final List<Long> results =this.jdbcTemplate.queryForList(sql, Long.class, output_record_id);
        TimingLogger.stop("MarcAggregationServiceDAO.getInputRecordsMappedToOutputRecord");
        return results;
    }

    /**
     * what output record corresponds to this input record?
     * @param input_record_id
     * @return there will only be 1 record number returned.
     */
    public List<Long> getOutputRecordForInputRecord(Long input_record_id) {
        TimingLogger.start("MarcAggregationServiceDAO.getOutputRecordForInputRecord");
        String sql = "select output_record_id from " + bib_records_table +
                            " where input_record_id = ? ";
        final List<Long> results =this.jdbcTemplate.queryForList(sql, Long.class, input_record_id);
        TimingLogger.stop("MarcAggregationServiceDAO.getOutputRecordForInputRecord");
        return results;
    }

    private TLongLongHashMap getBibRecords(int page) {
        TimingLogger.start("MarcAggregationServiceDAO.getBibRecords");

        String sql = "select input_record_id, output_record_id from " + bib_records_table +
                " limit " + (page * RECORDS_AT_ONCE) + "," + RECORDS_AT_ONCE;

        LOG.info(sql);
        List<Map<String, Object>> rowList = this.jdbcTemplate.queryForList(sql);
        TLongLongHashMap results = new TLongLongHashMap();
        for (Map<String, Object> row : rowList) {
            Long in_id = (Long) row.get("input_record_id");
            Long out_id = (Long) row.get("output_record_id");
            results.put(in_id, out_id);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getBibRecords");
        return results;
    }

    public TLongLongHashMap getBibRecordsCache() {
        TimingLogger.start("getBibRecordsCache");
        int page = 0;
        TLongLongHashMap  records = getBibRecords(page);
        boolean gotResults = records != null && records.size() > 0;
        while (gotResults) {
            TLongLongHashMap _records = getBibRecords(++page);
            if (_records != null && _records.size() > 0) {
                records.putAll(_records);
            }
            else {
                gotResults = false;
            }
        }
        TimingLogger.stop("getBibRecordsCache");
        return records;
    }

    /**
     * use to load into memory at service start time.
     * @return
     */
    private TLongLongHashMap getLccnRecords(int page, Long id) {
        TimingLogger.start("MarcAggregationServiceDAO.getLccnRecords");

        String sql = "select input_record_id, numeric_id from " + matchpoints_010a_table;
        if (id != null) {
        	sql += " where input_record_id = ? ";
        }      
    	sql += " limit " + (page * RECORDS_AT_ONCE) + "," + RECORDS_AT_ONCE;
        LOG.info(sql);

        List<Map<String, Object>> rowList;
        if (id != null) {
        	rowList = this.jdbcTemplate.queryForList(sql, new Object[] {id});
        } else {
        	rowList = this.jdbcTemplate.queryForList(sql);
        }
        TLongLongHashMap results = new TLongLongHashMap();
        for (Map<String, Object> row : rowList) {
            Long in_id = (Long) row.get("input_record_id");
            Long num_id = (Long) row.get("numeric_id");
            results.put(in_id, num_id);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getLccnRecords");
        return results;
    }

    public TLongLongHashMap getLccnRecordsCache() {
    	return getLccnRecordsCache(null);
    }
    
    public TLongLongHashMap getLccnRecordsCache(Long id) {
        TimingLogger.start("getLccnRecordsCache");
        int page = 0;
        TLongLongHashMap  records = getLccnRecords(page, id);
        boolean gotResults = records != null && records.size() > 0;
        while (gotResults) {
            TLongLongHashMap _records = getLccnRecords(++page, id);
            if (_records != null && _records.size() > 0) {
                records.putAll(_records);
            }
            else {
                gotResults = false;
            }
        }
        TimingLogger.stop("getLccnRecordsCache");
        return records;
    }

    /**
     * use to load into memory at service start time.
     * @return
     */
    private Map<Long, List<SCNData>> getSCCNRecords(int page, Long id) {
        TimingLogger.start("MarcAggregationServiceDAO.getSCCNRecords");

        String sql = "select full_string, prefix_id, numeric_id, input_record_id from " +
                matchpoints_035a_table;
        if (id != null) {
        	sql += " where input_record_id = ? ";
        }       
    	sql += " limit " + (page * RECORDS_AT_ONCE) + "," + RECORDS_AT_ONCE;
        LOG.info(sql);

        List<Map<String, Object>> rowList;
        if (id != null) {
        	rowList = this.jdbcTemplate.queryForList(sql, new Object[] {id});
        } else { 
        	rowList = this.jdbcTemplate.queryForList(sql);
        }
        Map<Long, List<SCNData>> results = new TreeMap<Long, List<SCNData>>();
        for (Map<String, Object> row : rowList) {
            String full = (String) row.get("full_string");
            Integer prefix_id = ((Long) row.get("prefix_id")).intValue();
            String num_id = (String) row.get("numeric_id");
            Long in_id = (Long) row.get("input_record_id");
            SCNData goods = new SCNData(prefix_id, num_id, full);

            List<SCNData> goodsList = results.get(in_id);
            if (goodsList == null || goodsList.size() == 0) {
                goodsList = new ArrayList<SCNData>();
                goodsList.add(goods);
                results.put(in_id, goodsList);
            }
            else if (!goodsList.contains(goods)) {
                goodsList.add(goods);
                results.put(in_id, goodsList);
            }
            else {
                LOG.debug("we have already seen " + goods + " for recordId: " + in_id);
            }
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getSCCNRecords");
        return results;
    }

    public Map<Long, List<SCNData>> getSCCNRecordsCache() {
    	return getSCCNRecordsCache(null);
    }
    
    public Map<Long, List<SCNData>> getSCCNRecordsCache(Long id) {
        TimingLogger.start("getSCCNRecordsCache");
        int page = 0;
        Map<Long, List<SCNData>>  records = getSCCNRecords(page, id);
        boolean gotResults = records != null && records.size() > 0;
        while (gotResults) {
            //got to go through and look for common id's
            Map<Long, List<SCNData>> _records = getSCCNRecords(++page, id);
            if (_records != null && _records.size() > 0) {
                for (Long key: _records.keySet()) {
                    // maybe its as simple as this:
                    //records.putAll(_records);
                    // but I think I have to check carefully as 1 input_id can have
                    // mult. 035's and could be unlucky enough to have db return them in sep. pages.
                    List<SCNData> goodsList = _records.get(key);
                    if (records.containsKey(key)) {
                        goodsList.addAll(records.get(key));
                    }
                    records.put(key, goodsList);
                }
            }
            else {
                gotResults = false;
            }
        }
        TimingLogger.stop("getSCCNRecordsCache");
        return records;
    }
    
    /*
     * Used to load "generic" matchpoints data (string_id, input_record_id)
     */
    
    private Map<Long, List<String>> get1StrMachpointRecords(int page, Long id, String tableName) {
        TimingLogger.start("MarcAggregationServiceDAO.get1StrMatchpointsRecordsCache");

        String sql = "select input_record_id, string_id from " + tableName;
        if (id != null) {
        	sql += " where input_record_id = ? ";
        }      
    	sql += " limit " + (page * RECORDS_AT_ONCE) + "," + RECORDS_AT_ONCE;
        LOG.info(sql);

        List<Map<String, Object>> rowList;
        if (id != null) {
        	rowList = this.jdbcTemplate.queryForList(sql, new Object[] {id});
        } else {
        	rowList = this.jdbcTemplate.queryForList(sql);
        }
        Map<Long, List<String>> results = new HashMap<Long, List<String>>();
        for (Map<String, Object> row : rowList) {
            Long in_id = (Long) row.get("input_record_id");
            String string_id = (String) row.get("string_id");
            
            List<String> goodsList = results.get(in_id);
            if (goodsList == null || goodsList.size() == 0) {
                goodsList = new ArrayList<String>();
                goodsList.add(string_id);
                results.put(in_id, goodsList);
            }
            else if (!goodsList.contains(in_id)) {
                goodsList.add(string_id);
                results.put(in_id, goodsList);
            }
            else {
                LOG.debug("we have already seen " + string_id + " for recordId: " + in_id);
            }
        }
        TimingLogger.stop("MarcAggregationServiceDAO.get1StrMatchpointsRecordsCache");
        return results;
    }

    public Map<Long, List<String>> get1StrMatchpointsRecordsCache(String tableName) {
    	return get1StrMatchpointsRecordsCache(null, tableName);
    }
    
    public Map<Long, List<String>> get1StrMatchpointsRecordsCache(Long id, String tableName) {
        TimingLogger.start("get1StrMatchpointsRecordsCache");
        int page = 0;
        Map<Long, List<String>>  records = get1StrMachpointRecords(page, id, tableName);
        boolean gotResults = records != null && records.size() > 0;
        while (gotResults) {
        	Map<Long, List<String>> _records = get1StrMachpointRecords(++page, id, tableName);
            if (_records != null && _records.size() > 0) {
                records.putAll(_records);
            }
            else {
                gotResults = false;
            }
        }
        TimingLogger.stop("get1StrMatchpointsRecordsCache");
        return records;
    }

    public List<Long> getMergedInputRecordsCache() {
        TimingLogger.start("getMergedInputRecordsCache");
        int page = 0;
        List<Long> records = getMergedInputRecords(page);
        boolean gotResults = records != null && records.size() > 0;
        while (gotResults) {
            List<Long> _records = getMergedInputRecords(++page);
            if (_records != null && _records.size() > 0) {
                records.addAll(_records);
            }
            else {
                gotResults = false;
            }
        }
        TimingLogger.stop("getMergedInputRecordsCache");
        return records;
    }

    /**
     * input records that are part of a merge set (>1 corresponds to an output record)
     * @return
     */
    private List<Long> getMergedInputRecords(int page) {
        TimingLogger.start("MarcAggregationServiceDAO.getMergedInputRecords");

      String sql = "select input_record_id from " + merged_records_table +
              " limit " + (page * RECORDS_AT_ONCE) + "," + RECORDS_AT_ONCE;
      LOG.info(sql);
      List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(sql);
      List<Long> results = new ArrayList<Long>();
      if (rows != null) {
          for (Map<String, Object> row : rows) {
              results.add((Long) row.values().iterator().next());
          }
      }
      TimingLogger.stop("MarcAggregationServiceDAO.getMergedInputRecords");
      return results;
    }

    public Map<Integer, String> getPrefixes() {
        TimingLogger.start("MarcAggregationServiceDAO.getPrefixes");

        String sql = "select prefix_id, prefix from " + prefixes_035a_table; // +
        //        " limit " + (page * RECORDS_AT_ONCE) + "," + RECORDS_AT_ONCE;
        //LOG.info(sql);

        List<Map<String, Object>> rowList = this.jdbcTemplate.queryForList(sql);
        Map<Integer, String> results = new TreeMap<Integer, String>();
        for (Map<String, Object> row : rowList) {
            Long id = (Long) row.get("prefix_id");
            String prefix = (String) row.get("prefix");
            Integer id_i = id.intValue();
            results.put(id_i,prefix);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getPrefixes");
        return results;
    }

    //TODO
    /*
    public TLongObjectHashMap<RecordOfSourceData> getScores() {

    }
    */

    public RecordOfSourceData getScoreData(Long num) {
        TimingLogger.start("MarcAggregationServiceDAO.getScoreData");

        final String tableName = merge_scores_table;

        String sql = "select "+ leaderByte17_field +", "+ size_field +
                " from " + tableName+ " where "+ input_record_id_field +" = ?";

        List<RecordOfSourceData> rowList = this.jdbcTemplate.query(sql, new Object[] {num}, new RecordOfSourceDataMapper());

        final int size = rowList.size();
        if (size == 0) {
            LOG.error("No rows returned for merge_scores for "+num);
            return null;
        }
        else if (size>1) {
            // enforce through schema?
            LOG.error("multiple rows returned for merge_scores for "+num);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getScoreData");

        return rowList.get(0);
    }

    /**
     * about RowMapper:
     * An interface used by JdbcTemplate for mapping rows of a ResultSet on a per-row basis.
     * @see RowMapper
     * @author John Brand
     *
     */
    private static final class RecordOfSourceDataMapper implements RowMapper<RecordOfSourceData> {

        public RecordOfSourceData mapRow(ResultSet rs, int rowNum) throws SQLException {
            RecordOfSourceData source;
            char encoding;

            // the ' ' is not getting into the db. Is it a big deal, or is this
            // hack good enough?
            //
            if (StringUtils.isNotEmpty(rs.getString("leaderByte17"))) {
                encoding = rs.getString("leaderByte17").charAt(0);
            }
            else encoding=' ';

            source = new RecordOfSourceData(encoding,rs.getInt("size"));
            return source;
        }
    }

    /**
     * given a string_id in String form to match on. (currently used by ISSN, ISBN, SCCN, x024 matchers)
     * note - this method adds the quoting, which was added for ISBN 020$a others don't necessarily need it (depending on how they were inserted)
     *
     *  for instance:
     * mysql -u root --password=root -D xc_marcaggregation -e 'select input_record_id  from matchpoints_035a where string_id = "24094664" '
     *
     * @param tableName
     * @param record_id_field
     * @param string_id_field
     * @param itemToMatch
     * @return
     */
    public List<Long> getMatchingRecords(String tableName, String record_id_field, String string_id_field, String itemToMatch) {
        TimingLogger.start("MarcAggregationServiceDAO.getMatchingRecords");

        String sql = "select "+ record_id_field + " from " + tableName+ " where "+ string_id_field+ " = ?";

        List<Map<String, Object>> rowList = this.jdbcTemplate.queryForList(sql, new Object[] {itemToMatch});

        List<Long> results = new ArrayList<Long>();
        for (Map<String, Object> row : rowList) {
            Long id = (Long) row.get("input_record_id");
            results.add(id);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getMatchingRecords");
        return results;
    }

    /**
     * given a numeric_id in String form to match on.
     * note - this method adds the quoting, which was added for ISBN 020$a others don't necessarily need it (depending on how they were inserted)
     *
     *  for instance:
     * mysql -u root --password=root -D xc_marcaggregation -e 'select input_record_id  from matchpoints_035a where prefix_id = "0" and numeric_id = "24094664" '
     *
     * @param tableName
     * @param record_id_field
     * @param string_id_field
     * @param itemToMatch
     * @return
     */
    public List<Long> getMatchingSCCNRecords(String tableName, String record_id_field, String _numeric_id_field, String _prefix_id_field,SCNData itemsToMatch) {
        TimingLogger.start("MarcAggregationServiceDAO.getMatchingSCCNRecords");

        String sql = "select "+ record_id_field + " from " + tableName+ " where "+ _prefix_id_field+ " = ?" + " and "+ _numeric_id_field+ " = ?";

        List<Map<String, Object>> rowList = this.jdbcTemplate.queryForList(sql, new Object[] {itemsToMatch.prefixNum, itemsToMatch.scn});

        List<Long> results = new ArrayList<Long>();
        for (Map<String, Object> row : rowList) {
            Long id = (Long) row.get("input_record_id");
            results.add(id);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getMatchingSCCNRecords");
        return results;
    }

    /**
     * given a numeric_id in Long form to match on. (currently used by LCCN matcher)
     *
     * for instance:
     * mysql -u root --password=root -D xc_marcaggregation -e 'select input_record_id  from matchpoints_035a where string_id = "24094664" '
     *
     * @param tableName
     * @param record_id_field for query
     * @param numeric_id_field for query
     * @param itemToMatch
     * @return
     */
    public List<Long> getMatchingRecords(String tableName, String record_id_field, String numeric_id_field, Long itemToMatch) {
        TimingLogger.start("MarcAggregationServiceDAO.getMatchingRecords");

        String sql = "select "+ record_id_field + " from " + tableName+ " where "+ numeric_id_field +" = ?";

        List<Map<String, Object>> rowList = this.jdbcTemplate.queryForList(sql, new Object[] {itemToMatch});

        List<Long> results = new ArrayList<Long>();
        for (Map<String, Object> row : rowList) {
            Long id = (Long) row.get("input_record_id");
            results.add(id);
        }
        TimingLogger.stop("MarcAggregationServiceDAO.getMatchingRecords");
        return results;
    }

    public int getNumRecords(String tableName) {
        return this.jdbcTemplate.queryForInt("select count(*) from " + tableName);
    }

    public int getNumUniqueStringIds(String tableName) {
        return this.jdbcTemplate.queryForInt("select count(distinct string_id) from " + tableName);
    }

    public int getNumUniqueNumericIds(String tableName) {
        return this.jdbcTemplate.queryForInt("select count(distinct numeric_id) from " + tableName);
    }

    public int getNumUniqueRecordIds(String tableName) {
        return this.jdbcTemplate.queryForInt("select count(distinct input_record_id) from " + tableName);
    }

    /***
     * Generically, given a input_record_id, delete that row (or rows) from given table.
     * All the tables we will delete from with this method have 'input_record_id' column.
     * @param name
     */
    public void deleteMergeRow(String table, Long input_record_id) {
        this.jdbcTemplate.update(
                "delete from " + table + " where "+input_record_id_field+" = ? ", input_record_id);
    }

    /**
     * call this one if a record is deleted or perhaps updated
     * @param input_record_id
     */
    public void deleteAllMASRecordDetails(Long input_record_id) {

        TimingLogger.start("MarcAggregationServiceDAO.deleteAllMASRecordDetails");

        deleteAllMatchpointDetails(input_record_id);
        deleteAllMergeDetails(     input_record_id);

        TimingLogger.stop("MarcAggregationServiceDAO.deleteAllMASRecordDetails");
    }

    /**
     * call this one if a merge set has to be reformed because
     * a different member record was deleted or perhaps updated
     * @param input_record_id
     */
    public void deleteAllMergeDetails(Long input_record_id) {

        TimingLogger.start("MarcAggregationServiceDAO.deleteAllMergeDetails");

        deleteMergeRow(merge_scores_table,     input_record_id);
        deleteMergeRow(merged_records_table,   input_record_id);
        deleteMergeRow(bib_records_table,      input_record_id);

        TimingLogger.stop("MarcAggregationServiceDAO.deleteAllMergeDetails");
    }

    /**
     * call this one if a merge set has to be reformed because
     * a different member record was deleted or perhaps updated
     * don't want to delete the merge_scores because the record_id
     * still exists in the system, just want to break its bonds
     * to the old merge.
     *
     * @param input_record_id
     */
    public void deleteMergeMemberDetails(Long input_record_id) {

        TimingLogger.start("MarcAggregationServiceDAO.deleteMergeMemberDetails");

        deleteMergeRow(merged_records_table,   input_record_id);
        deleteMergeRow(bib_records_table,      input_record_id);

        TimingLogger.stop("MarcAggregationServiceDAO.deleteMergeMemberDetails");
    }

    /**
     * leave it private till we see if we need it on the outside.
     * @param input_record_id
     */
    private void deleteAllMatchpointDetails(Long input_record_id) {
        deleteMergeRow(matchpoints_010a_table, input_record_id);
        deleteMergeRow(matchpoints_020a_table, input_record_id);
        deleteMergeRow(matchpoints_022a_table, input_record_id);
        deleteMergeRow(matchpoints_024a_table, input_record_id);
        deleteMergeRow(matchpoints_035a_table, input_record_id);
    }



    /*
     * a convoluted query to find merged records (>1 input_record_id for a given output_record_id)
     * (there is probably a VERY simple query to do this!)
     *
    mysql -u root --password=root -D xc_marcaggregation -e
    'select a.* from merged_records as a left join merged_records as b using(output_record_id) where  a.input_record_id != b.input_record_id'

     * also, not 100% clear how fast this will be, so for now I am adding another table instead, that just contains the merged_input_id's (or should
     * it be merged output_id's?
     */

    /**
     * reserved, in case there is ability and need to do it
     */
    public void loadMaps(
        ) {
    }
    
    
    public void createIndicesIfNecessary() {
        TimingLogger.start("MarcAggregationServiceDAO.createIndicesIfNecessary");

    	boolean createdIndices = false;
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList("show indexes from " + MarcAggregationServiceDAO.matchpoints_020a_table);
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                String indexName = (String) row.get("Key_name");
                LOG.debug("indexName: " + indexName);
                if ("idx_mp_020a_string_id".equals(indexName)) {
                	createdIndices = true;
                	break;
                }
            }
        }

        if (!createdIndices) {
            String[] indices2create = new String[] {
                    "create index idx_mp_010a_numeric_id on " + MarcAggregationServiceDAO.matchpoints_010a_table + " (numeric_id)",

                    "create index idx_mp_020a_string_id on " + MarcAggregationServiceDAO.matchpoints_020a_table + " (string_id)",
                    
                    "create index idx_mp_022a_string_id on " + MarcAggregationServiceDAO.matchpoints_022a_table + " (string_id)",
                    
                    "create index idx_mp_024a_string_id on " + MarcAggregationServiceDAO.matchpoints_024a_table + " (string_id)",
                    
                    "create index idx_mp_035a_prefix on " + MarcAggregationServiceDAO.prefixes_035a_table + " (prefix)",
                    
                    "create index idx_mp_035a_numeric_id on " + MarcAggregationServiceDAO.matchpoints_035a_table + " (numeric_id)",
                    "create index idx_mp_035a_prefix_id on " + MarcAggregationServiceDAO.matchpoints_035a_table + " (prefix_id)",
                    
            };
            for (String i2c : indices2create) {
                TimingLogger.start(i2c.split(" ")[2]);
                try {
                    this.jdbcTemplate.execute(i2c);
                } catch (Throwable t) {
                    LOG.error("", t);
                }
                TimingLogger.stop(i2c.split(" ")[2]);
            }
        }
        
        TimingLogger.stop("MarcAggregationServiceDAO.createIndicesIfNecessary");
        TimingLogger.reset();
    }
}
