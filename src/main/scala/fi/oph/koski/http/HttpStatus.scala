package fi.oph.koski.http

import fi.oph.koski.json.JsonSerializer
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s.JValue
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.{universe => ru}
import reflect.runtime.universe.TypeTag

case class HttpStatus(statusCode: Int, errors: List[ErrorDetail]) {
  if (statusCode == 200 && errors.nonEmpty) throw new RuntimeException("HttpStatus 200 with error message " + errors.mkString(","))
  def isOk = statusCode < 300
  def isError = !isOk

  /** Pick given status if this one is ok. Otherwise stick with this one */
  def onSuccess(status: => HttpStatus) = if (isOk) { status } else { this }

  def errorString: Option[String] = errors.headOption.flatMap(_.message match {
    case JString(s) => Some(s)
    case otherJValue => None
  })
}

// Constructor is private to force force explicit usage of ErrorMessage. This allows us
// to quickly analyze what kind of things we put into our errors
case class ErrorDetail private(key: String, message: JValue) {
  override def toString = key + " (" + JsonMethods.compact(message) + ")"
}

object ErrorDetail {
  def apply(key: String, message: String): ErrorDetail = apply(key, StringErrorMessage(message))
  def apply(key: String, message: ErrorMessage): ErrorDetail = new ErrorDetail(key, message.asJValue)
}

object HttpStatus {
  val ok = HttpStatus(200, Nil)

  // Combinators

  /** If predicate is true, yield 200/ok, else run given block */
  def validate(predicate: => Boolean)(status: => HttpStatus) = if (predicate) { ok } else { status }
  /** Combine two statii: concatenate errors list, pick highest status code */
  def append(a: HttpStatus, b: HttpStatus) = {
    HttpStatus(Math.max(a.statusCode, b.statusCode), a.errors ++ b.errors)
  }
  /** Append all given statii into one, concatenating error list, picking highest status code */
  def fold(statii: Iterable[HttpStatus]): HttpStatus = statii.fold(ok)(append)

  /** Append all given statii into one, concatenating error list, picking highest status code */
  def fold(statii: HttpStatus*): HttpStatus = fold(statii.toList)

  def foldEithers[T](xs: Iterable[Either[HttpStatus, T]]): Either[HttpStatus, List[T]] = xs.collect { case Left(e) => e} match {
    case Nil =>
      Right(xs.collect { case Right(oo) => oo }.toList)
    case errors =>
      Left(HttpStatus.fold(errors))
  }

  def justStatus[A](either: Either[HttpStatus, A]) = either match {
    case Right(_) => HttpStatus.ok
    case Left(status) => status
  }
}

trait ErrorMessage {
  def asJValue: JValue
}
object ErrorMessage {
  implicit def str2ErrorMessage(str: String) = StringErrorMessage(str)
}
case class StringErrorMessage(str: String) extends ErrorMessage {
  override def asJValue: JValue = JString(str)
}

case class JsonErrorMessage[T : TypeTag](value: T) extends ErrorMessage {
  override def asJValue: JValue = JsonSerializer.serializeWithRoot(value)
}
