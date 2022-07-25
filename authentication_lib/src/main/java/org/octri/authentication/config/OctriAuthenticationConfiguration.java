package org.octri.authentication.config;

import static org.octri.authentication.config.OctriAuthenticationProperties.DEFAULT_BASE_URL;

import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.octri.authentication.server.security.AuthenticationUrlHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration for the authentication library.
 */
@Configuration
@EnableConfigurationProperties({ AuthenticationRouteProperties.class, ContentSecurityPolicyProperties.class,
		OctriAuthenticationProperties.class })
public class OctriAuthenticationConfiguration {

	private static final Log log = LogFactory.getLog(OctriAuthenticationConfiguration.class);
	private static final String BASE_URL_ERROR = "Table-based authentication is enabled but octri.authentication.base-url is "
			+ DEFAULT_BASE_URL + ". Check the application's configuration.";

	private static final String[] AUTH_METHOD_PROPERTIES = new String[] {
			"octri.authentication.enable-ldap",
			"octri.authentication.enable-table-based",
			"octri.authentication.saml.enabled"
	};

	private String contextPath;
	private OctriAuthenticationProperties authenticationProperties;

	public OctriAuthenticationConfiguration(Environment env, OctriAuthenticationProperties authenticationProperties,
			@Value("${server.servlet.context-path:}") String contextPath) {
		this.authenticationProperties = authenticationProperties;
		this.contextPath = contextPath;

		validateAuthenticationMethods(env);
		validateBaseUrl();
	}

	@Bean
	public Boolean ldapEnabled() {
		return authenticationProperties.getEnableLdap();
	}

	@Bean
	public Boolean tableBasedEnabled() {
		return authenticationProperties.getEnableTableBased();
	}

	@Bean
	public AuthenticationUrlHelper authenticatonUrlHelper() {
		return new AuthenticationUrlHelper(authenticationProperties.getBaseUrl(), contextPath);
	}

	/**
	 * Throws an exception unless at least one authentication method has been enabled.
	 */
	private void validateAuthenticationMethods(Environment env) {
		String errorMessage = "The authentication_lib requires at least one authentication method to be enabled. "
				+ "Set at least one of: " + String.join(", ", AUTH_METHOD_PROPERTIES);

		log.info("Checking for enabled authentication methods ...");
		boolean anyMethodEnabled = Stream.of(AUTH_METHOD_PROPERTIES)
				.map(propName -> env.getProperty(propName))
				.anyMatch(Boolean::parseBoolean);

		if (!anyMethodEnabled) {
			log.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		} else {
			log.info("At least one authentication method is enabled.");
		}
	}

	/**
	 * Logs an error if table-based authentication is enabled and the base URL property has not been set.
	 */
	private void validateBaseUrl() {
		if (DEFAULT_BASE_URL.equals(authenticationProperties.getBaseUrl())
				&& authenticationProperties.getEnableTableBased()) {
			log.error(BASE_URL_ERROR);
		}
	}

}
