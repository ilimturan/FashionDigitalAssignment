package org.ilimturan.validator

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.validator.routines.UrlValidator
import org.ilimturan.exceptions.SpeechExceptions.ValidationError

import scala.util.Try

object SpeechValidator extends StrictLogging {

  private val urlValidator: UrlValidator = new UrlValidator(Array("http", "https"))

  def isValidUrl(url: String): Boolean = {
    urlValidator.isValid(url)
  }

  def queryParamToYearFilter(sTypeMaybe: Option[String]): Option[Int] = {

    sTypeMaybe match {
      case Some(value) =>
        Try(
          value.trim.toInt
        ).toEither match {
          case Left(_)         => throw ValidationError("Parameter is wrong", Some(value))
          case Right(valueInt) => Some(valueInt)
        }
      case None        => None
    }
  }

}
