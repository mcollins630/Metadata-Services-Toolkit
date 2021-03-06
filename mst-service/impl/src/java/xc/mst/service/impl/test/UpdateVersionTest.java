/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */
package xc.mst.service.impl.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import xc.mst.bo.service.Service;
import xc.mst.common.test.BaseTest;
import xc.mst.services.GenericMetadataService;

public class UpdateVersionTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(UpdateVersionTest.class);

    @Test
    public void test() {
        try {
            Service norm = getServicesService().getServiceByName("MARCNormalization");
            List<String> fileNames = new ArrayList<String>();
            fileNames.add("update.1.0.sql");
            fileNames.add("update.0.2.sql");
            fileNames.add("update.0.2.1.sql");
            fileNames.add("update.0.2.2.sql");
            fileNames.add("update.0.2.1.1.1.1.1.sql");
            fileNames.add("update.0.3.sql");
            fileNames.add("update.0.3.0.sql");
            fileNames.add("update.0.3.1.sql");
            List<String> files2run = ((GenericMetadataService) norm.getMetadataService()).internalUpdate("0.2.1", "0.3", fileNames);

            assert files2run.get(0).equals("update.0.2.1.1.1.1.1.sql");
            assert files2run.get(1).equals("update.0.2.2.sql");
            assert files2run.get(2).equals("update.0.3.sql");
            assert files2run.get(3).equals("update.0.3.0.sql");
        } catch (Throwable t) {
            LOG.error("", t);
            throw new RuntimeException(t);
        }
    }

}
