package org.ilimturan.enums

import spray.json.RootJsonFormat

object STATUS extends Enumeration with EnumHelper {
  type STATUS = Value
  val ACTIVE  = Value(1, "ACTIVE")
  val PASSIVE = Value(2, "PASSIVE")

  implicit val format: RootJsonFormat[STATUS.Value] = enumJsonFormat(this)
}

object SPEECH_PROCESS_STATUS extends Enumeration with EnumHelper {
  type SPEECH_PROCESS_STATUS = Value
  val READY      = Value(1, "READY")
  val DOWNLOADED = Value(2, "DOWNLOADED")
  val PARSING    = Value(3, "PARSING")
  val COMPLETED  = Value(4, "COMPLETED")
  val FAILED     = Value(5, "FAILED")

  implicit val format: RootJsonFormat[SPEECH_PROCESS_STATUS.Value] = enumJsonFormat(this)
}
