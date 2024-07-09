-- AUTHLIB-134: Increases the size of the username field to accommodate using email addresses
-- as usernames, and increases the size of the email field to fit all valid addresses.
ALTER TABLE `user`
	MODIFY `username` varchar(320) NOT NULL,
	MODIFY `email` varchar(320);