package com.github.alextokarew.telegram.bots.platform

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.github.alextokarew.telegram.bots.domain.Protocol
import com.github.alextokarew.telegram.bots.platform.flow.{Printer, Router}
import com.typesafe.config.ConfigFactory

/**
  * Created by alextokarew on 30.08.16.
  */
object Application extends Protocol {

  def main(args: Array[String]): Unit = {
    println("Starting bot's backend") //TODO log

    implicit val system = ActorSystem("asuperusefulbot")
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load()
    val token = config.getString("telegram.bot.token")
//    val apiConnector = TelegramApiConnector(config.getConfig("telegram.api.http"))

    val url = config.getString("telegram.api.url").replace("<token>", token)
    val timeout = config.getInt("telegram.poller.timeout")

    val printer = system.actorOf(Props[Printer])
    val router = system.actorOf(Router.props())
// TODO: val poller = system.actorOf(Poller.props(url, timeout, router, apiConnector))

    sys.addShutdownHook {
      system.terminate()
    }
  }
}
