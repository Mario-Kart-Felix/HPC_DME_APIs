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
import gov.nih.nci.hpc.domain.metadata.HpcHierarchicalMetadataEntry;
import gov.nih.nci.hpc.domain.metadata.HpcMetadataEntry;
import gov.nih.nci.hpc.domain.metadata.HpcMetadataQuery;
import gov.nih.nci.hpc.exception.HpcException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
    
	// Metadata Query Operators
	private static final String EQUAL_OPERATOR = "EQUAL"; 
	private static final String NOT_EQUAL_OPERATOR = "NOT_EQUAL"; 
	private static final String LIKE_OPERATOR = "LIKE"; 
			
    // SQL Queries.
	private static final String GET_COLLECTION_IDS_EQUAL_SQL = 
		    "select distinct collection.object_id from public.\"r_coll_hierarchy_meta_main_ovrd\" collection " +
	        "where collection.meta_attr_name = ? and collection.meta_attr_value = ?";
	
	private static final String GET_COLLECTION_IDS_NOT_EQUAL_SQL = 
		    "select distinct collection.object_id from public.\"r_coll_hierarchy_meta_main_ovrd\" collection " +
	        "where collection.meta_attr_name = ? and collection.meta_attr_value <> ?";
	
	private static final String GET_COLLECTION_IDS_LIKE_SQL = 
			"select distinct collection.object_id from public.\"r_coll_hierarchy_meta_main_ovrd\" collection " +
		    "where collection.meta_attr_name = ? and collection.meta_attr_value like ?";
	
	private static final String GET_DATA_OBJECT_IDS_EQUAL_SQL = 
			"select distinct dataObject.object_id from public.\"r_data_hierarchy_meta_main_ovrd\" dataObject " +
		    "where dataObject.meta_attr_name = ? and dataObject.meta_attr_value = ?";
	
	private static final String GET_DATA_OBJECT_IDS_NOT_EQUAL_SQL = 
			"select distinct dataObject.object_id from public.\"r_data_hierarchy_meta_main_ovrd\" dataObject " +
		    "where dataObject.meta_attr_name = ? and dataObject.meta_attr_value <> ?";
	
	private static final String GET_DATA_OBJECT_IDS_LIKE_SQL = 
			"select distinct dataObject.object_id from public.\"r_data_hierarchy_meta_main_ovrd\" dataObject " +
		    "where dataObject.meta_attr_name = ? and dataObject.meta_attr_value like ?";
		   
	private static final String ENTITY_USER_ACCESS_SQL = 
			"select access.object_id from public.\"r_objt_access\" access, public.\"r_user_main\" account " +
	        "where account.user_name = ? and access.user_id = account.user_id";
	
	private static final String GET_COLLECTION_METADATA_SQL = 
			"select coll_metadata.meta_attr_name,  coll_metadata.meta_attr_value, coll_metadata.level " + 
	        "from public.\"r_coll_hierarchy_meta_main\" coll_metadata,  " +
			"public.\"r_coll_main\" collection where collection.coll_name = ? " +
	        "and collection.coll_id = coll_metadata.object_id";
	
	private static final String GET_DATA_OBJECT_METADATA_SQL = 
			"select dataObject_metadata.meta_attr_name,  dataObject_metadata.meta_attr_value, dataObject_metadata.level " + 
	        "from public.\"r_data_hierarchy_meta_main\" dataObject_metadata,  " +
			"public.\"r_data_main\" dataObject where dataObject.data_name = ? " +
	        "and dataObject.data_id = dataObject_metadata.object_id";
			 
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//
	
	// The Spring JDBC Template instance.
	@Autowired
	private JdbcTemplate jdbcTemplate = null;
	
	// Row mappers.
	private HpcObjectIdRowMapper objectIdRowMapper = new HpcObjectIdRowMapper();
	HpcHierarchicalMetadataEntryRowMapper hierarchicalMetadataEntryRowMapper = 
			                              new HpcHierarchicalMetadataEntryRowMapper();
	
	// Maps between metadata query operator to its SQL query
	private Map<String, String> dataObjectSQLQueries = new HashMap<>();
	private Map<String, String> collectionSQLQueries = new HashMap<>();
	
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
    private HpcMetadataDAOImpl()
    {
    	dataObjectSQLQueries.put(EQUAL_OPERATOR, GET_DATA_OBJECT_IDS_EQUAL_SQL);
    	dataObjectSQLQueries.put(NOT_EQUAL_OPERATOR, GET_DATA_OBJECT_IDS_NOT_EQUAL_SQL);
    	dataObjectSQLQueries.put(LIKE_OPERATOR, GET_DATA_OBJECT_IDS_LIKE_SQL);
    	
    	collectionSQLQueries.put(EQUAL_OPERATOR, GET_COLLECTION_IDS_EQUAL_SQL);
    	collectionSQLQueries.put(NOT_EQUAL_OPERATOR, GET_COLLECTION_IDS_NOT_EQUAL_SQL);
    	collectionSQLQueries.put(LIKE_OPERATOR, GET_COLLECTION_IDS_LIKE_SQL);
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcMetadataDAO Interface Implementation
    //---------------------------------------------------------------------//  
    
	@Override
    public List<Integer> getCollectionIds(List<HpcMetadataQuery> metadataQueries,
    		                              String dataManagementUsername) 
                                         throws HpcException
    {
		try {
			 HpcPreparedQuery prepareQuery = prepareQuery(collectionSQLQueries, metadataQueries,
					                                      dataManagementUsername);
		     return jdbcTemplate.query(prepareQuery.sql, objectIdRowMapper, prepareQuery.args);
		     
		} catch(DataAccessException e) {
		        throw new HpcException("Failed to get collection IDs: " + 
		                               e.getMessage(),
		    	    	               HpcErrorType.DATABASE_ERROR, e);
		}		
    }

	@Override 
	public List<Integer> getDataObjectIds(List<HpcMetadataQuery> metadataQueries,
			                              String dataManagementUsername) 
                                         throws HpcException
    {
		try {
			 HpcPreparedQuery prepareQuery = prepareQuery(dataObjectSQLQueries, metadataQueries, 
					                                      dataManagementUsername);
		     return jdbcTemplate.query(prepareQuery.sql, objectIdRowMapper, prepareQuery.args);
		     
		} catch(DataAccessException e) {
		        throw new HpcException("Failed to get data object IDs: " + 
		                               e.getMessage(),
		    	    	               HpcErrorType.DATABASE_ERROR, e);
		}		
    }
	
    @Override
    public List<HpcHierarchicalMetadataEntry> getCollectionMetadata(String path) throws HpcException
    {
		try {
		     return jdbcTemplate.query(GET_COLLECTION_METADATA_SQL, hierarchicalMetadataEntryRowMapper, path);
		     
		} catch(DataAccessException e) {
		        throw new HpcException("Failed to get collection hierarchical metadata: " + 
		                               e.getMessage(),
		    	    	               HpcErrorType.DATABASE_ERROR, e);
		}	
    }
    
    @Override
    public List<HpcHierarchicalMetadataEntry> getDataObjectMetadata(String path) throws HpcException
    {
		try {
		     return jdbcTemplate.query(GET_DATA_OBJECT_METADATA_SQL, hierarchicalMetadataEntryRowMapper, path);
		     
		} catch(DataAccessException e) {
		        throw new HpcException("Failed to get data object hierarchical metadata: " + 
		                               e.getMessage(),
		    	    	               HpcErrorType.DATABASE_ERROR, e);
		}	
    }
	
    //---------------------------------------------------------------------//
    // Helper Methods
    //---------------------------------------------------------------------//  
	
	// Map Object ID (from Long to Integer)
	private class HpcObjectIdRowMapper implements RowMapper<Integer>
	{
		@Override
		public Integer mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			Long objectId = rs.getLong("OBJECT_ID");
			return objectId != null ? objectId.intValue() : null;
		}
	}
	
	private class HpcHierarchicalMetadataEntryRowMapper implements RowMapper<HpcHierarchicalMetadataEntry>
	{
		@Override
		public HpcHierarchicalMetadataEntry mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			HpcHierarchicalMetadataEntry hierarchicalMetadataEntry = new HpcHierarchicalMetadataEntry();
			Long level = rs.getLong("LEVEL");
			hierarchicalMetadataEntry.setLevel(level != null ? level.intValue() : null);
			HpcMetadataEntry metadataEntry = new HpcMetadataEntry();
			metadataEntry.setAttribute(rs.getString("META_ATTR_NAME"));
			metadataEntry.setValue(rs.getString("META_ATTR_VALUE"));
			hierarchicalMetadataEntry.setMetadataEntry(metadataEntry);
			
			return hierarchicalMetadataEntry;
		}
	}
	
	// Prepared query.
	private class HpcPreparedQuery
	{
		public String sql = null;
		public Object[] args = null;
	}
	
    /**
     * Prepare a SQL query. Map operators to SQL and concatenate them with 'intersect'.
     *
     * @param sqlQueries The map from metadata query operator to SQL queries.
     * @param metadataQueries The metadata queries.
     * @param dataManagementUsername The data management user name.
     * 
     * @throws HpcException
     */
    private HpcPreparedQuery prepareQuery(Map<String, String> sqlQueries, 
    		                              List<HpcMetadataQuery> metadataQueries,
    		                              String dataManagementUsername) 
    		                             throws HpcException
    {
    	StringBuilder sqlQueryBuilder = new StringBuilder();
    	List<String> args = new ArrayList<>();
    	
    	// Combine the metadata queries into a single SQL statement.
    	for(HpcMetadataQuery metadataQuery : metadataQueries) {
    		String sqlQuery = sqlQueries.get(metadataQuery.getOperator());
    		if(sqlQuery == null) {
    		   throw new HpcException("Invalid metadata query operator: " + metadataQuery.getOperator(),
    				                  HpcErrorType.INVALID_REQUEST_INPUT);
    		}
    		
    		if(!args.isEmpty()) {
    			sqlQueryBuilder.append(" INTERSECT ");
    		}
    		sqlQueryBuilder.append(sqlQuery);
    		
    		args.add(metadataQuery.getAttribute());
    		args.add(metadataQuery.getValue());
    	}
    	
    	// Add a query to only include entities the user can access.
    	sqlQueryBuilder.append(" INTERSECT ");
    	sqlQueryBuilder.append(ENTITY_USER_ACCESS_SQL);
    	args.add(dataManagementUsername);
    	
    	HpcPreparedQuery preparedQuery = new HpcPreparedQuery();
    	preparedQuery.sql = sqlQueryBuilder.toString();
    	preparedQuery.args = args.toArray();
    	return preparedQuery;
    }
}

 