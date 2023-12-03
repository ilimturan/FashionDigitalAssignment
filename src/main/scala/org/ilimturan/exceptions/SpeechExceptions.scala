package org.ilimturan.exceptions

import scala.util.control.NoStackTrace

object SpeechExceptions {

  sealed trait WithDetail extends NoStackTrace {
    def message: String
    def data: Option[String]
  }

  case class ValidationError(message: String, data: Option[String] = None) extends WithDetail

}
