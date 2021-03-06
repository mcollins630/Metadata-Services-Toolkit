/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.manager.processingDirective;

import java.util.List;

import org.apache.log4j.Logger;

import xc.mst.bo.processing.Job;
import xc.mst.bo.processing.ProcessingDirective;
import xc.mst.constants.Constants;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.manager.BaseService;

/**
 * Service Class that is used for the creation/deletion/updating of Processing Directives.
 * 
 * @author Tejaswi Haramurali
 */
public class DefaultProcessingDirectiveService extends BaseService implements ProcessingDirectiveService {

    /**
     * A reference to the logger for this class
     */
    protected static Logger log = Logger.getLogger(Constants.LOGGER_GENERAL);

    /**
     * Returns a procesisng directive with the given ID
     * 
     * @param processingDirectiveId
     *            Processing Directive ID
     * @return processing directive object
     * @throws DatabaseConfigException
     */
    public ProcessingDirective getByProcessingDirectiveId(int processingDirectiveId) throws DatabaseConfigException {
        return getProcessingDirectiveDAO().getById(processingDirectiveId);
    }

    /**
     * Insert a new processing directive
     * 
     * @param processingDirective
     *            Processing Directive
     */
    public void insertProcessingDirective(ProcessingDirective processingDirective) {
        try {
            getProcessingDirectiveDAO().insert(processingDirective);

            // If the processing directive has an output set that is not
            // already in the target service, we need to add it as an input set
            // to that service
            if (processingDirective.getOutputSet() != null) {
                processingDirective.getService().addOutputSet(processingDirective.getOutputSet());
                getServiceDAO().update(processingDirective.getService());
            }

            runProcessingDirective(processingDirective);
        } catch (DataException e) {
            log.error("Data Exception", e);
        }
    }

    /**
     * Deletes a processing directive
     * 
     * @param processingDirective
     *            processing directive object
     */
    public void deleteProcessingDirective(ProcessingDirective processingDirective) {
        try {
            getProcessingDirectiveDAO().delete(processingDirective);
        } catch (DataException e) {
            log.error("Data Exception", e);
        }
    }

    /**
     * Updates the details of a procesisng directive
     * 
     * @param processingDirective
     *            processing directive object
     */
    public void updateProcessingDirective(ProcessingDirective processingDirective) {
        try {
            getProcessingDirectiveDAO().update(processingDirective);

            // If the processing directive has an output set that is not
            // already in the target service, we need to add it as an input set
            // to that service
            if (processingDirective.getOutputSet() != null) {
                processingDirective.getService().addOutputSet(processingDirective.getOutputSet());
                getServiceDAO().update(processingDirective.getService());
            }

            runProcessingDirective(processingDirective);
        } catch (DataException e) {
            log.error("Data Exception", e);
        }
    }

    /**
     * Returns a list of all processing directives
     * 
     * @return list of processing directives
     * @throws DatabaseConfigException
     */
    public List<ProcessingDirective> getAllProcessingDirectives() throws DatabaseConfigException {
        return getProcessingDirectiveDAO().getAll();
    }

    /**
     * Returns a processing directives associated with a provider
     * 
     * @param providerId
     *            provider ID
     * @return list of processing directives
     * @throws DatabaseConfigException
     */
    public List<ProcessingDirective> getBySourceProviderId(int providerId) throws DatabaseConfigException {
        return getProcessingDirectiveDAO().getBySourceProviderId(providerId);
    }

    /**
     * Returns a list of processing directives associated with a service
     * 
     * @param serviceId
     *            service ID
     * @return list of processing directives
     * @throws DatabaseConfigException
     */
    public List<ProcessingDirective> getBySourceServiceId(int serviceId) throws DatabaseConfigException {
        return getProcessingDirectiveDAO().getBySourceServiceId(serviceId);
    }

    /**
     * Returns a list of processing directives associated with a service, in this case we want the PDs
     * where the given serviceId represents the service that is going to do the work
     * 
     * @param serviceId
     *            service ID
     * @return list of processing directives
     * @throws DatabaseConfigException
     */
    public List<ProcessingDirective> getByDestinationServiceId(int serviceId) throws DatabaseConfigException {
        return getProcessingDirectiveDAO().getByDestinationServiceId(serviceId);
    }

    /**
     * Checks a processing directive against all records from its source.
     * If any records match, they are marked as input for the target service and
     * that service is scheduled to be run.
     * 
     * @param pd
     *            The processing directive to check
     */
    private void runProcessingDirective(ProcessingDirective pd) {
        JobService jobService = (JobService) config.getBean("JobService");

        // Delete the solr-indexer's service harvest for the incoming repo
        // since running this service could attach messages to the incoming repo
        String incomingRepoName = null;
        if (pd.getSourceProvider() != null) {
            incomingRepoName = pd.getSourceProvider().getName();
        } else if (pd.getSourceService() != null) {
            incomingRepoName = pd.getSourceService().getName();
        }
        getServiceDAO().deleteServiceHarvests(-1, incomingRepoName);

        // Add job to database queue
        try {
            Job job = new Job();
            job.setService(pd.getService());
            if (pd.getOutputSet() != null) {
                job.setOutputSetId(pd.getOutputSet().getId());
            }
            job.setJobType(Constants.THREAD_SERVICE);
            job.setOrder(getJobService().getMaxOrder() + 1);
            job.setProcessingDirective(pd);
            getJobService().insertJob(job);
        } catch (DatabaseConfigException dce) {
            log.error("DatabaseConfig exception occured when ading jobs to database", dce);
        }
    } // end method runNewProcessingDirective(ProcessingDirective)
}
