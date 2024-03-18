package RabbitMQ.RabbitMQOperation.Repo

import RabbitMQ.RabbitMQModel.RabbitMQModel
import RabbitMQ.RabbitMQOperation.Operations.Formatter.{StringToObjectConverter}
import RabbitMQ.RabbitMQOperation.Operations.Subscription
import RabbitMQ.RabbitMQTemple.CreateUserCommand
import akka.stream.Materializer
import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpLocalConnectionProvider}
import org.mongodb.scala.{MongoClient, MongoDatabase}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.InsertOneResult

import scala.concurrent.{ExecutionContext, Future}
import domain.{PrepareUser, User, UserUpdate}
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt32, BsonString, ObjectId}

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.postfixOps


object RabbitRepo {

  private val mongodbClient = MongoClient()
  implicit val db: MongoDatabase = mongodbClient.getDatabase("library")
  private val collection = db.getCollection("users")

  def addOrUpdateUserFromPrepareData(str: String , prepareUser: PrepareUser=PrepareUser("Password",List.empty))(implicit mat: Materializer, ec: ExecutionContext): Future[String] = {
    val futureCreateUserCommand = Future {
      StringToObjectConverter.stringToObject[CreateUserCommand](str)
    }

    futureCreateUserCommand.flatMap { createUserCommand =>
      val userDocument = Document(
        "_id" -> createUserCommand._id,
        "name" -> createUserCommand.name,
        "age" -> createUserCommand.age,
        "email" -> createUserCommand.email,
        "password" -> prepareUser.password,
        "phoneNumber" -> createUserCommand.phoneNumber,
        "typeUser" -> createUserCommand.userType,
        "booksBorrowed" -> prepareUser.booksBorrowed
      )

      val filter = equal("_id", createUserCommand._id)

      collection.find(filter).toFuture().flatMap { existingUser =>
        if (existingUser.isEmpty) {
          collection.insertOne(userDocument).toFuture().map { result =>
            if (result.wasAcknowledged()) {
              s"Пользователь успешно добавлен в базу данных с идентификатором: ${createUserCommand._id}"
            } else {
              "Ошибка при добавлении пользователя в базу данных"
            }
          }
        } else {
          val update = collection.replaceOne(filter, userDocument).toFuture().map { result =>
            if (result.wasAcknowledged()) {
              s"Пользователь успешно обновлен в базе данных с идентификатором: ${createUserCommand._id}"
            } else {
              "Ошибка при обновлении пользователя в базе данных"
            }
          }
          update
        }
      }
    }
  }


}
