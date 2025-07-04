package org.octri.authentication.server.security.entity;

import org.springframework.util.Assert;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Join model representing a {@link User} that has been assigned a particular {@link UserRole}.
 *
 * @author yateam
 *
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "user_role", "user" }) })
public class UserUserRole extends AbstractEntity {

	/**
	 * The user role assigned.
	 */
	@NotNull
	@ManyToOne
	private UserRole userRole;

	/**
	 * The user granted the role.
	 */
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	/**
	 * Default constructor.
	 */
	public UserUserRole() {
	}

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            the user granted the role
	 * @param userRole
	 *            the role being granted
	 */
	public UserUserRole(User user, UserRole userRole) {
		Assert.notNull(user, "user may not be null");
		Assert.notNull(userRole, "userRole may not be null");

		this.user = user;
		this.userRole = userRole;
	}

	/**
	 * Gets the role granted to the user.
	 * 
	 * @return the role granted
	 */
	public UserRole getUserRole() {
		return this.userRole;
	}

	/**
	 * Sets the role granted to the user.
	 * 
	 * @param userRole
	 *            the role granted
	 */
	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	/**
	 * Gets the user granted the role.
	 * 
	 * @return the user
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Sets the user being granted the role.
	 * 
	 * @param user
	 *            the user
	 */
	public void setUser(User user) {
		this.user = user;
	}

}
