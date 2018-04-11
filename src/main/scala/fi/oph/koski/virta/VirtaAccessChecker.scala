package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

/** Checks whether the user potentially has some access to Virta data. This is used for performance optimization: Virta
    fetch can be prevented if user has no access
*/
class VirtaAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSession): Boolean = {
    hasGlobalAccess(user) ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user.user)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }

  override def hasGlobalAccess(user: KoskiSession): Boolean =
    user.hasGlobalReadAccess || user.allowedOpiskeluoikeusTyypit.contains("korkeakoulutus")
}
