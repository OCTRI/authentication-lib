package org.octri.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Configuration properties for SAML authentication.
 */
@ConfigurationProperties(prefix = "octri.authentication.saml")
public class SamlProperties {

	/**
	 * Whether to enable SAML authentication. Defaults to false.
	 */
	private Boolean enabled = false;

	/**
	 * Location of the private key for the X509 certificate to use to sign requests. Most likely a file URI or
	 * classpath location. Key must be in PEM-encoded PKCS#8 format, beginning with "-----BEGIN PRIVATE KEY-----".
	 */
	private Resource signingKeyLocation;

	/**
	 * Location of the X509 certificate to use to sign requests. Most likely a file URI or classpath location. Must be
	 * PEM-encoded X509 format, beginning with "-----BEGIN CERTIFICATE-----".
	 */
	private Resource signingCertLocation;

	/**
	 * Location of the private key for the X509 certificate to use to decrypt requests. Most likely a file URI or
	 * classpath location. Key must be in PEM-encoded PKCS#8 format, beginning with "-----BEGIN PRIVATE KEY-----".
	 */
	private Resource decryptionKeyLocation;

	/**
	 * Location of the X509 certificate to use to decrypt requests. Most likely a file URI or classpath location. Must
	 * be
	 * PEM-encoded X509 format, beginning with "-----BEGIN CERTIFICATE-----".
	 */
	private Resource decryptionCertLocation;

	/**
	 * URI of the IdP's metadata XML.
	 */
	private String idpMetadataUri;

	/**
	 * Users must be a member of this group to access the application.
	 */
	private String requiredGroup;

	/**
	 * ID of the SAML assertion attribute that stores the principal's userid / username. Defaults to
	 * "urn:oid:0.9.2342.19200300.100.1.1".
	 */
	private String useridAttribute = "urn:oid:0.9.2342.19200300.100.1.1";

	/**
	 * ID of the SAML assertion attribute that stores the principal's email address. Defaults to
	 * "urn:oid:0.9.2342.19200300.100.1.3".
	 */
	private String emailAttribute = "urn:oid:0.9.2342.19200300.100.1.3";

	/**
	 * ID of the SAML assertion attribute that stores the principal's first name. Defaults to "urn:oid:2.5.4.42".
	 */
	private String firstNameAttribute = "urn:oid:2.5.4.42";

	/**
	 * ID of the SAML assertion attribute that stores the principal's last name. Defaults to "urn:oid:2.5.4.4";
	 */
	private String lastNameAttribute = "urn:oid:2.5.4.4";

	/**
	 * ID of the SAML assertion attribute that stores the principal's group membership information. Defaults to "role".
	 */
	private String groupAttribute = "role";

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Resource getSigningKeyLocation() {
		return signingKeyLocation;
	}

	public void setSigningKeyLocation(Resource signingKeyLocation) {
		this.signingKeyLocation = signingKeyLocation;
	}

	public Resource getSigningCertLocation() {
		return signingCertLocation;
	}

	public void setSigningCertLocation(Resource signingCertLocation) {
		this.signingCertLocation = signingCertLocation;
	}

	public Resource getDecryptionKeyLocation() {
		return decryptionKeyLocation;
	}

	public void setDecryptionKeyLocation(Resource decryptionKeyLocation) {
		this.decryptionKeyLocation = decryptionKeyLocation;
	}

	public Resource getDecryptionCertLocation() {
		return decryptionCertLocation;
	}

	public void setDecryptionCertLocation(Resource decryptionCertLocation) {
		this.decryptionCertLocation = decryptionCertLocation;
	}

	public String getIdpMetadataUri() {
		return idpMetadataUri;
	}

	public void setIdpMetadataUri(String idpMetadataUri) {
		this.idpMetadataUri = idpMetadataUri;
	}

	public String getRequiredGroup() {
		return requiredGroup;
	}

	public void setRequiredGroup(String requiredGroup) {
		this.requiredGroup = requiredGroup;
	}

	public String getUseridAttribute() {
		return useridAttribute;
	}

	public void setUseridAttribute(String useridAttribute) {
		this.useridAttribute = useridAttribute;
	}

	public String getEmailAttribute() {
		return emailAttribute;
	}

	public void setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
	}

	public String getFirstNameAttribute() {
		return firstNameAttribute;
	}

	public void setFirstNameAttribute(String firstNameAttribute) {
		this.firstNameAttribute = firstNameAttribute;
	}

	public String getLastNameAttribute() {
		return lastNameAttribute;
	}

	public void setLastNameAttribute(String lastNameAttribute) {
		this.lastNameAttribute = lastNameAttribute;
	}

	public String getGroupAttribute() {
		return groupAttribute;
	}

	public void setGroupAttribute(String groupAttribute) {
		this.groupAttribute = groupAttribute;
	}

	@Override
	public String toString() {
		return "SamlProperties [enabled=" + enabled + ", signingKeyLocation=" + signingKeyLocation
				+ ", signingCertLocation=" + signingCertLocation + ", decryptionKeyLocation=" + decryptionKeyLocation
				+ ", decryptionCertLocation=" + decryptionCertLocation + ", idpMetadataUri=" + idpMetadataUri
				+ ", requiredGroup=" + requiredGroup + ", useridAttribute=" + useridAttribute + ", emailAttribute="
				+ emailAttribute + ", firstNameAttribute=" + firstNameAttribute + ", lastNameAttribute="
				+ lastNameAttribute + ", groupAttribute=" + groupAttribute + "]";
	}

}
