package com.simprints.id.activities.checklogin.openedbyintent

// TODO: Fix android tests when we modularize the UI components

//@RunWith(AndroidJUnit4::class)
//@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
//class CheckLoginFromIntentActivityTest {
//
//
//    @Test
//    fun `confirmationText should be visible in AppConfirmIdentity Requests`() {
//        createAndStartActivity<CheckLoginFromIntentActivity>(bundleForAppConfirmIdentityRequest())
//        verifyConfirmationUIVisibility(ViewMatchers.Visibility.VISIBLE)
//    }
//
//    @Test
//    fun `confirmationText should be hidden in any other type of Requests`() {
//        createAndStartActivity<CheckLoginFromIntentActivity>(bundleForAppEnrolRequest())
//        verifyConfirmationUIVisibility(ViewMatchers.Visibility.GONE)
//
//    }
//
//    private fun verifyConfirmationUIVisibility(expectedVisibility: ViewMatchers.Visibility) {
//        onView(withId(R.id.confirmationSent)).check(
//            matches(withEffectiveVisibility(expectedVisibility))
//        )
//        onView(withId(R.id.redirectingBack)).check(
//            matches(withEffectiveVisibility(expectedVisibility))
//        )
//
//    }
//
//    private fun bundleForAppConfirmIdentityRequest() = Bundle().apply {
//        putParcelable(
//            IAppRequest.BUNDLE_KEY,
//            AppConfirmationConfirmIdentityRequestModuleApi(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                GUID1,
//                GUID2
//            )
//        )
//    }
//
//    private fun bundleForAppEnrolRequest() = Bundle().apply {
//        putParcelable(
//            IAppRequest.BUNDLE_KEY,
//            AppEnrolRequestModuleApi(
//                DEFAULT_PROJECT_ID,
//                DEFAULT_USER_ID,
//                DEFAULT_MODULE_ID,
//                DEFAULT_METADATA
//            )
//        )
//    }
//}
