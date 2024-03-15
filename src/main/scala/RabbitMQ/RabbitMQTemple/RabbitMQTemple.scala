package RabbitMQ.RabbitMQTemple

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

abstract class RequestReplyMessage(val requestId: String)

abstract class CommandMessage

abstract class NotificationMessage

abstract class DataMessage[T](val data: T)

case class CreateUserCommand(
                              _id: String,
                              name: Option[String],
                              age: Option[Int],
                              email: Option[String],
                              phoneNumber: Option[String],
                              userType:String)


import play.api.libs.json._

object CreateUserCommand {
  implicit val format = Json.format[CreateUserCommand]
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def fromFutureString(futureString: Future[String]): CreateUserCommand = {
    val jsonString = Await.result(futureString, 10.seconds) // Максимальное время ожидания 10 секунд
    Json.fromJson[CreateUserCommand](Json.parse(jsonString)).getOrElse(
      throw new RuntimeException("Failed to parse JSON into CreateUserCommand")
    )
  }
}


