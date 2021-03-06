import React from 'baret'
import {Popup} from '../../components/Popup'
import {RaportoiVirheestäForm} from '../virheraportointi/RaportoiVirheestaForm'
import {FormState} from './Header'

export const HeaderVirheraportointiSection = ({uiModeA, henkilö, opiskeluoikeudet}) => (
  <section className='virheraportointi' data-show={uiModeA.map(mode => mode === FormState.VIRHERAPORTOINTI)}>
    <Popup showStateAtom={uiModeA} dismissedStateValue={FormState.NONE} inline={true}>
      <RaportoiVirheestäForm
        henkilö={henkilö}
        opiskeluoikeudet={opiskeluoikeudet}
      />
    </Popup>
  </section>
)
