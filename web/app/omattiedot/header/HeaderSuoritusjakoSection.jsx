import React from 'baret'
import {Popup} from '../../components/Popup'
import {SuoritusjakoForm} from '../suoritusjako/SuoritusjakoForm'
import {FormState} from './Header'

export const HeaderSuoritusjakoSection = ({uiModeA, opiskeluoikeudet}) => (
  <section className='suoritusjako' data-show={uiModeA.map(mode => mode === FormState.SUORITUSJAKO)}>
    <Popup showStateAtom={uiModeA} dismissedStateValue={FormState.NONE} inline={true}>
      <SuoritusjakoForm opiskeluoikeudet={opiskeluoikeudet}/>
    </Popup>
  </section>
)
