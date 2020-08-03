package com.simprints.id.data.db.event.remote

//@RunWith(AndroidJUnit4::class)
//@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
//class EventRemoteDataSourceImplTest {
//
//    private val timeHelper: TimeHelper = TimeHelperImpl()
//
//    @MockK
//    lateinit var simApiClientFactory: SimApiClientFactory
//    @MockK
//    lateinit var simApiClient: SimApiClient<EventRemoteInterface>
//    lateinit var eventRemoteDataSource: EventRemoteDataSource
//
//    @Before
//    @ExperimentalCoroutinesApi
//    fun setUp() {
//        MockKAnnotations.init(this, relaxed = true)
//        UnitTestConfig(this).setupFirebase()
//
//        coEvery { simApiClientFactory.buildClient(EventRemoteInterface::class) } returns simApiClient
//        eventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory)
//    }

//    @Test
//    fun successfulResponseOnUpload() {
//        runBlocking {
//
//            val sessions = listOf(
//                createFakeClosedSession(timeHelper),
//                createFakeClosedSession(timeHelper)
//            )
//
//            eventRemoteDataSource.uploadSessions("projectId", sessions)
//
//            coVerify(exactly = 1) { simApiClient.executeCall(any(), any()) }
//        }
//    }
//}
