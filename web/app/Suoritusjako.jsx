import './polyfills/polyfills.js'
import './style/main.less'
import React from 'react'
import * as R from 'ramda'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, errorP, handleError, isTopLevel, TopLevelError} from './util/Error'
import {t} from './i18n/i18n'
import Http from './util/http'
import {Editor} from './editor/Editor'
import Text from './i18n/Text'
import editorMapping from './oppija/editors'
import {addContext} from './editor/EditorModel'
import {locationP} from './util/location'
import {SuoritusjakoHeader} from './suoritusjako/SuoritusjakoHeader'
import Link from './components/Link'

const secret = R.last(document.location.pathname.split('/'))

const tiedotP = () => Bacon.combineWith(
  Http.post('/koski/api/suoritusjako/editor', { secret }, { errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error(e)}).toProperty(),
  (tiedot) => {
    return tiedot && addContext(tiedot, {kansalainen: true})
  }
)

const SuoritusjakoTopBar = () => {
  return (
    <header id='topbar' className="local">
      <div id='logo'><Text name="Opintopolku.fi"/></div>
      <h1>
        <Link href="/koski/"><Text name="Koski"/></Link>
        <span>{' - '}<Text name="Opinnot"/></span>
      </h1>
    </header>
  )
}

const contentP = locationP.flatMapLatest(() => tiedotP().map(oppija =>
    oppija
      ? <div className="main-content oppija"><Oppija oppija={Editor.setupContext(oppija, {editorMapping})} stateP={Bacon.constant('viewing')}/></div>
      : <div className="main-content suoritusjako-virhe"><p><Text name="Suoritusjako virhe 1"/></p><p><Text name="Suoritusjako virhe 2"/></p></div>
    )
).toProperty().startWith(<div className="main-content ajax-indicator-bg"><Text name="Ladataan..."/></div>)

const allErrorsP = errorP(contentP)

// Rendered Virtual DOM
const domP = Bacon.combineWith(contentP, allErrorsP, (content, error) =>
  (<div>
    <Error error={error}/>
    <SuoritusjakoTopBar/>
    {
      isTopLevel(error)
        ? <TopLevelError error={error} />
        : (<div className="content-area suoritusjako">
            {content}
          </div>)
    }
  </div>)
)

document.querySelector('title').innerHTML = t('Opinnot') + ' - ' + t('Koski') + ' - ' + t('Opintopolku.fi')

// Render to DOM
domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))

// Handle errors
domP.onError(handleError)

const Oppija = ({oppija}) => {
  return oppija.loading
    ? <div className="loading"/>
    : (
      <div>
        <div className="oppija-content">
          <SuoritusjakoHeader oppija={oppija}/>
          <Editor key={document.location.toString()} model={oppija}/>
        </div>
      </div>
    )
}
