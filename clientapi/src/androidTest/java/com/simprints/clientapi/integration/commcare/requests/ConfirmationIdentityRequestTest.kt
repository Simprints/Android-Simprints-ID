package com.simprints.clientapi.integration.commcare.requests

//Uncomment it when the GUID confirmation Service gets replaced with an Activity
//@RunWith(AndroidJUnit4::class)
//class ConfirmationIdentityRequestTest : KoinTest {
//
//    @Before
//    fun setUp() {
//        Intents.init()
//
//        KoinInjector.loadClientApiKoinModules()
//        declare {
//            factory { buildDummySessionEventsManagerMock() }
//        }
//    }

//    @Test
//    fun aConfirmIdentityRequest_shouldGenerateAnAppConfirmIdentityRequest() {
//        ActivityScenario.launch<CommCareActivity>(baseIntentRequest.apply {
//            action = commcareConfirmIdentityAction
//            putExtra(sessionIdField.key(), sessionIdField.value())
//            putExtra(selectedGuidField.key(), selectedGuidField.value())
//        })
//
//        val expectedAppRequest = AppIdentifyConfirmationRequest(
//            projectIdField.value(),
//            sessionIdField.value(),
//            selectedGuidField.value())
//
//        intended(hasAction(appConfirmIdentityAction))
//        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, getMatcherFor(expectedAppRequest))))
//    }
//
//    @After
//    fun tearDown() {
//        Intents.release()
//    }
//}
