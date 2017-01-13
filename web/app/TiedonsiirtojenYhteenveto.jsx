import React from 'react'
import Http from './http'
import fecha from 'fecha'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import Link from './Link.jsx'

const yhteenvetoP = () => Http.cachedGet('/koski/api/tiedonsiirrot/yhteenveto')

export const tiedonsiirtojenYhteenvetoContentP = () => tiedonsiirrotContentP('/koski/tiedonsiirrot/yhteenveto', yhteenvetoP().map((rivit) =>
  ({
    content: (<div className="tiedonsiirto-yhteenveto">
                Yhteenveto KOSKI-palveluun siirretyistä opiskelijatiedoista oppilaitoksittain.
                <table>
                  <thead>
                  <tr>
                    <th className="oppilaitos">Oppilaitos</th>
                    <th className="aika">Viimeisin siirto</th>
                    <th className="siirretyt">Siirrettyjen lukumäärä</th>
                    <th className="virheelliset">Virheellisten lukumäärä</th>
                    <th className="opiskeluoikeudet">Opiskelu-oikeuksien lukumäärä</th>
                    <th className="lähdejärjestelmä">Lähdejärjestelmä</th>
                    <th className="lähdejärjestelmä">Käyttäjä</th>
                  </tr>
                  </thead>
                  <tbody>
                 { rivit.map((rivi, i) => {
                     return (<tr key={i}>
                       <td className="oppilaitos"><Link href={'/koski/tiedonsiirrot?oppilaitos=' + rivi.oppilaitos.oid}>{rivi.oppilaitos.nimi.fi}</Link></td>
                       <td className="aika">{fecha.format(fecha.parse(rivi.viimeisin, 'YYYY-MM-DDThh:mm'), 'D.M.YYYY H:mm')}</td>
                       <td className="siirretyt">{rivi.siirretyt}</td>
                       <td className="virheelliset">{ rivi.virheelliset ? <Link href={'/koski/tiedonsiirrot/virheet?oppilaitos=' + rivi.oppilaitos.oid}>{rivi.virheelliset}</Link> : '0'}</td>
                       <td className="opiskeluoikeudet">{rivi.opiskeluoikeudet}</td>
                       <td className="lähdejärjestelmä">{rivi.lähdejärjestelmä ? rivi.lähdejärjestelmä.nimi.fi : ''}</td>
                       <td className="käyttäjä">{rivi.käyttäjä.käyttäjätunnus || rivi.käyttäjä.oid}</td>
                     </tr>)
                    })
                  }
                  </tbody>
                </table>
              </div>),
      title: 'Tiedonsiirrot'
  })
))