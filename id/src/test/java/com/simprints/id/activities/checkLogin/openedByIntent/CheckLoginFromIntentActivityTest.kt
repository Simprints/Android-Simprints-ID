package com.simprints.id.activities.checkLogin.openedByIntent

// for O_MR1 = 27, Roboletric hangs around after it calls SharedPrefs.'apply' and then it accesses to
// the SharedPrefs again. https://github.com/robolectric/robolectric/issues/3641
//@RunWith(AndroidJUnit4::class)
//@Config(
//    application = TestApplication::class,
//    sdk = [Build.VERSION_CODES.N_MR1], shadows = [ShadowAndroidXMultiDex::class])
//StopShip: the CheckLogin doesn't manage the activities flow anymore, so these tests are out of date.
//class CheckLoginFromIntentActivityTest {

//    companion object {
//        const val DEFAULT_PROJECT_ID = "some_project_id"
//        const val DEFAULT_PROJECT_SECRET = "some_project_secret"
//        const val DEFAULT_REALM_KEY = "enc_some_key"
//        const val DEFAULT_USER_ID = "some_user_id"
//        const val DEFAULT_LEGACY_API_KEY = "96307ff9-873b-45e0-8ef0-b2efd5bef12d"
//        private const val DEFAULT_MODULE_ID = "some_module_id"
//
//        @Parcelize
//        private data class AppEnrollRequest(
//            override val projectId: String,
//            override val userId: String,
//            override val moduleId: String,
//            override val metadata: String
//        ) : IAppEnrollRequest {
//            fun toIntent() = Intent(RequestAction.ENROL.toString())
//                .apply { putExtra(IAppRequest.BUNDLE_KEY, this@AppEnrollRequest) }
//        }
//
//        private val defaultRequest = AppEnrollRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metaData")
//    }
//
//    private val app = ApplicationProvider.getApplicationContext() as TestApplication
//
//    private lateinit var sharedPrefs: SharedPreferences
//
//    @Inject lateinit var sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager
//    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
//    @Inject lateinit var localDbManagerMock: LocalDbManager
//    @Inject lateinit var crashReportManagerMock: CrashReportManager
//    @Inject lateinit var dbManager: DbManager
//
//    private val module by lazy {
//        TestAppModule(app,
//            crashReportManagerRule = MockRule,
//            localDbManagerRule = MockRule,
//            remoteDbManagerRule = MockRule,
//            scheduledSessionsSyncManagerRule = MockRule,
//            sessionEventsLocalDbManagerRule = MockRule,
//            keystoreManagerRule = ReplaceRule { setupFakeKeyStore() })
//    }
//
//    @Before
//    fun setUp() {
//        UnitTestConfig(this, module).fullSetup()
//
//        sharedPrefs = getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME)
//
//        RobolectricTestMocker
//            .setupLocalAndRemoteManagersForApiTesting(localDbManagerMock, remoteDbManagerMock, sessionEventsLocalDbManagerMock)
//            .initLogInStateMock(sharedPrefs, remoteDbManagerMock)
//
//        dbManager.initialiseDb()
//    }
//
//    @Test
//    fun unknownCallingAppSource_shouldLogEvent() {
//        buildActivity(CheckLoginFromIntentActivityWithInvalidCallingPackage::class.java).setup()
//        verifyExceptionWasThrownAlongWithNoSessionAndNoRequestException(3)
//    }
//
//    @Test
//    fun knownCallingAppSource_shouldNotLogEvent() {
//        val pm = app.packageManager
//        pm.setInstallerPackageName("com.app.installed.from.playstore", "com.android.vending")
//
//        buildActivity(CheckLoginFromIntentActivityWithValidCallingPackage::class.java).setup()
//        verifyExceptionWasThrownAlongWithNoSessionAndNoRequestException(2)
//    }
//
//    private fun verifyExceptionWasThrownAlongWithNoSessionAndNoRequestException(times: Int) {
//        verifyExactly(times, crashReportManagerMock) { logExceptionOrSafeException(anyNotNull()) }
//    }
//
//    @Test
//    fun invalidParams_shouldAlertActComeUp() {
//
//        val controller = createRoboCheckLoginFromIntentViewActivity(Intent()).start()
//        val activity = controller.get() as CheckLoginFromIntentActivity
//        controller.visible()
//
//        assertActivityStarted(AlertActivity::class.java, activity)
//    }
//
//    @Test
//    fun userIsLogged_shouldLaunchActComeUp() {
//
//        setUserLogInState(true, sharedPrefs)
//        val checkLoginIntent = createACallingAppIntentWithLegacyApiKey(defaultRequest)
//        val activity = startCheckLoginFromIntentActivity(checkLoginIntent)
//
//        assertActivityStarted(CheckLoginFromIntentActivity.launchActivityClassName, activity)
//    }
//
//    @Test
//    fun userIsNotLogged_shouldLoginActComeUp() {
//
//        setUserLogInState(false, sharedPrefs)
//        val checkLoginIntent = defaultRequest.toIntent()
//        val activity = startCheckLoginFromIntentActivity(checkLoginIntent)
//
//        assertActivityStarted(LoginActivity::class.java, activity)
//    }
//
//    @Test
//    fun userIsNotLoggedAndCallingAppUsesApiLegacyKey_shouldLoginActComeUp() {
//
//        setUserLogInState(false, sharedPrefs)
//        val checkLoginIntent = createACallingAppIntentWithLegacyApiKey(defaultRequest)
//        val activity = startCheckLoginFromIntentActivity(checkLoginIntent)
//        val sActivity = shadowOf(activity)
//        val intent = sActivity.nextStartedActivity
//        assertActivityStarted(LoginActivity::class.java, intent)
//    }
//
//    @Test
//    fun normalFlowReturnsAResult_shouldForwardItBackToCallingApp() {
//        setUserLogInState(true, sharedPrefs)
//        val loginIntent = defaultRequest.toIntent()
//        val activity = startCheckLoginFromIntentActivity(loginIntent)
//        val sActivity = shadowOf(activity)
//
//        assertFalse(activity.isFinishing)
//        val intent = sActivity.nextStartedActivity
//        assertActivityStarted(CheckLoginFromIntentActivity.launchActivityClassName, intent)
//
//        val returnIntent = Intent().putExtra(Response.BUNDLE_KEY, EnrolResponse("someGuid"))
//        sActivity.receiveResult(
//            intent,
//            Activity.RESULT_OK,
//            returnIntent)
//
//        assertTrue(activity.isFinishing)
//        assertEquals(Activity.RESULT_OK, sActivity.resultCode)
//        assertNotNull(sActivity.resultIntent.getParcelableExtra(IClientApiResponse.BUNDLE_KEY))
//    }
//
//    @Test
//    fun loginSucceed_shouldOpenLaunchActivity() {
//        setUserLogInState(false, sharedPrefs)
//        val loginIntent = defaultRequest.toIntent()
//        val controller = createRoboCheckLoginFromIntentViewActivity(loginIntent).start()
//        val activity = controller.get() as CheckLoginFromIntentActivity
//        controller.resume().visible()
//        val sActivity = shadowOf(activity)
//
//        assertFalse(activity.isFinishing)
//        val intent = sActivity.nextStartedActivity
//        assertActivityStarted(LoginActivity::class.java, intent)
//
//        setUserLogInState(true, sharedPrefs)
//
//        sActivity.receiveResult(
//            intent,
//            LoginActivity.LOGIN_SUCCEED,
//            Intent())
//
//        controller.resume()
//        assertActivityStarted(CheckLoginFromIntentActivity.launchActivityClassName, sActivity)
//        assertFalse(activity.isFinishing)
//    }
//
//    @Test
//    fun loginFailed_shouldCloseApp() {
//        setUserLogInState(false, sharedPrefs)
//        val loginIntent = defaultRequest.toIntent()
//        val controller = createRoboCheckLoginFromIntentViewActivity(loginIntent).start()
//        val activity = controller.get() as CheckLoginFromIntentActivity
//        controller.resume().visible()
//        val sActivity = shadowOf(activity)
//
//        assertFalse(activity.isFinishing)
//        val intent = sActivity.nextStartedActivity
//        assertActivityStarted(LoginActivity::class.java, intent)
//
//        sActivity.receiveResult(
//            intent,
//            Activity.RESULT_CANCELED,
//            Intent())
//
//        controller.resume()
//        assertTrue(activity.isFinishing)
//    }
//
//    private fun startCheckLoginFromIntentActivity(intent: Intent): CheckLoginFromIntentActivity {
//
//        val controller = createRoboCheckLoginFromIntentViewActivity(intent).start()
//        val activity = controller.get() as CheckLoginFromIntentActivity
//        controller.resume().visible()
//        return activity
//    }
//
//    private fun createRoboCheckLoginFromIntentViewActivity(intent: Intent) =
//        createActivity<CheckLoginFromIntentActivity>(intent)
//
//    private fun createACallingAppIntentWithLegacyApiKey(request: AppEnrollRequest,
//                                                        legacyApiKey: String = DEFAULT_LEGACY_API_KEY): Intent =
//        request.toIntent().also { it.putExtra("apiKey", legacyApiKey) }
//
//    class CheckLoginFromIntentActivityWithInvalidCallingPackage : CheckLoginFromIntentActivity() {
//
//        override fun getCallingPackageName(): String {
//            return "com.app.installed.manually"
//        }
//    }
//
//    class CheckLoginFromIntentActivityWithValidCallingPackage : CheckLoginFromIntentActivity() {
//
//        override fun getCallingPackageName(): String {
//            return "com.app.installed.from.playstore"
//        }
//    }
//}
