/**
 * HpcManagedDatasetDAO.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.dao;

import gov.nih.nci.hpc.domain.model.HpcManagedDataset;
import gov.nih.nci.hpc.domain.dataset.HpcDatasetUserAssociation;
import gov.nih.nci.hpc.exception.HpcException;

import java.util.List;

/**
 * <p>
 * HPC Managed Dataset DAO Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public interface HpcManagedDatasetDAO 
{         
    /**
     * Add managed dataset to the repository.
     *
     * @param managedDataset The managed dataset to add to the DB.
     * 
     * @throws HpcException
     */
    public void add(HpcManagedDataset managedDataset) throws HpcException;
    
    /**
     * Get managed dataset from the repository by ID.
     *
     * @param id the managed dataset ID.
     * @return The managed dataset if found, or null otherwise.
     * 
     * @throws HpcException
     */
    public HpcManagedDataset get(String id) throws HpcException;
    
    /**
     * Get managed datasets associated with a specific user.
     *
     * @param userId the user id.
     * @param association The association between the dataset and the user.
     * @return ManagedDataset collection, or null if no results found.
     * 
     * @throws HpcException
     */
    public List<HpcManagedDataset> 
           get(String userId, HpcDatasetUserAssociation association) 
        	  throws HpcException;
}

 