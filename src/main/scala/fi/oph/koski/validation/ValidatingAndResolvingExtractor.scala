package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoResolvingCustomDeserializer, KoodistoViitePalvelu}
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioResolvingCustomDeserializer}
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s._

import scala.reflect.runtime.{universe => ru}

object ValidatingAndResolvingExtractor {
  import KoskiSchema.deserializationContext
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue, context: ValidationAndResolvingContext)(implicit tag: ru.TypeTag[T]): Either[HttpStatus, T] = {
    SchemaValidatingExtractor.extract(json)(deserializationContext.copy(customDeserializers = List(
      OrganisaatioResolvingCustomDeserializer(context.organisaatioRepository),
      KoodistoResolvingCustomDeserializer(context.koodistoPalvelu)
    )), tag) match {
      case Right(t) => Right(t)
      case Left(errors: List[ValidationError]) => Left(KoskiErrorCategory.badRequest.validation.jsonSchema.apply(JsonErrorMessage(errors)))
    }
  }
}

case class ValidationAndResolvingContext(koodistoPalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository)



