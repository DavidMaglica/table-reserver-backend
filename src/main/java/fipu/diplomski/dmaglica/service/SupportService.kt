package fipu.diplomski.dmaglica.service

import fipu.diplomski.dmaglica.configuration.MailSenderConfiguration
import fipu.diplomski.dmaglica.model.response.BasicResponse
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class SupportService(
    private val mailSender: JavaMailSender,
    private val mailSenderConfiguration: MailSenderConfiguration
) {

    fun sendEmail(userEmail: String, subject: String, body: String): BasicResponse {
        val message = SimpleMailMessage()
        message.setTo(mailSenderConfiguration.getUsername())
        message.subject = "Support Ticket from $userEmail - $subject"
        message.text = body

        return try {
            mailSender.send(message)
            BasicResponse(true, "Email sent successfully")
        } catch (e: Exception) {
            return BasicResponse(false, "There was an error while sending the email")
        }
    }
}