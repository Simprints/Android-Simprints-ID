//package com.simprints.id.data.analytics.eventData.controllers.domain
//
//import androidx.test.core.app.applicationprovider
//import androidx.test.ext.junit.runners.androidjunit4
//import androidx.test.filters.smalltest
//import com.google.common.truth.truth
//import com.nhaarman.mockito_kotlin.argumentcaptor
//import com.simprints.fingerprint.commontesttools.defaulttestconstants.default_project_id
//import com.simprints.fingerprint.commontesttools.defaulttestconstants.default_project_secret
//import com.simprints.fingerprint.commontesttools.defaulttestconstants.default_realm_key
//import com.simprints.fingerprint.commontesttools.defaulttestconstants.default_test_callout_credentials
//import com.simprints.fingerprint.commontesttools.peoplegeneratorutils
//import com.simprints.fingerprint.commontesttools.di.testappmodule
//import com.simprints.fingerprint.commontesttools.di.testpreferencesmodule
//import com.simprints.fingerprint.commontesttools.sessionevents.createfakesession
//import com.simprints.fingerprint.integration.testsnippets.*
//import com.simprints.fingerprint.testtools.androidtestconfig
//import com.simprints.fingerprint.testtools.checkloginfromintentactivitytestrule
//import com.simprints.fingerprint.testtools.state.setuprandomgeneratortogeneratekey
//import com.simprints.fingerprintscannermock.mockbluetoothadapter
//import com.simprints.fingerprintscannermock.mockfinger
//import com.simprints.fingerprintscannermock.mockscannermanager
//import com.simprints.id.application
//import com.simprints.id.fingeridentifier
//import com.simprints.id.data.db.eventdata.controllers.domain.sessioneventsmanager
//import com.simprints.id.data.db.eventdata.controllers.local.realmsessioneventsdbmanagerimpl
//import com.simprints.id.data.db.eventdata.controllers.local.sessioneventslocaldbmanager
//import com.simprints.id.data.db.eventdata.models.domain.events.artificialterminationevent
//import com.simprints.id.data.db.eventdata.models.domain.events.fingerprintcaptureevent
//import com.simprints.id.data.db.eventdata.models.domain.events.personcreationevent
//import com.simprints.id.data.db.eventdata.models.domain.session.sessionevents
//import com.simprints.id.data.db.eventdata.models.local.dbsession
//import com.simprints.id.data.db.eventdata.models.local.todomainsession
//import com.simprints.id.data.db.dbmanager
//import com.simprints.id.data.db.local.localdbmanager
//import com.simprints.id.data.db.remote.remotedbmanager
//import com.simprints.id.data.prefs.settings.settingspreferencesmanager
//import com.simprints.id.domain.fingerprint.person
//import com.simprints.id.tools.randomgenerator
//import com.simprints.id.tools.timehelper
//import com.simprints.id.tools.utils.encodingutils
//import com.simprints.testtools.android.waitingutils.ui_polling_interval_long
//import com.simprints.testtools.android.waitingutils.ui_timeout
//import com.simprints.testtools.android.tryonsystemuntiltimeout
//import com.simprints.testtools.android.waitonui
//import com.simprints.testtools.common.di.dependencyrule
//import com.simprints.testtools.common.syntax.anynotnull
//import com.simprints.testtools.common.syntax.awaitandassertsuccess
//import com.simprints.testtools.common.syntax.whenever
//import io.realm.sort
//import junit.framework.testcase.*
//import org.junit.before
//import org.junit.ignore
//import org.junit.rule
//import org.junit.test
//import org.junit.runner.runwith
//import org.mockito.mockito
//import java.util.*
//import javax.inject.inject
//
//@runwith(androidjunit4::class)
//@smalltest
//class sessioneventsmanagerimpltest { // todo : failing since sessions realm is being decrypted with wrong key
//
//    private val app = applicationprovider.getapplicationcontext<application>()
//
//    @get:rule val simprintsactiontestrule = checkloginfromintentactivitytestrule()
//
//    @inject lateinit var randomgeneratormock: randomgenerator
//    @inject lateinit var realmsessioneventsmanager: sessioneventslocaldbmanager
//    @inject lateinit var sessioneventsmanagerspy: sessioneventsmanager
//    @inject lateinit var settingspreferencesmanagerspy: settingspreferencesmanager
//    @inject lateinit var remotedbmanager: remotedbmanager
//    @inject lateinit var localdbmanager: localdbmanager
//    @inject lateinit var dbmanagerspy: dbmanager
//    @inject lateinit var timehelper: timehelper
//
//    private val preferencesmodule by lazy {
//        testpreferencesmodule(settingspreferencesmanagerrule = dependencyrule.spyrule)
//    }
//
//    private val module by lazy {
//        testappmodule(
//            app,
//            dbmanagerrule = dependencyrule.spyrule,
//            localdbmanagerrule = dependencyrule.spyrule,
//            remotedbmanagerrule = dependencyrule.spyrule,
//            remotesessionsmanagerrule = dependencyrule.spyrule,
//            sessioneventsmanagerrule = dependencyrule.spyrule,
//            scheduledsessionssyncmanagerrule = dependencyrule.mockrule,
//            randomgeneratorrule = dependencyrule.mockrule,
//            bluetoothcomponentadapterrule = dependencyrule.replacerule { mockbluetoothadapter }
//        )
//    }
//
//    private lateinit var mockbluetoothadapter: mockbluetoothadapter
//    private val realmfordataevent
//        get() = (realmsessioneventsmanager as realmsessioneventsdbmanagerimpl).getrealminstance().blockingget()
//
//    private val mostrecentsessionindb: sessionevents
//        get() {
//            realmfordataevent.refresh()
//            return realmfordataevent
//                .where(dbsession::class.java)
//                .findall()
//                .sort("starttime", sort.descending).first()!!.todomainsession()
//        }
//
//    @before
//    fun setup() {
//        androidtestconfig(this, module, preferencesmodule).fullsetup()
//
//        setuprandomgeneratortogeneratekey(default_realm_key, randomgeneratormock)
//
//        signout()
//
//        whenever(settingspreferencesmanagerspy.fingerstatus).thenreturn(mapof(
//            fingeridentifier.left_thumb to true,
//            fingeridentifier.left_index_finger to true))
//    }
//
//    @test
//    fun createsession_shouldreturnasession() {
//        val result = sessioneventsmanagerspy.createsession("app_version_name").test()
//
//        result.awaitandassertsuccess()
//        val newsession = result.values().first()
//        verifysessionisopen(newsession)
//    }
//
//    @test
//    @ignore("need to fix sessioneventsapiadapterfactory")
//    fun sessioncount_shouldbeaccurate() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//        val numberofprevioussessions = 5
//        repeat(numberofprevioussessions) { createandsaveclosesession(projectid = default_project_id, id = uuid.randomuuid().tostring()) }
//
//        launchactivityenrol(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        setupactivityandcontinue()
//        waitonui(100)
//
//        realmfordataevent.refresh()
//        with(mostrecentsessionindb) {
////            val jsonstring = sessioneventsapiadapterfactory().gson.tojson(this)
////            val jsonobject = jsonobject(jsonstring)
////            asserttrue(jsonobject.getjsonobject("databaseinfo").has("sessioncount"))
////            assertequals(numberofprevioussessions + 1, jsonobject.getjsonobject("databaseinfo").getint("sessioncount"))
//        }
//    }
//
//    @test
//    fun createsession_shouldstopprevioussessions() {
//        val oldsession = createfakesession(projectid = default_project_id, id = "oldsession")
//            .also { savesessionindb(it, realmsessioneventsmanager) }
//
//        sessioneventsmanagerspy.createsession("app_version_name").blockingget()
//        sessioneventsmanagerspy.updatesession { it.projectid = default_project_id }.blockingget()
//
//        val sessions = realmsessioneventsmanager.loadsessions(default_project_id).blockingget()
//        val oldsessionfromdb = sessions[0]
//        oldsessionfromdb.also {
//            asserttrue(it.isopen())
//        }
//        val newsessionfromdb = sessions[1]
//        newsessionfromdb.also {
//            assertequals(it.id, oldsession.id)
//            asserttrue(it.isclosed())
//            val finalevent = it.events.filterisinstance(artificialterminationevent::class.java).first()
//            assertequals(finalevent.reason, artificialterminationevent.reason.new_session)
//        }
//    }
//
//    @test
//    fun userrefusesconsent_sessionshouldnothavethelocation() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//
//        launchactivityenrol(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        setupactivityanddecline()
//        thread.sleep(100)
//
//        assertnull(mostrecentsessionindb.location)
//    }
//
//    @test
//    fun useracceptsconsent_sessionshouldhavethelocation() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//
//        launchactivityenrol(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        setupactivityandcontinue()
//        thread.sleep(100)
//
//        assertnotnull(mostrecentsessionindb.location)
//    }
//
//    @test
//    fun anerrorwithevents_shouldbeswallowed() {
//        realmsessioneventsmanager.deletesessions()
//
//        // there is not activesession open or pending in the db. so it should fail, but it swallows the reason
//        sessioneventsmanagerspy.updatesession {
//            it.location = null
//        }.test().awaitandassertsuccess()
//    }
//
//    @test
//    fun enrol_shouldgeneratetherightevents() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//
//        // launch and sign in
//        launchactivityenrol(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        // once signed in proceed to enrol person1
//        fullhappyworkflow()
//        collectfingerprintsenrolmentcheckfinished(simprintsactiontestrule)
//
//        realmfordataevent.refresh()
//        verifyeventsafterenrolment(mostrecentsessionindb.events, realmfordataevent)
//    }
//
//    @test
//    fun launchsimprints_shouldgeneratetherightevents() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//
//        // launch
//        launchactivityenrol(default_test_callout_credentials, simprintsactiontestrule)
//
//        tryonsystemuntiltimeout(ui_timeout, ui_polling_interval_long) {
//            verifyeventswhensimprintsislaunched(mostrecentsessionindb.events)
//        }
//    }
//
//    @test
//    fun login_shouldgeneratetherightevents() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//
//        // launch and sign in
//        launchactivityenrol(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret + "wrong")
//        presssignin()
//        thread.sleep(6000)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        setupactivityandcontinue()
//
//        verifyeventsforfailedsignedidfollowedbysucceedsignin(mostrecentsessionindb.events)
//    }
//
//    @test
//    fun verify_shouldgeneratetherightevents() {
//        val guid = "123e4567-e89b-12d3-a456-426655440000"
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//
//        mocklocaltoaddfakepersonafterlogin(guid)
//
//        launchactivityverify(default_test_callout_credentials, simprintsactiontestrule, guid)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        fullhappyworkflow()
//        matchingactivityverificationcheckfinished(simprintsactiontestrule)
//
//        verifyeventsafterverification(mostrecentsessionindb.events, realmfordataevent)
//    }
//
//    @test
//    fun identify_shouldgeneratetherightevents() {
//        val guid = "123e4567-e89b-12d3-a456-426655440000"
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(*mockfinger.person1twofingersgoodscan)))
//        mocklocaltoaddfakepersonafterlogin(guid)
//
//        launchactivityidentify(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        fullhappyworkflow()
//        matchingactivityidentificationcheckfinished(simprintsactiontestrule)
//
//        verifyeventsafteridentification(mostrecentsessionindb.events, realmfordataevent)
//    }
//
//    @test
//    fun multiplescans_shouldgenerateacreatepersoneventwithrighttemplates() {
//        mockbluetoothadapter = mockbluetoothadapter(mockscannermanager(mockfingers = arrayof(
//            mockfinger.person_1_version_1_left_thumb_bad_scan,
//            mockfinger.person_1_version_2_left_index_good_scan,
//            mockfinger.person_1_version_1_left_thumb_good_scan,
//            mockfinger.person_1_version_1_left_index_good_scan)))
//
//        launchactivityidentify(default_test_callout_credentials, simprintsactiontestrule)
//        entercredentialsdirectly(default_test_callout_credentials, default_project_secret)
//        presssignin()
//        setupactivityandcontinue()
//        collectfingerprintspressscan()
//        skipfinger()
//        waitforsplashscreenappearsanddisappears()
//        collectfingerprintspressscan()
//        collectfingerprintspressscan()
//        checkifdialogisdisplayedwithresultandclickconfirm("× left thumb\n✓ left index finger\n✓ right thumb\n")
//        matchingactivityidentificationcheckfinished(simprintsactiontestrule)
//
//        val personcreatedarg = argumentcaptor<person>()
//        mockito.verify(sessioneventsmanagerspy, mockito.times(1)).addpersoncreationeventinbackground(personcreatedarg.capture())
//
//        val eventsinmostrecentsession = mostrecentsessionindb.events
//        val personcreatedformatchingactivity = personcreatedarg.firstvalue
//        val personcreationevent = eventsinmostrecentsession.filterisinstance(personcreationevent::class.java)[0]
//        val usefultemplatesfromevents = eventsinmostrecentsession
//            .filterisinstance(fingerprintcaptureevent::class.java)
//            .filter { it.id in personcreationevent.fingerprintcaptureids }
//            .map { it.fingerprint?.template }
//
//        truth.assertthat(usefultemplatesfromevents)
//            .containsexactlyelementsin(personcreatedformatchingactivity.fingerprints.map {
//                encodingutils.bytearraytobase64(it.templatebytes)
//            })
//
//        val skippedfingercaptureevent = eventsinmostrecentsession
//            .filterisinstance(fingerprintcaptureevent::class.java)
//            .findlast { it.result == fingerprintcaptureevent.result.skipped }
//
//        assertnotnull(skippedfingercaptureevent)
//    }
//
//    private fun mocklocaltoaddfakepersonafterlogin(guid: string) {
//        whenever(dbmanagerspy) { signin(anynotnull(), anynotnull(), anynotnull()) } then {
//            it.callrealmethod()
//            localdbmanager.insertorupdatepersoninlocal(peoplegeneratorutils.getrandomperson(patientid = guid))
//                .onerrorcomplete().blockingawait()
//        }
//    }
//
//    private fun createandsaveclosesession(projectid: string = default_project_id, id: string) =
//        createandsaveclosefakesession(timehelper, realmsessioneventsmanager, projectid, id)
//
//    private fun signout() {
//        remotedbmanager.signoutofremotedb()
//    }
//}
