package com.github.alextokarew.telegram.bots.platform.actors

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.alextokarew.telegram.bots.platform.domain.Protocol.Responses.Message
import com.github.alextokarew.telegram.bots.platform.test.WireMock
import com.github.tomakehurst.wiremock.client.{WireMock => WM}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class PollerSpec extends TestKit(ActorSystem("PollerSpec")) with WordSpecLike with WireMock with Matchers {

  val timeout = 30
  val retryInterval = 1000

  "Poller" should {
    "poll getUpdates method" in {
      val url = s"http://localhost:${wireMockServer.port()}/botToken"
      val poller = system.actorOf(Poller.props(testActor, url, timeout, retryInterval))

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

    "handle unpredictable situations" in {
      val url = s"http://bad-url.xxxxx"
      val poller = system.actorOf(Poller.props(testActor, url, timeout, retryInterval))

      expectNoMessage(1.second)
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
}
