package RabbitMQ.RabbitMQOperation

import RabbitMQ.RabbitMQModel.RabbitMQModel
import RabbitMQ.RabbitMQTemple.CreateUserCommand
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.alpakka.amqp._
import akka.util.ByteString
import com.rabbitmq.client.{AMQP, ConnectionFactory, DefaultConsumer, Envelope}
import play.api.libs.json.Json

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future, Promise}

object MessageQueue {
  def createQueue(connectionProvider: AmqpConnectionProvider,
                  queueName: String,
                  queueDeclaration: QueueDeclaration,
                  bufferSize: Int = 10): Source[ReadResult, NotUsed] =
    AmqpSource.atMostOnceSource(
      NamedQueueSourceSettings(connectionProvider, queueName)
        .withDeclaration(queueDeclaration)
        .withAckRequired(true),
      bufferSize = bufferSize
    )
}

object RabbitMQConsumer {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val system: ActorSystem = ActorSystem("AlpakkaRabbitMQConsumer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  object RabbitMQConsumer {
    implicit val system: ActorSystem = ActorSystem("AlpakkaRabbitMQConsumer")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

//    def subscription(connectionProvider: AmqpConnectionProvider, rabbitModel: RabbitMQModel): CreateUserCommand = {
//      val queueName = rabbitModel.queueName
//      val routingKey = rabbitModel.routingKeyName
//
//      val amqpSource: Source[ReadResult, NotUsed] = AmqpSource.atMostOnceSource(
//        NamedQueueSourceSettings(connectionProvider, queueName)
//          .withDeclaration(QueueDeclaration(queueName).withDurable(true))
//          .withAckRequired(true),
//        bufferSize = 10
//      )
//
//      val processMessage: Flow[ReadResult, String, NotUsed] = Flow[ReadResult].mapAsync(1) { readResult =>
//        val message = new String(readResult.bytes.toArray, "UTF-8")
//        val receivedRoutingKey = readResult.envelope.getRoutingKey
//        if (receivedRoutingKey == routingKey) {
//          println(s"Received '$message' with expected routing key '$receivedRoutingKey'")
//          // Your processing logic here
//          Future.successful(message)
//        } else {
//          println(s"Ignored message with unexpected routing key '$receivedRoutingKey'")
//          Future.failed(new RuntimeException("Unexpected routing key"))
//        }
//      }
//
//      val futureResult: Future[String] = amqpSource
//        .via(processMessage)
//        .runWith(Sink.head)
//
//      val jsonString = Await.result(futureResult, 10.seconds)
//      Json.fromJson[CreateUserCommand](Json.parse(jsonString)).getOrElse(
//        throw new RuntimeException("Failed to parse JSON into CreateUserCommand")
//      )
//    }

//    def subscription1(
//                      connectionProvider: AmqpConnectionProvider,
//                      rabbitModel: RabbitMQModel
//                    )(implicit mat: Materializer): Future[CreateUserCommand] = {
//      val queueName = rabbitModel.queueName
//      val routingKey = rabbitModel.routingKeyName
//
//      val amqpSource: Source[ReadResult, NotUsed] = AmqpSource.atMostOnceSource(
//        NamedQueueSourceSettings(connectionProvider, queueName)
//          .withDeclaration(QueueDeclaration(queueName).withDurable(true))
//          .withAckRequired(true),
//        bufferSize = 10
//      )
//
//      val processMessage: Flow[ReadResult, String, NotUsed] =
//        Flow[ReadResult].mapAsync(1) { readResult =>
//          val message = new String(readResult.bytes.toArray, "UTF-8")
//          val receivedRoutingKey = readResult.envelope.getRoutingKey
//          if (receivedRoutingKey == routingKey) {
//            println(s"Received '$message' with expected routing key '$receivedRoutingKey'")
//            // Your processing logic here
//            Future.successful(message)
//          } else {
//            println(s"Ignored message with unexpected routing key '$receivedRoutingKey'")
//            Future.failed(new RuntimeException("Unexpected routing key"))
//          }
//        }
//
//      val futureResult: Future[CreateUserCommand] =
//        amqpSource.via(processMessage).map { jsonString =>
//          Json.fromJson[CreateUserCommand](Json.parse(jsonString)).getOrElse(
//            throw new RuntimeException("Failed to parse JSON into CreateUserCommand")
//          )
//        }.runWith(Sink.head)
//
//      futureResult
//    }


    def subscription(
                       connectionProvider: AmqpConnectionProvider,
                       rabbitModel: RabbitMQModel
                     )(implicit mat: Materializer): Future[CreateUserCommand] = {
      val queueName = rabbitModel.queueName
      val routingKey = rabbitModel.routingKeyName

      val amqpSource: Source[ReadResult, NotUsed] = AmqpSource.atMostOnceSource(
        NamedQueueSourceSettings(connectionProvider, queueName)
          .withDeclaration(QueueDeclaration(queueName).withDurable(true))
          .withAckRequired(true),
        bufferSize = 10
      )

      val processMessage: Flow[ReadResult, String, NotUsed] =
        Flow[ReadResult].mapAsync(1) { readResult =>
          val message = new String(readResult.bytes.toArray, "UTF-8")
          val receivedRoutingKey = readResult.envelope.getRoutingKey
          if (receivedRoutingKey == routingKey) {
            println(s"Received '$message' with expected routing key '$receivedRoutingKey'")
            // Your processing logic here
            Future.successful(message)
          } else {
            println(s"Ignored message with unexpected routing key '$receivedRoutingKey'")
            Future.failed(new RuntimeException("Unexpected routing key"))
          }
        }

      val timeoutDuration = 10.seconds
      val timeoutFlow: Flow[String, String, NotUsed] =
        Flow[String].completionTimeout(timeoutDuration).recover { case _ =>
          "Timeout"
        }

      val futureResult: Future[CreateUserCommand] =
        amqpSource
          .via(processMessage)
          .via(timeoutFlow)
          .map {
            case "Timeout" =>
              CreateUserCommand("", None, None, None, None,"") // Создайте объект с пустыми значениями
            case jsonString =>
              Json.fromJson[CreateUserCommand](Json.parse(jsonString)).getOrElse(
                throw new RuntimeException("Failed to parse JSON into CreateUserCommand")
              )
          }
          .runWith(Sink.head)

      futureResult
    }


//    def subscription1(
//                       connectionProvider: AmqpConnectionProvider,
//                       rabbitModel: RabbitMQModel
//                     )(implicit mat: Materializer): Future[CreateUserCommand] = {
//      val queueName = rabbitModel.queueName
//      val routingKey = rabbitModel.routingKeyName
//
//      val amqpSource: Source[ReadResult, NotUsed] = AmqpSource.atMostOnceSource(
//        NamedQueueSourceSettings(connectionProvider, queueName)
//          .withDeclaration(QueueDeclaration(queueName).withDurable(true))
//          .withAckRequired(true),
//        bufferSize = 10
//      )
//
//      val processMessage: Flow[ReadResult, String, NotUsed] =
//        Flow[ReadResult].mapAsync(1) { readResult =>
//          val message = new String(readResult.bytes.toArray, "UTF-8")
//          val receivedRoutingKey = readResult.envelope.getRoutingKey
//          if (receivedRoutingKey == routingKey) {
//            println(s"Received '$message' with expected routing key '$receivedRoutingKey'")
//            // Your processing logic here
//            Future.successful(message)
//          } else {
//            println(s"Ignored message with unexpected routing key '$receivedRoutingKey'")
//            Future.failed(new RuntimeException("Unexpected routing key"))
//          }
//        }
//
//      val timeoutDuration = 3.seconds
//      val timeoutFlow: Flow[String, String, NotUsed] =
//        Flow[String].completionTimeout(timeoutDuration).recoverWithRetries(1, { case _ =>
//          Source.single("Timeout")
//        })
//
//      val futureResult: Future[CreateUserCommand] =
//        amqpSource
//          .via(processMessage)
//          .via(timeoutFlow)
//          .map {
//            case "Timeout" =>
//              throw new RuntimeException("Processing timeout")
//            case jsonString =>
//              Json.fromJson[CreateUserCommand](Json.parse(jsonString)).getOrElse(
//                throw new RuntimeException("Failed to parse JSON into CreateUserCommand")
//              )
//          }
//          .runWith(Sink.head)
//
//      futureResult
//    }
  }


}



//  def subscription(connectionFactory: ConnectionFactory, rabbitModel: RabbitMQModel)
//                  (implicit ec: ExecutionContext) : Future[String] = {
//    val promise = Promise[String]()
//    val connection = connectionFactory.newConnection()
//    val channel = connection.createChannel()
//
//    // Замените на вашу очередь и ключ маршрутизации
//    val queueName = rabbitModel.queueName
//    val routingKey = rabbitModel.routingKeyName
//
//    // Объявление очереди и установка маршрутизации
//    channel.queueDeclare(queueName, true, false, false, null)
//    channel.queueBind(queueName, rabbitModel.exchangeName, routingKey)
//
//    val consumer = new DefaultConsumer(channel) {
//      override def handleDelivery(
//                                   consumerTag: String,
//                                   envelope: Envelope,
//                                   properties: AMQP.BasicProperties,
//                                   body: Array[Byte]
//                                 ): Unit = {
//        val message = new String(body, "UTF-8")
//        val receivedRoutingKey = envelope.getRoutingKey
//        if (receivedRoutingKey == routingKey) {
//          println(s"Received '$message' with expected routing key '$receivedRoutingKey'")
//          promise.success(message)
//        } else {
//          println(s"Ignored message with unexpected routing key '$receivedRoutingKey'")
//          promise.failure(new RuntimeException("Unexpected routing key"))
//        }
//      }
//    }
//
//    Future {
//      channel.basicConsume(queueName, true, consumer)
//    }.flatMap(consumerTag => promise.future)
//  }

