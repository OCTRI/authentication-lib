package org.octri.authentication.server.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.octri.authentication.server.security.entity.User;

public class SecurityHelperTest {

	private User user;

	@BeforeEach
	public void setUp() {
		user = new User();
	}

	@Test
	public void testHasOHSUEmail() {
		user.setEmail(null);
		assertFalse(SecurityHelper.hasOHSUEmail(user), "Should be false with NULL email");

		user.setEmail("");
		assertFalse(SecurityHelper.hasOHSUEmail(user), "Should be false with empty string");

		user.setEmail("  \n  \t  ");
		assertFalse(SecurityHelper.hasOHSUEmail(user), "Should be false with only whitespace");

		user.setEmail("foo@example.com");
		assertFalse(SecurityHelper.hasOHSUEmail(user),
				"Should be false unless using an email address with ohsu.edu domain");

		user.setEmail("foo@ohsu.edu");
		assertTrue(SecurityHelper.hasOHSUEmail(user),
				"Should be true when using an email address with ohsu.edu domain");
	}

	@Test
	public void testHasEmailDomain() {
		user.setEmail(null);
		assertFalse(SecurityHelper.hasEmailDomain(user, "ohsu.edu"), "Should be false with NULL email");

		user.setEmail("");
		assertFalse(SecurityHelper.hasEmailDomain(user, "ohsu.edu"), "Should be false with empty string");

		user.setEmail("  \n  \t  ");
		assertFalse(SecurityHelper.hasEmailDomain(user, "ohsu.edu"), "Should be false with only whitespace");

		user.setEmail("foo@example.com");
		assertFalse(SecurityHelper.hasEmailDomain(user, "ohsu.edu"),
				"Should be false unless the email domain matches");

		user.setEmail("foo@ohsu.edu");
		assertTrue(SecurityHelper.hasEmailDomain(user, "ohsu.edu"),
				"Should be true when the email domain matches");
	}
}