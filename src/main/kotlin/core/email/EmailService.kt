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

    fun sendVerificationEmail(toEmail: String, toName: String, verificationToken: String, baseUrl: String) {
        val mailConfig = appConfig.config.mail
        val verificationLink = "$baseUrl/email-verify/$verificationToken"

        val email = EmailBuilder.startingBlank()
            .from(mailConfig.from_name, mailConfig.from_address)
            .to(toName, toEmail)
            .withSubject("Verify your email address - 3D Model Manager")
            .withHTMLText(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h1 style="color: #2c3e50;">Welcome to 3D Model Manager!</h1>
                        <p>Hello $toName,</p>
                        <p>Thank you for registering. Please click the button below to verify your email address:</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="$verificationLink"
                               style="background-color: #28a745; color: white; padding: 12px 30px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Verify Email Address
                            </a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #666;">$verificationLink</p>
                        <p>This link will expire in 24 hours.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                        <p style="color: #666; font-size: 12px;">
                            If you did not create an account, please ignore this email.
                        </p>
                    </div>
                </body>
                </html>
                """.trimIndent(),
            )
            .withPlainText(
                """
                Welcome to 3D Model Manager!

                Hello $toName,

                Thank you for registering. Please click the link below to verify your email address:

                $verificationLink

                This link will expire in 24 hours.

                If you did not create an account, please ignore this email.
                """.trimIndent(),
            )
            .buildEmail()

        try {
            mailer.sendMail(email)
            logger.info("Verification email sent to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send verification email to $toEmail", e)
            throw e
        }
    }
}
