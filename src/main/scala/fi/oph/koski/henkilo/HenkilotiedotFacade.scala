package fi.oph.koski.henkilo

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusRepository
import fi.oph.koski.schema.HenkilötiedotJaOid

case class HenkilötiedotFacade(henkilöRepository: HenkilöRepository, opiskeluoikeusRepository: OpiskeluoikeusRepository) {
  def find(query: String)(implicit koskiSession: KoskiSession): HenkilötiedotSearchResponse = {
    val henkilöt = findHenkilötiedot(query.toUpperCase).toList
    val canAddNew = henkilöt.isEmpty && koskiSession.hasAnyWriteAccess

    if (Hetu.validFormat(query).isRight) {
      Hetu.validate(query) match {
        case Right(hetu) =>
          val canAddNew = henkilöt.isEmpty && koskiSession.hasAnyWriteAccess
          HenkilötiedotSearchResponse(henkilöt, canAddNew, hetu = Some(query))
        case Left(status) =>
          henkilöt match {
            case Nil =>
              HenkilötiedotSearchResponse(henkilöt, false, status.errorString) // TODO: i18n for error messages here
            case _ =>
              HenkilötiedotSearchResponse(henkilöt, false)
          }
      }
    } else if (henkilöt.isEmpty && HenkilöOid.isValidHenkilöOid(query)) {
      findByOid(query)(koskiSession) match {
        case Right(oppija) =>
          oppija.headOption match {
            case Some(tiedot) => HenkilötiedotSearchResponse(henkilöt, canAddNew, oid = Some(query))
            case None => HenkilötiedotSearchResponse(henkilöt, false)
          }
        case Left(_) => HenkilötiedotSearchResponse(henkilöt, false)
      }
    } else {
      HenkilötiedotSearchResponse(henkilöt, false)
    }
  }

  // Hakee oppijoita kyselyllä. Sisällyttää vain henkilöt, joilta löytyy vähintään yksi opiskeluoikeus, johon käyttäjällä katseluoikeus
  def findHenkilötiedot(queryString: String)(implicit user: KoskiSession): Seq[HenkilötiedotJaOid] = {
    val oppijat: List[HenkilötiedotJaOid] = henkilöRepository.findOppijat(queryString)
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> queryString)))
    val filtered = opiskeluoikeusRepository.filterOppijat(oppijat)
    filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet))
  }

  def findByHetu(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> hetu)))
    Hetu.validate(hetu).right.map(henkilöRepository.findOppijat)
  }

  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> oid)))
    HenkilöOid.validateHenkilöOid(oid).right.map(henkilöRepository.findOppijat)
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean, error: Option[String] = None, hetu: Option[String] = None, oid: Option[String] = None)
