/**
 * HpcDatafileController.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See https://ncisvn.nci.nih.gov/svn/HPC_Data_Management/branches/hpc-prototype-dev/LICENSE.txt for details.
 */
package gov.nih.nci.hpc.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import gov.nih.nci.hpc.domain.datatransfer.HpcDownloadTaskType;
import gov.nih.nci.hpc.dto.datamanagement.HpcBulkDataObjectRegistrationStatusDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcBulkDataObjectRegistrationTaskDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcCollectionDownloadStatusDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectDownloadStatusDTO;
import gov.nih.nci.hpc.dto.security.HpcUserDTO;
import gov.nih.nci.hpc.web.model.HpcLogin;
import gov.nih.nci.hpc.web.util.HpcClientUtil;

/**
 * <p>
 * Controller to display task details
 * </p>
 *
 * @author <a href="mailto:Prasad.Konka@nih.gov">Prasad Konka</a>
 * @version $Id$
 */

@Controller
@EnableAutoConfiguration
@RequestMapping("/uploadtask")
public class HpcUploadTaskController extends AbstractHpcController {
	@Value("${gov.nih.nci.hpc.server.bulkregistration}")
	private String registrationServiceURL;

	/**
	 * Get operation to display registration task details and its metadata
	 * 
	 * @param body
	 * @param taskId
	 * @param type
	 * @param model
	 * @param bindingResult
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String home(@RequestBody(required = false) String body, @RequestParam String taskId,
			@RequestParam String type, Model model, BindingResult bindingResult, HttpSession session,
			HttpServletRequest request) {
		try {
			HpcUserDTO user = (HpcUserDTO) session.getAttribute("hpcUser");
			String userId = (String) session.getAttribute("hpcUserId");
			String authToken = (String) session.getAttribute("hpcUserToken");
			if (user == null || authToken == null) {
				ObjectError error = new ObjectError("hpcLogin", "Invalid user session!");
				bindingResult.addError(error);
				HpcLogin hpcLogin = new HpcLogin();
				model.addAttribute("hpcLogin", hpcLogin);
				return "redirect:/login?returnPath=task&taskId=" + taskId + "&type=" + type;
			}

			if (taskId == null || type == null)
				return "redirect:/uploadtasks";

			String queryServiceURL = registrationServiceURL + "/" + taskId;
			HpcBulkDataObjectRegistrationStatusDTO uploadTask = HpcClientUtil.getDataObjectRegistrationTask(authToken,
					queryServiceURL, sslCertPath, sslCertPassword);
			model.addAttribute("hpcBulkDataObjectRegistrationTaskDTO", uploadTask.getTask());
		} catch (Exception e) {
			model.addAttribute("error", "Failed to get registration status: " + e.getMessage());
			return "redirect:/uploadtasks";
		}
		return "dataobjectsuploadtask";
	}
}
