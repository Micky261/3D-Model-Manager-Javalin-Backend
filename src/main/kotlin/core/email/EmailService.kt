package core.email

import com.google.inject.Inject
import com.google.inject.Singleton
import core.config.AppConfig
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.Logger

@Singleton
class EmailService @Inject constructor(
    private val appConfig: AppConfig,
    private val logger: Logger,
    private val translationService: EmailTranslationService,
) {
    private val mailer: Mailer by lazy {
        val mailConfig = appConfig.config.mail
        val strategy = when (mailConfig.encryption.lowercase()) {
            "tls", "starttls" -> TransportStrategy.SMTP_TLS
            "ssl" -> TransportStrategy.SMTPS
            else -> TransportStrategy.SMTP
        }

        MailerBuilder
            .withSMTPServer(mailConfig.host, mailConfig.port, mailConfig.username, mailConfig.password)
            .withTransportStrategy(strategy)
            .buildMailer()
    }

    fun sendVerificationEmail(
        toEmail: String,
        toName: String,
        verificationToken: String,
        baseUrl: String,
        locale: String,
    ) {
        val link = "$baseUrl/auth/email-verify/$verificationToken"
        val params = mapOf("name" to toName, "link" to link, "buttonColor" to "#28a745")
        val subject = translationService.resolve(translationService.get(locale, "verification", "subject"), params)
        val htmlBody = translationService.renderHtml("verification", locale, params)
        val textBody = translationService.renderText("verification", locale, params)

        sendEmail(toEmail, toName, subject, htmlBody, textBody)
        logger.info("Verification email sent to $toEmail")
    }

    fun sendEmailChangeVerification(
        toEmail: String,
        toName: String,
        verificationToken: String,
        baseUrl: String,
        locale: String,
    ) {
        val link = "$baseUrl/auth/email-verify/$verificationToken"
        val params = mapOf("name" to toName, "link" to link, "buttonColor" to "#28a745")
        val subject = translationService.resolve(translationService.get(locale, "emailChange", "subject"), params)
        val htmlBody = translationService.renderHtml("emailChange", locale, params)
        val textBody = translationService.renderText("emailChange", locale, params)

        sendEmail(toEmail, toName, subject, htmlBody, textBody)
        logger.info("Email change verification sent to $toEmail")
    }

    fun sendPasswordResetEmail(toEmail: String, resetToken: String, baseUrl: String, locale: String) {
        val link = "$baseUrl/auth/password-reset/$resetToken"
        val params = mapOf("link" to link, "buttonColor" to "#007bff")
        val subject = translationService.resolve(translationService.get(locale, "passwordReset", "subject"), params)
        val htmlBody = translationService.renderHtml("passwordReset", locale, params)
        val textBody = translationService.renderText("passwordReset", locale, params)

        sendEmail(toEmail, toEmail, subject, htmlBody, textBody)
        logger.info("Password reset email sent to $toEmail")
    }

    private fun sendEmail(toEmail: String, toName: String, subject: String, htmlBody: String, textBody: String) {
        val mailConfig = appConfig.config.mail

        val email = EmailBuilder.startingBlank()
            .from(mailConfig.from_name, mailConfig.from_address)
            .to(toName, toEmail)
            .withSubject(subject)
            .withHTMLText(htmlBody)
            .withPlainText(textBody)
            .buildEmail()

        try {
            mailer.sendMail(email)
        } catch (e: Exception) {
            logger.error("Failed to send email to $toEmail", e)
            throw e
        }
    }
}
