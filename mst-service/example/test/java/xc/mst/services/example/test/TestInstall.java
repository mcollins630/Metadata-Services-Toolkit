/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */
package xc.mst.services.example.test;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import xc.mst.bo.service.Service;
import xc.mst.manager.processingDirective.ServicesService;
import xc.mst.service.impl.test.BaseMetadataServiceTest;
import xc.mst.utils.MSTConfiguration;

public class TestInstall extends BaseMetadataServiceTest {

    protected static final Logger LOG = Logger.getLogger(TestInstall.class);

    @Test
    public void testIntall() {
        try {
            String repoName = MSTConfiguration.getInstance().getProperty("service.name");
            ServicesService ss = (ServicesService) MSTConfiguration.getInstance().getBean("ServicesService");
            Service s = ss.getServiceByName(repoName);
            if (s != null) {
                ss.deleteService(s);
            }
            ss.addNewService(repoName);
            LOG.debug("repoName: " + repoName);
            s = ss.getServiceByName(repoName);
            // GenericMetadataDAO genericMetadataDAO = (GenericMetadataDAO) ss.getBean(repoName, "GenericMetadataDAO");
            // genericMetadataDAO.executeServiceDBScripts("xc/mst/repo/sql/create_oai_id_seq.sql");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}
