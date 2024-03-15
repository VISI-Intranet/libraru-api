package routing

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import com.rabbitmq.client.ConnectionFactory
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import domain.{PrepareUser, User}
import org.json4s.{DefaultFormats, jackson}
import repositories.UserRepository

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

class GoodUserRoutes()(implicit system: ActorSystem, mat: akka.stream.Materializer , val userRepo: UserRepository)
  extends Directives with Json4sSupport {

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val route: Route =
     path("prepareUser") {
      post {
        entity(as[PrepareUser]) { user =>
          val futureResult: Future[String] = userRepo.addOrUpdateUserFromPrepareData(user)

          onComplete(futureResult) {
            case Success(result) =>
              complete(result)
            case Failure(ex) =>
              // Обработка ошибки. В данном случае, если ответ не получен, возвращаем "Ошибка"
              complete("Ошибка")
          }
        }
      }

    }
}

object GoodUserRoutes {
  def apply()(implicit system: ActorSystem, mat: akka.stream.Materializer,  userRepo: UserRepository) : GoodUserRoutes =
    new GoodUserRoutes()
}
