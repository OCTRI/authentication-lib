package org.octri.authentication.server.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.octri.authentication.MethodSecurityExpressions;
import org.octri.authentication.config.LdapContextProperties;
import org.octri.authentication.config.OctriAuthenticationProperties;
import org.octri.authentication.config.OctriAuthenticationProperties.UsernameStyle;
import org.octri.authentication.server.customizer.UserManagementCustomizer;
import org.octri.authentication.server.security.AuthenticationUrlHelper;
import org.octri.authentication.server.security.SecurityHelper;
import org.octri.authentication.server.security.entity.PasswordResetToken;
import org.octri.authentication.server.security.entity.User;
import org.octri.authentication.server.security.entity.UserRole;
import org.octri.authentication.server.security.exception.UserManagementException;
import org.octri.authentication.server.security.service.PasswordGeneratorService;
import org.octri.authentication.server.security.service.PasswordResetTokenService;
import org.octri.authentication.server.security.service.UserRoleService;
import org.octri.authentication.server.security.service.UserService;
import org.octri.authentication.server.view.OptionList;
import org.octri.authentication.utils.ValidationUtils;
import org.octri.authentication.validation.Emailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

/**
 * Controller for managing {@link User}.
 *
 * @author sams
 */
@Controller
@Scope("session")
public class UserController {

	private static final Log log = LogFactory.getLog(UserController.class);

	@Autowired
	private AuthenticationUrlHelper urlHelper;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private PasswordResetTokenService passwordResetTokenService;

	@Autowired
	private Validator validator;

	@Autowired
	private ValidationUtils<User> validationUtils;

	@Autowired
	private PasswordGeneratorService passwordGeneratorService;

	@Autowired
	private OctriAuthenticationProperties authenticationProperties;

	@Autowired
	private UserManagementCustomizer userManagementCustomizer;

	@Autowired(required = false)
	private LdapContextProperties ldapContextProperties;

	/**
	 * Returns view for displaying a list of all users.
	 *
	 * @param model
	 *            Object holding view data
	 * @return List view
	 */
	@PreAuthorize(MethodSecurityExpressions.ADMIN_OR_SUPER)
	@GetMapping("admin/user/list")
	public ModelAndView listUsers(ModelMap model) {
		model.addAttribute("users", users());
		model.addAttribute("userRoles", userRoles());
		model.addAttribute("listView", true);
		return new ModelAndView("admin/user/list", model);
	}

	/**
	 * User create/edit form.
	 *
	 * @param request
	 *            The {@link HttpServletRequest}
	 * @param model
	 *            Object holding view data
	 * @return User form template
	 */
	@PreAuthorize(MethodSecurityExpressions.ADMIN_OR_SUPER)
	@GetMapping("admin/user/form")
	public ModelAndView userForm(HttpServletRequest request, ModelMap model) {
		String id = request.getParameter("id");
		if (id == null) {
			model.addAttribute("user", new User());
			model.addAttribute("userRoles", userRoles());
			model.addAttribute("pageTitle", "New User");
			model.addAttribute("newUser", true);
			model.addAttribute("formView", true);
		} else {
			SecurityHelper securityHelper = new SecurityHelper(SecurityContextHolder.getContext());
			if (securityHelper.canEditUser(Long.valueOf(id))) {
				User user = userService.find(Long.valueOf(id));
				Assert.notNull(user, "Could not find a user for id " + id);
				model.addAttribute("user", user);
				model.addAttribute("userRoles", OptionList.multiFromSearch(userRoles(), user.getUserRoles()));
				model.addAttribute("pageTitle", "Edit User");
				model.addAttribute("newUser", false);
				model.addAttribute("formView", true);

				// If the user can reset the password, show admin additional options
				if (userService.canResetPassword(user)) {
					// Show either a valid reset URL or allow the admin to generate one
					Optional<PasswordResetToken> latestToken = passwordResetTokenService.findLatest(user.getId());
					if (latestToken.isPresent()
							&& !latestToken.get().isExpired()) {
						final String url = urlHelper.getPasswordResetUrl(latestToken.get().getToken());
						model.addAttribute("passwordResetUrl", url);
					} else {
						model.addAttribute("showNewTokenButton", true);
					}

					if (passwordGeneratorService.isEnabled()) {
						model.addAttribute("allowPasswordGeneration", true);
					}
				}
			} else {
				log.info(securityHelper.username() + " does not have access to edit user " + id);
				model.addAttribute("status", 403);
				model.addAttribute("error", "Access Denied");
				model.addAttribute("message", "You may not edit yourself.");
				model.addAttribute("timestamp", new Date());
				return new ModelAndView("error", model);
			}
		}
		return new ModelAndView("admin/user/form", model);
	}

	/**
	 * Persists a new or existing user.
	 *
	 * @param user
	 *            User being created
	 * @param bindingResult
	 *            Binding result for 'user' parameter
	 * @param model
	 *            Object holding view data
	 * @param request
	 *            The {@link HttpServletRequest} for the request
	 * @return new user template on error, or redirects to user list
	 */
	@PreAuthorize(MethodSecurityExpressions.ADMIN_OR_SUPER)
	@PostMapping("admin/user/form")
	public ModelAndView userForm(@ModelAttribute User user, BindingResult bindingResult, final ModelMap model,
			HttpServletRequest request) {
		Assert.notNull(user, "User must not be null");

		final boolean newUser = user.getId() == null;

		if (newUser) {
			model.addAttribute("pageTitle", "New User");
		} else {
			model.addAttribute("pageTitle", "Edit User");
		}

		model.addAttribute("newUser", newUser);
		model.addAttribute("user", user);
		model.addAttribute("userRoles", OptionList.multiFromSearch(userRoles(), user.getUserRoles()));
		model.addAttribute("formView", true);

		Boolean emailRequired = authenticationProperties.getEmailRequired();

		Set<ConstraintViolation<User>> validationResult = !emailRequired
				&& StringUtils.isBlank(user.getEmail())
						? validator.validate(user, Default.class)
						: validator.validate(user, Emailable.class);

		if (validationResult.size() > 0) {
			model.addAttribute("error", true);
			model.addAttribute("errors", validationUtils.getErrors(user, validationResult));
			return new ModelAndView("admin/user/form", model);
		}

		try {
			var optionalView = userManagementCustomizer.beforeSaveAction(user, model, request);
			if (optionalView.isPresent()) {
				return optionalView.get();
			}
			User savedUser = userService.save(user);

			if (newUser) {
				return userManagementCustomizer.postCreateAction(savedUser, model, request);
			}
			return userManagementCustomizer.postUpdateAction(savedUser, model, request);
		} catch (UserManagementException ex) {
			log.info("Checked exception thrown", ex);
			model.addAttribute("error", true);
			model.addAttribute("errorMessage", ex.getMessage());
			return new ModelAndView("admin/user/form", model);
		} catch (RuntimeException ex) {
			log.error("Unexpected runtime exception while adding new user", ex);
			model.addAttribute("error", true);
			model.addAttribute("errorMessage", "Unexpected exception while adding new user");
			return new ModelAndView("admin/user/form", model);
		}
	}

	/**
	 *
	 * @return Returns a list of all {@link UserRole}.
	 */
	private List<UserRole> userRoles() {
		return userRoleService.findAll();
	}

	/**
	 *
	 * @return Returns a list of all {@link User}.
	 */
	private List<User> users() {
		return userService.findAll();
	}

	/**
	 * @return Boolean indicating LDAP is enabled or disabled.
	 */
	@ModelAttribute("ldapEnabled")
	public Boolean getLdapEnabled() {
		return authenticationProperties.getEnableLdap();
	}

	/**
	 * @return Boolean indicating table-based authentication is enabled or disabled.
	 */
	@ModelAttribute("tableBasedEnabled")
	public Boolean getTableBasedEnabled() {
		return authenticationProperties.getEnableTableBased();
	}

	/**
	 * @return the email domain of LDAP accounts, if LDAP authentication is enabled.
	 */
	@ModelAttribute("ldapEmailDomain")
	public String getLdapEmailDomain() {
		return ldapContextProperties != null ? ldapContextProperties.getEmailDomain() : null;
	}

	/**
	 * @return the style of usernames supported (plain, email address, or mixed)
	 */
	@ModelAttribute("usernameStyle")
	public String getUsernameStyle() {
		UsernameStyle usernameStyle = authenticationProperties.getUsernameStyle();
		return usernameStyle != null ? usernameStyle.toString() : null;
	}

	/**
	 * Ensure that empty strings are saved as NULL values.
	 *
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

}
