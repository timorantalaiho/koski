import React from 'baret'
import {modelEmpty, modelItems, modelLookup, modelProperties} from '../editor/EditorModel'
import {ArrayEditor} from '../editor/ArrayEditor'
import {Editor} from '../editor/Editor'
import {PropertyTitle} from '../editor/PropertiesEditor'

export const LukukausiIlmoittautuminenEditor = ({model}) => {
  let items = modelItems(modelLookup(model, 'ilmoittautumisjaksot'))
  if (!items.length) return null
  if (model.context.edit) return <ArrayEditor {...this.props}/>
  let properties = modelProperties(items[0])
  return (<table className='tabular-array'>
    <thead>
    <tr>{properties.filter(p => !p.hidden && !modelEmpty(p.model)).map((p, i) => <th key={i}><PropertyTitle property={p}/></th>)}</tr>
    </thead>
    <tbody>
    {
      items.map((item, i) => {
        return (<tr key={i}>
          {
            modelProperties(item).filter(p => !p.hidden && !modelEmpty(p.model)).map((p, j) => {
              return (<td key={j}><Editor model={p.model}/></td>)
            })
          }
        </tr>)
      })
    }
    </tbody>
  </table>)
}
