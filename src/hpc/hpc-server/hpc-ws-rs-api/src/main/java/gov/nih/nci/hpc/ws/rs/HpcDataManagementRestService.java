/**
 * HpcDataManagementRestService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.ws.rs;

import gov.nih.nci.hpc.dto.datamanagement.HpcCollectionRegistrationDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectRegistrationDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDownloadRequestDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcEntityPermissionsDTO;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 * <p>
 * HPC Data Management REST Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

@Path("/")
public interface HpcDataManagementRestService
{
    /**
     * PUT Collection registration request.
     *
     * @param path The collection path.
     * @param collectionRegistration A DTO contains the list of metadata entries to attach to the collection.
     * @return The REST service response.
     */
	@PUT
	@Path("/collection/{path:.*}")
	@Consumes(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response registerCollection(@PathParam("path") String path,
			                           HpcCollectionRegistrationDTO collectionRegistration);
	
    /**
     * GET Collection.
     *
     * @param path The collection path.
     * @param list An indicator to list sub-collections and data-objects.
     * @return The REST service response.
     */
	@GET
	@Path("/collection/{path:.*}")
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response getCollection(@PathParam("path") String path,
			                      @QueryParam("list") Boolean list);
	
    /**
     * POST Download Collection.
     *
     * @param path The collection path.
     * @param downloadRequest The download request.
     * @param mc The message context.
     * @return The REST service response.
     */
	@POST
	@Path("/collection/{path:.*}/download")
	@Consumes(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," +
			  MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadCollection(@PathParam("path") String path,
			                           HpcDownloadRequestDTO downloadRequest,
			                           @Context MessageContext mc);
	
    /**
     * POST Set collection permissions.
     *
     * @param path The collection path.
     * @param collectionPermissionsRequest Request to set collection permissions.
     * @return The REST service response.
     */
	@POST
	@Path("/collection/{path:.*}/acl")
	@Consumes(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response setCollectionPermissions(@PathParam("path") String path,
			                                 HpcEntityPermissionsDTO collectionPermissionsRequest);
	
    /**
     * GET collection permissions.
     *
     * @param path The collection path.
     * @return The REST service response.
     */
	@GET
	@Path("/collection/{path:.*}/acl")
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response getCollectionPermissions(@PathParam("path") String path);
	
    /**
     * PUT Data object registration request.
     *
     * @param path The data object path.
     * @param dataObjectRegistration A DTO contains the metadata and data transfer locations.
     * @param dataObjectInputStream The data object input stream.
     * @return The REST service response.
     */
	@PUT
	@Path("/dataObject/{path:.*}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response registerDataObject(
			        @PathParam("path") String path,
			        @Multipart(value = "dataObjectRegistration", type = "application/json")
			        HpcDataObjectRegistrationDTO dataObjectRegistration,
			        @Multipart(value = "dataObject", type = "application/octet-stream", required = false)
			        InputStream dataObjectInputStream);

    /**
     * GET Data Object.
     *
     * @param path The data object path.
     * @return The REST service response.
     */
	@GET
	@Path("/dataObject/{path:.*}")
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response getDataObject(@PathParam("path") String path);
	
    /**
     * POST Download Data Object.
     *
     * @param path The data object path.
     * @param downloadRequest The download request.
     * @param mc The message context.
     * @return The REST service response.
     */
	@POST
	@Path("/dataObject/{path:.*}/download")
	@Consumes(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," +
			  MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadDataObject(@PathParam("path") String path,
			                           HpcDownloadRequestDTO downloadRequest,
			                           @Context MessageContext mc);

    /**
     * POST Set Data Object permissions.
     *
     * @param path The data object path.
     * @param dataObjectPermissionsRequest Request to set data object permissions.
     * @return The REST service response.
     */
	@POST
	@Path("/dataObject/{path:.*}/acl")
	@Consumes(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response setDataObjectPermissions(@PathParam("path") String path,
			                                 HpcEntityPermissionsDTO dataObjectPermissionsRequest);
	
    /**
     * GET Data Object permissions.
     *
     * @param path The data object path.
     * @return The REST service response.
     */
	@GET
	@Path("/dataObject/{path:.*}/acl")
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response getDataObjectPermissions(@PathParam("path") String path);
	
    /**
     * GET Data Management Model (Metadata validation rules and hierarchy definition).
     *
     * @param doc The DOC to get the model for.
     * @return The REST service response.
     */
	@GET
	@Path("/model/{doc}")
	@Produces(MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML)
	public Response getDataManagementModel(@PathParam("doc") String doc);
}

