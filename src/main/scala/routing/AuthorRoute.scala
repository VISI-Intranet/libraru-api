package routing

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import domain._
import repositories.{AuthorRepository, BookRepository,getFields}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AuthorRoute(implicit val authorRepo: AuthorRepository,val bookRepo:BookRepository, val ex: ExecutionContext)
  extends JsonSupport {

  private val fields: Set[String] = getFields(classOf[Author])

  val route: Route = pathPrefix("authors") {
    pathEndOrSingleSlash {
      (get & parameters("field", "parameter")) { (field, parameter) =>
        validate(fields.contains(field),
          s"Вы ввели неправильное имя поля таблицы! Допустимые поля: ${fields.mkString(", ")}") {
          val convertedParameter = if (parameter.matches("-?\\d+")) parameter.toInt else parameter
          onComplete(authorRepo.customFilter(field, convertedParameter)) {
            case Success(queryResponse) =>
              complete(StatusCodes.OK, queryResponse)
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, s"Не удалось сделать запрос! ${ex.getMessage}")
          }
        }
      } ~
      get {
        onComplete(authorRepo.getAllAuthors()) {
          case Success(result) => complete(StatusCodes.OK, result)
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Ошибка в коде: ${ex.getMessage}")
        }
      } ~
      post {
        entity(as[Author]) { newAuthor =>
          // Все книги существуют, продолжаем создание автора
          onComplete(authorRepo.addAuthor(newAuthor)) {
            case Success(newAuthorId) =>
              complete(StatusCodes.Created, s"ID нового автора: $newAuthorId")
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, s"Не удалось создать автора: ${ex.getMessage}")
          }
        }
      }
    }~
    path(Segment) { authorId =>
      get {
        onComplete(authorRepo.getAuthorById(authorId)) {
          case Success(Some(author)) => complete(StatusCodes.OK, author)
          case Success(None) => complete(StatusCodes.NotFound, s"Автора под ID $authorId не существует!")
          case Failure(ex) =>
            complete(StatusCodes.InternalServerError, s"Ошибка в коде: ${ex.getMessage}")
        }
      } ~
      put {
        entity(as[AuthorUpdate]) { updatedAuthor => {
            onComplete(authorRepo.updateAuthor(authorId, updatedAuthor)) {
              case Success(updatedAuthorId) =>
                complete(StatusCodes.OK, updatedAuthorId)
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, s"Ошибка в коде: ${ex.getMessage}")
            }
          }
        }
      } ~
      delete {
        onComplete(authorRepo.deleteAuthor(authorId)) {
          case Success(true) => complete(StatusCodes.OK, s"Автор под айди $authorId удален!")
          case Success(false) => complete(StatusCodes.OK, s"Автор под айди $authorId не существует!")
          case Failure(ex) =>
            complete(StatusCodes.InternalServerError, s"Ошибка в коде: ${ex.getMessage}")
        }
      }
    }
  }
}
