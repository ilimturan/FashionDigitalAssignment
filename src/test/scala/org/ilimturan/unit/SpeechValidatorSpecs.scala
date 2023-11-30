package org.ilimturan.unit

import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.validator.SpeechValidator
import org.scalatest.{Matchers, WordSpec}

class SpeechValidatorSpecs extends WordSpec with Matchers with StrictLogging {

  "run 'SpeechValidator' tests" should {

    "return true when urls are valid" in {
      SpeechValidator.isValidUrl(
        "https://stackoverflow.com/questions/48717646/docker-compose-down-with-a-non-default-yml-file-name"
      ) shouldBe true
      SpeechValidator.isValidUrl("https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv") shouldBe true
      SpeechValidator.isValidUrl("http://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv") shouldBe true
      SpeechValidator.isValidUrl("https://fid-recruiting.s3-eu-west-1.amazonaws.com") shouldBe true
      SpeechValidator.isValidUrl("https://amazonaws.com/politics1.csv") shouldBe true
      SpeechValidator.isValidUrl("https://amazonaws.com/xxxx") shouldBe true
    }

    "return false when urls are invalid" in {
      SpeechValidator.isValidUrl(
        "https:stackoverflow.com/questions/48717646/docker-compose-down-with-a-non-default-yml-file-name"
      ) shouldBe false
      SpeechValidator.isValidUrl("htt://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv") shouldBe false
      SpeechValidator.isValidUrl("http://") shouldBe false
      SpeechValidator.isValidUrl("fid-recruiting.s3-eu-west-1.amazonaws.com") shouldBe false
      SpeechValidator.isValidUrl("amazonaws.com/politics1.csv") shouldBe false
      SpeechValidator.isValidUrl("https://com/xxxx") shouldBe false
    }

  }
}
