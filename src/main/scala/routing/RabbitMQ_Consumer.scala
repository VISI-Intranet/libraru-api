package routing

import akka.actor.ActorSystem
import akka.util.Timeout
import amqp._
import domain.{TextBook, User}
import org.mongodb.scala.MongoDatabase
import repositories.{BookRepository, UserRepository}

import scala.util.{Failure, Success}
import java.sql.Timestamp
import scala.concurrent._
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


class RabbitMQ_Consumer(implicit val system:ActorSystem,val db:MongoDatabase) extends JsonSupport{

  implicit val executionContext: ExecutionContext = system.dispatcher
  val amqpActor = system.actorSelection("user/amqpActor")
  implicit val timeout = Timeout(3 second)
  val bookRepo = new BookRepository()
  val userRepo = new UserRepository()

  def handle(message:Message)={
    message.routingKey match {
      // Ерасылдың байланысы
      case "univer.library_api.postCreateUser" =>{
        val json = message.body.parseJson.asJsObject.fields

        val _id: Option[String] = json("_id").convertTo[Option[String]]
        val name: String = json ("name").convertTo[String]
        val ageString: String = json("age").convertTo[String]
        val email: String = json("email").convertTo[String]
        val phoneNumber: String = json ("phoneNumber").convertTo[String]
        val userType: String = json ("userType").convertTo[String]
        val password: String = "hell"
        val age: Int = ageString.toInt

        val user = User(_id ,name ,age , email , password, phoneNumber, booksBorrowed = List.empty )
        userRepo.addUser(user)
      }



      case ""=>{
        // TODO: Тут добавляется новая обработка!!!!!!!
      }

      // Жандарбектің байланысы. Студентпен
      case "univer.library_api.bookRequest" =>{
        val json = message.body.parseJson.asJsObject.fields
        val email = json("studentEmail").convertTo[String]
        val bookId = json("bookId").convertTo[String]
        println(email)
        println(bookId)
        val bookFuture = bookRepo.getBookById(bookId)
        bookFuture onComplete{
          case Success(Some(value)) =>{
            println(value)
            val book = value.asInstanceOf[TextBook]

            sendBook(email = email,
              textMessage =
                s"""
                  |Ваша запрашеваемая книга ${book.name}.
                  |""".stripMargin)
          }
          case Success(None) =>{
            println("Нет книги!")
            ifNoExistBook(email,"Вашей запрашеваемой книги нет в нашей библиотеке!")
          }
          case Failure(exception) => {
            println("Ошибка при отправке запрашеваемой книги по почте!")
          }
        }
      }
    }
  }
}
