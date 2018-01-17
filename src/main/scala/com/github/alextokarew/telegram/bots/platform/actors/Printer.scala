package com.github.alextokarew.telegram.bots.platform.actors

import akka.actor.{Actor, ActorLogging}

/**
  * Created by alextokarev on 05.09.16.
  * TODO: deleteme
  */
class Printer extends Actor with ActorLogging {
  override def receive: Receive = {
    case m => log.info("Message: {}", m)
  }
}
