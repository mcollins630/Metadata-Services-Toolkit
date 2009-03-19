/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */



package xc.mst.manager.test;

import java.util.Date;
import org.testng.annotations.Test;
import xc.mst.bo.provider.Provider;
import xc.mst.bo.user.User;
import xc.mst.dao.DataException;
import xc.mst.manager.repository.DefaultProviderService;
import xc.mst.manager.repository.ProviderService;
import xc.mst.manager.user.DefaultServerService;
import xc.mst.manager.user.DefaultUserService;
import xc.mst.manager.user.ServerService;


/**
 * Tests for Providers
 *
 * @author Tejaswi Haramurali
 */
@Test(groups = { "baseTests" }, enabled = true)
public class ProviderServiceTest
{
    /**
     * Method which tests all the functionality related to Providers
     * @throws xc.mst.dao.DataException
     */
    public void addScheduleTest() throws DataException
	{
        try
        {
        	ServerService serverService = new DefaultServerService();
            User user = new DefaultUserService().getUserByUserName("admin", serverService.getServerByName("Local"));
            ProviderService providerService = new DefaultProviderService();
            Provider provider = new Provider();
            provider.setName("repositoryname");
            provider.setDescription("description");
            provider.setOaiProviderUrl("http://oaitoolkit.com");
            provider.setCreatedAt(new Date(new java.util.Date().getTime()));
            provider.setUser(user);
            providerService.insertProvider(provider);

            Provider anotherProvider = providerService.getProviderById(provider.getId());

            assert provider.getName().equals(anotherProvider.getName()) : "Name should be repositoryname";
            assert provider.getOaiProviderUrl().equals(anotherProvider.getOaiProviderUrl()) : "Name should be http://oaitoolkit.com";
            assert provider.getDescription().equals(anotherProvider.getDescription()) : "Description should be description";
            assert provider.getUser().getUsername().equals(anotherProvider.getUser().getUsername()) : "Name should be admin";

            providerService.deleteProvider(provider);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

}
