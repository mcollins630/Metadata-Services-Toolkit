/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.manager.record;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import xc.mst.bo.harvest.HarvestSchedule;
import xc.mst.bo.provider.Set;
import xc.mst.bo.record.Record;
import xc.mst.bo.service.Service;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.dao.harvest.DefaultHarvestDAO;
import xc.mst.dao.harvest.DefaultHarvestScheduleDAO;
import xc.mst.dao.harvest.HarvestDAO;
import xc.mst.dao.harvest.HarvestScheduleDAO;
import xc.mst.dao.provider.DefaultFormatDAO;
import xc.mst.dao.provider.DefaultProviderDAO;
import xc.mst.dao.provider.DefaultSetDAO;
import xc.mst.dao.provider.FormatDAO;
import xc.mst.dao.provider.ProviderDAO;
import xc.mst.dao.provider.SetDAO;
import xc.mst.dao.record.DefaultXcIdentifierForFrbrElementDAO;
import xc.mst.dao.record.XcIdentifierForFrbrElementDAO;
import xc.mst.dao.service.DefaultServiceDAO;
import xc.mst.dao.service.ServiceDAO;
import xc.mst.manager.IndexException;
import xc.mst.utils.index.RecordList;

/**
 * Solr implementation of the service class to query, add, update and
 * delete records from an index.
 *
 * @author Eric Osisek
 */
public class DefaultRecordService extends RecordService
{
	/**
	 * Data access object for getting sets
	 */
	private SetDAO setDao = new DefaultSetDAO();

	/**
	 * Data access object for getting formats
	 */
	private FormatDAO formatDao = new DefaultFormatDAO();

	/**
	 * Data access object for getting providers
	 */
	private ProviderDAO providerDao = new DefaultProviderDAO();

	/**
	 * Data access object for getting harvests
	 */
	private HarvestDAO harvestDao = new DefaultHarvestDAO();
	
	/**
	 * Data access object for getting harvest schedules
	 */
	private HarvestScheduleDAO harvestScheduleDao = new DefaultHarvestScheduleDAO();

	/**
	 * Data access object for getting services
	 */
	private ServiceDAO serviceDao = new DefaultServiceDAO();

	/**
	 * Data access object for getting FRBR level IDs
	 */
	protected static XcIdentifierForFrbrElementDAO frbrLevelIdDao = new DefaultXcIdentifierForFrbrElementDAO();

	/**
	 * The FRBR level ID term
	 */
	protected final static Term TERM_FRBR_LEVEL_ID = new Term(FIELD_FRBR_LEVEL_ID, "");

	/**
	 * The up link term
	 */
	protected final static Term TERM_UP_LINK = new Term(FIELD_UP_LINK, "");

	/**
	 * The trait term
	 */
	protected final static Term TERM_TRAIT = new Term(FIELD_TRAIT, "");

	@Override
	public RecordList getAll() throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records");

		// Create a query to get all records
		SolrQuery query = new SolrQuery().setQuery("*:*");

		// Return the list of results
		return new RecordList(query);
	} // end method getAll()

	@Override
	public Record getById(long id) throws DatabaseConfigException, IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting the record with ID " + id);

		// Create a query to get the record with the requested record ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_RECORD_ID + ":" + Long.toString(id));

		// Get the result of the query
		SolrDocumentList docs = null;
		docs = indexMgr.getDocumentList(query);

		// Return null if we couldn't find the record with the correct ID
		if(docs == null || docs.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find the record with ID " + id + ".");

			return null;
		} // end if(record not found)

		if(log.isDebugEnabled())
			log.debug("Parcing the record with ID " + id + " from the Lucene Document it was stored in.");

		return getRecordFromDocument(docs.get(0));
	} // end method getById(long)

	@Override
	public Record loadBasicRecord(long id) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting the record with ID " + id);

		// Create a query to get the record with the requested record ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_RECORD_ID + ":" + Long.toString(id));

		// Get the result of the query
		SolrDocumentList docs = null;
		docs = indexMgr.getDocumentList(query);

		// Return null if we couldn't find the record with the correct ID
		if(docs == null || docs.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find the record with ID " + id + ".");

			return null;
		} // end if(record not found)

		if(log.isDebugEnabled())
			log.debug("Parcing the record with ID " + id + " from the Lucene Document it was stored in.");

		return getBasicRecordFromDocument(docs.get(0));
	} // end method loadBasicRecord(long)

	@Override
	public RecordList getByLuceneQuery(String queryString)throws ParseException, IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records that match the query " + queryString);

		SolrQuery query = new SolrQuery().setQuery(queryString);

		// Return the list of results
		return new RecordList(query);
	} // end method getByLuceneQuery(String)

	@Override
	public RecordList getByProviderId(int providerId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with provider ID " + providerId);

		// Create a query to get the Documents with the requested provider ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_PROVIDER_ID + ":" + Integer.toString(providerId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByProviderId(int)

	@Override
	public RecordList getByServiceId(int serviceId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with service ID " + serviceId);

		// Create a query to get the Documents with the requested service ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_SERVICE_ID + ":" +  Integer.toString(serviceId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByServiceId(int)

	@Override
	public long getNumberOfRecordsByServiceId(int serviceId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with service ID " + serviceId);
		
		// Create a query to get the Documents with the requested service ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_SERVICE_ID + ":" +  Integer.toString(serviceId));

		// Return the list of results
		return new RecordList(query).size();
	} // end method getByServiceId(long)
	
	@Override
	public RecordList getByProcessingServiceId(int serviceId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with processing service ID " + serviceId);

		// Create a query to get the Documents with the requested service ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_PROCESSED_BY_SERVICE_ID + ":" +  Integer.toString(serviceId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByProcessingServiceId(int)
	
	@Override
	public RecordList getByHarvestId(int harvestId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with harvest ID " + harvestId);

		// Create a query to get the Documents with the requested harvest ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_HARVEST_ID + ":" + Integer.toString(harvestId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByHarvestId(int)

	@Override
	public RecordList getByHarvestScheduleId(int harvestScheduleId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with harvest schedule ID " + harvestScheduleId);

		// Create a query to get the Documents with the requested harvest schedule ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_HARVEST_SCHEDULE_ID + ":" +  Integer.toString(harvestScheduleId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByHarvestScheduleId(int)

	@Override
	public RecordList getByFormatIdAndServiceId(int formatId, int serviceId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with format ID " + formatId + " and service ID " + serviceId);

		// Create a query to get the Documents with the requested format ID and service ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_FORMAT_ID + ":" + Integer.toString(formatId) + " AND "
				+ FIELD_SERVICE_ID + ":" + Integer.toString(serviceId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByFormatIdAndServiceId(int, int)

	@Override
	public RecordList getInputForService(int serviceId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records that are input for the service with service ID " + serviceId);

		// Create a query to get the Documents with the requested input for service IDs
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_INPUT_FOR_SERVICE_ID + ":" + Integer.toString(serviceId));

		// Return the list of results
		return new RecordList(query);
	} // end method getInputForService(int)

	@Override
	public RecordList getByProviderName(String providerName) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records harvested from the provider with the name " + providerName);

		// Create a query to get the Documents with the requested provider name
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_PROVIDER_NAME + ":" + providerName);

		// Return the list of results
		return new RecordList(query);
	} // end method getByProviderName(String)

	@Override
	public RecordList getByProviderUrl(String providerUrl) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records harvested from the provider with the URL " + providerUrl);

		// Create a query to get the Documents with the requested provider URL
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_PROVIDER_URL + ":" + providerUrl);

		// Return the list of results
		return new RecordList(query);
	} // end method getByProviderUrl(String)
	
	@Override
	public RecordList getBySetName(String setName) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records from the set with the name " + setName);

		// Create a query to get the Documents with the requested set name
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_SET_NAME + ":" + setName);

		// Return the list of results
		return new RecordList(query);
	} // end method getBySetName(String)

	@Override
	public RecordList getBySetSpec(String setSpec) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records from the set with the setSpec " + setSpec);

		// Create a query to get the Documents with the requested set spec
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_SET_SPEC  + ":" + setSpec.replaceAll(":", "\\\\:"));

		// Return the list of results
		return new RecordList(query);
	} // end method getBySetSpec(String)

	@Override
	public RecordList getByFormatName(String formatName) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records from the format with the name " + formatName);

		// Create a query to get the Documents with the requested format name
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_FORMAT_NAME + ":" + formatName);

		// Return the list of results
		return new RecordList(query);
	} // end method getByFormatName(String)

	@Override
	public Record getByOaiIdentifier(String identifier) throws DatabaseConfigException, IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting the record with the OAI identifier " + identifier);

		// Create a query to get the record with the correct identifier
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_OAI_IDENTIFIER + ":" + identifier.replaceAll(" ", "_").replaceAll(":", "\\\\:"));

		// Get the result of the query
		RecordList records = new RecordList(query);

		// Return null if we couldn't find the record
		if(records == null || records.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find the record with the OAI identifier " + identifier + ".");

			return null;
		} // end if(record not found)

		if(log.isDebugEnabled())
			log.debug("Parcing the record with the OAI identifier " + identifier + " from the Lucene Document it was stored in.");

		return records.get(0);
	} // end method getByOaiIdentifier(String)

	@Override
	public Record getByOaiIdentifierAndProvider(String identifier, int providerId) throws DatabaseConfigException, IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting the record with the OAI identifier " + identifier + " and provider ID " + providerId);

		// Create a query to get the record with the requested OAI identifier and provider ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_OAI_IDENTIFIER + ":" + identifier.replaceAll(" ", "_").replaceAll(":", "\\\\:") + " AND "
				+ FIELD_PROVIDER_ID + ":" + Integer.toString(providerId));

		// Get the result of the query
		RecordList records = new RecordList(query);
		
		// Return null if we couldn't find the record
		if(records == null || records.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find the record with the OAI identifier " + identifier  + " and provider ID " + providerId + ".");

			return null;
		} // end if(record not found)

		if(log.isDebugEnabled())
			log.debug("Parcing the record with the OAI identifier " + identifier + " and provider ID " + providerId + " from the Lucene Document it was stored in.");

		return records.get(0);
	} // end method getByOaiIdentifierAndProvider(String, int)

	@Override
	public Record getByOaiIdentifierAndService(String identifier, int serviceId) throws DatabaseConfigException, IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting the record with the OAI identifier " + identifier + " and service ID " + serviceId);

		// Create a query to get the record with the requested OAI identifier and service ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_OAI_IDENTIFIER + ":" + identifier.replaceAll(" ", "_").replaceAll(":", "\\\\:") + " AND "
				+ FIELD_SERVICE_ID + ":" + Integer.toString(serviceId));
		
		// Get the result of the query
		RecordList records = new RecordList(query);

		// Return null if we couldn't find the record
		if(records == null || records.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find the record with the OAI identifier " + identifier  + " and service ID " + serviceId + ".");

			return null;
		} // end if(record not found)

		if(log.isDebugEnabled())
			log.debug("Parcing the record with the OAI identifier " + identifier + " and service ID " + serviceId + " from the Lucene Document it was stored in.");

		return records.get(0);
	} // end method getByOaiIdentifierAndService(String, int)

	@Override
	public Record getInputForServiceByOaiIdentifier(String identifier, int serviceId) throws DatabaseConfigException, IndexException 
	{
		if(log.isDebugEnabled())
			log.debug("Getting the input record with the OAI identifier " + identifier + " and service ID " + serviceId);

		// Create a query to get the record with the requested OAI identifier and service ID
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_OAI_IDENTIFIER + ":" + identifier.replaceAll(" ", "_").replaceAll(":", "\\\\:") + " AND "
				+ FIELD_PROCESSED_BY_SERVICE_ID + ":" + Integer.toString(serviceId));
		
		// Get the result of the query
		RecordList records = new RecordList(query);

		// Return null if we couldn't find the record
		if(records == null || records.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find the input record with the OAI identifier " + identifier  + " and service ID " + serviceId + ".");

			return null;
		} // end if(record not found)

		if(log.isDebugEnabled())
			log.debug("Parcing the input record with the OAI identifier " + identifier + " and service ID " + serviceId + " from the Lucene Document it was stored in.");

		return records.get(0);
	}

	@Override
	public RecordList getByProcessedFrom(long processedFromId) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records that were processed from the record with ID " + processedFromId);

		// Create a query to get the Documents with the requested input for service IDs
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_PROCESSED_FROM + ":" + Long.toString(processedFromId));

		// Return the list of results
		return new RecordList(query);
	} // end method getByProcessedFrom(long)

	@Override
	public RecordList getByTrait(String trait) throws IndexException
	{
		if(log.isDebugEnabled())
			log.debug("Getting all records with the trait " + trait);

		// Create a query to get the Documents with the requested trait
		SolrQuery query = new SolrQuery();
		query.setQuery(FIELD_TRAIT + ":" + trait.replaceAll(" ", "_").replaceAll(":", "\\\\:"));

		// Return the list of results
		return new RecordList(query);
	} // end method getByTrait(String)

	@Override
	public Record getEarliest(int serviceId)
	{
		// TODO Implement for Solr
		return null;
	} // end method getEarliest(int)

	@Override
	public long getCount(Date fromDate, Date untilDate, int setId, int formatId, int serviceId) throws IndexException
	{
		Date from; // fromDate, or the minimum value for a Date if fromDate is null
		Date until; // toDate, or now if toDate is null

		// If from is null, set it to the minimum possible value
		// Otherwise set it to the same value as fromDate
		if(fromDate == null)
			from = new Date(0);
		else
			from = fromDate;

		// If to is null, set it to now
		// Otherwise set it to the same value as toDate
		if(untilDate == null)
			until = new Date();
		else
			until = untilDate;

		// True if we're getting the count for a specific set, false if we're getting it for all records
		boolean useSet = (setId > 0);

		// True if we're getting the count for a specific metadataPrefix, false if we're getting it for all records
		boolean useMetadataPrefix = (formatId > 0);

		DateFormat format = DateFormat.getInstance();

		if(log.isDebugEnabled())
			log.debug("Counting the records updated later than " + format.format(from) + " and earlier than " + format.format(until) + (useSet ? "" : " with set ID " + setId) + (useMetadataPrefix ? "" : " with format ID " + formatId));

		// Create a query to get the Documents for unprocessed records
		SolrQuery query = new SolrQuery();
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append(FIELD_SERVICE_ID).append(":").append(Integer.toString(serviceId));
		if(useSet)
			queryBuffer.append(" AND ").append(FIELD_SET_SPEC).append(":").append(Integer.toString(setId));
		if(useMetadataPrefix)
			queryBuffer.append(" AND ").append(FIELD_FORMAT_ID).append(":").append(Integer.toString(formatId));
		
		// TODO
		//if(fromDate != null || untilDate != null)
			//queryBuffer.append(new ConstantScoreRangeQuery(FIELD_UPDATED_AT, DateTools.dateToString(from, DateTools.Resolution.SECOND), DateTools.dateToString(until, DateTools.Resolution.SECOND), true, true), Occur.MUST);

		query.setQuery(queryBuffer.toString());
		
		// Remove the limit on the number of results returned
		query.setRows(Integer.MAX_VALUE);
		
		// Get the result of the query
		RecordList records = new RecordList(query);

		if(log.isDebugEnabled())
			log.debug("Found " + records.size() + " records updated later than " + format.format(from) + " and earlier than " + format.format(until) + (useSet ? "" : " with set ID " + setId) + (useMetadataPrefix ? "" : " with format ID " + formatId));

		// Return the list of results
		return records.size();
	} // end method getCount(Date, Date, int, int, int)

	@Override
	public List<Record> getOutgoingRecordsInRange(Date fromDate, Date untilDate, int setId, int formatId, int offset, int numResults, int serviceId)
			throws IndexException
	{
		Date from; // fromDate, or the minimum value for a Date if fromDate is null
		Date until; // toDate, or now if toDate is null

		// If from is null, set it to the minimum possible value
		// Otherwise set it to the same value as fromDate
		if(fromDate == null)
			from = new Date(0);
		else
			from = fromDate;

		// If to is null, set it to now
		// Otherwise set it to the same value as toDate
		if(untilDate == null)
			until = new Date();
		else
			until = untilDate;

		// True if we're getting the records for a specific set, false if we're getting all records
		boolean useSet = (setId > 0);

		// True if we're getting the count for a specific metadataPrefix, false if we're getting it for all records
		boolean useMetadataPrefix = (formatId > 0);

		DateFormat format = DateFormat.getInstance();

		if(log.isDebugEnabled())
			log.debug("Getting the records updated later than " + format.format(from) + " and earlier than " + format.format(until) + (useSet ? "" : " in set with ID " + setId) + (useMetadataPrefix ? "" : " with format ID " + formatId));

		// Create a query to get the Documents for unprocessed records
		SolrQuery query = new SolrQuery();
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append(FIELD_SERVICE_ID).append(":").append(Integer.toString(serviceId));
		if(useSet)
			queryBuffer.append(" AND ").append(FIELD_SET_SPEC).append(":").append(Integer.toString(setId));
		if(useMetadataPrefix)
			queryBuffer.append(" AND ").append(FIELD_FORMAT_ID + ":").append(Integer.toString(formatId));
		
		// TODO
		//if(fromDate != null || untilDate != null)
			//query.add((Query)new ConstantScoreRangeQuery(FIELD_UPDATED_AT, DateTools.dateToString(from, DateTools.Resolution.SECOND), DateTools.dateToString(until, DateTools.Resolution.SECOND), true, true), Occur.MUST);

		query.setQuery(queryBuffer.toString());
		
		// Get the result of the query
		RecordList records = new RecordList(query);

		ArrayList<Record> results = new ArrayList<Record>();

		// Return the empty list if we couldn't find the records
		if(records.size() == 0)
		{
			if(log.isDebugEnabled())
				log.debug("Could not find any records updated later than " + format.format(from) + " and earlier than " + format.format(until) + (useSet ? "" : " in set with ID " + setId) + (useMetadataPrefix ? "" : " for format ID " + formatId));

			return results;
		} // end if(no results found)

		if(log.isDebugEnabled())
			log.debug("Found " + records.size() + " records updated later than " + format.format(from) + " and earlier than " + format.format(until) + (useSet ? "" : " in set with ID " + setId) + (useMetadataPrefix ? "" : " for format ID " + formatId));

		// The upper bound for the results to return
		int upperLimit = (offset+numResults < records.size() ? offset+numResults : records.size());

		// Get each Record from the docs in the target range
		for(int counter = offset; counter < upperLimit; counter++)
		{
			try
			{
				results.add(records.get(counter));
			} // end try(get the record)
			catch(Exception e)
			{
				log.error("An error occurred getting a document from the Lucene index.");
			} // end catch(Exception)
		} // end loop over results in the target range

		return results;
	} // end method getOutgoingRecordsInRange(Date, Date, int, int, int, int, int)

	@Override
	public Record getBasicRecordFromDocument(SolrDocument doc)
	{
		// Create a Record object to store the result
		Record record = new Record();

		// Set the fields on the record Object and return it
		record.setId(Long.parseLong((String)doc.getFieldValue(FIELD_RECORD_ID)));
		record.setFrbrLevelId(Long.parseLong((String)doc.getFieldValue(FIELD_FRBR_LEVEL_ID)));
		record.setDeleted(Boolean.parseBoolean((String)doc.getFieldValue(FIELD_DELETED)));
		record.setOaiDatestamp((String)doc.getFieldValue(FIELD_OAI_DATESTAMP));
		record.setOaiHeader((String)doc.getFieldValue(FIELD_OAI_HEADER));
		record.setOaiIdentifier((String) doc.getFieldValue(FIELD_OAI_IDENTIFIER));
		record.setOaiXml((String)doc.getFieldValue(FIELD_OAI_XML));
		record.setHarvestScheduleName((String)doc.getFieldValue(FIELD_HARVEST_SCHEDULE_NAME));

		try
		{
			if (doc.getFieldValue(FIELD_CREATED_AT) != null)
				record.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse((String)doc.getFieldValue(FIELD_CREATED_AT)));
			if(doc.getFieldValue(FIELD_UPDATED_AT) != null)
				record.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse((String)doc.getFieldValue(FIELD_UPDATED_AT)));
		} // end try(parse created at and updated at dates
		catch (java.text.ParseException e)
		{
			log.error("An error occurred parsing the created at and updated at dates from the Lucene document.", e);
		} // end catch(ParseException)

		Collection<Object> traits = doc.getFieldValues(FIELD_TRAIT);
		if(traits != null) {
			Iterator<Object> itr = traits.iterator();
			while (itr.hasNext()) {
				record.addTrait((String)itr.next());
			}
		}

		// Return the record we parsed from the document
		return record;
	} // end method getBasicRecordFromDocument(Document)

	@Override
	public Record getRecordFromDocument(SolrDocument doc) throws DatabaseConfigException, IndexException
	{

		log.debug("getRecordFromSolrDocument::"+doc);
		// Create a Record object to store the result
		Record record = new Record();

		// The OAI identifier
		String oaiId = (String) doc.getFieldValue(FIELD_OAI_IDENTIFIER);

		// Set the fields on the record Object and return it
		record.setId(Long.parseLong((String)doc.getFieldValue(FIELD_RECORD_ID)));
		record.setFrbrLevelId(Long.parseLong((String)doc.getFieldValue(FIELD_FRBR_LEVEL_ID)));
		record.setDeleted(Boolean.parseBoolean((String)doc.getFieldValue(FIELD_DELETED)));
		record.setFormat(formatDao.getById(Integer.parseInt((String)doc.getFieldValue(FIELD_FORMAT_ID))));
		record.setOaiDatestamp((String)doc.getFieldValue(FIELD_OAI_DATESTAMP));
		record.setOaiHeader((String)doc.getFieldValue(FIELD_OAI_HEADER));
		record.setOaiIdentifier(oaiId);
		record.setOaiXml((String)doc.getFieldValue(FIELD_OAI_XML));
		record.setProvider(providerDao.loadBasicProvider(Integer.parseInt((String)doc.getFieldValue(FIELD_PROVIDER_ID))));
		record.setService(serviceDao.loadBasicService(Integer.parseInt((String)doc.getFieldValue(FIELD_SERVICE_ID))));
		record.setHarvest(harvestDao.getById(Integer.parseInt((String)doc.getFieldValue(FIELD_HARVEST_ID))));
		record.setHarvestScheduleName((String)doc.getFieldValue(FIELD_HARVEST_SCHEDULE_NAME));

		Collection<Object> sets = doc.getFieldValues(FIELD_SET_SPEC);
		if(sets != null)
			for(Object set : sets)
				record.addSet(setDao.getBySetSpec((String)set));

		Collection<Object> errors = doc.getFieldValues(FIELD_ERROR);
		if(errors != null)
			for(Object error : errors)
				record.addError((String)error);
		
		Collection<Object> uplinks = doc.getFieldValues(FIELD_UP_LINK);
		if(uplinks != null)
			for(Object uplink : uplinks)
				record.addUpLink(getByOaiIdentifier((String)uplink));
		
		Collection<Object> processedFroms = doc.getFieldValues(FIELD_PROCESSED_FROM);
		if(processedFroms != null)
			for(Object processedFrom : processedFroms)
				record.addProcessedFrom(loadBasicRecord(Long.parseLong((String)processedFrom)));

		Collection<Object> successors = doc.getFieldValues(FIELD_SUCCESSOR);
		if(successors != null)
			for(Object successor : successors)
				record.addSuccessor(loadBasicRecord(Long.parseLong((String)successor)));
		
		Collection<Object> inputForServices = doc.getFieldValues(FIELD_INPUT_FOR_SERVICE_ID);
		if(inputForServices != null)
			for(Object inputForService : inputForServices)
				record.addInputForService(serviceDao.loadBasicService(Integer.parseInt((String)inputForService)));

		Collection<Object> processedByServices = doc.getFieldValues(FIELD_PROCESSED_BY_SERVICE_ID);
		if(processedByServices != null)
			for(Object processedByService : processedByServices)
				record.addProcessedByService(serviceDao.loadBasicService(Integer.parseInt((String)processedByService)));
		
		Collection<Object> traits = doc.getFieldValues(FIELD_TRAIT);
		if(traits != null)
			for(Object trait : traits)
				record.addTrait((String)trait);
		
		try
		{
			if (doc.getFieldValue(FIELD_CREATED_AT) != null) {
				record.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse((String)doc.getFieldValue(FIELD_CREATED_AT)));
			}
			if(doc.getFieldValue(FIELD_UPDATED_AT) != null) {
				record.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse((String)doc.getFieldValue(FIELD_UPDATED_AT)));
			}
		} // end try(parse created at and updated at dates
		catch (java.text.ParseException e)
		{
			log.error("An error occurred parsing the created at and updated at dates from the Lucene document.", e);
		} // end catch(ParseException)

		
		// Return the record we parsed from the document
		return record;
	} // end method getRecordFromDocument(Document)

	@Override
	protected SolrInputDocument setFieldsOnDocument(Record record, SolrInputDocument doc, boolean generateNewId) throws DatabaseConfigException
	{
		if(log.isDebugEnabled())
			log.debug("Set Field on Document");

		// If we need to generate an ID, set the record's ID to the next available record ID
		if(generateNewId)
			record.setId(frbrLevelIdDao.getNextXcIdForFrbrElement(XcIdentifierForFrbrElementDAO.ELEMENT_ID_RECORD));

		// If the oaiDatestamp is null, set it to the current time
		if(record.getOaiDatestamp() == null || record.getOaiDatestamp().length() <= 0)
			record.setOaiDatestamp(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(generateNewId ? record.getCreatedAt() : record.getUpdatedAt()));

		// If the header is null, set it based on the identifier, datestamp, and sets
		if(record.getOaiHeader() == null || record.getOaiHeader().length() <= 0)
		{
			StringBuilder header = new StringBuilder();
			header.append("<header>\n");
			header.append("\t<identifier>").append(record.getOaiIdentifier()).append("</identifier>\n");
			header.append("\t<datestamp>").append(record.getOaiDatestamp()).append("</datestamp>\n");

			// Get each set from the list of set IDs this record belongs to.  If the set is
			// not null, add its setSpec to the header.
			for(Set set : record.getSets())
				if(set != null)
					header.append("\t<setSpec>").append(set.getSetSpec()).append("</setSpec>\n");

			header.append("</header>");

			record.setOaiHeader(header.toString());
		} // end if(header needs to be set)

		// Set the appropriate fields on it.
		doc.addField(FIELD_RECORD_ID, Long.toString(record.getId()));
		doc.addField(FIELD_FRBR_LEVEL_ID, Long.toString(record.getFrbrLevelId()));
		
		if (record.getCreatedAt() != null) {
			doc.addField(FIELD_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(record.getCreatedAt()));
		}
		doc.addField(FIELD_DELETED, Boolean.toString(record.getDeleted()));

		doc.addField(FIELD_FORMAT_ID, Integer.toString(record.getFormat().getId()));
		doc.addField(FIELD_FORMAT_NAME, record.getFormat().getName());

		doc.addField(FIELD_PROVIDER_ID, (record.getProvider() == null ? "0" : Integer.toString(record.getProvider().getId())));
		if(record.getProvider() != null)
		{
			doc.addField(FIELD_PROVIDER_NAME, (record.getProvider().getName() == null ? "" : record.getProvider().getName()));
			doc.addField(FIELD_PROVIDER_URL, (record.getProvider().getOaiProviderUrl() == null ? "" : record.getProvider().getOaiProviderUrl()));
		}

		doc.addField(FIELD_HARVEST_ID, (record.getHarvest() == null ? "0" : Integer.toString(record.getHarvest().getId())));
		
		if (record.getHarvest() != null) 
		{
			HarvestSchedule schedule = record.getHarvest().getHarvestSchedule();
			if(schedule != null) {
				doc.addField(FIELD_HARVEST_SCHEDULE_NAME, schedule.getScheduleName());
			}
		}

		if (record.getHarvest() != null && record.getProvider() != null) 
		{
			doc.addField(FIELD_HARVEST_START_TIME,record.getProvider().getName() + " " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(record.getHarvest().getStartTime()));
		}
		
		doc.addField(FIELD_SERVICE_ID, (record.getService() == null ? "0" : Integer.toString(record.getService().getId())));

		
		if (record.getService() != null) {
			doc.addField(FIELD_SERVICE_NAME, record.getService().getName());
		}

		doc.addField(FIELD_OAI_IDENTIFIER, record.getOaiIdentifier());
		doc.addField(FIELD_OAI_DATESTAMP, record.getOaiDatestamp());
		doc.addField(FIELD_OAI_HEADER, record.getOaiHeader());
		doc.addField(FIELD_OAI_XML, record.getOaiXml());

		if(record.getUpdatedAt() != null)
			doc.addField(FIELD_UPDATED_AT, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(record.getUpdatedAt()));

		for(Record upLink : record.getUpLinks())
			doc.addField(FIELD_UP_LINK, upLink.getOaiIdentifier());

		for(Set set : record.getSets())
		{
			doc.addField(FIELD_SET_SPEC, set.getSetSpec());
			doc.addField(FIELD_SET_NAME, set.getDisplayName());
		} // end loop over sets

		for(Record processedFrom : record.getProcessedFrom())
			doc.addField(FIELD_PROCESSED_FROM, Long.toString(processedFrom.getId()));

		for(Record successor : record.getSuccessors())
			doc.addField(FIELD_SUCCESSOR, Long.toString(successor.getId()));
		
		for(Service inputForService : record.getInputForServices())
			doc.addField(FIELD_INPUT_FOR_SERVICE_ID, Long.toString(inputForService.getId()));

		for(Service processedByService : record.getProcessedByServices())
			doc.addField(FIELD_PROCESSED_BY_SERVICE_ID, Long.toString(processedByService.getId()));

		for(String trait : record.getTraits())
			doc.addField(FIELD_TRAIT, trait.replaceAll(" ", "_"));

		for(String error : record.getErrors())
			doc.addField(FIELD_ERROR, error);

		StringBuffer all = new StringBuffer();
		if (record.getFormat() != null) {
			all.append(record.getFormat().getName());
			all.append(" ");
		}
		if (record.getProvider() != null) {
			all.append(record.getProvider().getName());
			all.append(" ");
		}
		
		for(Set set : record.getSets())
		{
			all.append(set.getSetSpec());
			all.append(" ");
			all.append(set.getDisplayName());
			all.append(" ");
		}

		if(record.getService() != null) {
			all.append(record.getService().getName());
			all.append(" ");
		}
		
		for(String error : record.getErrors())
		{
			all.append(error);
			all.append(" ");
		}
		
		if (record.getHarvest() != null) {
			all.append(record.getHarvest().getStartTime());
		}
		
		doc.addField(FIELD_ALL, all.toString());


		return doc;
	} // end method setFieldsOnDocument(Record, Document, boolean)

	@Override
	protected String escapeString(String str) 
	{
		return str.replaceAll(":", "\\\\:")
				  .replaceAll("\\(", "\\\\\\(")
				  .replaceAll("\\)", "\\\\\\)")
				  .replaceAll("!", "\\\\!");
	}
} // end class DefaultRecordService
