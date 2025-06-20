package org.octri.authentication.server.controller;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ocpsoft.prettytime.PrettyTime;
import org.octri.authentication.MethodSecurityExpressions;
import org.octri.authentication.server.security.entity.PasswordResetToken;
import org.octri.authentication.server.security.entity.User;
import org.octri.authentication.server.security.exception.UserManagementException;
import org.octri.authentication.server.security.password.Messages;
import org.octri.authentication.server.security.service.EmailNotificationService;
import org.octri.authentication.server.security.service.PasswordGeneratorService;
import org.octri.authentication.server.security.service.PasswordResetTokenService;
import org.octri.authentication.server.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for all the reset password functionality
 *
 * @author yateam
 *
 */
@Controller
@Scope("session")
public class UserPasswordController {

	/**
	 * Message displayed when a password reset request is submitted.
	 */
	public static final String EMAIL_SENT_CONFIRMATION_TEMPLATE = "An email was sent to your address containing instructions to change your password. This request will expire %s"
			+ ". If you do not receive an email in a few minutes please try again or contact your system administrator.";

	/**
	 * Message displayed when an unexpected exception occurs while handling a password reset request.
	 */
	public static final String GENERIC_ERROR_MESSAGE = "There was an unexpected error processing your request. "
			+ "Please try again or contact your system administrator if this persists.";

	/**
	 * Message displayed when password reset is attempted for an LDAP account.
	 */
	public static final String LDAP_USER_WARNING_MESSAGE = "The account you entered cannot have the password reset. "
			+ "If you feel this is in error, please contact your system administrator.";

	/**
	 * Message displayed when there is no account with the specified email address.
	 */
	public static final String UNKNOWN_EMAIL_MESSAGE = "The email address you entered cannot be found. "
			+ "Please try a different address or contact your system administrator if you feel this is in error.";

	private static final Log log = LogFactory.getLog(UserPasswordController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordResetTokenService passwordResetTokenService;

	@Autowired
	private EmailNotificationService emailNotificationService;

	@Autowired
	private PasswordGeneratorService generator;

	/**
	 * Present a form for changing a password when credentials are expired.
	 *
	 * @param model
	 *            object holding view data
	 * @param request
	 *            the current {@link HttpServletRequest}
	 * @return change template
	 */
	@PreAuthorize(MethodSecurityExpressions.ANONYMOUS)
	@GetMapping("user/password/change")
	public String changePassword(ModelMap model, HttpServletRequest request) {
		model.addAttribute("formTitle", Messages.TITLE_CHANGE_PASSWORD);
		model.addAttribute("formRoute", "/user/password/change");

		final String username = (String) request.getSession().getAttribute("lastUsername");
		Assert.notNull(username, Messages.COULD_NOT_FIND_USERNAME_IN_SESSION);

		final User user = userService.findByUsername(username);
		Assert.notNull(user, Messages.COULD_NOT_FIND_AN_EXISTING_USER);
		model.addAttribute("user", user);

		return "user/password/form";
	}

	/**
	 * Persist changed password.
	 *
	 * @param currentPassword
	 *            The user's current password
	 * @param newPassword
	 *            The user's new password
	 * @param confirmPassword
	 *            Confirming the user's new password
	 * @param redirectAttributes
	 *            Used to hold attributes for a redirect.
	 * @param request
	 *            The {@link HttpServletRequest}
	 * @param model
	 *            Object holding view data
	 * @return Redirects to /login
	 */
	@PreAuthorize(MethodSecurityExpressions.ANONYMOUS)
	@PostMapping("user/password/change")
	public String changePassword(@ModelAttribute("currentPassword") String currentPassword,
			@ModelAttribute("newPassword") String newPassword,
			@ModelAttribute("confirmPassword") String confirmPassword, RedirectAttributes redirectAttributes,
			HttpServletRequest request, ModelMap model) {
		final String username = (String) request.getSession().getAttribute("lastUsername");
		Assert.notNull(username, Messages.COULD_NOT_FIND_USERNAME_IN_SESSION);

		final User user = userService.findByUsername(username);
		Assert.notNull(user, Messages.COULD_NOT_FIND_AN_EXISTING_USER);

		model.addAttribute("user", user);
		model.addAttribute("formTitle", Messages.TITLE_CHANGE_PASSWORD);
		model.addAttribute("formRoute", "/user/password/change");

		try {
			ImmutablePair<User, List<String>> result = userService.changePassword(user, currentPassword, newPassword,
					confirmPassword, request.getParameterMap());

			if (result.right.isEmpty()) {
				redirectAttributes.addFlashAttribute("passwordChanged", true);
				model.clear();
				return "redirect:/login";
			} else {
				model.addAttribute("errorMessages", result.right);
				model.addAttribute("currentPasswordIncorrect",
						result.right.contains(Messages.CURRENT_PASSWORD_INCORRECT));
				model.addAttribute("passwordValidationError", true);
				return "user/password/form";
			}
		} catch (UserManagementException ex) {
			log.info("Exception while changing password", ex);
			model.addAttribute("errorMessage", ex.getMessage());
			return "user/password/form";
		} catch (RuntimeException ex) {
			log.error("Unexpected runtime exception when " + username + " tried to change their password", ex);
			model.addAttribute("errorMessage", Messages.DEFAULT_ERROR_MESSAGE);
			return "user/password/form";
		}
	}

	/**
	 * A form for initiating a password reset.
	 *
	 * @return forgot template
	 */
	@PreAuthorize(MethodSecurityExpressions.ANONYMOUS)
	@GetMapping("user/password/forgot")
	public String forgotPassword() {
		return "user/password/forgot";
	}

	/**
	 * Handles generating and sending a password reset token.
	 *
	 * @param email
	 *            The user's email address.
	 * @param model
	 *            Object holding view data
	 * @param request
	 *            The {@link HttpServletRequest}
	 * @param redirectAttributes
	 *            The {@link RedirectAttributes}
	 * @return Returns to the same forgot template and presents a confirmation message.
	 */
	@PreAuthorize(MethodSecurityExpressions.ANONYMOUS)
	@PostMapping("user/password/forgot")
	public String forgotPassword(@ModelAttribute("email") String email, ModelMap model, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		try {
			final User user = userService.findByEmail(email);

			if (user == null) {
				redirectAttributes.addFlashAttribute("errorMessage", UNKNOWN_EMAIL_MESSAGE);
				return "redirect:/user/password/forgot";
			}

			if (!userService.canResetPassword(user)) {
				redirectAttributes.addFlashAttribute("errorMessage", LDAP_USER_WARNING_MESSAGE);
				return "redirect:/user/password/forgot";
			}

			// User-initiated password resets get a shorter timeout
			// TODO: Introduce another configuration property if study teams want to customize this
			PasswordResetToken token = passwordResetTokenService.generatePasswordResetToken(user,
					Duration.ofMinutes(30));
			PrettyTime formatter = new PrettyTime();
			String successMessage = String.format(EMAIL_SENT_CONFIRMATION_TEMPLATE,
					formatter.format(token.getExpiryDate()));
			emailNotificationService.sendPasswordResetTokenEmail(token, request, false);
			redirectAttributes.addFlashAttribute("successMessage", successMessage);
			return "redirect:/user/password/forgot";
		} catch (Exception ex) {
			log.error("Unexpected error while processing forgotten password", ex);
			redirectAttributes.addFlashAttribute("errorMessage", GENERIC_ERROR_MESSAGE);
			return "redirect:/user/password/forgot";
		}
	}

	/**
	 * A form for resetting a password.
	 *
	 * @param token
	 *            The user's password reset token.
	 * @param model
	 *            Object holding view data
	 * @return Password reset template
	 */
	@PreAuthorize(MethodSecurityExpressions.ANONYMOUS)
	@GetMapping("user/password/reset")
	public String resetPassword(@RequestParam("token") String token, ModelMap model) {

		// A record should exist in the database and be not expired.
		User user = this.getTokenUser(token);

		if (user == null || !userService.canResetPassword(user)) {
			model.addAttribute("errorMessage", Messages.INVALID_PASSWORD_RESET);
		} else {
			model.addAttribute("user", user);
		}
		model.addAttribute("formTitle", Messages.TITLE_RESET_PASSWORD);
		model.addAttribute("formRoute", "/user/password/reset");

		model.addAttribute("token", token);
		return "user/password/form";
	}

	/**
	 * Persist reset password.
	 *
	 * @param newPassword
	 *            The user's new password
	 * @param confirmPassword
	 *            Confirmation of the user's new password
	 * @param token
	 *            The user's password reset token.
	 * @param redirectAttributes
	 *            Used to hold attributes for a redirect.
	 * @param request
	 *            The {@link HttpServletRequest}
	 * @param model
	 *            Object holding view data
	 * @return Redirects to /login
	 */
	@PreAuthorize(MethodSecurityExpressions.ANONYMOUS)
	@PostMapping("user/password/reset")
	public String resetPassword(@ModelAttribute("newPassword") String newPassword,
			@ModelAttribute("confirmPassword") String confirmPassword, @ModelAttribute("token") String token,
			RedirectAttributes redirectAttributes, HttpServletRequest request, ModelMap model) {

		User user = this.getTokenUser(token);

		// The user cannot change their password if they don't have a valid token or are locked/disabled in some way.
		// They will have already received a message about this on the reset password page, but if they continue to
		// try they will be redirected to login.
		if (user == null || !userService.canResetPassword(user)) {
			model.clear();
			return "redirect:/login?error=true";
		}

		model.addAttribute("formTitle", Messages.TITLE_RESET_PASSWORD);
		model.addAttribute("formRoute", "/user/password/reset");
		model.addAttribute("user", user);

		try {
			ImmutablePair<User, List<String>> result = userService.resetPassword(user, newPassword, confirmPassword,
					token,
					request.getParameterMap());

			if (result.right.isEmpty()) {
				emailNotificationService.sendPasswordResetEmailConfirmation(token, request);
				redirectAttributes.addFlashAttribute("passwordReset", true);
				model.clear();
				return "redirect:/login";
			} else {
				log.info("Error saving; redirectoring to form");
				model.addAttribute("errorMessages", result.right);
				model.addAttribute("passwordValidationError", true);
				return "user/password/form";
			}
		} catch (UserManagementException ex) {
			log.info("Exception while resetting password", ex);
			model.addAttribute("errorMessage", ex.getMessage());
			return "user/password/form";
		} catch (RuntimeException ex) {
			log.error("Unexpected error while resetting password", ex);
			model.addAttribute("errorMessage", Messages.DEFAULT_ERROR_MESSAGE);
			return "user/password/form";
		}
	}

	/**
	 * Given a token, find the associated user and check expiration.
	 *
	 * @param token
	 *            single-use reset token value
	 * @return the user if the token exists and is active, null otherwise
	 */
	private User getTokenUser(String token) {
		PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token);
		if (passwordResetToken != null && !passwordResetToken.isExpired()) {
			return passwordResetToken.getUser();
		}
		return null;
	}

	/**
	 * Handles administrator requests to create a token for a user.
	 *
	 * @param model
	 *            object holding view data
	 * @param userId
	 *            ID of the user to create a token for
	 * @param redirectAttributes
	 *            redirect attributes
	 * @return redirects to the user form
	 */
	@PreAuthorize(MethodSecurityExpressions.ADMIN_OR_SUPER)
	@PostMapping("admin/password/token/refresh")
	public String passwordTokenRefresh(final ModelMap model, @RequestParam(name = "userId") Long userId,
			RedirectAttributes redirectAttributes) {
		final User user = userService.find(userId);
		Assert.notNull(user, "Could not find a user");
		passwordResetTokenService.generatePasswordResetToken(user);
		return "redirect:/admin/user/" + userId;
	}

	/**
	 * Handles administrator requests to create a temporary password for a user.
	 *
	 * @param model
	 *            object holding view data
	 * @param userId
	 *            ID of the user to create a temporary password for
	 * @param redirectAttributes
	 *            redirect attributes
	 * @return redirects to the user form
	 * @throws UserManagementException
	 *             if there is an error saving the user
	 */
	@PreAuthorize(MethodSecurityExpressions.ADMIN_OR_SUPER)
	@PostMapping("admin/password/generate")
	public String generatePassword(final ModelMap model, @RequestParam(name = "userId") Long userId,
			RedirectAttributes redirectAttributes) throws UserManagementException {

		final User user = userService.find(userId);
		Assert.notNull(user, "Could not find a user");
		Assert.isTrue(generator.isEnabled(), "Cannot generate temporary passwords");

		String newPassword = generator.generatePassword();

		userService.setEncodedPassword(user, newPassword);
		// set to the epoch date to ensure credentials are expired
		user.setCredentialsExpirationDate(new Date(0L));
		userService.save(user);

		redirectAttributes.addFlashAttribute("generatedPassword", newPassword);

		return "redirect:/admin/user/" + userId;
	}
}