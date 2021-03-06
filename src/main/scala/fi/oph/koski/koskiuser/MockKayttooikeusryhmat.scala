package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.schema.OidOrganisaatio

object MockKäyttöoikeusryhmät {
  implicit def stringToOrg(oid: String): OidOrganisaatio = OidOrganisaatio(oid)

  def oppilaitosKatselija(organisaatioOid: String) = KäyttöoikeusOrg(organisaatioOid, List(Palvelurooli(READ), Palvelurooli(LUOTTAMUKSELLINEN)), true, None)
  def oppilaitosTodistuksenMyöntäjä(organisaatioOid: String) = KäyttöoikeusOrg(organisaatioOid, List(Palvelurooli(READ)), true, None)
  def oppilaitosTallentaja(organisaatioOid: String) = KäyttöoikeusOrg(organisaatioOid, List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(LUOTTAMUKSELLINEN)), true, None)
  def oppilaitosPalvelukäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(organisaatioOid, List(Palvelurooli(READ), Palvelurooli(READ_UPDATE), Palvelurooli(TIEDONSIIRTO), Palvelurooli(LUOTTAMUKSELLINEN)), true, None)
  def oppilaitosPääkäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(organisaatioOid, List(Palvelurooli(READ), Palvelurooli(TIEDONSIIRRON_MITATOINTI), Palvelurooli(LUOTTAMUKSELLINEN)), true, None)
  def vastuukäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(organisaatioOid, List(Palvelurooli(READ)), true, None)

  val ophPääkäyttäjä = KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(YLLAPITAJA), Palvelurooli(LUOTTAMUKSELLINEN)))
  val ophKatselija = KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA), Palvelurooli(LUOTTAMUKSELLINEN)))
  val viranomaisKatselija = KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))
  val localizationAdmin = KäyttöoikeusGlobal(List(Palvelurooli("LOKALISOINTI", "CRUD")))

}
