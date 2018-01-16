package com.github.alextokarew.telegram.bots.platform

import akka.actor.{ActorSystem, Props}
import com.github.alextokarew.telegram.bots.domain.Protocol
import com.github.alextokarew.telegram.bots.platform.flow.{Poller, Printer}
import com.typesafe.config.ConfigFactory

/**
  * Created by alextokarew on 30.08.16.
  */
object Application extends Protocol {

  def main(args: Array[String]): Unit = {
    println("Starting bot's backend") //TODO log

    implicit val system: ActorSystem = ActorSystem("reminderbot")

    val config = ConfigFactory.load()
    val token = config.getString("telegram.bot.token")
    val url = config.getString("telegram.api.url").replace("<token>", token)
    val timeout = config.getInt("telegram.poller.timeout")

    val printer = system.actorOf(Props[Printer])
    system.actorOf(Poller.props(printer, url, timeout))
// TODO: val poller = system.actorOf(Poller.props(url, timeout, router, apiConnector))

    sys.addShutdownHook {
      system.terminate()
    }
  }
}
