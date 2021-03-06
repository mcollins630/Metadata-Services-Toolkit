/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.manager.record;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.jdom.Element;

import xc.mst.bo.provider.Provider;
import xc.mst.bo.provider.Set;
import xc.mst.bo.record.Expression;
import xc.mst.bo.record.Holdings;
import xc.mst.bo.record.Item;
import xc.mst.bo.record.Manifestation;
import xc.mst.bo.record.OutputRecord;
import xc.mst.bo.record.Record;
import xc.mst.bo.record.SolrBrowseResult;
import xc.mst.bo.record.Work;
import xc.mst.bo.service.Service;
import xc.mst.constants.Constants;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.manager.BaseService;
import xc.mst.manager.IndexException;
import xc.mst.utils.index.Records;
import xc.mst.utils.index.SolrIndexManager;

/**
 * Service class to query, add, update and delete records from an index.
 *
 * @author Eric Osisek
 */
public abstract class RecordService extends BaseService {
    public static final String OAI_NS_2_0 = "http://www.openarchives.org/OAI/2.0/";

    /**
     * A reference to the logger for this class
     */
    public static Logger log = Logger.getLogger(Constants.LOGGER_GENERAL);

    /**
     * The field name for the indexed object type
     */
    public static final String FIELD_INDEXED_OBJECT_TYPE = "indexed_object_type";

    /**
     * The name of the record ID field
     */
    public final static String FIELD_RECORD_ID = "record_id";

    /**
     * The name of the record type field
     */
    public final static String FIELD_RECORD_TYPE = "record_type";

    /**
     * The name of the frbr level ID field
     */
    public final static String FIELD_FRBR_LEVEL_ID = "frbr_level_id";

    /**
     * The name of the up link field
     */
    public final static String FIELD_UP_LINK = "up_link";

    /**
     * The name of the created at field
     */
    public final static String FIELD_CREATED_AT = "created_at";

    /**
     * The name of the updated at field
     */
    public final static String FIELD_UPDATED_AT = "updated_at";

    /**
     * The name of the deleted field
     */
    public final static String FIELD_DELETED = "deleted";

    /**
     * The name of the format ID field
     */
    public final static String FIELD_FORMAT_ID = "format_id";

    /**
     * The name of the provider ID field
     */
    public final static String FIELD_PROVIDER_ID = "provider_id";

    /**
     * The name of the service ID field
     */
    public final static String FIELD_SERVICE_ID = "service_id";

    /**
     * The name of the service field
     */
    public final static String FIELD_SERVICE_NAME = "service_name";

    /**
     * The name of the harvest ID field
     */
    public final static String FIELD_HARVEST_ID = "harvest_id";

    /**
     * The name of the harvest schedule field
     */
    public final static String FIELD_HARVEST_SCHEDULE_NAME = "harvest_schedule_name";

    /**
     * The name of the provider name field
     */
    public final static String FIELD_PROVIDER_NAME = "provider_name";

    /**
     * The name of the provider URL field
     */
    public final static String FIELD_PROVIDER_URL = "provider_url";

    /**
     * The name of the provider/service and harvest start date and time
     */
    public final static String FIELD_HARVEST_START_TIME = "harvest_start_time";

    /**
     * The name of the format name field
     */
    public final static String FIELD_FORMAT_NAME = "format_name";

    /**
     * The name of the OAI identifier field
     */
    public final static String FIELD_OAI_IDENTIFIER = "oai_identifier";

    /**
     * The name of the OAI datestamp field
     */
    public final static String FIELD_OAI_DATESTAMP = "oai_datestamp";

    /**
     * The name of the OAI header field
     */
    public final static String FIELD_OAI_HEADER = "oai_header";

    /**
     * The name of the XML field
     */
    public final static String FIELD_OAI_XML = "oai_xml";

    /**
     * The name of the set name field
     */
    public final static String FIELD_SET_NAME = "set_name";

    /**
     * The name of the setSpec field
     */
    public final static String FIELD_SET_SPEC = "set_spec";

    /**
     * The name of the processed from field
     */
    public final static String FIELD_PROCESSED_FROM = "processed_from";

    /**
     * The name of the successor field
     */
    public final static String FIELD_SUCCESSOR = "successor";

    /**
     * The name of the input for service IDs field
     */
    public final static String FIELD_INPUT_FOR_SERVICE_ID = "input_for_service_id";

    /**
     * The name of the processed by service IDs field
     */
    public final static String FIELD_PROCESSED_BY_SERVICE_ID = "processed_by_service_id";

    /**
     * The name of the traits field
     */
    public final static String FIELD_TRAIT = "trait";

    /**
     * The name of the errors field
     */
    public final static String FIELD_ERROR = "error";

    /**
     * All default search fields
     */
    public final static String FIELD_ALL = "all";

    public abstract OutputRecord createRecord();

    public abstract void injectNewId(Record r);

    /**
     * Gets all records from the index
     *
     * @return A list of all records in the index
     */
    public abstract List<Record> getAll() throws IndexException;

    /**
     * Gets the record from the index with the passed record ID
     *
     * @param id
     *            The record's ID
     * @return The record with the passed record ID
     * @throws DatabaseConfigException
     */
    public abstract Record getById(long id) throws DatabaseConfigException, IndexException;

    /**
     * Gets the basic information for a a record from the index with the passed record ID
     *
     * @param id
     *            The record's ID
     * @return The basic information for a record with the passed record ID
     * @throws DatabaseConfigException
     */
    public abstract Record loadBasicRecord(long id) throws IndexException;

    /**
     * Gets all records from the index with the passed provider ID
     *
     * @param providerId
     *            The provider ID of the records to retrieve
     * @return A list of all records in the index with the passed provider ID
     */
    public abstract List<Record> getByProviderId(int providerId) throws IndexException;

    /**
     * Gets number of records from the index with the passed provider ID
     *
     * @param providerId
     *            The provider ID of the records to retrieve
     * @return Number of records in the index with the passed provider ID
     */
    public abstract int getCountByProviderId(int providerId) throws IndexException;

    /**
     * Gets all records from the index with the passed service ID
     *
     * @param serviceId
     *            The service ID of the records to retrieve
     * @return A list of all records in the index with the passed provider ID
     */
    public abstract List<Record> getByServiceId(int serviceId) throws IndexException;

    /**
     * Gets number of records from the index with the passed service ID
     *
     * @param serviceId
     *            The service ID of the records to retrieve
     * @return Number of records in the index with the passed provider ID
     */
    public abstract long getNumberOfRecordsByServiceId(int serviceId) throws IndexException;

    /**
     * Gets all records from the index with the passed processing service ID
     *
     * @param serviceId
     *            The service ID of the service that processed records to retrieve
     * @return A list of all records in the index with the passed processing service ID
     */
    public abstract List<Record> getProcessedByServiceId(int serviceId) throws IndexException;

    /**
     * Gets all records from the index with the passed harvest ID
     *
     * @param harvestId
     *            The harvest ID of the records to retrieve
     * @return A list of all records in the index with the passed harvest ID
     */
    public abstract List<Record> getByHarvestId(int harvestId) throws IndexException;

    /**
     * Gets all records from the index with the passed format ID and service ID
     *
     * @param formatId
     *            The format ID of the records to retrieve
     * @param serviceId
     *            The service that processed the records to retrieve
     * @return A list all records in the index with the passed format ID
     */
    public abstract List<Record> getByFormatIdAndServiceId(int formatId, int serviceId) throws IndexException;

    /**
     * Gets all records from the index contained in the set with the passed name
     *
     * @param setName
     *            the name of the set whose records should be returned
     * @return A list all records in the index contained in the set with the passed name
     */
    public abstract List<Record> getBySetName(String setName) throws IndexException;

    /**
     * Get successors of given records id created by specified service id
     *
     * @param recordId
     *            Id of record
     * @param serviceId
     *            , id of service created the successors
     * @return Successor records
     * @throws IndexException
     */
    public abstract List<Record> getSuccessorsCreatedByServiceId(long recordId, long serviceId) throws IndexException;

    /**
     * Gets all records from the index contained in the set with the passed setSpec
     *
     * @param setSpec
     *            the setSpec of the set whose records should be returned
     * @return A list all records in the index contained in the set with the passed setSpec
     */
    public abstract List<Record> getBySetSpec(String setSpec) throws IndexException;

    /**
     * Gets all records from the index harvested from the provider with the passed name
     *
     * @param providerName
     *            the name of the provider whose records should be returned
     * @return A list all records in the index harvested from the provider with the passed name
     */
    public abstract List<Record> getByProviderName(String providerName) throws IndexException;

    /**
     * Gets all records from the index harvested from the provider with the passed URL
     *
     * @param providerUrl
     *            the URL of the provider whose records should be returned
     * @return A list all records in the index harvested from the provider with the passed URL
     */
    public abstract List<Record> getByProviderUrl(String providerUrl) throws IndexException;

    /**
     * Gets all records from the index with the format with the passed name
     *
     * @param formatName
     *            the name of the format whose records should be returned
     * @return A list all records in the index with the format with the passed name
     */
    public abstract List<Record> getByFormatName(String formatName) throws IndexException;

    /**
     * Gets all record inputs that were not processed for a given service
     *
     * @return A list of all records that need to be processed for a given service
     */
    public abstract List<Record> getInputForServiceToProcess(int serviceId) throws IndexException;

    /**
     * Gets all record inputs that were not processed for a given Service and Record Type
     *
     * @return A list of all records that need to be processed for a given service
     */
    public abstract Records getByInputToServiceAndRecordType(int serviceId, String recordType) throws IndexException;

    /**
     * Gets all record inputs that were not processed for a given service
     *
     * @return A list of all records that need to be processed for a given service
     */
    public abstract List<Record> getInputForService(int serviceId) throws IndexException;

    // /**
    // * Gets specified number of record inputs that were not processed for a given service
    // *
    // * @param serviceId
    // * @param start
    // * @param rows
    // * @return A list of all records that need to be processed for a given service
    // */
    // public abstract List<Record> getInputForService(int serviceId, int start, int rows) throws IndexException;

    /**
     * Gets count of record inputs that were not processed for a given service
     *
     * @return count of record inputs that were not processed for a given service
     */
    public abstract int getCountOfRecordsToBeProcessedVyService(int serviceId) throws IndexException;

    /**
     * Gets the record from the index with the passed OAI Identifier
     *
     * @param identifier
     *            The record's OAI Identifer
     * @return A Record Object representing the record with the passed OAI Identifier
     * @throws DatabaseConfigException
     */
    public abstract Record getByOaiIdentifier(String identifier) throws DatabaseConfigException, IndexException;

    /**
     * Gets all matching record from the index with the passed OAI Identifier
     *
     * @param identifier
     *            List of record's OAI Identifer
     * @return A List of Record Object representing the record with the passed OAI Identifier
     * @throws IndexException
     */
    public abstract List<Record> getByOaiIdentifiers(List<String> identifiers) throws IndexException;

    /**
     * Gets the record from the index with the passed OAI Identifier
     *
     * @param identifier
     *            The record's OAI Identifer
     * @param providerId
     *            The Id of the provider from which this record was harvested
     * @return The record with the passed OAI Identifier
     * @throws DatabaseConfigException
     */
    public abstract Record getByOaiIdentifierAndProvider(String identifier, int providerId) throws DatabaseConfigException, IndexException;

    /**
     * Gets the record from the index with the passed OAI Identifier
     *
     * @param identifier
     *            The record's OAI Identifer
     * @param serviceId
     *            The Id of the service that processed this record
     * @return The record with the passed OAI Identifier
     * @throws DatabaseConfigException
     */
    public abstract Record getByOaiIdentifierAndService(String identifier, int serviceId) throws DatabaseConfigException, IndexException;

    /**
     * Gets the record from the index with the passed OAI Identifier
     *
     * @param identifier
     *            The record's OAI Identifer
     * @param serviceId
     *            The Id of the service for which this record is input
     * @return The record with the passed OAI Identifier
     * @throws DatabaseConfigException
     */
    public abstract Record getInputForServiceByOaiIdentifier(String identifier, int serviceId) throws DatabaseConfigException, IndexException;

    /**
     * Gets all records from the index which have been processed from the specified record
     *
     * @param processedFromId
     *            The ID of the original record whose processed Records we're getting
     * @return A list of all records in the index which have been processed from the specified record
     */
    public abstract List<Record> getByProcessedFrom(long processedFromId) throws IndexException;

    /**
     * Gets all records including deleted , from the index which have been processed from the specified record
     *
     * @param processedFromId
     *            The ID of the original record whose processed Records we're getting
     * @return A list of all records in the index which have been processed from the specified record
     */
    public abstract List<Record> getSuccessorsCreatedByServiceIdIncludingDeletedRecords(long recordId, long serviceId) throws IndexException;

    /**
     * Gets all records from the index with the passed trait
     *
     * @param trait
     *            The trait of the records to retrieve
     * @return A list of all records in the index with the passed trait
     */
    public abstract List<Record> getByTrait(String trait) throws IndexException;

    /**
     * Gets the record from the index with the earliest datestamp processed by a given service
     *
     * @param serviceId
     *            The ID of the service whose earliest processed record we're looking for
     * @return The record with earliest datestamp that was processed by the target service
     */
    public abstract Record getEarliest(int serviceId);

    /**
     * Returns the number of processed records between the specified dates contained in the specified set
     *
     * @param fromDate
     *            The lower bound for the date for the records to count
     * @param untilDate
     *            The upper bound for the date for the records to count
     * @param set
     *            The set of the set for the records to count. If this is null,
     *            the count will include all sets.
     * @param formatId
     *            The ID of the metadata format of the records to count. If less than 0,
     *            the count will include all metadata types.
     * @param serviceId
     *            The service which processed the outgoing records
     * @return The number of records matching the parameters queried for
     */
    public abstract long getCount(Date fromDate, Date untilDate, Set set, int formatId, int serviceId) throws IndexException;

    /**
     * Returns the records between the specified dates within the specified set
     *
     * @param fromDate
     *            The lower bound for the date for the records to return
     * @param untilDate
     *            The upper bound for the date for the records to return
     * @param set
     *            The set for the records to return
     * @param formatId
     *            The ID of the format for the records to return
     * @param offset
     *            The offset into the list of matching records representing the first record to return
     * @param numResults
     *            The number of records to return
     * @param serviceId
     *            The service which processed the outgoing records
     * @return A list of records matching the parameters queried for
     */
    public abstract SolrBrowseResult getOutgoingRecordsInRange(Date fromDate, Date untilDate, Set set, int formatId, int offset, int numResults, int serviceId)
            throws IndexException;

    /**
     * Inserts a record into the index
     *
     * @param record
     *            The record to insert
     * @return true on success, false on failure
     */
    public boolean insert(Record record) throws DataException, IndexException {
        //TODO This is dead code.  The method may get overridden at best.

        // Check that the non-ID fields on the record are valid
        validateFields(record, false, true);

        if (log.isDebugEnabled())
            log.debug("Inserting a new " + record.getType());

        Date now = new Date();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        // Create a Document object and set it's type field
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField(FIELD_INDEXED_OBJECT_TYPE, record.getType());

        // Set up the fields for the specific type of indexed object
        if (record instanceof Work)
            doc = getWorkService().setFieldsOnDocument((Work) record, doc, true);
        else if (record instanceof Expression)
            doc = getExpressionService().setFieldsOnDocument((Expression) record, doc, true);
        else if (record instanceof Manifestation)
            doc = getManifestationService().setFieldsOnDocument((Manifestation) record, doc, true);
        else if (record instanceof Holdings)
            doc = getHoldingsService().setFieldsOnDocument((Holdings) record, doc, true);
        else if (record instanceof Item)
            doc = getItemService().setFieldsOnDocument((Item) record, doc, true);
        else
            doc = setFieldsOnDocument(record, doc, true);

        SolrIndexManager sim = (SolrIndexManager) config.getBean("SolrIndexManager");
        boolean retVal = sim.addDoc(doc);

        return retVal;
    } // end method insert(Record)

    /**
     * Updates a record in the index
     *
     * @param record
     *            The record to update
     * @return true on success, false on failure
     */
    public boolean update(Record record) throws DataException, IndexException {
        // Check that the fields on the record are valid
        validateFields(record, true, true);

        if (log.isDebugEnabled())
            log.debug("Updating the record with ID " + record.getId());

        // Set up a Document Object to insert the updated set into the Lucene index
        // Create a Document object and set it's type field

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField(FIELD_INDEXED_OBJECT_TYPE, record.getType());

        // Set up the fields for the Record
        doc = setFieldsOnDocument(record, doc, false);

        SolrIndexManager sim = (SolrIndexManager) config.getBean("SolrIndexManager");
        return sim.addDoc(doc);
    } // end method update(Record)

    /**
     * Deletes a record from the index
     *
     * @param record
     *            The record to delete
     * @return true on success, false on failure
     */
    public boolean delete(Record record) throws DataException {
        // Check that the ID field on the record are valid
        validateFields(record, true, false);

        if (log.isDebugEnabled())
            log.debug("Deleting the record with ID " + record.getId());

        String deleteQuery = FIELD_RECORD_ID + ":" + Long.toString(record.getId()) + "  AND "
                             + FIELD_INDEXED_OBJECT_TYPE + ":" + record.type;

        // Delete all records with the matching record ID
        SolrIndexManager sim = (SolrIndexManager) config.getBean("SolrIndexManager");
        boolean result = sim.deleteByQuery(deleteQuery);

        // If the delete was successful, also delete rows in the MySQL tables which reference it
        // if(result)
        // harvestRecordDao.deleteForRecord(record.getId());

        // Return the result of the delete
        return result;
    } // end method delete(Record)

    /**
     * Parses a Record from the fields in a Document from the index.
     *
     * @param doc
     *            The document containing information on the Record.
     * @return The record which was contained in the passed Document.
     * @throws DatabaseConfigException
     */
    public abstract Record getRecordFromDocument(SolrDocument doc) throws DatabaseConfigException, IndexException;

    /**
     * Parses a Record from the fields in a Document from the index.
     *
     * @param doc
     *            The document containing information on the Record.
     * @return The record which was contained in the passed Document.
     */
    public abstract Record getBasicRecordFromDocument(SolrDocument doc);

    /**
     * Sets the fields on the document which need to be stored in the
     * index.
     *
     * @param record
     *            The record to use to set the fields on the document
     * @param doc
     *            The document whose fields need to be set.
     * @param generateNewId
     *            True to generate a new ID for the record, false to use the record's current ID
     * @return A reference to the Document after its fields have been set
     * @throws DatabaseConfigException
     */
    protected abstract SolrInputDocument setFieldsOnDocument(Record record, SolrInputDocument doc, boolean generateNewId) throws DatabaseConfigException;

    /**
     * Escapes characters that aren't allowed in queries run on the index
     *
     * @param str
     *            The String to be escaped
     * @return The escaped String
     */
    protected abstract String escapeString(String str);

    /**
     * Get the last created record in given service
     *
     * @param serviceId
     *            Service id in which to get last created record
     * @return Last created record
     * @throws IndexException
     */
    public abstract Record getLastCreatedRecord(int serviceId) throws IndexException, DatabaseConfigException;

    /**
     * Load Record only with OAI header and OAI XML
     *
     * @param doc
     *            Solr document
     * @return Record
     */
    public abstract Record getRecordXMLFromDocument(SolrDocument doc);

    /**
     * Parses a Record from the fields in a Document from the index.
     * Loads only fields required to be displayed on Browse records results page
     *
     * @param doc
     *            The document containing information on the Record.
     * @return The record which was contained in the passed Document.
     * @throws DatabaseConfigException
     */
    public abstract Record getRecordFieldsForBrowseFromDocument(SolrDocument doc) throws DatabaseConfigException, IndexException;

    public abstract Record parse(Element e);

    public abstract Record parse(Element recordEl, Provider provider);

    public abstract Element createJDomElement(Record r);

    public abstract Element createJDomElement(Record r, String namespace);

    public abstract String getOaiIdentifier(long id, Provider p);

    public abstract String getOaiIdentifier(long id, Service s);

    public abstract String getOaiIdentifier(long id, Provider p, Service s);

    /**
     * Validates the fields on the passed Record Object
     *
     * @param record
     *            The record to validate
     * @param validateId
     *            true if the ID field should be validated
     * @param validateNonId
     *            true if the non-ID fields should be validated
     * @throws DataException
     *             If one or more of the fields on the passed record were invalid
     */
    protected void validateFields(Record record, boolean validateId, boolean validateNonId) throws DataException {
        StringBuilder errorMessage = new StringBuilder();

        // Check the ID field if we're supposed to
        if (validateId) {
            if (log.isDebugEnabled())
                log.debug("Checking the ID");

            if (record.getId() < 0)
                errorMessage.append("The record's id is invalid. ");
        } // end if(we should check the ID field)

        // Check the non-ID fields if we're supposed to
        if (validateNonId) {
            if (log.isDebugEnabled())
                log.debug("Checking the non-ID fields");

            if (record.getFormat() == null)
                errorMessage.append("The record's format is invalid. ");

            if (record.getOaiIdentifier() == null)
                errorMessage.append("The record's OAI identifier is invalid.");
        } // end if(we should check the non-ID fields)

        // Log the error and throw the exception if any fields are invalid
        if (errorMessage.length() > 0) {
            String errors = errorMessage.toString();
            log.error("The following errors occurred: " + errors);
            throw new DataException(errors);
        } // end if(we found an error)
    } // end method validateFields(Record, boolean, boolean)
} // end class RecordService
