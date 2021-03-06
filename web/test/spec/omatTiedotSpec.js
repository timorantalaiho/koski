describe('Omat tiedot', function() {
  var omattiedot = OmatTiedotPage()
  var opinnot = OpinnotPage()
  var authentication = Authentication()
  var login = LoginPage()
  before(authentication.login(), resetFixtures)

  describe('Virkailijana', function() {
    before(authentication.login('Oili'), openPage('/koski/omattiedot'), wait.until(login.isVisible))
    it('siirrytään login-sivulle', function () {
    })
  })

  describe('Kansalaisena', function() {
    var etusivu = LandingPage()
    var korhopankki = KorhoPankki()
    before(authentication.logout, etusivu.openPage)

    describe('Kun kirjaudutaan sisään', function() {
      before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('251029-7230', 'Kansalainen', 'VÃ¤inÃ¶ TÃµnis', 'VÃ¤inÃ¶'), wait.until(omattiedot.isVisible))
      describe('Sivun sisältö', function() {
        it('Näytetään opiskeluoikeudet', function() {
          expect(omattiedot.nimi()).to.equal('Väinö Tõnis Kansalainen')
          expect(omattiedot.oppija()).to.equal('Opintoni')
          expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
            'Itä-Suomen yliopisto' ])
        })

        it('Näytetään opintoni-ingressi', function() {
          expect(omattiedot.ingressi()).to.equal(
            'Tällä sivulla näkyvät kaikki sähköisesti tallennetut opintosuoritukset yksittäisistä kursseista kokonaisiin tutkintoihin.'
          )
        })

        it("Näytetään nimi ja syntymäaika", function() {
          expect(omattiedot.headerNimi()).to.equal(
            'Väinö Tõnis Kansalainen\n' +
            's. 25.10.1929'
          )
        })

        it('Näytetään virheraportointi-painike', function() {
          expect(!!omattiedot.virheraportointiButton().length).to.equal(true)
        })

        describe('Ruotsinkielinen sisältö', function () {
          before(click(findSingle('#logout')), wait.until(etusivu.isVisible), etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('251029-7230', 'Kansalainen', 'VÃ¤inÃ¶ TÃµnis', 'VÃ¤inÃ¶', 'sv'), wait.until(omattiedot.isVisible))

          it('Näytetään ruotsinkielinen ingressi', function() {
            expect(omattiedot.ingressi()).to.equal(
              'På denna sida syns alla studieprestationer som sparats elektroniskt, från enskilda kurser till hela examina.'
            )
          })
        })
      })

      describe('Kun kirjaudutaan ulos', function () {
        before(click(findSingle('#logout')), wait.until(etusivu.isVisible))
        it('Näytetään länderi', function() {

        })
      })

      describe('Kun henkilöllä on syntymäaika-tieto', function () {
        before(authentication.logout, etusivu.openPage)
        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('220627-833V'), wait.until(omattiedot.isVisible))

        it('Näytetään nimi ja syntymäaika', function() {
          expect(omattiedot.headerNimi()).to.equal(
            'Sylvi Syntynyt\n' +
            's. 1.1.1970'
          )
        })
      })

      describe('Virheistä raportointi', function () {
        before(authentication.logout, etusivu.openPage)
        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('180497-112F'), wait.until(omattiedot.isVisible))

        it('Aluksi ei näytetä lomaketta', function () {
          expect(omattiedot.virheraportointiForm.isVisible()).to.equal(false)
        })

        describe('Kun painetaan painiketta', function () {
          before(click(omattiedot.virheraportointiButton))

          var form = omattiedot.virheraportointiForm

          it('näytetään lista tiedoista, joita palvelussa ei pystytä näyttämään', function () {
            expect(form.contentsAsText()).to.equal(
              'Huomioithan, että Koski-palvelussa ei pystytä näyttämään seuraavia tietoja:\n' +
              'Korkeakoulututkintoja ennen vuotta 1995. Tässä voi olla korkeakoulukohtaisia poikkeuksia.\n' +
              'Ennen vuotta 1990 suoritettuja ylioppilastutkintoja.\n' +
              'Ennen vuoden 2018 tammikuuta suoritettuja peruskoulun, lukion tai ammattikoulun suorituksia ja opiskeluoikeuksia.\n' +
              'Asiani koskee tietoa, joka näkyy, tai kuuluisi yllämainitun perusteella näkyä Koski-palvelussa.'
            )
          })

          describe('Kun hyväksytään huomio palvelusta löytyvistä tiedoista', function () {
            before(form.acceptDisclaimer)

            it('näytetään oppilaitosvaihtoehdot', function () {
              expect(form.oppilaitosNames()).to.deep.equal([
                'Kulosaaren ala-aste',
                'Jyväskylän normaalikoulu',
                'Muu'
              ])
            })

            it('oppilaitoksilla on oikeat OIDit', function () {
              expect(form.oppilaitosOids()).to.deep.equal([
                '1.2.246.562.10.64353470871',
                '1.2.246.562.10.14613773812',
                'other'
              ])
            })

            it('ei vielä näytetä yhteystietoja', function () {
              expect(form.oppilaitosOptionsText()).to.equal(
                'Voit tiedustella asiaa oppilaitokseltasi.\n' +
                'Kulosaaren ala-aste\n' +
                'Jyväskylän normaalikoulu\n' +
                'Muu'
              )
            })

            describe('Kun valitaan oppilaitos, jolle löytyy sähköpostiosoite', function () {
              before(form.selectOppilaitos('1.2.246.562.10.14613773812'))

              it('näytetään sähköpostiosoite ja oppilaitoksen nimi', function () {
                expect(form.yhteystiedot()).to.equal(
                  'joku.osoite@example.com\n' +
                  'Jyväskylän normaalikoulu'
                )
              })

              it('näytetään sähköposti-painike', function () {
                expect(!!form.sähköpostiButton().length).to.equal(true)
              })

              it('näytetään yhteystiedot kopioitavana tekstinä', function () {
                expect(form.yhteystiedotTekstinä()).to.equal(
                  'Muista mainita sähköpostissa seuraavat tiedot:\n' +
                  'Nimi: Miia Monikoululainen\n' +
                  'Syntymäaika: 18.4.1997\n' +
                  'Oppijanumero: 1.2.246.562.24.00000000010' +
                  ' ' +
                  'Kopioi'
                )
              })

              it('mailto-linkissä on oikea viestipohja', function () {
                expect(form.sähköpostiButtonMailtoContents()).to.equal(
                  'mailto:joku.osoite@example.com?' +
                  'subject=Tiedustelu%20opintopolun%20tiedoista&' +
                  'body=' +
                  encodeURIComponent(
                    '***Kirjoita viestisi tähän***\n\n' +
                    '———————————————————————————————\n\n' +
                    'Allaoleva teksti on luotu automaattisesti Opintopolun tiedoista. Koulu tarvitsee näitä tietoja pystyäkseen käsittelemään kysymystäsi.\n\n' +
                    'Nimi: Miia Monikoululainen\n' +
                    'Syntymäaika: 18.4.1997\n' +
                    'Oppijanumero: 1.2.246.562.24.00000000010'
                  )
                )
              })
            })

            describe('Kun valitaan oppilaitos, jolle ei löydy sähköpostiosoitetta', function () {
              before(form.selectOppilaitos('1.2.246.562.10.64353470871'))

              it('näytetään ainoastaan virheviesti', function () {
                expect(form.oppilaitosOptionsText()).to.equal(
                  'Voit tiedustella asiaa oppilaitokseltasi.\n' +
                  'Kulosaaren ala-aste\n' +
                  'Jyväskylän normaalikoulu\n' +
                  'Muu\n' +
                  'Oppilaitokselle ei löytynyt yhteystietoja.'
                )
              })
            })

            describe("Kun valitaan 'muu'", function () {
              before(form.selectOppilaitos('other'))

              it('näytetään oppilaitos-picker', function () {
                expect(isElementVisible(form.oppilaitosPicker)).to.equal(true)
              })

              describe('Kun valitaan pickerillä oppilaitos', function () {
                before(form.selectMuuOppilaitos('Ressun lukio'))

                it('näytetään sähköpostiosoite ja oppilaitoksen nimi', function () {
                  expect(form.yhteystiedot()).to.equal(
                    'joku.osoite@example.com\n' +
                    'Ressun lukio'
                  )
                })

                it('näytetään sähköposti-painike', function () {
                  expect(!!form.sähköpostiButton().length).to.equal(true)
                })

                it('näytetään yhteystiedot kopioitavana tekstinä', function () {
                  expect(form.yhteystiedotTekstinä()).to.equal(
                    'Muista mainita sähköpostissa seuraavat tiedot:\n' +
                    'Nimi: Miia Monikoululainen\n' +
                    'Syntymäaika: 18.4.1997\n' +
                    'Oppijanumero: 1.2.246.562.24.00000000010' +
                    ' ' +
                    'Kopioi'
                  )
                })
              })
            })
          })
        })

        describe('Ylioppilastutkinnoille', function () {
          var form = omattiedot.virheraportointiForm

          describe('kun ei lukiosuorituksia', function () {
            before(authentication.logout, etusivu.openPage)
            before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('210244-374K'), wait.until(omattiedot.isVisible))
            before(click(omattiedot.virheraportointiButton), form.acceptDisclaimer)

            it('näytetään oppilaitoksissa ylioppilastutkintolautakunta, ei lukiota, lisäksi suorituksen tyyppi (ylioppilastutkinto)', function () {
              expect(form.oppilaitosNames()).to.deep.equal([
                'Ylioppilastutkintolautakunta (ylioppilastutkinto)',
                'Muu'
              ])
            })

            it('ylioppilastutkintolautakunnalla on oikea OID', function () {
              expect(form.oppilaitosOids()).to.deep.equal([
                '1.2.246.562.10.43628088406',
                'other'
              ])
            })
          })

          describe('kun myös lukiosuorituksia', function () {
            before(authentication.logout, etusivu.openPage)
            before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('080698-967F'), wait.until(omattiedot.isVisible))
            before(click(omattiedot.virheraportointiButton), form.acceptDisclaimer)

            it('näytetään oppilaitoksissa ylioppilastutkintolautakunta ja lukio', function () {
              expect(form.oppilaitosNames()).to.deep.equal([
                'Ylioppilastutkintolautakunta (ylioppilastutkinto)',
                'Jyväskylän normaalikoulu',
                'Muu'
              ])
            })

            it('oppilaitoksilla on oikeat OIDit', function () {
              expect(form.oppilaitosOids()).to.deep.equal([
                '1.2.246.562.10.43628088406',
                '1.2.246.562.10.14613773812',
                'other'
              ])
            })
          })
        })
      })

      describe('Suoritusjako', function() {
        before(authentication.logout, etusivu.openPage)
        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('180497-112F'), wait.until(omattiedot.isVisible))

        var form = omattiedot.suoritusjakoForm
        window.secrets = {}

        describe('Jakaminen', function () {
          it('Aluksi ei näytetä lomaketta', function() {
            expect(form.isVisible()).to.equal(false)
          })

          describe('Kun painetaan painiketta', function() {
            before(click(omattiedot.suoritusjakoButton))

            it('näytetään ingressi', function() {
              expect(form.ingressi()).to.equal(
                'Luomalla jakolinkin voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
                'Luotuasi linkin voit tarkistaa tarkan sisällön Esikatsele-painikkeella.'
              )
            })

            it('näytetään suoritusvaihtoehtojen otsikko', function() {
              expect(form.suoritusvaihtoehdotOtsikkoText()).to.equal('Valitse jaettavat suoritustiedot')
            })

            it('näytetään suoritusvaihtoehdot', function() {
              expect(form.suoritusvaihtoehdotText()).to.equal(
                'Kulosaaren ala-aste\n' +
                '7. vuosiluokka\n' +
                '6. vuosiluokka\n' +
                'Jyväskylän normaalikoulu\n' +
                '9. vuosiluokka\n' +
                '8. vuosiluokka'
              )
            })

            it('jakopainike on disabloitu', function() {
              expect(form.canCreateSuoritusjako()).to.equal(false)
            })
          })

          describe('Kun valitaan suoritus', function() {
            before(form.selectSuoritus(null, '1.2.246.562.10.14613773812', 'perusopetuksenvuosiluokka', '8'))

            it('jakopainike on enabloitu', function() {
              expect(form.canCreateSuoritusjako()).to.equal(true)
            })

            describe('Kun painetaan suoritusjaon luomispainiketta', function () {
              before(form.createSuoritusjako(), wait.until(form.suoritusjako(1).isVisible))

              it('suoritusjako näytetään', function() {
                var jako = form.suoritusjako(1)
                var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
                window.secrets.perusopetus = secret[secret.length - 1]

                expect(jako.isVisible()).to.equal(true)
              })

              it('suoritusjaon tiedot näytetään', function() {
                var jako = form.suoritusjako(1)

                var date = new Date()
                var targetMonth = date.getMonth() + 6
                date.setMonth(targetMonth)
                if (date.getMonth() != targetMonth) {
                  // match java.time.LocalDate.plusMonths behavior, in case e.g. today is May 31st, and
                  // November 31st doesn't exist
                  date.setDate(0)
                }

                expect(jako.url()).to.match(/^.+\/opinnot\/[0-9a-f]{32}$/)
                expect(jako.voimassaoloaika()).to.equal('' +
                  date.getDate() + '.' +
                  (date.getMonth() + 1) + '.' +
                  date.getFullYear()
                )
                expect(jako.esikatseluLinkHref()).to.equal(jako.url())
              })
            })
          })

          describe('Voimassaoloajan muuttaminen', function () {
            var date = new Date()
            date.setDate(date.getDate() + 1)
            var dateFormatted = '' + date.getDate() + '.' + (date.getMonth() + 1) + '.' + date.getFullYear()

            before(form.suoritusjako(1).setVoimassaoloaika(dateFormatted))

            it('toimii', function() {
              expect(form.suoritusjako(1).voimassaoloaika()).to.equal(dateFormatted)
            })
          })
        })

        describe('Katselu', function () {
          var suoritusjako = SuoritusjakoPage()

          before(authentication.logout, suoritusjako.openPage('perusopetus'), wait.until(suoritusjako.isVisible))

          it('linkki toimii', function () {
            expect(suoritusjako.isVisible()).to.equal(true)
          })

          describe('Sivun sisältö', function() {
            it('Näytetään otsikko, nimi ja syntymäaika', function() {
              expect(suoritusjako.headerText()).to.equal(
                'Opinnot' +
                'Miia Monikoululainen' +
                's. 18.4.1997'
              )
            })

            it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function() {
              expect(suoritusjako.opiskeluoikeudetText()).to.deep.equal([
                'Jyväskylän normaalikoulu (2008—, läsnä)'
              ])
            })

            it('Ei näytetä virheraportointi-painiketta', function() {
              expect(!!omattiedot.virheraportointiButton().length).to.equal(false)
            })

            it('Ei näytetä suoritusjako-painiketta', function() {
              expect(!!omattiedot.suoritusjakoButton().length).to.equal(false)
            })

            describe('Kun avataan oppilaitos', function () {
              before(suoritusjako.avaaOpiskeluoikeus('Jyväskylän normaalikoulu (2008—, läsnä)'))

              it('näytetään oikeat opiskeluoikeudet', function() {
                expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(1)
                expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal([
                  'Jyväskylän normaalikoulu,  (2008—, läsnä)'
                ])
              })
            })
          })

          describe('Kielen vaihto', function() {
            before(
              click(suoritusjako.changeLanguageButton),
              wait.forMilliseconds(150), // page reloads
              wait.until(function() { return isElementVisible(suoritusjako.header()) })
            )

            it('toimii', function() {
              expect(suoritusjako.headerText()).to.equal(
                'Studier' +
                'Miia Monikoululainen' +
                'f. 18.4.1997'
              )
            })

            after(click(suoritusjako.changeLanguageButton))
          })
        })

        describe('Korkeakoulusuoritukset', function () {
          before(
            authentication.logout,
            etusivu.openPage,
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login('100869-192W', 'Dippainssi', 'Dilbert'),
            wait.until(omattiedot.isVisible),
            click(omattiedot.suoritusjakoButton)
          )

          describe('Tutkintosuorituksen jakaminen', function () {
            before(
              form.selectSuoritus('1114082125', '1.2.246.562.10.56753942459', 'korkeakoulututkinto', '751101'),
              form.createSuoritusjako(),
              wait.until(form.suoritusjako(1).isVisible)
            )

            it('onnistuu', function() {
              var jako = form.suoritusjako(1)
              var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
              window.secrets.korkeakoulututkinto = secret[secret.length - 1]

              expect(jako.isVisible()).to.equal(true)
            })
          })

          describe('Irralliset opintojaksot', function () {
            describe('voidaan jakaa', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus(null, '1.2.246.562.10.56753942459', 'korkeakoulunopintojakso', null),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(2).isVisible)
              )

              it('yhtenä kokonaisuutena', function() {
                var jako = form.suoritusjako(2)
                var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
                window.secrets.korkeakoulunopintojaksot = secret[secret.length - 1]

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('ei voida', function () {
              before(form.openAdditionalSuoritusjakoForm())

              it('valita jaettaviksi yksittäin', function() {
                expect(form.suoritusvaihtoehdotText()).to.equal(
                  'Aalto-yliopisto\n' +
                  'Dipl.ins., konetekniikka ( 2013 — 2016 , päättynyt )\n' +
                  '8 opintojaksoa'
                )
              })
            })
          })

          describe('Muiden korkeakoulusuoritusten jakaminen', function () {
            before(
              authentication.logout,
              etusivu.openPage,
              etusivu.login(),
              wait.until(korhopankki.isReady),
              korhopankki.login('060458-331R', 'Korkeakoululainen', 'Kompleksi'),
              wait.until(omattiedot.isVisible),
              click(omattiedot.suoritusjakoButton)
            )

            describe('Opiskelijaliikkuvuus', function () {
              before(
                form.selectSuoritus('10065_1700969', '1.2.246.562.10.56753942459', 'muukorkeakoulunsuoritus', '8'),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(1).isVisible)
              )

              it('jakaminen onnistuu', function() {
                var jako = form.suoritusjako(1)

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('Erikoistumisopinnot', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus('1927', '1.2.246.562.10.56753942459', 'muukorkeakoulunsuoritus', '12'),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(2).isVisible)
              )

              it('jakaminen onnistuu', function() {
                var jako = form.suoritusjako(2)

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('Täydennyskoulutus', function () {
              before(
                form.openAdditionalSuoritusjakoForm(),
                form.selectSuoritus('46737839', '1.2.246.562.10.91392558028', 'muukorkeakoulunsuoritus', '10'),
                form.createSuoritusjako(),
                wait.until(form.suoritusjako(3).isVisible)
              )

              it('jakaminen onnistuu', function() {
                var jako = form.suoritusjako(3)

                expect(jako.isVisible()).to.equal(true)
              })
            })

            describe('Liittyviä opintojaksoja', function () {
              before(form.openAdditionalSuoritusjakoForm())

              it('ei voida valita jaettaviksi yksittäin', function() {
                expect(form.suoritusvaihtoehdotText()).to.equal(
                  'Aalto-yliopisto\n' +
                  'Kotimainen opiskelijaliikkuvuus ( 2017 — 2017 , päättynyt )\n' +
                  'Erikoistumisopinnot ( 2011 — 2013 , päättynyt )\n' +
                  'Jyväskylän yliopisto\n' +
                  'Täydennyskoulutus ( 2015 — 2016 , päättynyt )'
                )
              })
            })
          })

          describe('Katselu', function () {
            before(authentication.logout)

            var suoritusjako = SuoritusjakoPage()

            describe('Tutkintosuorituksen jako', function () {
              before(suoritusjako.openPage('korkeakoulututkinto'), wait.until(suoritusjako.isVisible))

              it('linkki toimii', function () {
                expect(suoritusjako.isVisible()).to.equal(true)
              })

              describe('Sivun sisältö', function() {
                it('Näytetään oikea otsikko, nimi ja syntymäaika', function() {
                  expect(suoritusjako.headerText()).to.equal(
                    'Opinnot' +
                    'Dilbert Dippainssi' +
                    's. 10.8.1969'
                  )
                })

                it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function() {
                  expect(suoritusjako.opiskeluoikeudetText()).to.deep.equal(['Aalto-yliopistoDipl.ins., konetekniikka (2013—2016, päättynyt)'])
                })

                describe('Kun avataan oppilaitos', function () {
                  before(suoritusjako.avaaOpiskeluoikeus('Aalto-yliopistoDipl.ins., konetekniikka (2013—2016, päättynyt)'))

                  it('näytetään oikeat opiskeluoikeudet', function() {
                    expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(1)
                    expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal([
                      'Aalto-yliopisto, Dipl.ins., konetekniikka (2013—2016, päättynyt)'
                    ])
                  })
                })
              })
            })

            describe('Opintojaksojen jako', function () {
              before(suoritusjako.openPage('korkeakoulunopintojaksot'), wait.until(suoritusjako.isVisible))

              it('linkki toimii', function () {
                expect(suoritusjako.isVisible()).to.equal(true)
              })

              describe('Sivun sisältö', function() {
                it('Näytetään oikea otsikko, nimi ja syntymäaika', function() {
                  expect(suoritusjako.headerText()).to.equal(
                    'Opinnot' +
                    'Dilbert Dippainssi' +
                    's. 10.8.1969'
                  )
                })

                it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function() {
                  expect(suoritusjako.opiskeluoikeudetText()).to.deep.equal(['Aalto-yliopisto8 opintojaksoa'])
                })

                describe('Kun avataan oppilaitos', function () {
                  before(suoritusjako.avaaOpiskeluoikeus('Aalto-yliopisto8 opintojaksoa'))

                  it('näytetään oikeat opiskeluoikeudet', function() {
                    expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(1)
                    expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal([
                      'Aalto-yliopisto, 8 opintojaksoa'
                    ])
                  })
                })
              })
            })
          })
        })

        describe('Peruskoulun vuosiluokan tuplaus', function () {
          before(
            authentication.logout,
            etusivu.openPage,
            etusivu.login(),
            wait.until(korhopankki.isReady),
            korhopankki.login('170186-6520', 'Luokallejäänyt', 'Lasse'),
            wait.until(omattiedot.isVisible)
          )

          describe('Suoritusvaihtoehdoissa', function () {
            before(click(omattiedot.suoritusjakoButton))

            it('näytetään tuplattu luokka vain kerran', function() {
              expect(form.suoritusvaihtoehdotText()).to.equal(
                'Jyväskylän normaalikoulu\n' +
                'Päättötodistus\n' +
                '9. vuosiluokka\n' +
                '8. vuosiluokka\n' +
                '7. vuosiluokka'
              )
            })
          })

          describe('Jakaminen', function () {
            before(
              form.selectSuoritus(null, '1.2.246.562.10.14613773812', 'perusopetuksenvuosiluokka', '7'),
              form.createSuoritusjako(),
              wait.until(form.suoritusjako(1).isVisible)
            )

            it('onnistuu', function() {
              var jako = form.suoritusjako(1)
              var secret = jako.url().split('/') // otetaan salaisuus talteen jaon hakemista varten
              window.secrets.tuplattu = secret[secret.length - 1]

              expect(jako.isVisible()).to.equal(true)
            })
          })

          describe('Katselu', function () {
            var suoritusjako = SuoritusjakoPage()

            before(authentication.logout, suoritusjako.openPage('tuplattu'), wait.until(suoritusjako.isVisible))

            it('linkki toimii', function () {
              expect(suoritusjako.isVisible()).to.equal(true)
            })

            describe('Sivun sisältö', function() {
              it('Näytetään oikea otsikko, nimi ja syntymäaika', function() {
                expect(suoritusjako.headerText()).to.equal(
                  'Opinnot' +
                  'Lasse Luokallejäänyt' +
                  's. 17.1.1986'
                )
              })

              it('Näytetään jaetut opiskeluoikeudet oppilaitoksittain', function() {
                expect(suoritusjako.opiskeluoikeudetText()).to.deep.equal(['Jyväskylän normaalikoulu (2008—2016, valmistunut)'])
              })

              describe('Kun avataan oppilaitos', function () {
                before(suoritusjako.avaaOpiskeluoikeus('Jyväskylän normaalikoulu (2008—2016, valmistunut)'))

                it('näytetään oikeat opiskeluoikeudet', function() {
                  expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(1)
                  expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienOtsikot()).to.deep.equal([
                    'Jyväskylän normaalikoulu,  (2008—2016, valmistunut)'
                  ])
                })

                it('näytetään oikea suoritus (ei luokallejäänti-suoritusta)', function() {
                  expect(opinnot.suoritusTabs('Jyväskylän normaalikoulu')).to.deep.equal(['7. vuosiluokka'])
                  expect(opinnot.opiskeluoikeusEditor().property('luokka').getValue()).to.equal('7A')
                })
              })
            })
          })
        })
      })

      describe('Kun tiedot löytyvät vain YTR:stä', function() {
        before(authentication.logout, etusivu.openPage)

        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('010342-8411'), wait.until(omattiedot.isVisible))

        describe('Sivun sisältö', function() {
          it('Näytetään opiskeluoikeudet', function() {
            expect(omattiedot.nimi()).to.equal('Mia Orvokki Numminen')
            expect(omattiedot.oppija()).to.equal('Opintoni')
            expect(opinnot.opiskeluoikeudet.oppilaitokset()).to.deep.equal([
              'Ylioppilastutkintolautakunta' ])
          })
        })
      })

      describe('Kun Virta-tietoja ei saada haettua', function() {
        before(authentication.logout, etusivu.openPage)

        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('250390-680P'), wait.until(omattiedot.isVisible))
        it('Näytetään varoitusteksti', function() {
          expect(omattiedot.nimi()).to.equal('Eivastaa Virtanen')
          expect(omattiedot.varoitukset()).to.equal('Korkeakoulujen opintoja ei juuri nyt saada haettua. Yritä myöhemmin uudestaan.')
        })
      })

      describe('Virhetilanne', function() {
        before(authentication.logout, etusivu.openPage)

        before(etusivu.login(), wait.until(korhopankki.isReady), korhopankki.login('010342-8413'), wait.until(VirhePage().isVisible))

        describe('Sivun sisältö', function() {
          it('Näytetään virhesivu', function() {
            expect(VirhePage().teksti().trim()).to.equalIgnoreNewlines('Koski-järjestelmässä tapahtui virhe, ole hyvä ja yritä myöhemmin uudelleen\n          Palaa etusivulle')
          })
        })
      })
    })
  })
})
