package org.ilimturan.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.typesafe.scalalogging.StrictLogging
import io.getquill.{CamelCase, PostgresAsyncContext}
import org.ilimturan.config.PostgresConfig
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.routes.RoutesQuery
import org.ilimturan.services.SpeechService
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt

class QueryIntegrationSpecs
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with StrictLogging
    with SprayJsonSupport {

  implicit val timeout: RouteTestTimeout                = RouteTestTimeout(10.seconds.dilated)
  implicit val actorSystem                              = ActorSystem()
  implicit val executionContext                         = actorSystem.dispatcher
  val postgresCtx: PostgresAsyncContext[CamelCase.type] =
    new PostgresAsyncContext(CamelCase, PostgresConfig.dbPostgresConfig)
  val speechRepo                                        = new SpeechRepo()(postgresCtx, executionContext)
  val speechService                                     = new SpeechService(speechRepo)
  val route                                             = new RoutesQuery(speechService)

  "run Query endpoint tests" should {
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

    "return error when 'mostYear' parameter is wrong" in {

      Get(s"/report?mostYear=XXXXXX") ~> route.routes ~> check {

        status shouldBe StatusCodes.NotAcceptable

      }
    }

    "return ok when 'mostYear' parameter is right" in {

      Get(s"/report?mostYear=2023") ~> route.routes ~> check {

        status shouldBe StatusCodes.OK

      }
    }

    "return ok without parameter" in {

      Get(s"/report") ~> route.routes ~> check {

        status shouldBe StatusCodes.OK

      }
    }
  }

}
