package fipu.diplomski.dmaglica.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "spring.mail")
class MailSenderProperties {
    lateinit var username: String
    lateinit var password: String
}

@Configuration
class MailSenderConfiguration(
    private val mailSenderProperties: MailSenderProperties
) {

    @Bean
    fun getJavaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587

        mailSender.username = mailSenderProperties.username
        mailSender.password = mailSenderProperties.password

        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.auth.mechanisms"] = "LOGIN PLAIN"

        return mailSender
    }

    fun getUsername(): String = mailSenderProperties.username
}
