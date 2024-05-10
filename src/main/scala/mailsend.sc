import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Message, PasswordAuthentication, Session, Transport}

val username = "orakbayev03@mail.ru"
val password = "WsQQZW3bmcJ3LihJCDXk"

val props = new Properties()
props.put("mail.smtp.host", "smtp.mail.com")
props.put("mail.smtp.port", "587")
props.put("mail.smtp.auth", "true")
props.put("mail.smtp.ssl.protocols", "TLSv1.2")
props.put("mail.smtp.starttls.enable", "true")

val session = Session.getInstance(props,
  new javax.mail.Authenticator() {
    override def getPasswordAuthentication(): PasswordAuthentication = {
      new PasswordAuthentication(username, password)
    }
  })

try {
  val message = new MimeMessage(session)
  message.setFrom(new InternetAddress(username))

  // Указываем получателя/получателей
  message.setRecipients(Message.RecipientType.TO, "zhandar1503@gmail.com")

  message.setSubject("Тестовое письмо")
  message.setText("Это тестовое письмо, отправленное из Scala.")

  // Отправляем письмо
  Transport.send(message)

  println("Письмо успешно отправлено!")
} catch {
  case e: Exception =>
    println(s"Ошибка при отправке письма: ${e.getMessage}")
}
