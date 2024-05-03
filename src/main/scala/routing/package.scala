import java.io.File
import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, PasswordAuthentication, Session, Transport}

package object routing {


  def ifNoExistBook(email:String,textMessage:String): Unit ={

    val username = "orakbayev03@mail.ru"
    val password = "WsQQZW3bmcJ3LihJCDXk"

    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.mail.ru")
    props.put("mail.smtp.port", "587")

    val session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        override def getPasswordAuthentication(): PasswordAuthentication = {
          new PasswordAuthentication(username, password)
        }
      })
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(username))
      message.setRecipients(Message.RecipientType.TO, email)
      message.setSubject("Отправка c электронной библиотеки!")

      // Создаем многочастное сообщение
      val multipart = new MimeMultipart()

      // Добавляем текстовую часть сообщения
      val textPart = new MimeBodyPart()
      textPart.setText(textMessage)
      multipart.addBodyPart(textPart)

      message.setContent(multipart)
      // Отправляем письмо
      Transport.send(message)

      println("Email sent successfully!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def sendBook(email:String,textMessage:String): Unit ={
    val username = "orakbayev03@mail.ru"
    val password = "WsQQZW3bmcJ3LihJCDXk"

    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.mail.ru")
    props.put("mail.smtp.port", "587")

    val session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        override def getPasswordAuthentication(): PasswordAuthentication = {
          new PasswordAuthentication(username, password)
        }
      })
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(username))
      message.setRecipients(Message.RecipientType.TO, email)
      message.setSubject("Отправка электронной книги с библиотеки!")

      // Создаем многочастное сообщение
      val multipart = new MimeMultipart()

      // Добавляем текстовую часть сообщения
      val textPart = new MimeBodyPart()
      textPart.setText(textMessage)
      multipart.addBodyPart(textPart)

      // Добавляем изображение
      val imagePart = new MimeBodyPart()
      val imageFile = new File("C:/Users/Жандарбек/OneDrive/Рабочий стол/Visi_Organination/libraru-api/src/main/scala/routing/Book.pdf") // Замените на путь к вашему изображению
      imagePart.attachFile(imageFile)
      multipart.addBodyPart(imagePart)

      // Устанавливаем многочастное сообщение как тело письма
      message.setContent(multipart)

      // Отправляем письмо
      Transport.send(message)

      println("Email sent successfully!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}
