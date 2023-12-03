package org.ilimturan.models

import spray.json.{DefaultJsonProtocol, JsNull, JsString, JsValue, JsonFormat, RootJsonFormat}

case class SpeechResponseData(
    urlCount: Int,
    jobCount: Int
)

case class SpeechResponse(
    message: Option[String] = None,
    data: Option[SpeechResponseData] = None,
    errors: Seq[String] = Nil
)

case class SpeechAggResponse(
    mostSpeeches: Option[String] = None,
    mostSecurity: Option[String] = None,
    leastWordy: Option[String] = None
)

//For showing "null"
object MySpeechAggResponse extends DefaultJsonProtocol {

  implicit object OptionStringFormat extends JsonFormat[Option[String]] {

    def write(option: Option[String]): JsValue = option match {
      case Some(value) => JsString(value)
      case None        => JsNull
    }

    def read(json: JsValue): Option[String] = json match {
      case JsString(value) => Some(value)
      case JsNull          => None
      case _               => throw new Exception("Expected Option[String]")
    }
  }

  implicit val mySpeechAggFormat: RootJsonFormat[SpeechAggResponse] = jsonFormat3(SpeechAggResponse)
}
