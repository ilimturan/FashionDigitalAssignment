package org.ilimturan.integration

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.routes.RoutesConsumer
import org.scalatest.{Matchers, WordSpec}

class ConsumerIntegrationSpecs extends WordSpec with Matchers with ScalatestRouteTest with StrictLogging {

  "run Consumer endpoint tests" should {

    val route = new RoutesConsumer()

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
