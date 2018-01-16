package com.github.alextokarew.telegram.bots.platform.flow

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.alextokarew.telegram.bots.domain.Protocol.Responses.Message
import com.github.alextokarew.telegram.bots.platform.test.WireMock
import com.github.tomakehurst.wiremock.client.{WireMock => WM}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class PollerSpec extends TestKit(ActorSystem("PollerSpec")) with WordSpecLike with WireMock with Matchers {

  val timeout = 30

  "Poller" should {
    "poll getUpdates method" in {
      val url = s"http://localhost:${wireMockServer.port()}/botToken"
      val poller = system.actorOf(Poller.props(testActor, url, timeout))

      val messages = receiveWhile(messages = 4) {
        case m: Message => m
      }

      wireMockServer.verify(WM.getRequestedFor(
        WM.urlPathEqualTo("/botToken/getUpdates"))
          .withQueryParam("offset", WM.equalTo("0"))
          .withQueryParam("timeout", WM.equalTo(timeout.toString))
      )

      messages should have size 4

      expectNoMessage(1.second)

      wireMockServer.verify(WM.getRequestedFor(
        WM.urlPathEqualTo("/botToken/getUpdates"))
        .withQueryParam("offset", WM.equalTo("219169025"))
        .withQueryParam("timeout", WM.equalTo(timeout.toString))
      )
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
}
