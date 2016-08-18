/**
 * HpcMetadataDAOImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.dao.postgresql.impl;

import gov.nih.nci.hpc.dao.HpcMetadataDAO;
import gov.nih.nci.hpc.domain.error.HpcErrorType;
import gov.nih.nci.hpc.exception.HpcException;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>
 * HPC Metadata DAO Implementation.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcMetadataDAOImpl implements HpcMetadataDAO
{ 
    //---------------------------------------------------------------------//
    // Constants
    //---------------------------------------------------------------------//    
    
    // SQL Queries.
	public static final String ASSOCIATE_METADATA_SQL = 
		   "insert into public.\"r_objt_metamap\" ( " +
                   "\"object_id\", \"meta_id\", \"create_ts\", \"modify_ts\" ) " +
                   "values (?, ?, ?, ?) " +
           "on conflict(\"object_id\", \"meta_id\") do nothing";

    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//
	
	// The Spring JDBC Template instance.
	@Autowired
	private JdbcTemplate jdbcTemplate = null;
	
    // The logger instance.
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Constructor for Spring Dependency Injection. 
     * 
     */
    private HpcMetadataDAOImpl()
    {
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcMetadataDAO Interface Implementation
    //---------------------------------------------------------------------//  
    
	@Override
	public void associateMetadata(int objectId, int metadataId) throws HpcException
    {
		try {
			 String now = String.valueOf((new Date()).getTime());
			 logger.error("ERAN: associating: " + objectId + " -> " + metadataId);
		     jdbcTemplate.update(ASSOCIATE_METADATA_SQL, objectId, metadataId, now, now);
		     logger.error("ERAN: associating: success");
		     
		} catch(DataAccessException dae) {
			    logger.error("ERAN: associating: failed");
			    throw new HpcException("Failed to associate metadata: " + dae.getMessage(),
			    		               HpcErrorType.DATABASE_ERROR, dae);
		}
    }
}

 