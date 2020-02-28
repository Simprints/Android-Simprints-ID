package com.simprints.id.services

//TODO: Test only GuidSelectionManager. Currently it's testing SessionEventsLocalDbManager.
//@RunWith(AndroidJUnit4::class)
//@LargeTest
//class GuidSelectionManagerTest {
//
//    private val app = ApplicationProvider.getApplicationContext<Application>()
//
//    private val module by lazy {
//        TestAppModule(app,
//            randomGeneratorRule = DependencyRule.ReplaceRule { mock<RandomGenerator>().apply { setupRandomGeneratorToGenerateKey(this) } },
//            sessionEventsLocalDbManagerRule = DependencyRule.MockRule,
//            crashReportManagerRule = DependencyRule.MockRule,
//            secureDataManagerRule = DependencyRule.SpyRule,
//            remoteDbManagerRule = DependencyRule.SpyRule)
//    }
//
//    @Inject lateinit var sessionRepository: SessionRepository
//    @Inject lateinit var loginInfoManager: LoginInfoManager
//    @Inject lateinit var realmSessionEventsManagerMock: SessionEventsLocalDbManager
//    @Inject lateinit var guidSelectionManager: GuidSelectionManager
//    @Inject lateinit var secureDataManagerSpy: SecureDataManager
//    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
//
//    private var sessionsInFakeDb = mutableListOf<SessionEvents>()
//
//    @Before
//    fun setUp() {
//        AndroidTestConfig(this, module).fullSetup()
//
//        LoginStateMocker.setupLoginStateFullyToBeSignedIn(
//            app.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE),
//            secureDataManagerSpy,
//            remoteDbManagerSpy,
//            DEFAULT_PROJECT_ID,
//            DEFAULT_USER_ID,
//            DEFAULT_PROJECT_SECRET,
//            LocalDbKey(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_REALM_KEY),
//            "token")
//
//        mockSessionEventsManager(realmSessionEventsManagerMock, sessionsInFakeDb)
//    }
//
//    @Test
//    fun testWithStartedService() {
//        var session = sessionRepository.createSession("").blockingGet()
//
//        sessionRepository.updateSession {
//            it.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
//        }.blockingGet()
//
//        val request = AppIdentityConfirmationRequest(
//            DEFAULT_PROJECT_ID,
//            session.id,
//            "some_guid_confirmed")
//
//        guidSelectionManager
//            .handleIdentityConfirmationRequest(request)
//            .test()
//            .awaitAndAssertSuccess()
//
//        session = realmSessionEventsManagerMock.loadSessionById(session.id).blockingGet()
//        val potentialGuidSelectionEvent = session.events.filterIsInstance(GuidSelectionEvent::class.java).firstOrNull()
//        Assert.assertNotNull(potentialGuidSelectionEvent)
//        Assert.assertEquals(potentialGuidSelectionEvent?.selectedId, "some_guid_confirmed")
//    }
//}
