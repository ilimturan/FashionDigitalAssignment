package org.ilimturan.unit

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Sink
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.parser.SpeechCsvParser
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

class SpeechCsvParserSpecs extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  // for futureValue timeout
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(3, Seconds)))

  "run SpeechCsvParser tests" should {

    val parser = new SpeechCsvParser()

    "when parse file inputstream" in {

      val inputStream    = getClass.getResourceAsStream("/politics.csv")
      val bomInputStream = new BOMInputStream(inputStream)

      val source = parser.toAkkaSource(bomInputStream)
      val result = source.runWith(Sink.seq).map(_.size)

      result.futureValue shouldEqual 4
    }

  }
}
