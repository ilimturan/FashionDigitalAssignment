package org.ilimturan.integration

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.routes.RoutesAggregator
import org.scalatest.{Matchers, WordSpec}

class AggregatorIntegrationSpecs extends WordSpec with Matchers with ScalatestRouteTest with StrictLogging {

  "run Aggregator endpoint tests" should {

    val route = new RoutesAggregator()

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
