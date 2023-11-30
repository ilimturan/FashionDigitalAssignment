package org.ilimturan.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.routes.RoutesConsumer
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt

class ConsumerIntegrationSpecs extends WordSpec with Matchers with ScalatestRouteTest with StrictLogging {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(30.seconds.dilated)
  implicit val actorSystem               = ActorSystem()
  implicit val executionContext          = actorSystem.dispatcher

  val route = new RoutesConsumer()

  "run 'health' endpoint tests" should {

    "return success health" in {
      Get(s"/health") ~> route.routes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "return fail health when wrong req type" in {
      Post(s"/health") ~> route.routes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
    }
  }

}
