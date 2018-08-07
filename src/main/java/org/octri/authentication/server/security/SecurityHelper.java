package org.octri.authentication.server.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

/**
 * A class for querying Spring Security Authentication. This was added after porting the ThymeLeaf templates to
 * Mustache. ThymeLeaf templates used a tag library for interacting with Spring Security, but with Mustache we need to
 * handle this in the Controller.<br>
 * <br>
 * With this class you may check user roles, determine if a user is logged in, or anonymous, and get the authenticated
 * username.<br>
 * <br>
 * All available roles for the system are kept in the {@link SecurityHelper.Role} enum.
 */
public class SecurityHelper {

	public static enum Role {
		ROLE_USER, ROLE_ADMIN, ROLE_SUPER;
	}

	private Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

	private Authentication authentication;

	public SecurityHelper(SecurityContext context) {
		authentication = context.getAuthentication();
		if (authentication != null) {
			authorities = authentication.getAuthorities();
		}
	}

	public boolean isAuthenticated() {
		return authentication == null ? false : authentication.isAuthenticated();
	}

	public boolean isAnonymous() {
		return authentication == null ? true : authentication instanceof AnonymousAuthenticationToken;
	}

	public String username() {
		return authentication == null ? "" : authentication.getName();
	}

	public boolean isAdminOrSuper() {
		return hasAnyRole(Arrays.asList(Role.ROLE_ADMIN, Role.ROLE_SUPER));
	}

	/**
	 * Checks if user contains the given role.
	 *
	 * @param role
	 *            A user role.
	 * @return True if the user contains the role.
	 */
	public boolean hasRole(Role role) {
		return authorities == null ? false
				: authorities.stream().anyMatch(authority -> authority.getAuthority().equals(role.name()));
	}

	/**
	 * Checks if a user contains at least one of the roles.
	 *
	 * @param roles
	 *            A list of user roles.
	 * @return True if the user contains one of the roles.
	 */
	public boolean hasAnyRole(List<Role> roles) {
		return roles.stream().anyMatch(role -> hasRole(role));
	}
}
