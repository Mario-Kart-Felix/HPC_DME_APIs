/**
 * HpcUserRegistrationRestService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.ws.rs;

import gov.nih.nci.hpc.dto.user.HpcUserCredentialsDTO;
import gov.nih.nci.hpc.dto.user.HpcUserDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * <p>
 * HPC User REST Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

@Path("/")
public interface HpcUserRestService
{    
    /**
     * GET user by ID.
     *
     * @param userRegistrationDTO The user DTO to register.
     */
    @POST
    @Path("/user")
    @Consumes("application/json,application/xml")
    public Response registerUser(HpcUserDTO userRegistrationDTO);
    
    /**
     * Get user by NCI User ID.
     *
     * @param nciUserId The registered user ID.
     * @return gov.nih.nci.hpc.dto.user.HpcUserDTO entity.
     */
    @GET
    @Path("/user/{nciUserId}")
    @Produces("application/json,application/xml")
    public Response getUser(@PathParam("nciUserId") String nciUserId); 

    /**
     * Authenticate
     *
     * @param nciUserId The registered user ID.
     * @param password NCI LDAP password
     * @return boolean entity.
     */
    @POST
    @Path("/user/authenticate")
    @Consumes("application/json,application/xml")
    public Response authenticate(HpcUserCredentialsDTO credentials);
}

 