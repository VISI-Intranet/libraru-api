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
    private val mongodbClient = MongoClient("mongodb://mongodb:27017")
//  private val mongodbClient = MongoClient("mongodb://localhost:27017")
  implicit val db: MongoDatabase = mongodbClient.getDatabase("library")

  // Создание актора для брокера сообщений
  val amqpActor = system.actorOf(Props(new AmqpActor("UniverSystem", serviceName)), "amqpActor")

  // Обявить актора слушателя
  amqpActor ! RabbitMQ.DeclareListener(
    queue = "Library",
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
  private val bindingFuture = Http().newServerAt("0.0.0.0", 8090).bind(allRoutes)
  println(s"Server online at http://0.0.0.0:8090 - docker create 2/")

//  private val bindingFuture = Http().newServerAt("localhost", 8080).bind(allRoutes)
//  println(s"Server online at http://localhost:8080 - docker create 2/")

  // Остановка сервера при завершении приложения
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
