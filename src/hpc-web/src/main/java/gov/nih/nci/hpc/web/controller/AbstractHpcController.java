package gov.nih.nci.hpc.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import gov.nih.nci.hpc.dto.security.HpcUserDTO;
import gov.nih.nci.hpc.web.util.HpcClientUtil;

public abstract class AbstractHpcController {
	@Value("${gov.nih.nci.hpc.ssl.cert}")
	protected String sslCertPath;
	@Value("${gov.nih.nci.hpc.ssl.cert.password}")
	protected String sslCertPassword;
	@Value("${gov.nih.nci.hpc.server.model}")
	private String hpcModelURL;
	@Value("${hpc.SYSTEM_ADMIN}")
	protected String SYSTEM_ADMIN;
	@Value("${hpc.GROUP_ADMIN}")
	protected String GROUP_ADMIN;
	@Value("${hpc.USER}")
	protected String USER;
	

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler({ Exception.class, java.net.ConnectException.class })
	public @ResponseBody HpcResponse handleUncaughtException(Exception ex, WebRequest request,
			HttpServletResponse response) {
		log.info("Converting Uncaught exception to RestResponse : " + ex.getMessage());

		response.setHeader("Content-Type", "application/json");
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return new HpcResponse("Error occurred", ex.toString());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public @ResponseBody HpcResponse handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request,
			HttpServletResponse response) {
		log.info("Converting IllegalArgumentException to RestResponse : " + ex.getMessage());

		response.setHeader("Content-Type", "application/json");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return new HpcResponse("Error occurred", ex.toString());
	}
	
	
	protected void populateDOCs(Model model, String authToken, HpcUserDTO user, HttpSession session) {
		List<String> userDOCs = new ArrayList<String>();
		if (user.getUserRole().equals("SYSTEM_ADMIN")) {
			List<String> docs = HpcClientUtil.getDOCs(authToken, hpcModelURL, sslCertPath, sslCertPassword, session);
			model.addAttribute("docs", docs);
		} else {
			userDOCs.add(user.getDoc());
			model.addAttribute("docs", userDOCs);
		}
	}
}