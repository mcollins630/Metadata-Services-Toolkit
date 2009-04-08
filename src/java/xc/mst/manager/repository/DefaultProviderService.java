/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.manager.repository;

import java.util.List;
import xc.mst.bo.provider.Provider;
import xc.mst.dao.DataException;
import xc.mst.dao.provider.DefaultProviderDAO;
import xc.mst.dao.provider.ProviderDAO;
import xc.mst.utils.LogWriter;

/**
 * Service class that is used to Add/Delete/Update a Provider/Repository
 *
 * @author Tejaswi Haramurali
 */
public class DefaultProviderService implements ProviderService{

    /** The provider DAO object */
    ProviderDAO providerDao = new DefaultProviderDAO();

    /**
     * Returns a provider by its name
     *
     * @param providerName provider name
     * @return provider object
     */
    public Provider getProviderByName(String providerName){
       return providerDao.getByName(providerName);
    }

    /**
     * Returns a provider by its ID
     *
     * @param providerId provider ID
     * @return provider object
     */
    public Provider getProviderById(int providerId){
       return providerDao.getById(providerId);
    }

    /**
     * Returns a provider by its URL
     *
     * @param providerUrl provider URL
     * @return provider object
     */
    public Provider getProviderByURL(String providerUrl) {
        return providerDao.getByURL(providerUrl);
    }

    /**
     * Inserts a provider into the database
     *
     * @param provider provider object
     * @throws xc.mst.dao.DataException
     */
    public void insertProvider(Provider provider) throws DataException{
        providerDao.insert(provider);
        LogWriter.addInfo(provider.getLogFileName(), "Beginning logging for " + provider.getName());
    }

    /**
     * Deletes provider form the database
     *
     * @param provider provider object
     * @throws xc.mst.dao.DataException
     */
    public void deleteProvider(Provider provider) throws DataException{
        providerDao.delete(provider);
    }

    /**
     * Updates the details of a provider
     *
     * @param provider provider object
     * @throws xc.mst.dao.DataException
     */
    public void updateProvider(Provider provider) throws DataException{
        providerDao.update(provider);
    }

    /**
     * Returns a list of all the providers
     *
     * @return provider list
     */
    public List<Provider> getAllProviders()
    {
        return providerDao.getAll();
    }

    /**
   * Returns a list of all providers sorted
   *
   * @param sort determines if the rows are to be sorted in ascending or descending order
   * @param columnSorted column on which the sorting is done
   * @return list of providers
   */
    public List<Provider> getAllProvidersSorted(boolean sort,String columnSorted)
    {
        return providerDao.getSorted(sort, columnSorted);
    }
}
