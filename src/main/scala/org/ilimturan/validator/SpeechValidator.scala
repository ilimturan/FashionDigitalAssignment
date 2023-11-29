package org.ilimturan.validator

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.validator.routines.UrlValidator

object SpeechValidator extends StrictLogging {

  private val urlValidator: UrlValidator = new UrlValidator(Array("http", "https"))

  def isValidUrl(url: String): Boolean = {
    urlValidator.isValid(url)
  }

}
