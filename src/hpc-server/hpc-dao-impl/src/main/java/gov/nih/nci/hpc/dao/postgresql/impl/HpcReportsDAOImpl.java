/**
 * HpcEventDAOImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.dao.postgresql.impl;

import gov.nih.nci.hpc.dao.HpcReportsDAO;
import gov.nih.nci.hpc.domain.report.HpcReport;
import gov.nih.nci.hpc.domain.report.HpcReportCriteria;
import gov.nih.nci.hpc.domain.report.HpcReportEntry;
import gov.nih.nci.hpc.domain.report.HpcReportEntryAttribute;
import gov.nih.nci.hpc.domain.report.HpcReportType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * HPC Reports DAO Implementation.
 * </p>
 *
 * @author <a href="mailto:prasad.konka@nih.gov">Prasad Konka</a>
 */

public class HpcReportsDAOImpl implements HpcReportsDAO
{ 
    //---------------------------------------------------------------------//
    // Constants
    //---------------------------------------------------------------------//    
	
	// USAGE_SUMMARY.
	private static final String SUM_OF_DATA_SQL =
	"SELECT sum(to_number(a.meta_attr_value, '9999999999999999999')) totalSize, " + 
	"max(to_number(a.meta_attr_value, '9999999999999999999')) maxSize, "+
	"avg(to_number(a.meta_attr_value, '9999999999999999999')) avgSize FROM public.r_meta_main a "+
	"inner join r_objt_metamap b on a.meta_id=b.meta_id "+
	"inner join r_data_main c on b.object_id=c.data_id "+
	"where a.meta_attr_name = 'source_file_size'";
	
	private static final String TOTAL_NUM_OF_USERS_SQL = 
			"SELECT count(*) totalUsers FROM public.\"HPC_USER\"";
	
	private static final String TOTAL_NUM_OF_DATA_OBJECTS_SQL = 
			"SELECT count(*) totalObjs FROM public.r_data_main";

	private static final String TOTAL_NUM_OF_COLLECTIONS_BY_NAME_SQL = 
			"select a.meta_attr_value attr, count(a.meta_attr_name) cnt from r_meta_main a, r_coll_main b, r_objt_metamap c where b.coll_id=c.object_id and c.meta_id=a.meta_id and a.meta_attr_name='collection_type' group by a.meta_attr_value";

	private static final String TOTAL_NUM_OF_META_ATTRS_SQL = 
			"SELECT count(meta_id) totalAttrs FROM public.r_meta_main";
	
	private static final String FILE_SIZE_RANGE_SQL =
		"SELECT count(a.meta_id) FROM public.r_meta_main a " +
		"inner join r_objt_metamap b on a.meta_id=b.meta_id " +
		"inner join r_data_main c on b.object_id=c.data_id " + 
		"where a.meta_attr_name = 'source_file_size' and to_number(a.meta_attr_value, '9999999999999999999') BETWEEN ? AND ?";

	// USAGE_SUMMARY_DATE_RANGE.
	private static final String SUM_OF_DATA_BY_DATE_SQL =
		"SELECT sum(to_number(a.meta_attr_value, '9999999999999999999')) totalSize, " + 
		"max(to_number(a.meta_attr_value, '9999999999999999999')) maxSize, "+
		"avg(to_number(a.meta_attr_value, '9999999999999999999')) avgSize FROM public.r_meta_main a "+
		"inner join r_objt_metamap b on a.meta_id=b.meta_id "+
		"inner join r_data_main c on b.object_id=c.data_id "+
		"where a.meta_attr_name = 'source_file_size' and CAST(a.create_ts as double precision) BETWEEN ? AND ?";
	
	private static final String TOTAL_NUM_OF_USERS_BY_DATE_SQL = 
		"SELECT count(*) totalUsers FROM public.\"HPC_USER\" where \"CREATED\" BETWEEN ? and ?";
	
	private static final String TOTAL_NUM_OF_DATA_OBJECTS_BY_DATE_SQL =
		"SELECT count(distinct a.data_id) totalObjs FROM public.r_data_main a " +
		"inner join public.r_objt_metamap b on a.data_id=b.object_id "+
		"inner join public.r_meta_main c on b.meta_id=c.meta_id "+
		"where CAST(a.create_ts as double precision) BETWEEN ? AND ?";

	private static final String TOTAL_NUM_OF_COLLECTIONS_BY_NAME_AND_DATE_SQL = 
		"select a.meta_attr_value attr, count(a.meta_attr_name) cnt from r_meta_main a "+
		"inner join r_objt_metamap b on a.meta_id=b.meta_id "+
		"inner join r_coll_main c on b.object_id=c.coll_id "+
		"where a.meta_attr_name='collection_type' and CAST(b.create_ts as double precision) BETWEEN ? AND ? group by a.meta_attr_value";

	private static final String TOTAL_NUM_OF_META_ATTRS_BY_DATE_SQL = 
		"SELECT count(*) totalAttrs FROM public.r_meta_main where CAST(create_ts as double precision) BETWEEN ? AND ?";

	private static final String FILE_SIZE_RANGE_BY_DATE_SQL =
			"SELECT count(a.meta_id) FROM public.r_meta_main a " +
			"inner join r_objt_metamap b on a.meta_id=b.meta_id " +
			"inner join r_data_main c on b.object_id=c.data_id " + 
			"where a.meta_attr_name = 'source_file_size' and CAST(a.create_ts as double precision) BETWEEN ? AND ? and to_number(meta_attr_value, '9999999999999999999') BETWEEN ? AND ?";

	// USAGE_SUMMARY_BY_DOC.
	private static final String SUM_OF_DATA_BY_DOC_SQL = 
			"SELECT sum(to_number(a.meta_attr_value, '9999999999999999999')) totalSize, max(to_number(a.meta_attr_value, '9999999999999999999')) maxSize, avg(to_number(a.meta_attr_value, '9999999999999999999')) avgSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id=c.data_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c  where a.meta_attr_name='registered_by_doc' and b.object_id=c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id = c.data_id)";

	private static final String TOTAL_NUM_OF_USERS_BY_DOC_SQL = 
			"SELECT count(*) totalUsers FROM public.\"HPC_USER\" where \"DOC\"=?";

	private static final String TOTAL_NUM_OF_DATA_OBJECTS_BY_DOC_SQL =
			"SELECT count(distinct a.data_id) totalObjs FROM public.r_data_main a " +
			"inner join public.r_objt_metamap b on a.data_id=b.object_id "+
			"inner join public.r_meta_main c on b.meta_id=c.meta_id "+
			"where c.meta_attr_name='registered_by_doc' and c.meta_attr_value=?";

	private static final String TOTAL_NUM_OF_COLLECTIONS_BY_NAME_DOC_SQL = 
		"select a.meta_attr_value attr, count(a.meta_attr_name) cnt from r_meta_main a "+
		"inner join r_objt_metamap b on a.meta_id=b.meta_id "+
		"inner join r_coll_main c on b.object_id=c.coll_id "+
		"where a.meta_attr_name='collection_type' and c.coll_id in "+
		"(select distinct b.object_id from public.r_meta_main a "+
		"inner join public.r_objt_metamap b on a.meta_id=b.meta_id "+
		"inner join r_coll_main c on b.object_id=c.coll_id "+
		"where a.meta_attr_name='registered_by_doc' and a.meta_attr_value=?) "+ 
		"group by a.meta_attr_value";
	
	private static final String TOTAL_NUM_OF_META_ATTRS_BY_DOC_SQL = 
	"SELECT count(a.meta_id) totalAttrs FROM public.r_meta_main a inner join public.r_objt_metamap b  on a.meta_id = b.meta_id where b.object_id in "+ 
			"(select distinct b.object_id from public.r_meta_main a "+
			"inner join public.r_objt_metamap b on  a.meta_id=b.meta_id "+
			"inner join public.r_data_main c on b.object_id = c.data_id "+
			"where a.meta_attr_name='registered_by_doc' and a.meta_attr_value=? "+
			"union "+
			"select distinct b.object_id from public.r_meta_main a " +
			"inner join public.r_objt_metamap b on  a.meta_id=b.meta_id " +
			"inner join public.r_coll_main d on b.object_id = d.coll_id " +
			"where a.meta_attr_name='registered_by_doc' and a.meta_attr_value=?)";
	
	private static final String FILE_SIZE_RANGE_BY_DOC_SQL = 
			"SELECT count(*) FROM public.r_meta_main a "+
			"inner join public.r_objt_metamap b on a.meta_id = b.meta_id  "+
			"where a.meta_attr_name = 'source_file_size' and to_number(a.meta_attr_value, '9999999999999999999') BETWEEN ? AND ?  and b.object_id in "+ 
			"(select distinct b.object_id from public.r_meta_main a "+
			"inner join public.r_objt_metamap b on  a.meta_id=b.meta_id "+
			"inner join public.r_data_main c on b.object_id = c.data_id "+
			"where a.meta_attr_name='registered_by_doc' and a.meta_attr_value=?)";
	
	// USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE.
	private static final String SUM_OF_DATA_BY_DOC_DATE_SQL = 
			"SELECT sum(to_number(a.meta_attr_value, '9999999999999999999')) totalSize, max(to_number(a.meta_attr_value, '9999999999999999999')) maxSize, avg(to_number(a.meta_attr_value, '9999999999999999999')) avgSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and b.meta_id = a.meta_id and c.data_id = b.object_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c where a.meta_attr_name='registered_by_doc' and b.object_id=c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id = c.data_id " +
			") and CAST(c.create_ts as double precision) BETWEEN ? AND ?";

	private static final String LARGEST_FILE_BY_DOC_DATE_SQL = 
			"SELECT max(to_number(a.meta_attr_value, '9999999999999999999')) totalSize FROM public.r_meta_main a, public.r_objt_metamap b where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b where a.meta_attr_name='registered_by_doc' and a.meta_attr_value=? and a.meta_id=b.meta_id) and CAST(a.create_ts as double precision) BETWEEN ? AND ?";

	private static final String AVERAGE_FILE_BY_DOC_DATE_SQL = 
			"SELECT avg(to_number(a.meta_attr_value, '9999999999999999999')) totalSize FROM public.r_meta_main a, public.r_objt_metamap b where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c where a.meta_attr_name='registered_by_doc' and b.object_id=c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id = c.data_id " +
			") and CAST(a.create_ts as double precision) BETWEEN ? AND ?";

	private static final String TOTAL_NUM_OF_USERS_BY_DOC_DATE_SQL = 
			"SELECT count(*) totalUsers FROM public.\"HPC_USER\" where \"DOC\"=? and \"CREATED\" BETWEEN ?  AND ?";

	
	private static final String TOTAL_NUM_OF_DATA_OBJECTS_BY_DOC_DATE_SQL = 
			"SELECT count(distinct c.data_id) totalObjs FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where c.data_id = b.object_id and a.meta_attr_name='registered_by_doc' and a.meta_attr_value=? and a.meta_id=b.meta_id and CAST(c.create_ts as double precision) BETWEEN ? AND ? "; 

	private static final String TOTAL_NUM_OF_COLLECTIONS_BY_NAME_DOC_DATE_SQL = 
			"select a.meta_attr_value attr, count(a.meta_attr_name) cnt from r_meta_main a, r_coll_main b, r_objt_metamap c where b.coll_id=c.object_id and c.meta_id=a.meta_id and a.meta_attr_name='collection_type' and b.coll_id in"+
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_coll_main c where c.coll_id=b.object_id and a.meta_attr_name='registered_by_doc' and a.meta_attr_value=? and a.meta_id=b.meta_id and CAST(b.create_ts as double precision) BETWEEN ? AND ? ) "+
			"group by a.meta_attr_value";

	private static final String TOTAL_NUM_OF_META_ATTRS_BY_DOC_DATE_SQL = 
			"SELECT count(a.meta_id) totalAttrs FROM public.r_meta_main a, public.r_objt_metamap b  where a.meta_id = b.meta_id and b.object_id in "+
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c, public.r_coll_main d where a.meta_attr_name='registered_by_doc' and a.meta_attr_value=? and a.meta_id=b.meta_id and (c.data_id = b.object_id or d.coll_id = b.object_id)) and CAST(b.create_ts as double precision) BETWEEN ? AND ? ";

	private static final String FILE_SIZE_RANGE_BY_DOC_DATE_SQL = 
			"SELECT count(*) FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c  where a.meta_id = b.meta_id and a.meta_attr_name = 'source_file_size' and to_number(a.meta_attr_value, '9999999999999999999') BETWEEN ? AND ? and c.data_id = b.object_id and b.object_id in " +
					"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c  where a.meta_attr_name='registered_by_doc' and b.object_id = c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id) and CAST(c.create_ts as double precision) BETWEEN ? AND ?";
	
	// USAGE_SUMMARY_BY_USER. 
	private static final String SUM_OF_DATA_BY_USER_SQL =
			"SELECT sum(to_number(a.meta_attr_value, '9999999999999999999')) totalSize, max(to_number(a.meta_attr_value, '9999999999999999999')) maxSize, avg(to_number(a.meta_attr_value, '9999999999999999999')) avgSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id=c.data_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c  where a.meta_attr_name='registered_by' and b.object_id=c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id = c.data_id)";

	private static final String LARGEST_FILE_BY_USER_SQL = 
			"SELECT max(to_number(a.meta_attr_value, '9999999999999999999')) totalSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id = c.data_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b where a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id)";

	private static final String AVERAGE_FILE_BY_USER_SQL = 
			"SELECT avg(to_number(a.meta_attr_value, '9999999999999999999')) totalSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id=b.meta_id and b.object_id=c.data_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b,  public.r_data_main c where a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id=c.data_id)";

	private static final String TOTAL_NUM_OF_DATA_OBJECTS_BY_USER_SQL = 
			"SELECT count(distinct c.data_id) totalObjs FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where c.data_id = b.object_id and a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id"; 

	private static final String TOTAL_NUM_OF_COLLECTIONS_BY_NAME_USER_SQL = 
			"select a.meta_attr_value attr, count(a.meta_attr_name) cnt from r_meta_main a, r_coll_main b, r_objt_metamap c where b.coll_id=c.object_id and c.meta_id=a.meta_id and a.meta_attr_name='collection_type' and b.coll_id in"+
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_coll_main c where c.coll_id=b.object_id and a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id) "+
			"group by a.meta_attr_value";

	private static final String TOTAL_NUM_OF_META_ATTRS_BY_USER_SQL = 
			"SELECT count(a.meta_id) totalAttrs FROM public.r_meta_main a, public.r_objt_metamap b  where a.meta_id = b.meta_id and b.object_id in "+
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c, public.r_coll_main d where a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id  and (c.data_id = b.object_id or d.coll_id = b.object_id)) ";

	private static final String FILE_SIZE_RANGE_BY_USER_SQL = 
			"SELECT count(*) FROM public.r_meta_main a, public.r_objt_metamap b where a.meta_id = b.meta_id and a.meta_attr_name = 'source_file_size' and to_number(a.meta_attr_value, '9999999999999999999') BETWEEN ? AND ? and b.object_id in " +
					"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c where a.meta_attr_name='registered_by' and b.object_id = c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id)";
	
	// USAGE_SUMMARY_BY_USER_BY_DATE_RANGE.
	private static final String SUM_OF_DATA_BY_USER_DATE_SQL =
			"SELECT sum(to_number(a.meta_attr_value, '9999999999999999999')) totalSize, max(to_number(a.meta_attr_value, '9999999999999999999')) maxSize, avg(to_number(a.meta_attr_value, '9999999999999999999')) avgSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id=c.data_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c  where a.meta_attr_name='registered_by' and b.object_id=c.data_id and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id = c.data_id) and CAST(c.create_ts as double precision) BETWEEN ? AND ?";

	private static final String LARGEST_FILE_BY_USER_DATE_SQL = 
			"SELECT max(to_number(a.meta_attr_value, '9999999999999999999')) totalSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id = b.meta_id and b.object_id = c.data_id and b.object_id in " +
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b where a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id) and CAST(a.create_ts as double precision) BETWEEN ? AND ?";

	private static final String AVERAGE_FILE_BY_USER_DATE_SQL = 
			"SELECT avg(to_number(a.meta_attr_value, '9999999999999999999')) totalSize FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name = 'source_file_size' and a.meta_id=b.meta_id and b.object_id=c.data_id and b.object_id  in " +
			"(select b.object_id from public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id and b.object_id=c.data_id) and CAST(c.create_ts as double precision) BETWEEN ? AND ?";

	private static final String TOTAL_NUM_OF_DATA_OBJECTS_BY_USER_DATE_SQL = 
			"SELECT count(distinct c.data_id) totalObjs FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where c.data_id = b.object_id and a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id and CAST(c.create_ts as double precision) BETWEEN ? AND ? "; 

	private static final String TOTAL_NUM_OF_COLLECTIONS_BY_NAME_USER_DATE_SQL = 
			"select a.meta_attr_value attr, count(a.meta_attr_name) cnt from r_meta_main a, r_coll_main b, r_objt_metamap c where b.coll_id=c.object_id and c.meta_id=a.meta_id and a.meta_attr_name='collection_type' and b.coll_id in"+
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_coll_main c where c.coll_id=b.object_id and a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id) and CAST(b.create_ts as double precision) BETWEEN ? AND ? "+
			"group by a.meta_attr_value";

	private static final String TOTAL_NUM_OF_META_ATTRS_BY_USER_DATE_SQL = 
			"SELECT count(a.meta_id) totalAttrs FROM public.r_meta_main a, public.r_objt_metamap b  where a.meta_id = b.meta_id and b.object_id in "+
			"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c, public.r_coll_main d  where a.meta_attr_name='registered_by' and a.meta_attr_value=? and a.meta_id=b.meta_id  and (c.data_id = b.object_id or d.coll_id = b.object_id)) and CAST(a.create_ts as double precision) BETWEEN ? AND ?";

	private static final String FILE_SIZE_RANGE_BY_USER_DATE_SQL = 
			"SELECT count(*) FROM public.r_meta_main a, public.r_objt_metamap b, public.r_data_main c where a.meta_id = b.meta_id and a.meta_attr_name = 'source_file_size' and to_number(a.meta_attr_value, '9999999999999999999') BETWEEN ? AND ? and c.data_id = b.object_id and b.object_id in " +
					"(select distinct b.object_id from public.r_meta_main a, public.r_objt_metamap b, r_data_main c where a.meta_attr_name='registered_by' and a.meta_attr_value=? and b.object_id = c.data_id and a.meta_id=b.meta_id ) and CAST(c.create_ts as double precision) BETWEEN ? AND ?";
	
	private static final String USERS_SQL = "select \"USER_ID\" from public.\"HPC_USER\"";
	
	private static final String DOCS_SQL = "select distinct meta_attr_value from public.r_meta_main where meta_attr_name='registered_by_doc'";

	//---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//
	
	// The Spring JDBC Template instance.
	@Autowired
	private JdbcTemplate jdbcTemplate = null;
	
	// The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
	// Row mappers.
	private HpcReportRowMapper reportRowMapper = new HpcReportRowMapper();
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Constructor for Spring Dependency Injection. 
     * 
     */
    private HpcReportsDAOImpl()
    {
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcReportsDAO Interface Implementation
    //---------------------------------------------------------------------//  
    
	private String getUsersSize(HpcReportCriteria criteria, Date[] dates, Object[] docArg, Object[] docDateArgs)
	{
		Long usersSize = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			usersSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_USERS_SQL, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			usersSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_USERS_BY_DATE_SQL, dates, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			usersSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_USERS_BY_DOC_SQL, docArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			usersSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_USERS_BY_DOC_DATE_SQL, docDateArgs, Long.class);
		
		if(usersSize != null)
			return usersSize.toString();
		else return "0";
	}
	
	private String[] getTotalDataSize(HpcReportCriteria criteria, Long[] dates, String[] docArg, Object[] docDateArgs, String[] userArg, Object[] userDateArgs)
	{
		Map<String, Object> totals = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			totals = jdbcTemplate.queryForMap(SUM_OF_DATA_SQL);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			totals = jdbcTemplate.queryForMap(SUM_OF_DATA_BY_DATE_SQL, dates);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			totals = jdbcTemplate.queryForMap(SUM_OF_DATA_BY_DOC_SQL, docArg);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			totals = jdbcTemplate.queryForMap(SUM_OF_DATA_BY_DOC_DATE_SQL, docDateArgs);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			totals = jdbcTemplate.queryForMap(SUM_OF_DATA_BY_USER_SQL, userArg);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			totals = jdbcTemplate.queryForMap(SUM_OF_DATA_BY_USER_DATE_SQL, userDateArgs);
		
		String[] returnVal = new String[]{"0","0","0"};
		if(totals != null)
		{
			Iterator values = totals.values().iterator();
			Double totalSize = new Double(values.next().toString());
			returnVal[0] = humanReadableByteCount(totalSize, false);
			Double maxSize = new Double(values.next().toString());
			returnVal[1] = humanReadableByteCount(maxSize, false);
			Double avgSize = new Double(values.next().toString());
			returnVal[2] = humanReadableByteCount(avgSize, false);
			
		}
		return returnVal;
	}

/*	
	private String getLargestSize(HpcReportCriteria criteria, Long[] dates, String[] docArg, Object[] docDateArgs, String[] userArg, Object[] userDateArgs)
	{
		Long largestSize = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			largestSize = jdbcTemplate.queryForObject(LARGEST_FILE_SQL, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			largestSize = jdbcTemplate.queryForObject(LARGEST_FILE_BY_DATE_SQL, dates, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			largestSize = jdbcTemplate.queryForObject(LARGEST_FILE_BY_DOC_SQL, docArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			largestSize = jdbcTemplate.queryForObject(LARGEST_FILE_BY_DOC_DATE_SQL, docDateArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			largestSize = jdbcTemplate.queryForObject(LARGEST_FILE_BY_USER_SQL, userArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			largestSize = jdbcTemplate.queryForObject(LARGEST_FILE_BY_USER_DATE_SQL, userDateArgs, Long.class);
		
		if(largestSize != null)
		{
			Long longValue = new Long(largestSize);
			return humanReadableByteCount(longValue, false);
		}
		else return "0";
	}

	private String getAverageSize(HpcReportCriteria criteria, Long[] dates, String[] docArg, Object[] docDateArgs, String[] userArg, Object[] userDateArgs)
	{
		Long averageSize = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			averageSize = jdbcTemplate.queryForObject(AVERAGE_FILE_SQL, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			averageSize = jdbcTemplate.queryForObject(AVERAGE_FILE_BY_DATE_SQL, dates, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			averageSize = jdbcTemplate.queryForObject(AVERAGE_FILE_BY_DOC_SQL, docArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			averageSize = jdbcTemplate.queryForObject(AVERAGE_FILE_BY_DOC_DATE_SQL, docDateArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			averageSize = jdbcTemplate.queryForObject(AVERAGE_FILE_BY_USER_SQL, userArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			averageSize = jdbcTemplate.queryForObject(AVERAGE_FILE_BY_USER_DATE_SQL, userDateArgs, Long.class);
		
		if(averageSize != null)
		{
			Long longValue = new Long(averageSize);
			return humanReadableByteCount(longValue, false);
		}
		else return "0";
	}
*/
	private String getTotalDataObjSize(HpcReportCriteria criteria, Long[] dates, String[] docArg, Object[] docDateArgs, String[] userArg, Object[] userDateArgs)
	{
		Long dataSize = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			dataSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_DATA_OBJECTS_SQL, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			dataSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_DATA_OBJECTS_BY_DATE_SQL, dates, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			dataSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_DATA_OBJECTS_BY_DOC_SQL, docArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			dataSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_DATA_OBJECTS_BY_DOC_DATE_SQL, docDateArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			dataSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_DATA_OBJECTS_BY_USER_SQL, userArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			dataSize = jdbcTemplate.queryForObject(TOTAL_NUM_OF_DATA_OBJECTS_BY_USER_DATE_SQL, userDateArgs, Long.class);
		
		if(dataSize != null)
			return dataSize.toString();
		else return "0";
	}

	private List<Map<String, Object>> getTotalCollectionsSize(HpcReportCriteria criteria, Long[] dates, String[] docArg, Object[] docDateArgs, String[] userArg, Object[] userDateArgs)
	{
		List<Map<String, Object>> list = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			list = jdbcTemplate.queryForList(TOTAL_NUM_OF_COLLECTIONS_BY_NAME_SQL);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			list = jdbcTemplate.queryForList(TOTAL_NUM_OF_COLLECTIONS_BY_NAME_AND_DATE_SQL, dates);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			list = jdbcTemplate.queryForList(TOTAL_NUM_OF_COLLECTIONS_BY_NAME_DOC_SQL, docArg);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			list = jdbcTemplate.queryForList(TOTAL_NUM_OF_COLLECTIONS_BY_NAME_DOC_DATE_SQL, docDateArgs);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			list = jdbcTemplate.queryForList(TOTAL_NUM_OF_COLLECTIONS_BY_NAME_USER_SQL, userArg);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			list = jdbcTemplate.queryForList(TOTAL_NUM_OF_COLLECTIONS_BY_NAME_USER_DATE_SQL, userDateArgs);
		return list;
	}

	private String getTotalMetaAttrCount(HpcReportCriteria criteria, Long[] dates, String[] docArg, Object[] docDateArgs, String[] userArg, Object[] userDateArgs)
	{
		Long metaAttrCount = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			metaAttrCount = jdbcTemplate.queryForObject(TOTAL_NUM_OF_META_ATTRS_SQL, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			metaAttrCount = jdbcTemplate.queryForObject(TOTAL_NUM_OF_META_ATTRS_BY_DATE_SQL, dates, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
		{
			String[] newDoc = new String[2];
			newDoc[0] = docArg[0];
			newDoc[1] = docArg[0];
			metaAttrCount = jdbcTemplate.queryForObject(TOTAL_NUM_OF_META_ATTRS_BY_DOC_SQL, newDoc, Long.class);
		}
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			metaAttrCount = jdbcTemplate.queryForObject(TOTAL_NUM_OF_META_ATTRS_BY_DOC_DATE_SQL, docDateArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			metaAttrCount = jdbcTemplate.queryForObject(TOTAL_NUM_OF_META_ATTRS_BY_USER_SQL, userArg, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			metaAttrCount = jdbcTemplate.queryForObject(TOTAL_NUM_OF_META_ATTRS_BY_USER_DATE_SQL, userDateArgs, Long.class);
		
		if(metaAttrCount != null)
			return metaAttrCount.toString();
		else return "0";
	}
	
	private String getFileSize(HpcReportCriteria criteria, Object[] fileSizeArgs, Object[] filesizedateArgs, Object[] filesizedocArgs, Object[] filesizedocDateArgs, Object[] filesizeuserArgs, Object[] filesizeuserDateArgs)
	{
		Long fileSize = null;
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY))
			fileSize = jdbcTemplate.queryForObject(FILE_SIZE_RANGE_SQL, fileSizeArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			fileSize = jdbcTemplate.queryForObject(FILE_SIZE_RANGE_BY_DATE_SQL, filesizedateArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC))
			fileSize = jdbcTemplate.queryForObject(FILE_SIZE_RANGE_BY_DOC_SQL, filesizedocArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
			fileSize = jdbcTemplate.queryForObject(FILE_SIZE_RANGE_BY_DOC_DATE_SQL, filesizedocDateArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER))
			fileSize = jdbcTemplate.queryForObject(FILE_SIZE_RANGE_BY_USER_SQL, filesizeuserArgs, Long.class);
		else if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE))
			fileSize = jdbcTemplate.queryForObject(FILE_SIZE_RANGE_BY_USER_DATE_SQL, filesizeuserDateArgs, Long.class);
		
		if(fileSize != null)
			return fileSize.toString();
		else return "0";
	}	
	
	public List<HpcReport> generatReport(HpcReportCriteria criteria)
	{
		List<HpcReport> reports = new ArrayList<HpcReport>();
		
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY) || criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DATE_RANGE))
			reports.add(getReport(criteria));
		
		if(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC) || criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_DOC_BY_DATE_RANGE))
		{
			if(criteria.getDocs().isEmpty())
				criteria.getDocs().addAll(getDocs());
			
			List<String> docs = new ArrayList<String>();
			docs.addAll(criteria.getDocs());
			
			for(String doc : docs)
			{
				criteria.getDocs().clear();
				criteria.getDocs().add(doc);
				HpcReport report = getReport(criteria);
				reports.add(report);
			}
		}
			
		if((criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER) || criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE)))
		{
				if(criteria.getUsers().isEmpty())
					criteria.getUsers().addAll(getUsers());
				
				List<String> users = new ArrayList<String>();
				users.addAll(criteria.getUsers());
				
				for(String user : users)
				{
					criteria.getUsers().clear();
					criteria.getUsers().add(user);
					HpcReport report = getReport(criteria);
					reports.add(report);
				}
		}
		return reports;
	}

	private List<String> getUsers()
	{
		return jdbcTemplate.queryForList(USERS_SQL, String.class);
	}
	
	private List<String> getDocs()
	{
		return jdbcTemplate.queryForList(DOCS_SQL, String.class);
	}

	public HpcReport getReport(HpcReportCriteria criteria)
	{
		List<HpcReport> reports = new ArrayList<HpcReport>();
		HpcReport report = new HpcReport();

		//Total Users
		Date fromDate = null;
		Date toDate = null;
		Long fromDateLong = null;
		Long toDateLong = null;
		Long[] dateLongArgs = new Long[2];
		Date[] dateArgs = new Date[2];
		if(criteria.getFromDate() != null && criteria.getToDate() != null)
		{
			fromDate = criteria.getFromDate().getTime();
			toDate = criteria.getToDate().getTime();
			fromDateLong = criteria.getFromDate().getTime().getTime() / 1000;
			toDateLong = criteria.getToDate().getTime().getTime() / 1000;
			dateArgs[0] = fromDate;
			dateArgs[1] = toDate;
			dateLongArgs[0] = fromDateLong;
			dateLongArgs[1] = toDateLong;
		}
		
		String[] docArg = new String[1];
		if(criteria.getDocs() != null && criteria.getDocs().size()>0)
			docArg[0] = criteria.getDocs().get(0);

		Object[] docDateUsersArgs = new Object[3];
		if(criteria.getDocs() != null && criteria.getDocs().size()>0)
		{
			docDateUsersArgs[0] = criteria.getDocs().get(0);
			docDateUsersArgs[1] = fromDate;
			docDateUsersArgs[2] = toDate;
		}
		
		Object[] docDateArgs = new Object[3];
		if(criteria.getDocs() != null && criteria.getDocs().size()>0)
		{
			docDateArgs[0] = criteria.getDocs().get(0);
			docDateArgs[1] = fromDateLong;
			docDateArgs[2] = toDateLong;
		}
		
		String[] userArg = new String[1];
		if(criteria.getUsers() != null && criteria.getUsers().size()>0)
			userArg[0] = criteria.getUsers().get(0);
		
		Object[] userDateArgs = new Object[3];
		if(criteria.getUsers() != null && criteria.getUsers().size()>0)
		{
			userDateArgs[0] = criteria.getUsers().get(0);
			userDateArgs[1] = fromDateLong;
			userDateArgs[2] = toDateLong;
		}
		
		long start = System.currentTimeMillis();
		
		//TOTAL_NUM_OF_REGISTERED_USERS
		HpcReportEntry userSizeEntry = new HpcReportEntry();
		userSizeEntry.setAttribute(HpcReportEntryAttribute.TOTAL_NUM_OF_REGISTERED_USERS);
		userSizeEntry.setValue(getUsersSize(criteria, dateArgs, docArg, docDateUsersArgs));
		long stop = System.currentTimeMillis();
		logger.error(": " + (stop - start));
		start = System.currentTimeMillis();
		
		//Total Size - TOTAL_DATA_SIZE
		HpcReportEntry sizeEntry = new HpcReportEntry();
		sizeEntry.setAttribute(HpcReportEntryAttribute.TOTAL_DATA_SIZE);
		String[] totals = getTotalDataSize(criteria, dateLongArgs, docArg, docDateArgs, userArg, userDateArgs);
		sizeEntry.setValue(totals[0]);
		stop = System.currentTimeMillis();
		logger.error("TOTAL_DATA_SIZE " + (stop - start));
		start = System.currentTimeMillis();
		
		//Largest file - LARGEST_FILE_SIZE
		HpcReportEntry largestFileSizeEntry = new HpcReportEntry();
		largestFileSizeEntry.setAttribute(HpcReportEntryAttribute.LARGEST_FILE_SIZE);
		largestFileSizeEntry.setValue(totals[1]);
		stop = System.currentTimeMillis();
		logger.error("LARGEST_FILE_SIZE " + (stop - start));
		start = System.currentTimeMillis();
		
		//Average file - AVERAGE_FILE_SIZE
		HpcReportEntry averageFileSizeEntry = new HpcReportEntry();
		averageFileSizeEntry.setAttribute(HpcReportEntryAttribute.AVERAGE_FILE_SIZE);
		averageFileSizeEntry.setValue(totals[2]);
		stop = System.currentTimeMillis();
		logger.error("AVERAGE_FILE_SIZE " + (stop - start));
		start = System.currentTimeMillis();

		//Total number of data objects - TOTAL_NUM_OF_DATA_OBJECTS
		HpcReportEntry numOfDataObjEntry = new HpcReportEntry();
		numOfDataObjEntry.setAttribute(HpcReportEntryAttribute.TOTAL_NUM_OF_DATA_OBJECTS);
		numOfDataObjEntry.setValue(getTotalDataObjSize(criteria, dateLongArgs, docArg, docDateArgs, userArg, userDateArgs));
		stop = System.currentTimeMillis();
		logger.error("TOTAL_NUM_OF_DATA_OBJECTS " + (stop - start));
		start = System.currentTimeMillis();

		//Total number of collections - TOTAL_NUM_OF_COLLECTIONS
		List<Map<String, Object>> list = getTotalCollectionsSize(criteria, dateLongArgs, docArg, docDateArgs, userArg, userDateArgs);
		StringBuffer str = new StringBuffer();
        str.append("[");
        if(list != null)
        {
                for(Map<String, Object> listEntry : list)
                {
                        String type = null;
                        String count = null;
                        Iterator<String> iter = listEntry.keySet().iterator();
                        while(iter.hasNext())
                        {
                                String name = iter.next();
                                if(name.equals("cnt"))
                                {
                                        Long value = (Long)listEntry.get(name);
                                        count = value.toString();
                                } else
                                        type = (String) listEntry.get(name);
                        }
                        str.append("{"+type + ": "+ count + "}");
                }
        }
        str.append("]");
		HpcReportEntry numOfCollEntry = new HpcReportEntry();
		numOfCollEntry.setAttribute(HpcReportEntryAttribute.TOTAL_NUM_OF_COLLECTIONS);
		numOfCollEntry.setValue(str.toString());
		stop = System.currentTimeMillis();
		logger.error("TOTAL_NUM_OF_COLLECTIONS " + (stop - start));
		start = System.currentTimeMillis();

		//Total Meta attributes Size - TOTAL_NUMBER_OF_META_ATTRS
		HpcReportEntry metasizeEntry = new HpcReportEntry();
		metasizeEntry.setAttribute(HpcReportEntryAttribute.TOTAL_NUMBER_OF_META_ATTRS);
		metasizeEntry.setValue(getTotalMetaAttrCount(criteria, dateLongArgs, docArg, docDateArgs, userArg, userDateArgs));
		stop = System.currentTimeMillis();
		logger.error("TOTAL_NUMBER_OF_META_ATTRS " + (stop - start));
		start = System.currentTimeMillis();
		
		//Distribution of files - FILE_SIZE_BELOW_1_MB
		Long lower = Long.valueOf(0);
		Long upper = Long.valueOf(1000000);

		Object[] fileSizeArgs  = new Object[2];
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		Object[] filesizedateArgs = new Object[4];
		filesizedateArgs[0] = fromDateLong;
		filesizedateArgs[1] = toDateLong;
		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		Object[] filesizedocArgs = new Object[3];
		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;
		if(criteria.getDocs() != null && criteria.getDocs().size()>0)
			filesizedocArgs[2] = criteria.getDocs().get(0);

		Object[] filesizedocDateArgs = new Object[5];
		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;
		if(criteria.getDocs() != null && criteria.getDocs().size()>0)
			filesizedocDateArgs[2] = criteria.getDocs().get(0);
		filesizedocDateArgs[3] = fromDateLong;
		filesizedocDateArgs[4] = toDateLong;

		Object[] filesizeuserArgs = new Object[3];
		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;
		if(criteria.getUsers() != null && criteria.getUsers().size()>0)
			filesizeuserArgs[2] = criteria.getUsers().get(0);

		Object[] filesizeuserDateArgs = new Object[5];
		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;
		if(criteria.getUsers() != null && criteria.getUsers().size()>0)
			filesizeuserDateArgs[2] = criteria.getUsers().get(0);
		filesizeuserDateArgs[3] = fromDateLong;
		filesizeuserDateArgs[4] = toDateLong;
		
		HpcReportEntry oneMBEntry = new HpcReportEntry();
		oneMBEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_BELOW_1_MB);
		oneMBEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_BELOW_1_MB " + (stop - start));
		start = System.currentTimeMillis();
		
		lower = Long.valueOf(1000000);
		upper = Long.valueOf(10000000);
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;

		HpcReportEntry tenMBEntry = new HpcReportEntry();
		tenMBEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_1_MB_10_MB);
		tenMBEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_1_MB_10_MB " + (stop - start));
		start = System.currentTimeMillis();
		
		lower = Long.valueOf(10000000);
		upper = Long.valueOf(50000000);
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;

		HpcReportEntry fiftyMBEntry = new HpcReportEntry();
		fiftyMBEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_10_MB_50_MB);
		fiftyMBEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_10_MB_50_MB " + (stop - start));
		start = System.currentTimeMillis();

		lower = Long.valueOf(50000000);
		upper = Long.valueOf(100000000);
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;
		HpcReportEntry hundredMBEntry = new HpcReportEntry();
		hundredMBEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_50_MB_100_MB);
		hundredMBEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_50_MB_100_MB " + (stop - start));
		start = System.currentTimeMillis();

		lower = Long.valueOf(100000000);
		upper = Long.valueOf(500000000);
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;
		HpcReportEntry fivehundredMBEntry = new HpcReportEntry();
		fivehundredMBEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_100_MB_500_MB);
		fivehundredMBEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_100_MB_500_MB " + (stop - start));
		start = System.currentTimeMillis();

		lower = Long.valueOf(500000000);
		upper = Long.valueOf(1000000000);
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;
		HpcReportEntry onegbEntry = new HpcReportEntry();
		onegbEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_500_MB_1_GB);
		onegbEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_500_MB_1_GB " + (stop - start));
		start = System.currentTimeMillis();

		lower = Long.valueOf(1000000000);
		upper = new Long("10000000000");
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;
		HpcReportEntry tengbEntry = new HpcReportEntry();
		tengbEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_1_GB_10_GB);
		tengbEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_1_GB_10_GB " + (stop - start));
		start = System.currentTimeMillis();

		lower = new Long("10000000001");
		upper = new Long("100000000000000");
		fileSizeArgs[0] = lower;
		fileSizeArgs[1] = upper;

		filesizedateArgs[2] = lower;
		filesizedateArgs[3] = upper;

		filesizedocArgs[0] = lower;
		filesizedocArgs[1] = upper;

		filesizedocDateArgs[0] = lower;
		filesizedocDateArgs[1] = upper;

		filesizeuserArgs[0] = lower;
		filesizeuserArgs[1] = upper;

		filesizeuserDateArgs[0] = lower;
		filesizeuserDateArgs[1] = upper;
		HpcReportEntry overtengbEntry = new HpcReportEntry();
		overtengbEntry.setAttribute(HpcReportEntryAttribute.FILE_SIZE_OVER_10_GB);
		overtengbEntry.setValue(getFileSize(criteria, fileSizeArgs, filesizedateArgs, filesizedocArgs, filesizedocDateArgs, filesizeuserArgs, filesizeuserDateArgs));
		stop = System.currentTimeMillis();
		logger.error("FILE_SIZE_OVER_10_GB " + (stop - start));
		start = System.currentTimeMillis();

		report.setGeneratedOn(Calendar.getInstance());
		if(criteria.getDocs() != null && criteria.getDocs().size()>0)
			report.setDoc(criteria.getDocs().get(0));
		if(criteria.getUsers() != null && criteria.getUsers().size()>0)
			report.setUser(criteria.getUsers().get(0));
		report.setType(criteria.getType());
		if(criteria.getFromDate() != null)
			report.setFromDate(criteria.getFromDate());
		if(criteria.getToDate() != null)
			report.setToDate(criteria.getToDate());
		
		if(!(criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER) || criteria.getType().equals(HpcReportType.USAGE_SUMMARY_BY_USER_BY_DATE_RANGE)))
			report.getReportEntries().add(userSizeEntry);
		report.getReportEntries().add(sizeEntry);
		report.getReportEntries().add(largestFileSizeEntry);
		report.getReportEntries().add(averageFileSizeEntry);
		report.getReportEntries().add(numOfDataObjEntry);
		report.getReportEntries().add(numOfCollEntry);
		report.getReportEntries().add(metasizeEntry);
		report.getReportEntries().add(oneMBEntry);
		report.getReportEntries().add(tenMBEntry);
		report.getReportEntries().add(fiftyMBEntry);
		report.getReportEntries().add(hundredMBEntry);
		report.getReportEntries().add(fivehundredMBEntry);
		report.getReportEntries().add(onegbEntry);
		report.getReportEntries().add(tengbEntry);
		report.getReportEntries().add(overtengbEntry);
		
		return report;
	}
	private HpcReport generateUsageSummaryReportByDateRange(HpcReportCriteria criteria)
	{
		return jdbcTemplate.queryForObject(SUM_OF_DATA_SQL, reportRowMapper);
	}

	private HpcReport generateUsageSummaryByDocReport(HpcReportCriteria criteria)
	{
		return jdbcTemplate.queryForObject(SUM_OF_DATA_SQL, reportRowMapper);
	}

	private HpcReport generateUsageSummaryByDocDateRangeReport(HpcReportCriteria criteria)
	{
		return jdbcTemplate.queryForObject(SUM_OF_DATA_SQL, reportRowMapper);
	}

	private HpcReport generateUsageSummaryByUserReport(HpcReportCriteria criteria)
	{
		return jdbcTemplate.queryForObject(SUM_OF_DATA_SQL, reportRowMapper);
	}

	private HpcReport generateUsageSummaryByUserDateRangeReport(HpcReportCriteria criteria)
	{
		return jdbcTemplate.queryForObject(SUM_OF_DATA_SQL, reportRowMapper);
	}
	
	// HpcEvent Row to Object mapper.
	private class HpcReportRowMapper implements RowMapper<HpcReport>
	{
		@Override
		public HpcReport mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			HpcReport report = new HpcReport();
            
            return report;
		}
	}
	
	private static final String[] SI_UNITS = { "B", "kB", "MB", "GB", "TB", "PB", "EB" };
	private static final String[] BINARY_UNITS = { "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB" };

	public static String humanReadableByteCount(final double bytes, final boolean useSIUnits)
	{
	    final String[] units = useSIUnits ? SI_UNITS : BINARY_UNITS;
	    final int base = useSIUnits ? 1000 : 1024;

	    // When using the smallest unit no decimal point is needed, because it's the exact number.
	    if (bytes < base) {
	        return bytes + " " + units[0];
	    }

	    final int exponent = (int) (Math.log(bytes) / Math.log(base));
	    final String unit = units[exponent];
	    return String.format("%.1f %s", bytes / Math.pow(base, exponent), unit);
	}	
}

 