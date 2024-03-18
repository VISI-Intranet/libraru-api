package RabbitMQ.RabbitMQTemple

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

abstract class RequestReplyMessage(val requestId: String)

abstract class RecieveReplyMessage(val requestId: String)

abstract class CommandMessage

abstract class NotificationMessage

abstract class DataMessage[T](val data: T)

case class CreateUserCommand(
                              _id: String,
                              name: Option[String],
                              age: Option[Int],
                              email: Option[String],
                              phoneNumber: Option[String],
                              checkCreate: Boolean = false,
                              userType: String = "Teacher"
                            ) {
  // Дополнительный конструктор, принимающий строку для инициализации полей
  def this(str: String) {
    this(
      str.split(",")(0),
      Option(str.split(",")(1)).filter(_.nonEmpty),
      Option(str.split(",")(2)).map(_.toInt),
      Option(str.split(",")(3)).filter(_.nonEmpty),
      Option(str.split(",")(4)).filter(_.nonEmpty),
      str.split(",")(5).toBoolean,
      str.split(",")(6)
    )
  }
}


object CreateUserCommand {
  // Реализация для автоматической сериализации и десериализации с использованием Circe
  implicit val createUserCommandEncoder: Encoder[CreateUserCommand] = deriveEncoder[CreateUserCommand]
  implicit val createUserCommandDecoder: Decoder[CreateUserCommand] = deriveDecoder[CreateUserCommand]
}
