package org.octri.authentication.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for the authentication library.
 */
@ConfigurationProperties(prefix = "octri.authentication")
public class OctriAuthenticationProperties {

	/**
	 * Allowed username styles. Valid options are PLAIN (simple username), EMAIL (must be an email address), or MIXED
	 * (may be either a plain username or an email adress).
	 */
	public static enum UsernameStyle {
		PLAIN, EMAIL, MIXED
	}

	/**
	 * The style of roles allowed. Valid options are SINGLE (users may have only one role), MULTIPLE (a user may have
	 * any number of roles), or CUSTOM (application-specific behavior).
	 */
	public static enum RoleStyle {
		SINGLE, MULTIPLE, CUSTOM
	}

	/**
	 * The default value of "octri.authentication.base-url".
	 */
	public static final String DEFAULT_BASE_URL = "http://localhost:8080";

	/*
	 * The default value in minutes for "octri.authentication.password-token-validity"
	 */
	public static final Duration DEFAULT_PASSWORD_TOKEN_DURATION = Duration.ofMinutes(30);

	/**
	 * Whether LDAP authentication should be enabled.
	 */
	private Boolean enableLdap = false;

	/**
	 * Whether table-based authentication should be enabled.
	 */
	private Boolean enableTableBased = false;

	/**
	 * The base URL of the application without a context path. Used to construct links sent in emails. Defaults to
	 * the value of @link{OctriAuthenticationProperties.DEFAULT_BASE_URL}.
	 */
	private String baseUrl = DEFAULT_BASE_URL;

	/**
	 * Number of failed login attempts allowed before accounts are locked.
	 */
	private Integer maxLoginAttempts = 7;

	/**
	 * Length of time (in days) that credentials are valid. After this period has elapsed, users will be required
	 * to change their password.
	 */
	private Integer credentialsExpirationPeriod = 180;

	/**
	 * Style of usernames allowed. Valid options are PLAIN (simple username), EMAIL (must be an email address), or MIXED
	 * (may be either a plain username or an email adress). Defaults to PLAIN.
	 */
	private UsernameStyle usernameStyle = UsernameStyle.PLAIN;

	/**
	 * Length of time that password tokens will be valid for
	 */
	@DurationUnit(ChronoUnit.MINUTES)
	private Duration passwordTokenValidFor = DEFAULT_PASSWORD_TOKEN_DURATION;

	/**
	 * Whether the email field on the user form is required.
	 */
	private Boolean emailRequired = true;

	/**
	 * Whether email should be sent or logged to the server instead.
	 */
	private Boolean emailDryRun = false;

	/**
	 * The style of roles allowed. This determines how the role selector on the user form is rendered. Valid options are
	 * SINGLE (users may have only one role), MULTIPLE (a user may have any number of roles), or CUSTOM
	 * (application-specific behavior). Defaults to MULTIPLE.
	 */
	@NotNull
	private RoleStyle roleStyle = RoleStyle.MULTIPLE;

	/**
	 * Path to a JavaScript file to use when "octri.authentication.role-style" is "custom". This path should be relative
	 * to the application's context path. Only relevant when "octri.authentication.role-style" is "custom".
	 */
	private String customRoleScript;

	public Boolean getEnableLdap() {
		return enableLdap;
	}

	public void setEnableLdap(Boolean enableLdap) {
		this.enableLdap = enableLdap;
	}

	public Boolean getEnableTableBased() {
		return enableTableBased;
	}

	public void setEnableTableBased(Boolean enableTableBased) {
		this.enableTableBased = enableTableBased;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Integer getMaxLoginAttempts() {
		return maxLoginAttempts;
	}

	public void setMaxLoginAttempts(Integer maxLoginAttempts) {
		this.maxLoginAttempts = maxLoginAttempts;
	}

	public Integer getCredentialsExpirationPeriod() {
		return credentialsExpirationPeriod;
	}

	public void setCredentialsExpirationPeriod(Integer credentialsExpirationPeriod) {
		this.credentialsExpirationPeriod = credentialsExpirationPeriod;
	}

	public UsernameStyle getUsernameStyle() {
		return usernameStyle;
	}

	public void setUsernameStyle(UsernameStyle usernameStyle) {
		this.usernameStyle = usernameStyle;
	}

	public void setPasswordTokenValidFor(Duration validityTime) {
		this.passwordTokenValidFor = validityTime;
	}

	public Duration getPasswordTokenValidFor() {
		return passwordTokenValidFor;
	}

	public Boolean getEmailDryRun() {
		return emailDryRun;
	}

	public void setEmailDryRun(Boolean emailDryRun) {
		this.emailDryRun = emailDryRun;
	}

	public Boolean getEmailRequired() {
		return emailRequired;
	}

	public void setEmailRequired(Boolean emailRequired) {
		this.emailRequired = emailRequired;
	}

	public RoleStyle getRoleStyle() {
		return roleStyle;
	}

	public void setRoleStyle(RoleStyle roleStyle) {
		this.roleStyle = roleStyle;
	}

	public String getCustomRoleScript() {
		return customRoleScript;
	}

	public void setCustomRoleScript(String customRoleScript) {
		this.customRoleScript = customRoleScript;
	}

	@Override
	public String toString() {
		return "OctriAuthenticationProperties [enableLdap=" + enableLdap + ", enableTableBased=" + enableTableBased
				+ ", baseUrl=" + baseUrl + ", maxLoginAttempts=" + maxLoginAttempts + ", credentialsExpirationPeriod="
				+ credentialsExpirationPeriod + ", usernameStyle=" + usernameStyle + ", passwordTokenValidFor="
				+ passwordTokenValidFor + ", emailRequired=" + emailRequired + ", emailDryRun=" + emailDryRun
				+ ", roleStyle=" + roleStyle + ", customRoleScript=" + customRoleScript + "]";
	}

}
