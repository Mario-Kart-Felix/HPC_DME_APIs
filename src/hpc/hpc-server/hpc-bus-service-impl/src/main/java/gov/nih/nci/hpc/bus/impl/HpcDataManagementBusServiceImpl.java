/**
 * HpcDataManagementBusServiceImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.bus.impl;

import gov.nih.nci.hpc.bus.HpcDataManagementBusService;
import gov.nih.nci.hpc.domain.datamanagement.HpcCollection;
import gov.nih.nci.hpc.domain.datamanagement.HpcDataObject;
import gov.nih.nci.hpc.domain.error.HpcErrorType;
import gov.nih.nci.hpc.domain.metadata.HpcMetadataEntry;
import gov.nih.nci.hpc.domain.metadata.HpcMetadataQuery;
import gov.nih.nci.hpc.dto.datamanagement.HpcCollectionDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcCollectionListDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectListDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectRegistrationDTO;
import gov.nih.nci.hpc.exception.HpcException;
import gov.nih.nci.hpc.service.HpcDataManagementService;
import gov.nih.nci.hpc.service.HpcDataTransferService;
import gov.nih.nci.hpc.service.HpcUserService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * HPC Data Management Business Service Implementation.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcDataManagementBusServiceImpl implements HpcDataManagementBusService
{  
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//

    // Application service instances.
	
	@Autowired
    private HpcUserService userService = null;
	
	@Autowired
    private HpcDataTransferService dataTransferService = null;
	
	@Autowired
    private HpcDataManagementService dataManagementService = null;
	
    // The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Constructor for Spring Dependency Injection.
     * 
     */
    private HpcDataManagementBusServiceImpl()
    {
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcCollectionBusService Interface Implementation
    //---------------------------------------------------------------------//  
    
    @Override
    public void registerCollection(String path,
    					           List<HpcMetadataEntry> metadataEntries)  
    		                      throws HpcException
    {
    	logger.info("Invoking registerCollection(List<HpcMetadataEntry>): " + 
    			    metadataEntries);
    	
    	// Input validation.
    	if(path == null || metadataEntries == null) {
    	   throw new HpcException("Null path or metadata entries",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Create a collection directory.
    	dataManagementService.createDirectory(path);
    	
    	// Attach the metadata.
    	dataManagementService.addMetadataToCollection(path, metadataEntries);
    }
    
    @Override
    public HpcCollectionDTO getCollection(String path) throws HpcException
    {
    	logger.info("Invoking getCollection(String): " + path);
    	
    	// Input validation.
    	if(path == null) {
    	   throw new HpcException("Null colelction path",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Get the collection.
    	HpcCollection collection = dataManagementService.getCollection(path);
    	if(collection == null) {
    	   return null;
    	}
    		
    	// Get the metadata for this collection.
    	List<HpcMetadataEntry> metadataEntries = 
    		                   dataManagementService.getCollectionMetadata(path);
    		
    	return toDTO(collection, metadataEntries);
    }
    
    @Override
    public HpcCollectionListDTO getCollections(List<HpcMetadataQuery> metadataQueries) 
                                              throws HpcException
    {
    	logger.info("Invoking getCollections(List<HpcMetadataQuery>): " + 
    			    metadataQueries);
    	
    	// Input validation.
    	if(metadataQueries == null) {
    	   throw new HpcException("Null metadata queries",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Construct the DTO.
    	HpcCollectionListDTO collectionsDTO = new HpcCollectionListDTO();
    	for(HpcCollection collection : dataManagementService.getCollections(metadataQueries)) {
    		// Get the metadata for this collection.
    		List<HpcMetadataEntry> metadataEntries = 
    		dataManagementService.getCollectionMetadata(collection.getAbsolutePath());
    		
    		// Combine collection attributes and metadata into a single DTO.
    		collectionsDTO.getCollections().add(toDTO(collection, metadataEntries));
    	}
    	
    	return collectionsDTO;
    }
    
    @Override
    public void registerDataObject(String path,
    		                       HpcDataObjectRegistrationDTO dataObjectRegistrationDTO)  
    		                      throws HpcException
    {
    	logger.info("Invoking registerDataObject(HpcDataObjectRegistrationDTO): " + 
    			    dataObjectRegistrationDTO);
    	
    	// Input validation.
    	if(path == null || dataObjectRegistrationDTO == null) {
    	   throw new HpcException("Null path or dataObjectRegistrationDTO",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Calculated the data transfer destination absolute path by appending the path to the 
    	// destination's base path (if a base path provided).
    	if(dataObjectRegistrationDTO.getLocations() != null &&
    	   dataObjectRegistrationDTO.getLocations().getDestination() != null) {
    	   String basePath = dataObjectRegistrationDTO.getLocations().getDestination().getPath();	
    	   dataObjectRegistrationDTO.getLocations().getDestination().setPath(
    		                                           basePath != null ? basePath + path : path);
    	}
    	
    	// Create a data object file (in the data management system).
    	dataManagementService.createFile(path, false);
    	
		// Transfer the file. 
        dataTransferService.transferData(dataObjectRegistrationDTO.getLocations());				 
    	
    	// Attach the user provided metadata.
    	dataManagementService.addMetadataToDataObject(
    			                 path, 
    			                 dataObjectRegistrationDTO.getMetadataEntries());
    	
    	// Create and attach the file source and location metadata.
    	dataManagementService.addFileLocationsMetadataToDataObject(
    			                 path, 
    			                 dataObjectRegistrationDTO.getLocations().getDestination(),
    			                 dataObjectRegistrationDTO.getLocations().getSource()); 
    }
    
    @Override
    public HpcDataObjectListDTO getDataObjects(List<HpcMetadataQuery> metadataQueries) 
                                           throws HpcException
    {
    	logger.info("Invoking getDataObjects(List<HpcMetadataQuery>): " + 
    			    metadataQueries);
    	
    	// Input validation.
    	if(metadataQueries == null) {
    	   throw new HpcException("Null metadata queries",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Construct the DTO.
    	HpcDataObjectListDTO dataObjectsDTO = new HpcDataObjectListDTO();
    	for(HpcDataObject dataObject : dataManagementService.getDataObjects(metadataQueries)) {
    		// Get the metadata for this data object.
    		List<HpcMetadataEntry> metadataEntries = 
    		dataManagementService.getDataObjectMetadata(dataObject.getAbsolutePath());
    		
    		// Combine data object attributes and metadata into a single DTO.
    		dataObjectsDTO.getDataObjects().add(toDTO(dataObject, metadataEntries));
    	}
    	
    	return dataObjectsDTO;
    }
    
    @Override
    public void closeConnection()
    {
    	dataManagementService.closeConnection();
    }
    
    //---------------------------------------------------------------------//
    // Helper Methods
    //---------------------------------------------------------------------//
    
    /**
     * Create a collection DTO from domain objects.
     * 
     * @param collection The collection domain object.
     * @param metadataEntries The list of metadata domain objects.
     *
     * @return The DTO.
     */
    private HpcCollectionDTO toDTO(HpcCollection collection, List<HpcMetadataEntry> metadataEntries) 
    {
    	HpcCollectionDTO collectionDTO = new HpcCollectionDTO();
    	collectionDTO.setCollection(collection);
    	if(metadataEntries != null) {
    	   collectionDTO.getMetadataEntries().addAll(metadataEntries);
    	}
    	
    	return collectionDTO;
    }
    
    /**
     * Create a data object DTO from domain objects.
     * 
     * @param data object The data object domain object.
     * @param metadataEntries The list of metadata domain objects.
     *
     * @return The DTO.
     */
    private HpcDataObjectDTO toDTO(HpcDataObject dataObject, List<HpcMetadataEntry> metadataEntries) 
    {
    	HpcDataObjectDTO dataObjectDTO = new HpcDataObjectDTO();
    	dataObjectDTO.setDataObject(dataObject);
    	if(metadataEntries != null) {
    	   dataObjectDTO.getMetadataEntries().addAll(metadataEntries);
    	}
    	
    	return dataObjectDTO;
    }
}

 