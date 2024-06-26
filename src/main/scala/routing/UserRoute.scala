package routing

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import domain._
import repositories.{BookRepository, UserRepository, getFields}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class UserRoute(implicit val userRepo: UserRepository, val bookRepo:BookRepository, val ex:ExecutionContext)
  extends JsonSupport {

  // field параметрінің дұрыстығын тексеру үшін
  private val fields: Set[String] = getFields(classOf[User],"_id")

  val route: Route = pathPrefix("users") {
    pathEndOrSingleSlash {
      (get & parameters("field", "parameter")) {
        (field, parameter) => {
          validate(fields.contains(field),
            s"Вы ввели неправильное имя поля таблицы! Допустимые поля: ${fields.mkString(", ")}")
          {
            val convertedParameter = if (parameter.matches("-?\\d+")) parameter.toInt else parameter
            onComplete(userRepo.customFilter(field, convertedParameter)) {
              case Success(queryResponse) => complete(StatusCodes.OK, queryResponse)
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, s"Не удалось сделать запрос! ${ex.getMessage}")
            }
          }
        }
      } ~
      get {
        onComplete(userRepo.getAllUsers()) {
          case Success(result) => complete(StatusCodes.OK, result)
          case Failure(ex) => complete(StatusCodes.NotFound, s"Ошибка в коде: ${ex.getMessage}")
        }
      } ~
        post {
          entity(as[User]) { newUser => {
            onComplete(userRepo.addUser(newUser)) {
              case Success(newUserId) =>
                complete(StatusCodes.Created, s"ID нового пользователя $newUserId")
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, s"Не удалось создать пользователя: ${ex.getMessage}")
            }
          }
          }
        }
    } ~
      path(Segment) { userId =>
        get {
          onComplete(userRepo.getUserById(userId)) {
            case Success(Some(user)) => complete(StatusCodes.OK, user)
            case Success(None) => complete(StatusCodes.NotFound, s"Пользователя под ID $userId не существует!")
            case Failure(ex) => complete(StatusCodes.NotFound, s"Ошибка в коде: ${ex.getMessage}")
          }
        } ~
          put {
            entity(as[UserUpdate]) { updatedUser => {
              onComplete(userRepo.updateUser(userId, updatedUser)) {
                case Success(updatedUserId) =>
                  complete(StatusCodes.OK, updatedUserId)
                case Failure(ex) => complete(StatusCodes.NotFound, s"Ошибка в коде: ${ex.getMessage}")
              }
            }
            }
          } ~
          delete {
            onComplete(userRepo.deleteUser(userId)) {
              case Success(true) => complete(StatusCodes.OK, s"Пользователь по айди $userId удален!")
              case Success(false) => complete(StatusCodes.OK, s"Пользователя по айди $userId не существует!!")
              case Failure(ex) => complete(StatusCodes.NotFound, s"Ошибка в коде: ${ex.getMessage}")
            }
          }
      }
  }
}
