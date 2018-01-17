package com.github.alextokarew.telegram.bots.platform.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.github.alextokarew.telegram.bots.platform.domain.Protocol
import com.github.alextokarew.telegram.bots.platform.domain.Protocol.Responses.{OkWrapper, Update}

import scala.concurrent.duration._

/**
 * Long-polls Telegram API and retrieves updates and emits unwrapped messages to consumer behind passed actor reference.
 * @param consumer messages consumer actor ref
 * @param url base telegram url
 * @param timeout poll timeout in seconds
 * @param retryInterval poll retry interval in case of failures, ms
 */
class Poller(consumer: ActorRef, url: String, timeout: Int, retryInterval: Int) extends Actor with Protocol with ActorLogging {
  import Poller._
  import akka.pattern.pipe
  import context.dispatcher

  private final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  private val http = Http(context.system)

  override def preStart(): Unit = {
    super.preStart()
    self ! Poll
  }

  def process(nextId: Long): Receive = {
    case Poll =>
      val request = HttpRequest(uri = s"$url/getUpdates?timeout=$timeout&offset=$nextId")
      http.singleRequest(request) pipeTo self

    case HttpResponse(StatusCodes.OK, _, entity, _) =>
      Unmarshal(entity).to[Response] pipeTo self

    case response: Response =>
      response.result.foreach { update =>
        log.debug("Processing update: {}", update)
        update.message.orElse(update.edited_message).foreach { message =>
          consumer ! message
        }
      }

      if (response.result.nonEmpty) {
        context.become(process(response.result.last.update_id + 1))
        // TODO: persist messages before commit.
      }
      self ! Poll

    case unhandled =>
      log.warning("Some unpredictable result: {}", unhandled)
      context.system.scheduler.scheduleOnce(retryInterval.millis, self, Poll)
  }

  def receive: Receive = process(0)
}

object Poller extends Protocol {
  type Response = OkWrapper[Seq[Update]]

  case object Poll

  def props(consumer: ActorRef, url: String, timeout: Int, retryInterval: Int) =
    Props(classOf[Poller], consumer, url, timeout, retryInterval)
}
