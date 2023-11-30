package org.ilimturan.models

case class SpeechResponseData(
    urlCount: Int,
    jobCount: Int
)

case class SpeechResponse(
    message: Option[String] = None,
    data: Option[SpeechResponseData] = None,
    errors: Seq[String] = Nil
)
