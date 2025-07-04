package org.octri.authentication.server.security;

import java.util.Date;

import org.octri.authentication.RequestUtils;
import org.octri.authentication.server.security.entity.LoginAttempt;
import org.octri.authentication.server.security.entity.User;
import org.octri.authentication.server.security.exception.UserManagementException;
import org.octri.authentication.server.security.service.LoginAttemptService;
import org.octri.authentication.server.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This abstract success handler can be extended to provide auditing of the login and reset the failed attempts flag.
 *
 * @author yateam
 */
@Component
public abstract class AuditLoginAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private UserService userService;

	/**
	 * Records a successful login attempt.
	 *
	 * @param auth
	 *            authenticated principal
	 * @param request
	 *            the successful login request
	 */
	protected void recordLoginSuccess(Authentication auth, HttpServletRequest request) {
		LoginAttempt attempt = new LoginAttempt();
		attempt.setUsername(auth.getName());
		attempt.setAttemptedAt(new Date());
		attempt.setSuccessful(true);
		attempt.setIpAddress(RequestUtils.getClientIpAddr(request));
		loginAttemptService.save(attempt);
	}

	/**
	 * Resets the failed login attempts for the given authenticated principal back to zero.
	 *
	 * @param auth
	 *            authenticated principal
	 * @throws UserManagementException
	 *             if the user cannot be saved
	 */
	protected void resetUserFailedAttempts(Authentication auth)
			throws UserManagementException {
		User user = userService.findByUsername(auth.getName());
		if (user == null || !(user.getConsecutiveLoginFailures() > 0)) {
			return;
		}

		user.setConsecutiveLoginFailures(0);
		userService.save(user);
	}

}
