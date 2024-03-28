package routing

import akka.actor.ActorSystem
import akka.util.Timeout
import amqp._

import scala.util.{Failure, Success}
import java.sql.Timestamp
import scala.concurrent._
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


class RabbitMQ_Consumer(implicit val system:ActorSystem) extends JsonSupport{

  implicit val executionContext: ExecutionContext = system.dispatcher
  val amqpActor = system.actorSelection("user/amqpActor")
  implicit val timeout = Timeout(3 second)

  def handle(message:Message)={
    message.routingKey match {
      case ""=>{
        // TODO: Тут добавляется новая обработка!!!!!!!
      }
    }
  }
}
