import Http from '../util/http'

let dataPath = (organisaatioOid, type) => `/koski/api/preferences/${organisaatioOid}/${type}`
let editorPath = (organisaatioOid, type) => `/koski/api/editor/preferences/${organisaatioOid}/${type}`

export const saveOrganizationalPreference = (organisaatioOid, type, key, data) => {
  return Http.put(dataPath(organisaatioOid, type), { key, value: data}, { invalidateCache: [dataPath(organisaatioOid, type), editorPath(organisaatioOid, type)] })
}

export const getOrganizationalPreferences = (organisaatioOid, type) => {
  return Http.cachedGet(`/koski/api/editor/preferences/${organisaatioOid}/${type}`)
}

export const deleteOrganizationalPreference = (organisaatioOid, type, key) => {
  return Http.delete(`/koski/api/preferences/${organisaatioOid}/${type}/${key}`).flatMap(() =>
    Http.cachedGet(`/koski/api/editor/preferences/${organisaatioOid}/${type}`, {force: true}))
}
