/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.manager.test;

import org.apache.solr.client.solrj.SolrQuery;
import org.testng.annotations.Test;

import xc.mst.bo.harvest.HarvestSchedule;
import xc.mst.bo.provider.Format;
import xc.mst.bo.provider.Provider;
import xc.mst.bo.record.SolrBrowseResult;
import xc.mst.bo.user.User;
import xc.mst.common.test.BaseTest;
import xc.mst.dao.DataException;
import xc.mst.harvester.ValidateRepository;
import xc.mst.helper.TestHelper;
import xc.mst.manager.harvest.ScheduleService;
import xc.mst.manager.record.BrowseRecordService;
import xc.mst.manager.repository.FormatService;
import xc.mst.manager.repository.ProviderService;
import xc.mst.manager.repository.SetService;
import xc.mst.manager.user.ServerService;
import xc.mst.manager.user.UserService;

/**
 * Test for browse record
 * 
 * @author Sharmila Ranganathan
 * 
 */
@Test(groups = { "baseTests" }, enabled = true)
public class BrowseRecordServiceTest extends BaseTest {

    /**
     * Test creating schedule
     * 
     * @throws DataException
     */
    public void browseRecordTest() throws Exception {
        // Initialize Solr, database, log before testing
        TestHelper helper = TestHelper.getInstance();

        ProviderService providerService = (ProviderService) getBean("ProviderService");

        BrowseRecordService browseRecordService = (BrowseRecordService) getBean("BrowseRecordService");

        ScheduleService scheduleService = (ScheduleService) getBean("ScheduleService");

        FormatService formatService = (FormatService) getBean("FormatService");

        SetService setService = (SetService) getBean("SetService");

        UserService userService = (UserService) getBean("UserService");

        ServerService serverService = (ServerService) getBean("ServerService");

        User user = userService.getUserByUserName("admin", serverService.getServerByName("Local"));

        Provider provider = new Provider();

        provider.setName("Test Repository Name");
        provider.setDescription("description");
        provider.setOaiProviderUrl("http://www.cimec.org.ar/ojs/index.php/cimec-repo/oai");
        provider.setCreatedAt(new java.util.Date());
        providerService.insertProvider(provider);

        ValidateRepository validateRepository = new ValidateRepository();
        validateRepository.validate(provider.getId());

        HarvestSchedule schedule = new HarvestSchedule();
        schedule.setScheduleName("Test Schedule Name");
        schedule.setDayOfWeek(1);

        for (Format format : formatService.getAllFormats()) {

            if (format.getName().equalsIgnoreCase("marcxml")) {
                schedule.addFormat(format);
                break;
            }
        }

        schedule.setHour(5);
        schedule.setId(111);
        schedule.setMinute(5);
        schedule.setProvider(provider);
        schedule.setRecurrence("Daily");
        schedule.setStartDate(java.sql.Date.valueOf("2009-05-01"));

        scheduleService.insertSchedule(schedule);
        /* TODO: need a new test
        HarvestRunner harvestRunner = (HarvestRunner)getBean("HarvestRunner");
        harvestRunner.setScheduleId(schedule.getId());
        harvestRunner.runHarvest();
        */

        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.addFilterQuery("provider_name:\"Test Repository Name\"");
        SolrBrowseResult result = browseRecordService.search(query);

        assert result.getTotalNumberOfResults() == 22 : "Total number of records should be 22. But it is " + result.getTotalNumberOfResults();

        providerService.deleteProvider(provider);

    }

}
