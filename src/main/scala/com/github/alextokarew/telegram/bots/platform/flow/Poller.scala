package com.github.alextokarew.telegram.bots.platform.flow

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.github.alextokarew.telegram.bots.domain.Protocol
import com.github.alextokarew.telegram.bots.domain.Protocol.Responses.{OkWrapper, Update}

/**
 * Long-polls Telegram API and retrieves updates and emits unwrapped messages to consumer behind passed actor reference.
 * @param consumer messages consumer actor ref
 * @param url base telegram url
 * @param timeout poll timeout in seconds
 */
class Poller(consumer: ActorRef, url: String, timeout: Int) extends Actor with Protocol {
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
        update.message.foreach { message =>
          consumer ! message
        }
        //TODO deal with edited messages
      }

      if (response.result.nonEmpty) {
        context.become(process(response.result.last.update_id + 1))
        // TODO: persist messages before commit.
      }
      self ! Poll

    //TODO: deal with failures and with bad http status codes, maybe write to log and go to the feedback loop
  }

  def receive: Receive = process(0)
}

object Poller extends Protocol {
  type Response = OkWrapper[Seq[Update]]

  case object Poll

  def props(consumer: ActorRef, url: String, timeout: Int) = Props(classOf[Poller], consumer, url, timeout)
}
