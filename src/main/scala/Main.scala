import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import org.mongodb.scala.{MongoClient, MongoDatabase}

import scala.concurrent.ExecutionContextExecutor
import repositories._
import routing._
import amqp._
import com.typesafe.config.ConfigFactory
object Main extends App with JsonSupport {
  val config = ConfigFactory.load("service_app.conf")

  // Извлечение значения параметра serviceName
  val serviceName = config.getString("service.serviceName")


  // Создание акторной системы
  implicit val system: ActorSystem = ActorSystem(serviceName)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    private val mongodbClient = MongoClient("mongodb://root:root@mongodb:27017")
  //private val mongodbClient = MongoClient("mongodb://localhost:27017")
  implicit val db: MongoDatabase = mongodbClient.getDatabase("library")

  // Создание актора для брокера сообщений
  val amqpActor = system.actorOf(Props(new AmqpActor("X:routing.topic", serviceName)), "amqpActor")

  // Обявить актора слушателя
  amqpActor ! RabbitMQ.DeclareListener(
    queue = "library_api_queue",
    bind_routing_key = "univer.library_api.#",
    actorName = "consumer_actor_1",
    handle = new RabbitMQ_Consumer().handle)


  implicit val userRepository: UserRepository = new UserRepository()
  implicit val authorRepository: AuthorRepository = new AuthorRepository()
  implicit val bookRepository: BookRepository = new BookRepository()

  private val userRoute = new UserRoute()
  private val authorRoute = new AuthorRoute()
  private val bookRoute = new BookRoute()

  // Добавление путей
  private val allRoutes = userRoute.route ~
    authorRoute.route ~
    bookRoute.route

  // Старт сервера
  private val bindingFuture = Http().bindAndHandle(allRoutes, "0.0.0.0", 8080)
  println(s"Server online at http://0.0.0.0:8080/")

  // Остановка сервера при завершении приложения
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
