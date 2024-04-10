package routing

import akka.actor.ActorSystem
import akka.util.Timeout
import amqp._
import domain.User
import org.mongodb.scala.{MongoClient, MongoDatabase}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import repositories.UserRepository


class RabbitMQ_Consumer(implicit val system: ActorSystem) extends JsonSupport {

  implicit val executionContext: ExecutionContext = system.dispatcher
  val amqpActor = system.actorSelection("user/amqpActor")
  implicit val timeout = Timeout(3 second)

  def handle(message: Message) = {
    message.routingKey match {
      case "univer.library_api.postCreateUser" => {

        val mongodbClient = MongoClient()
        implicit val db: MongoDatabase = mongodbClient.getDatabase("library")

        val str = message.body

        println("\n\n\n"+str + "\n\n\n")




        val json: JsValue = Json.parse(message.body)




        val _id: Option[String] = (json \ "_id").asOpt[String]
        val name: String = (json \ "name").as[String]
        val ageString: String = (json \ "age").as[String]
        val email: String = (json \ "email").as[String]
        val phoneNumber: String = (json \ "phoneNumber").as[String]
        val userType: String = (json \ "userType").as[String]
        val password: String = "hell"
        val age: Int = ageString.toInt



        implicit val userRepository: UserRepository = new UserRepository()

        val user = User(_id ,name ,age , email , password, phoneNumber, booksBorrowed = List.empty )

        userRepository.addUser(user)


      }
    }
  }
}
