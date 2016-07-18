import React from 'react'
import { modelData, modelLookup, modelTitle } from './EditorModel.js'

export const OppijaEditor = React.createClass({
  render() {
    let {model} = this.props
    return model ? (
      <ul className="oppilaitokset">
        {
          modelLookup(model, 'opiskeluoikeudet').items.map((thing) => {
              let context = { oppijaOid: modelLookup(model, 'henkilö.oid').data }
              let oppilaitos = modelLookup(thing, 'oppilaitos')
              let opiskeluoikeudet = modelLookup(thing, 'opiskeluoikeudet').items
              return (<li className="oppilaitos" key={modelData(oppilaitos).oid}>
                <span className="oppilaitos">{oppilaitos.title}</span>
                <OppilaitoksenOpintosuoritusote oppilaitos={oppilaitos} tyyppi={modelData(opiskeluoikeudet[0], 'tyyppi').koodiarvo} context={context} />
                {
                  opiskeluoikeudet.map( (opiskeluoikeus, index) =>
                    <OpiskeluoikeusEditor key={ index } model={ opiskeluoikeus } context={context} />
                  )
                }
              </li>)
            }
          )}
      </ul>
    ) : null
  }
})

const OpiskeluoikeusEditor = React.createClass({
  render() {
    let {model, context} = this.props
    return (<div className="opiskeluoikeus">
      <div className="kuvaus">
        Opiskeluoikeus&nbsp;
          <span className="alku pvm">{modelTitle(model, 'alkamispäivä')}</span>-
          <span className="loppu pvm">{modelTitle(model, 'päättymispäivä')}</span>,&nbsp;
          <span className="tila">{modelTitle(model, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}</span>
        <FoldableEditor expanded={() => <PropertiesEditor properties={ model.properties.filter(property => property.key != 'suoritukset') } context={context}/>}/>
      </div>
      {
        modelLookup(model, 'suoritukset.items').map((suoritusModel, i) =>
          <SuoritusEditor model={suoritusModel} context={context} key={i}/>
        )
      }
      <OpiskeluoikeudenOpintosuoritusote opiskeluoikeus={model} context={context}/>
    </div>)
  }
})

const SuoritusEditor = React.createClass({
  render() {
    let {model, context} = this.props

    let title = modelTitle(model, 'koulutusmoduuli')
    return (<div className="suoritus">
      <span className="kuvaus">{title}</span>
      <Todistus suoritus={model} context={context}/>
      <FoldableEditor expanded={() => <PropertiesEditor properties={model.properties} context={context}/>}/>
    </div>)
  }
})


const FoldableEditor = React.createClass({
  render() {
    let {collapsed, expanded} = this.props
    let toggleDetails = () => { this.setState({showDetails: !showDetails})}
    let showDetails = this.state && this.state.showDetails
    return (<span>
      <a className="toggle-expand" onClick={toggleDetails}>{ showDetails ? '-' : '+' }</a>
      { showDetails ? expanded() : (collapsed ? collapsed() : null) }
    </span>)
  }
})

const Todistus = React.createClass({
  render() {
    let {suoritus, context: { oppijaOid }} = this.props
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let suoritusTila = modelData(suoritus, 'tila').koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusTila == 'VALMIS' && suoritustyyppi != 'korkeakoulututkinto'
      ? <a className="todistus" href={href}>näytä todistus</a>
      : null
  }
})

const OppilaitoksenOpintosuoritusote = React.createClass({
  render() {
    let {oppilaitos, tyyppi, context: { oppijaOid }} = this.props

    if (tyyppi == 'korkeakoulutus') { // vain korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(oppilaitos).oid
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})

const OpiskeluoikeudenOpintosuoritusote = React.createClass({
  render() {
    let {opiskeluoikeus, context: { oppijaOid }} = this.props
    if (modelData(opiskeluoikeus, 'tyyppi').koodiarvo == 'lukiokoulutus') { // vain lukiokoulutukselle näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'id')
      return <a className="opintosuoritusote" href={href}>näytä opintosuoritusote</a>
    } else {
      return null
    }
  }
})

const OppiaineEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="oppiaineensuoritus">
      <label className="oppiaine">{modelTitle(model, 'koulutusmoduuli')}</label>
      <span className="arvosana">{modelTitle(model, 'arviointi.-1')}</span>
    </div>)
  }
})

const VahvistusEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<span className="vahvistus simple">
      <span className="date">{modelTitle(model, 'päivä')}</span>&nbsp;
      <span className="allekirjoitus">{modelTitle(model, 'paikkakunta')}</span>&nbsp;
      {
        modelLookup(model, 'myöntäjäHenkilöt').items.map( henkilö =>
          <span className="nimi">{modelData(henkilö, 'nimi')}</span>
        )
      }
    </span>)
  }
})

const OpiskeluoikeusjaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="opiskeluoikeusjakso">
      <label className="date">{modelTitle(model, 'alku')}</label>
      <label className="tila">{modelTitle(model, 'tila')}</label>
    </div>)
  }
})

const TutkinnonosaEditor = React.createClass({
  render() {
    let {model, context} = this.props

    return (<div className="suoritus tutkinnonosa">
      <label className="nimi">{modelTitle(model, 'koulutusmoduuli')}</label>
      <span className="arvosana">{modelTitle(model, 'arviointi.-1')}</span>
      <FoldableEditor expanded={() => <PropertiesEditor properties={model.properties} context={context}/>}/>
    </div>)
  }
})

const ObjectEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let className = 'object ' + model.class
    let representative = model.properties.find(property => property.representative)
    let titleEditor = () => getModelEditor(representative.model, context)
    let objectEditor = () => <div className={className}><PropertiesEditor properties={model.properties} context={context}/></div>
    return representative
      ? model.properties.length == 1
        ? titleEditor()
        : <div>{titleEditor()}<FoldableEditor expanded={objectEditor} /></div>
      : objectEditor()
  }
})

const PropertiesEditor = React.createClass({
  render() {
    let {properties, context} = this.props
    return (<ul className="properties">
      {
        properties.filter(property => !property.model.empty && !property.hidden).map(property => {
          let propertyClassName = 'property ' + property.key
          return (<li className={propertyClassName} key={property.key}>
            <label>{property.title}</label>
            { getModelEditor(property.model, context) }
          </li>)
        })
      }
    </ul>)
  }
})

const ArrayEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let simple = !model.items[0] || model.items[0].simple
    let className = simple ? 'array simple' : 'array'
    return (
      <ul className={className}>
        {
          model.items.map((item, i) =>
            <li key={i}>{getModelEditor(item, context)}</li>
          )
        }
      </ul>
    )
  }
})

const StringEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span className="simple string">{model.data}</span>
  }
})

const BooleanEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span className="simple string">{model.title}</span>
  }
})

const DateEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span className="simple date">{model.title}</span>
  }
})

const EnumEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span className="simple enum">{model.title}</span>
  }
})


const NullEditor = React.createClass({
  render() {
    return null
  }
})

const editorTypes = {
  'perusopetuksenoppiaineensuoritus': OppiaineEditor,
  'perusopetukseenvalmistavanopetuksenoppiaineensuoritus': OppiaineEditor,
  'perusopetuksenlisäopetuksenoppiaineensuoritus': OppiaineEditor,
  'ammatillisentutkinnonosansuoritus': TutkinnonosaEditor,
  'lukionoppiaineensuoritus': TutkinnonosaEditor,
  'lukionkurssinsuoritus': OppiaineEditor,
  'ammatillinenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'lukionopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'perusopetuksenopiskeluoikeusjakso': OpiskeluoikeusjaksoEditor,
  'henkilövahvistus': VahvistusEditor,
  'object': ObjectEditor,
  'array': ArrayEditor,
  'string': StringEditor,
  'number': StringEditor,
  'date': DateEditor,
  'boolean': BooleanEditor,
  'enum': EnumEditor
}

const getModelEditor = (model, context) => {
  var Editor = model
    ? editorTypes[model.class] || editorTypes[model.type]
    : NullEditor
  if (!Editor) {
    if (!model.type) {
      console.log('Typeless model', model)
    }
    console.log('Missing editor ' + model.type)
    Editor = NullEditor
  }
  return <Editor model={model} context={context}/>
}