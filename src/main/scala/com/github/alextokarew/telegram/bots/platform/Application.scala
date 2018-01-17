package com.github.alextokarew.telegram.bots.platform

import akka.actor.{ActorSystem, Props}
import com.github.alextokarew.telegram.bots.domain.Protocol
import com.github.alextokarew.telegram.bots.platform.flow.{Poller, Printer}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

/**
  * Application entry point.
  */
object Application extends Protocol {

  private val log = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    log.info("Starting reminder bot's backend")

    implicit val system: ActorSystem = ActorSystem("reminderbot")

    val config = ConfigFactory.load()
    val token = config.getString("telegram.bot.token")
    val url = config.getString("telegram.api.url").replace("<token>", token)
    val timeout = config.getInt("telegram.poller.timeout")
    val retryInterval = config.getInt("telegram.poller.retryInterval")

    val printer = system.actorOf(Props[Printer], "printer")
    system.actorOf(Poller.props(printer, url, timeout, retryInterval), "poller")

    sys.addShutdownHook {
      system.terminate()
    }
  }
}
