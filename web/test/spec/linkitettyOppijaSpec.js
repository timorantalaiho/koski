describe('Linkitetyt oppijat', function() {
  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  before(Authentication().login(), resetFixtures)
  describe('Kun katsotaan master-henkilön tietoja', function() {
    before(page.openPage, page.oppijaHaku.searchAndSelect('Master'))
    describe('Tietojen katsominen', function() {
      it('Näytetään myös slave-henkilön opiskeluoikeudet', function() {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeustyypit()).to.deep.equal([
          'Lukiokoulutus', 'Perusopetus'
        ])
      })
    })
    describe('Slaveen liitettyjen tietojen muuttaminen', function() {
      before(
        editor.edit,
        editor.property('oppimäärä').setValue('Lukio suoritetaan aikuisten opetussuunnitelman mukaan'),
        editor.saveChanges
      )

      it('Tallennus onnistuu', function() {

      })

      describe('Siirryttäessä tarkastelemaan slave-henkilön tietoja', function() {
        before(
          refreshIndices, // ensure that elasticsearch index is refreshed to reflect the changes made above
          page.oppijaHaku.searchAndSelect('Slave')
        )

        it('Näytetään muuttuneet tiedot', function() {
          expect(editor.property('oppimäärä').getValue()).to.equal('Lukio suoritetaan aikuisten opetussuunnitelman mukaan')
        })

        // TODO: tarkistettava jotenkin, että opiskeluoikeus säilyy linkitettynä slaveen (koska jatkossa tässä siirrytään aina katsomaan masterin tietoja)
      })
    })

    describe('Versiohistorian katsominen', function() {
      var versiohistoria = opinnot.versiohistoria

      before(versiohistoria.avaa, versiohistoria.valitse('v1'))

      it('Toimii', function() {

      })
    })
  })

  describe('Kun siirrytään oppijataulukosta tarkastelemaan slave-henkilön tietoja', function() {
    before(
      page.openPage,
      page.oppijataulukko.filterBy('nimi', 'Slave'),
      page.oppijataulukko.clickFirstOppija,
      page.waitUntilOppijaSelected('Master')
    )

    it('Näytetään master-henkilön tiedot', function() {

    })
  })

  describe('Kun siirrytään oppijahaussa tarkastelemaan slave-henkilön tietoja', function() {
    before(
      refreshIndices, // ensure that elasticsearch index is refreshed to reflect the changes made above
      page.oppijaHaku.searchAndSelect('Slave')
    )

    it('Näytetään master-henkilön tiedot', function() {
      // TODO: jatketaan, kun on päätetty, näytetäänkö haussa ja listassa vain master-henkilön tiedot
    })
  })

})