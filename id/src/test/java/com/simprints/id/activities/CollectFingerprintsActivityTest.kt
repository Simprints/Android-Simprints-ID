package com.simprints.id.activities

// Attempt to write unit test for scanner with roboeletric
//
//
//@RunWith(RobolectricTestRunner::class)
//@Config(application = TestApplication::class, shadows = [ShadowViewPager::class])
//class CollectFingerprintsActivityTest : RxJavaTest, DaggerForTests() {
//
//    @Inject
//    lateinit var preferencesManager: PreferencesManager
//    @Inject
//    lateinit var simNetworkUtilsMock: SimNetworkUtils
//    @Inject
//    lateinit var appState: AppState
//
//    @Inject
//    lateinit var sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager
//
//    override var module by lazyVar {
//        AppModuleForTests(app,
//            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter },
//            localDbManagerRule = MockRule,
//            sessionEventsLocalDbManagerRule = MockRule,
//            simNetworkUtilsRule = MockRule)
//    }
//
//    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
//
//    @Before
//    override fun setUp() {
//        FirebaseApp.initializeApp(RuntimeEnvironment.application)
//        app = (RuntimeEnvironment.application as TestApplication)
//        super.setUp()
//        testAppComponent.inject(this)
//
//        whenever(simNetworkUtilsMock.mobileNetworkType).thenReturn("LTE")
//        whenever(simNetworkUtilsMock.connectionsStates).thenReturn(listOf(SimNetworkUtils.Connection("WIFI", NetworkInfo.DetailedState.CONNECTED)))
//
//        setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManagerMock)
//        ShadowLog.stream = System.out
//    }
//
//    @Test
//    @Config(sdk = [21])
//    fun test() {
//
//        val test = CompletableFuture<Boolean>()
//        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN)))
//
//        preferencesManager.scheduledPeopleSyncWorkRequestId = "request_id"
//        preferencesManager.scheduledSessionsSyncWorkRequestId = "request_id"
//
//        val controller = createRoboActivity<LaunchActivity>().start().resume().visible()
//        val activity = controller.get()
//
//        val consentButton = activity.findViewById<Button>(R.id.consentAcceptButton)
//        Assert.assertEquals(true, test.get())
//    }
//
//    @Test
//    @Config(sdk = [27])
//    fun threeBadScanAndNotMaxReached_thenAddAFinger() {
//
//        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN,
//            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN)))
//
//        appState.scanner = spy(Scanner("F0:AC:D7:C8:CB:22", mockBluetoothAdapter))
//
//        appState.scanner.connect( object : ScannerCallback {
//            override fun onSuccess() {
//
//                appState.scanner.un20Wakeup(object : ScannerCallback {
//                    override fun onFailure(error: SCANNER_ERROR?) {
//                        Log.d("Test", "Scanner onFailure: $error")
//                    }
//                    override fun onSuccess() {
//                        Log.d("Test", "Scanner success un20Wakeup")
//                    }
//                })
//            }
//
//            override fun onFailure(error: SCANNER_ERROR?) {
//                Log.d("Test", "Scanner onFailure: $error")
//            }
//        })
//
//        preferencesManager.calloutAction = CalloutAction.VERIFY
//
//        val controller = createRoboActivity<CollectFingerprintsActivity>().start().resume().visible()
//        val activity = controller.get()
//
//        activity.findViewById<Button>(R.id.scan_button).apply {
//            performClick()
//            Thread.sleep(10000)
//
//            performClick()
//            Thread.sleep(10000)
//
//            performClick()
//            Thread.sleep(10000)
//        }
//
//        activity.findViewById<ViewPagerCustom>(R.id.view_pager).also {
//            Assert.assertEquals(it.currentItem, 0)
//        }
//    }
//}
