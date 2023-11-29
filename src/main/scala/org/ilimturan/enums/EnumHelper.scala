package org.ilimturan.enums

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

trait EnumHelper {

  def enumJsonFormat[T <: Enumeration](`enum`: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      override def read(json: JsValue): T#Value =
        json match {
          case JsString(value) => `enum`.withName(value)
          case _               => throw DeserializationException(s"Enum json is wrong: $json")
        }

      override def write(obj: T#Value): JsValue = JsString(obj.toString)
    }

}
