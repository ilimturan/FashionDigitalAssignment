package org.ilimturan.utils

import java.text.SimpleDateFormat
import java.util.Date

object DateUtils {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def toDateFormat(date: Date) = {

    dateFormat.format(date)

  }
}
