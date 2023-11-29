package org.ilimturan.implicits

import io.getquill.MappedEncoding
import org.ilimturan.enums.SPEECH_PROCESS_STATUS.SPEECH_PROCESS_STATUS
import org.ilimturan.enums.{SPEECH_PROCESS_STATUS, STATUS}
import org.ilimturan.enums.STATUS.STATUS

class QuillImplicit {

  implicit val encodeStatus: MappedEncoding[String, STATUS] = MappedEncoding[String, STATUS](STATUS.withName)
  implicit val decodeStatus: MappedEncoding[STATUS, String] = MappedEncoding[STATUS, String](_.toString)

  implicit val encodeSpeechProcessStatus: MappedEncoding[String, SPEECH_PROCESS_STATUS] =
    MappedEncoding[String, SPEECH_PROCESS_STATUS](SPEECH_PROCESS_STATUS.withName)
  implicit val decodeSpeechProcessStatus: MappedEncoding[SPEECH_PROCESS_STATUS, String] =
    MappedEncoding[SPEECH_PROCESS_STATUS, String](_.toString)

}
