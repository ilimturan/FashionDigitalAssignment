package org.ilimturan.unit

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.services.DownloadService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

class DownloadServiceSpecs
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with StrictLogging
    with ScalaFutures {

  // for futureValue timeout
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(3, Seconds)))

  "run DownloadService tests" should {

    val service = new DownloadService()

    "when url is invalid" in {
      val url    = "test_wrong_url"
      val result = service.download(url)
      result.futureValue.isLeft shouldEqual true
    }

    "when url returns 4xx" in {
      val url    = "https://www.youtube.com/asdasdas"
      val result = service.download(url)
      whenReady(result) { value =>
        value.isLeft shouldEqual true
      }
    }

    "when url returns 2xx" in {
      val url    = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"
      val result = service.download(url)
      whenReady(result) { value =>
        value.isRight shouldEqual true
      }
    }

  }
}
