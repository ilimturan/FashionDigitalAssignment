package org.ilimturan.implicits

trait QuillOperators {

  /*
  this: Context[_, _] =>

  trait CompOperators[T] {
    def left: T
    def equal(right: T) = quote(infix"$left = $right".as[Boolean])
    def greater(right: T) = quote(infix"$left > $right".as[Boolean])
    def greaterThanEqual(right: T) = quote(infix"$left >= $right".as[Boolean])
    def lower(right: T) = quote(infix"$left < $right".as[Boolean])
    def lowerThanEqual(right: T) = quote(infix"$left <= $right".as[Boolean])
  }

  implicit class DateTimeComp(val left: DateTime) extends CompOperators[DateTime]
  implicit class DateComp(val left: Date) extends CompOperators[Date]

   */

  //implicit val encodeLocalTime = mappedEncoding[DateTime, Date](time => new Date(time.getMillis))

}
