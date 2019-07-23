package com.simprints.clientapi.integration.standard.requests

//Uncomment it when the GUID confirmation Service gets replaced with an Activity
//@RunWith(AndroidJUnit4::class)
//class ConfirmationIdentityRequestTest : KoinTest {
//
//private val intentResultOk = Instrumentation.ActivityResult(Activity.RESULT_OK, null)

//@Before
//fun setUp() {
//    Intents.init()
//    Intents.intending(IntentMatchers.hasAction(APP_CONFIRM_ACTION)).respondWith(intentResultOk)
////
//        KoinInjector.loadClientApiKoinModules()
//        declare {
//            factory { buildDummySessionEventsManagerMock() }
//        }
//    }

//    @Test
//    fun aConfirmIdentityRequest_shouldGenerateAnAppConfirmIdentityRequest() {
//        ActivityScenario.launch<CommCareActivity>(baseIntentRequest.apply {
//            action = standardConfirmIdentityAction
//            putExtra(sessionIdField.key(), sessionIdField.value())
//            putExtra(selectedGuidField.key(), selectedGuidField.value())
//        })
//
//        val expectedAppRequest = AppIdentifyConfirmationRequest(
//            projectIdField.value(),
//            sessionIdField.value(),
//            selectedGuidField.value())
//
//        intended(hasAction(APP_CONFIRM_ACTION))
//        intended(hasExtras(hasEntry(IAppRequest.BUNDLE_KEY, getMatcherFor(expectedAppRequest))))
//    }
//
//    @After
//    fun tearDown() {
//        Intents.release()
//    }
//}
