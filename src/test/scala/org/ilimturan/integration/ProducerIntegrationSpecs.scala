package org.ilimturan.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.typesafe.scalalogging.StrictLogging
import io.getquill.{CamelCase, PostgresAsyncContext}
import org.ilimturan.config.PostgresConfig
import org.ilimturan.models.Protocols._
import org.ilimturan.models.SpeechResponse
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.routes.RoutesProducer
import org.ilimturan.services.SpeechService
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt

class ProducerIntegrationSpecs
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with StrictLogging
    with SprayJsonSupport {

  implicit val timeout: RouteTestTimeout                = RouteTestTimeout(30.seconds.dilated)
  implicit val actorSystem                              = ActorSystem()
  implicit val executionContext                         = actorSystem.dispatcher
  val postgresCtx: PostgresAsyncContext[CamelCase.type] =
    new PostgresAsyncContext(CamelCase, PostgresConfig.dbPostgresConfig)
  val speechRepo                                        = new SpeechRepo()(postgresCtx, executionContext)
  val speechService                                     = new SpeechService(speechRepo)
  val route                                             = new RoutesProducer(speechService)

  "run Producer endpoint tests" should {

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

    "return fail health when wrong path" in {
      Post(s"/health-xx") ~> route.routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    val header = RawHeader("Content-Type", "text/html; charset=UTF-8")

    "return error message when url/urls empty" in {

      Get(s"/evaluation")
        .withHeaders(header) ~> route.routes ~> check {

        val response = responseAs[SpeechResponse]

        status shouldBe StatusCodes.OK
        response.errors.size shouldBe 1
        response.data.isEmpty shouldBe true
      }
    }

    "return error message when url wrong" in {
      val urlStr = "wrongurl"
      Get(s"/evaluation?url=$urlStr")
        .withHeaders(header) ~> route.routes ~> check {

        val response = responseAs[SpeechResponse]

        status shouldBe StatusCodes.OK
        response.errors.size shouldBe 1
        response.data.isEmpty shouldBe true

      }
    }

    "return error message when url first one wrong and other one right" in {
      val urlStr1 = "htt//fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv.xxx" //this wrong
      val urlStr2 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

      Get(s"/evaluation?url=$urlStr1&url=$urlStr2")
        .withHeaders(header) ~> route.routes ~> check {

        val response = responseAs[SpeechResponse]

        status shouldBe StatusCodes.OK
        response.errors.size shouldBe 1
        response.data.isEmpty shouldBe true

      }
    }

    "return success when url is right" in {
      val urlStr = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

      Get(s"/evaluation?url=$urlStr")
        .withHeaders(header) ~> route.routes ~> check {

        val response = responseAs[SpeechResponse]

        status shouldBe StatusCodes.OK
        response.errors.size shouldBe 0
        response.data.nonEmpty shouldBe true
        response.message.nonEmpty shouldBe true

      }
    }

    "return success when url is right and not uniq" in {
      val urlStr = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

      Get(s"/evaluation?url=$urlStr&url=$urlStr")
        .withHeaders(header) ~> route.routes ~> check {

        val response = responseAs[SpeechResponse]

        status shouldBe StatusCodes.OK
        response.errors.size shouldBe 0
        response.data.get.jobCount shouldBe 1
        response.data.get.urlCount shouldBe 1
        response.message.nonEmpty shouldBe true

      }
    }

    "return success when url/urls multiple and right" in {

      val urlStr1 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv"
      val urlStr2 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics2.csv"
      val urlStr3 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics3.csv"

      Get(s"/evaluation?url=$urlStr1&url=$urlStr2&url=$urlStr3")
        .withHeaders(header) ~> route.routes ~> check {

        val response = responseAs[SpeechResponse]

        status shouldBe StatusCodes.OK
        response.errors.size shouldBe 0
        response.data.get.jobCount shouldBe 3
        response.data.get.urlCount shouldBe 3
        response.message.nonEmpty shouldBe true

      }
    }

  }

}
