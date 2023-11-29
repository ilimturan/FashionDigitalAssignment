package org.ilimturan.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.typesafe.scalalogging.StrictLogging
import io.getquill.{CamelCase, PostgresAsyncContext}
import org.ilimturan.ConsumerSpeech.dbEc
import org.ilimturan.config.PostgresConfig
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.routes.RoutesProducer
import org.ilimturan.services.SpeechService
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt

class ProducerIntegrationSpecs
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with StrictLogging {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(30.seconds.dilated)
  implicit val actorSystem               = ActorSystem()
  implicit val executionContext          = actorSystem.dispatcher

  implicit lazy val postgresCtx: PostgresAsyncContext[CamelCase.type] =
    new PostgresAsyncContext(CamelCase, PostgresConfig.dbPostgresConfig)

  lazy val speechRepo    = new SpeechRepo()(postgresCtx, dbEc)
  lazy val speechService = new SpeechService(speechRepo)

  val route = new RoutesProducer(speechService)

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

    "return fail health when wrong path" in {
      Post(s"/health-xx") ~> route.routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }

  "run 'producer' endpoint tests" should {

    val header = RawHeader("Content-Type", "text/html; charset=UTF-8")

    "return error message when url/urls empty" in {

      Get(s"/evaluation")
        .withHeaders(header) ~> route.routes ~> check {

        val response         = responseAs[String].trim
        val expectedResponse = s"Your request url's empty".trim

        status shouldBe StatusCodes.OK
        response shouldBe expectedResponse
      }
    }

    "return error message when url wrong" in {
      val urlStr = "wrongurl"
      Get(s"/evaluation?url=$urlStr")
        .withHeaders(header) ~> route.routes ~> check {

        val response         = responseAs[String].trim
        val expectedResponse = s"Your request contains invalid urls, valid [], invalid [$urlStr]".trim

        status shouldBe StatusCodes.OK
        response shouldBe expectedResponse

      }
    }

    "return error message when url first one wrong and other one right" in {
      val urlStr1 = "htt//fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv.xxx" //this wrong
      val urlStr2 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

      Get(s"/evaluation?url=$urlStr1&url=$urlStr2")
        .withHeaders(header) ~> route.routes ~> check {

        val response         = responseAs[String].trim
        val expectedResponse = s"Your request contains invalid urls, valid [$urlStr2], invalid [$urlStr1]".trim

        status shouldBe StatusCodes.OK
        response shouldBe expectedResponse

      }
    }

    "return success when url is right" in {
      val urlStr = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

      Get(s"/evaluation?url=$urlStr")
        .withHeaders(header) ~> route.routes ~> check {

        val response         = responseAs[String].trim
        val expectedResponse = s"Your request has been queued, url count [1], job count [1]".trim

        status shouldBe StatusCodes.OK
        response shouldBe expectedResponse

      }
    }

    "return success when url is right and not uniq" in {
      val urlStr = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

      Get(s"/evaluation?url=$urlStr&url=$urlStr")
        .withHeaders(header) ~> route.routes ~> check {

        val response         = responseAs[String].trim
        val expectedResponse = s"Your request has been queued, url count [1], job count [1]".trim

        status shouldBe StatusCodes.OK
        response shouldBe expectedResponse

      }
    }

    "return success when url/urls multiple and right" in {

      val urlStr1 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv"
      val urlStr2 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics2.csv"
      val urlStr3 = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics3.csv"

      Get(s"/evaluation?url=$urlStr1&url=$urlStr2&url=$urlStr3")
        .withHeaders(header) ~> route.routes ~> check {
        val response         = responseAs[String].trim
        val expectedResponse = s"Your request has been queued, url count [3], job count [3]".trim

        status shouldBe StatusCodes.OK
        response shouldBe expectedResponse

      }
    }

  }

}
