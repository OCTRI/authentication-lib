package org.octri.authentication.server.rest;

import java.util.HashMap;
import java.util.Map;

import org.octri.authentication.MethodSecurityExpressions;
import org.octri.authentication.server.security.entity.User;
import org.octri.authentication.server.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A REST controller for API requests.
 * 
 * @author sams
 */
@RestController
public class UserApiController {

	@Autowired
	private UserService userService;

	@Autowired
	private FilterBasedLdapUserSearch ldapSearch;

	@Autowired
	private String ldapOrganization;

	/**
	 * Searches the database for the username to determine whether it is taken
	 * @param username
	 * @param model
	 * @return
	 */
	@RequestMapping(path = "admin/user/taken/{username}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Map<String, Object> taken(@PathVariable("username") String username, Model model) {
		User existing = userService.findByUsername(username);
		Map<String, Object> out = new HashMap<>();
		out.put("taken", existing == null ? false : true);
		return out;
	}

	/**
	 * Searches LDAP for the username and provides user information.
	 * 
	 * @param username
	 * @return
	 */
	@PreAuthorize(MethodSecurityExpressions.ADMIN_OR_SUPER)
	@PostMapping("admin/user/ldapLookup")
	public Map<String, Object> ldapLookup(String username) {
		Map<String, Object> out = new HashMap<>();
		try {
			DirContextOperations ldapUser = ldapSearch.searchForUser(username);
			out.put("firstName", ldapUser.getStringAttribute("givenName"));
			out.put("lastName", ldapUser.getStringAttribute("sn"));
			out.put("email", ldapUser.getStringAttribute("mail"));
			out.put("institution", ldapOrganization);
		} catch (Exception e) {
			out.put("ldapLookupError", "Could not find username in LDAP");			
		}
		
		return out;
	}


}
