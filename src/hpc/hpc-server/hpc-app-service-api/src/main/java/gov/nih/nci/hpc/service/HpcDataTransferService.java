/**
 * HpcDataTransferService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.service;

import gov.nih.nci.hpc.domain.datamanagement.HpcPathAttributes;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataObjectDownloadCleanup;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataObjectDownloadRequest;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataObjectDownloadResponse;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataObjectUploadRequest;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataObjectUploadResponse;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataTransferDownloadStatus;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataTransferType;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataTransferUploadStatus;
import gov.nih.nci.hpc.domain.datatransfer.HpcFileLocation;
import gov.nih.nci.hpc.exception.HpcException;

import java.io.File;
import java.util.List;

/**
 * <p>
 * HPC Data Transfer Service Interface.
 * </p>
 *
 * @author <a href="mailto:Mahidhar.Narra@nih.gov">Mahidhar Narra</a>
 * @version $Id$
 */

public interface HpcDataTransferService 
{    
    /** 
     * Upload data. Either upload from the input stream or submit a transfer request for the source.
     * 
     * @param sourceLocation The source for data transfer.
     * @param sourceFile The source file.
     * @param path The registration path.
     * @param userId The user-id who requested the data upload.
     * @param callerObjectId The caller's provided data object ID.
     * @return HpcDataObjectUploadResponse
     * 
     * @throws HpcException
     */
	public HpcDataObjectUploadResponse uploadDataObject(HpcFileLocation sourceLocation, 
                                                        File sourceFile, 
                                                        String path, String userId,
                                                        String callerObjectId)
                                                       throws HpcException;
    /**
     * Upload a data object file.
     *
     * @param dataUploadRequest The data upload request
     * @return HpcDataObjectUploadResponse A data object upload response.
     * 
     * @throws HpcException
     */
    public HpcDataObjectUploadResponse uploadDataObject(HpcDataObjectUploadRequest uploadRequest) 
    		                                           throws HpcException;
    
    /** 
     * Download a data object file.
     * 
     * @param archiveLocation The archive file location.
     * @param destinationLocation The user requested file destination.
     * @param dataTransferType The data transfer type.
     * @return HpcDataObjectDownloadResponse A data object download response.
     * 
     * @throws HpcException
     */
	public HpcDataObjectDownloadResponse downloadDataObject(
			                                     HpcFileLocation archiveLocation, 
			                                     HpcFileLocation destinationLocation,
			                                     HpcDataTransferType dataTransferType) 
			                                     throws HpcException;
    
    /**
     * Download a data object file.
     *
     * @param dataDownloadRequest The data object download request.
     * @return HpcDataObjectDownloadResponse A data object download response.
     * 
     * @throws HpcException
     */
    public HpcDataObjectDownloadResponse downloadDataObject(HpcDataObjectDownloadRequest downloadRequest) 
    		                                               throws HpcException;
    
    /**
     * Get a data transfer upload request status.
     *
     * @param dataTransferType The data transfer type.
     * @param dataTransferRequestId The data transfer request ID.
     * 
     * @return HpcDataTransferUploadStatus the data transfer upload request status.
     * 
     * @throws HpcException
     */
    public HpcDataTransferUploadStatus getDataTransferUploadStatus(HpcDataTransferType dataTransferType,
    		                                                       String dataTransferRequestId) 
    		                                                      throws HpcException;
    
    /**
     * Get a data transfer download request status.
     *
     * @param dataTransferType The data transfer type.
     * @param dataTransferRequestId The data transfer request ID.
     * 
     * @return HpcDataTransferDownloadStatus the data transfer download request status.
     * 
     * @throws HpcException
     */
    public HpcDataTransferDownloadStatus getDataTransferDownloadStatus(HpcDataTransferType dataTransferType,
    		                                                           String dataTransferRequestId) 
    		                                                          throws HpcException;
    
    /**
     * Get the size of the data transferred of a specific request.
     *
     * @param dataTransferType The data transfer type.
     * @param dataTransferRequestId The data transfer request ID.
     * 
     * @return The size of the data transferred in bytes.
     * 
     * @throws HpcException
     */
    public long getDataTransferSize(HpcDataTransferType dataTransferType,
    		                        String dataTransferRequestId) 
    		                       throws HpcException;
    
    /**
     * Get endpoint/path attributes .
     *
     * @param dataTransferType The data transfer type.
     * @param fileLocation The endpoint/path to get attributes for.
     * @param getSize If set to true, the file/directory size will be returned. 
     * @return HpcPathAttributes 
     * 
     * @throws HpcException
     */
    public HpcPathAttributes getPathAttributes(HpcDataTransferType dataTransferType,
    		                                   HpcFileLocation fileLocation,
    		                                   boolean getSize) 
    		                                  throws HpcException;
    
    /**
     * Get a file from the archive.
     *
     * @param dataTransferType The data transfer type.
     * @param fileId The file ID.
     * 
     * @throws HpcException
     */
    public File getArchiveFile(HpcDataTransferType dataTransferType,
    		                   String fileId)  
    		                  throws HpcException;
    
    /**
     * Get all data object download cleanup entries. 
     *
     * @return List<HpcDataObjectDownloadCleanup>
     * 
     * @throws HpcException
     */
    public List<HpcDataObjectDownloadCleanup> getDataObjectDownloadCleanups() throws HpcException;
    
    /**
     * Cleanup data object download file. 
     *
     * @param dataObjectDownloadCleanup The info about the file to cleanup.
     * 
     * @throws HpcException
     */
    public void cleanupDataObjectDownloadFile(
    		           HpcDataObjectDownloadCleanup dataObjectDownloadCleanup) throws HpcException;
}

 