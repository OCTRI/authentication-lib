package org.octri.authentication.server.security.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.octri.authentication.config.EmailConfiguration;
import org.octri.authentication.server.security.AuthenticationUrlHelper;
import org.octri.authentication.server.security.entity.PasswordResetToken;
import org.octri.authentication.server.security.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This service is responsible for constructing emails to send to table-based users for account management
 */
@Service
public class EmailNotificationService {

    private static final Log log = LogFactory.getLog(EmailNotificationService.class);

    @Value("${app.displayName}")
    private String displayName;

    @Value("${octri.authentication.email.dry-run}")
    private Boolean dryRun;

    @Autowired
    private AuthenticationUrlHelper urlHelper;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailConfiguration emailConfig;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    /**
     * Send email confirmation to user. If the user is new, a welcome email is sent. Otherwise a password
     * reset is sent.
     *
     * @param token
     *            the password reset token
     * @param request
     *            the request so the application url can be constructed
     * @param isNewUser
     *            whether the user is new and should receive a welcome email instead of a password reset
     * @param dryRun
     *            Will only print email contents to console if true
     */
    public void sendPasswordResetTokenEmail(final PasswordResetToken token, final HttpServletRequest request,
            final boolean isNewUser) {

        User user = token.getUser();
        final String resetPath = urlHelper.getPasswordResetUrl(token.getToken());

        SimpleMailMessage email = new SimpleMailMessage();
        String body;
        if (isNewUser) {
            email.setSubject("Welcome to " + displayName);
            body = "Hello " + user.getFirstName() + ",\n\nAn account has been created for you with username "
                    + user.getUsername() + ". To set your password, please follow this link: " + resetPath;

        } else {
            email.setSubject("Password reset request for " + displayName);
            body = "Hello " + user.getFirstName() + ",\n\nTo reset your password please follow this link: "
                    + resetPath + "\n\nIf you did not initiate this request, please contact your system administrator.";
        }
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(emailConfig.getFrom());
        if (isEmailDryRun(dryRun, user.getEmail())) {
            logDryRunEmail(email);
        } else {
            mailSender.send(email);
            log.info("Password reset confirmation email sent to " + user.getEmail());
        }
    }

    /**
     * Send email confirmation to user.
     *
     * @param user
     * @param request
     * @param dryRun
     *            Will only print email contents to console if true
     */
    public void sendPasswordResetEmailConfirmation(final String token, final HttpServletRequest request) {
        Assert.notNull(token, "Must provide a token");
        Assert.notNull(request, "Must provide an HttpServletRequest");

        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token);
        Assert.notNull(passwordResetToken, "Could not find a user for the provided token");

        final String userEmail = passwordResetToken.getUser().getEmail();

        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject("Your " + displayName + " password was reset");
        final String body = "Your password has been reset. You may now log into the application.\n\nUsername: "
                + passwordResetToken.getUser().getUsername()
                + "\nLink: " + urlHelper.getLoginUrl();
        email.setText(body);
        email.setTo(userEmail);
        email.setFrom(emailConfig.getFrom());

        if (isEmailDryRun(dryRun, userEmail)) {
            logDryRunEmail(email);
        } else {
            mailSender.send(email);
            log.info("Password reset confirmation email sent to " + userEmail);
        }
    }

    private boolean isEmailDryRun(final boolean dryRun, final String toEmail) {
        return dryRun || StringUtils.isBlank(toEmail);
    }

    private void logDryRunEmail(SimpleMailMessage message) {
        final String format = "DRY RUN, would have sent email to %s from %s with subject \"%s\" and contents \"%s\"";
        log.info(String.format(format, String.join(", ", message.getTo()), message.getFrom(), message.getSubject(),
                message.getText()));
    }

    /**
     * 
     * Send a notification to the original/current email address letting the user know their email address has been
     * changed.
     *
     * @deprecated This library allows email changes without notification. Applications needing a different behavior
     *             should override forms and logic and implement notifications.
     * @param user
     * @param currentEmail
     */
    @Deprecated(forRemoval = true, since = "1.3.0")
    public void sendNotificationEmail(final User user, final String currentEmail) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject("Your " + displayName + " email was changed");
        final String body = "Hello " + user.getFirstName()
                + ",\n\nWe are writing to let you know that your email address was changed on the " + displayName
                + " site. If this was you, no action is needed.\n\nIf this was not you please contact your system administrator.";
        email.setText(body);
        email.setTo(currentEmail);
        email.setFrom(emailConfig.getFrom());
        mailSender.send(email);
        log.info("Email changed for user id " + user.getId() + ". Email confirmation sent.");
    }

}
