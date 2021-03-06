package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

/** Checks whether the user potentially has some access to YTR data. This is used for performance optimization: YTR
    fetch can be prevented if user has no access
*/
class YtrAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSession): Boolean = {
    hasGlobalAccess(user) ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user.user)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }

  def hasGlobalAccess(user: KoskiSession): Boolean =
    user.hasGlobalReadAccess || user.allowedOpiskeluoikeusTyypit.contains("ylioppilastutkinto")
}
