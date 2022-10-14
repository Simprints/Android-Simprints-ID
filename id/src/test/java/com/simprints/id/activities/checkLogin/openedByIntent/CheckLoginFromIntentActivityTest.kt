package com.simprints.id.activities.checkLogin.openedByIntent

import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.id.R
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.moduleApi.AppConfirmationConfirmIdentityRequestModuleApi
import com.simprints.id.testtools.moduleApi.AppEnrolRequestModuleApi
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createAndStartActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CheckLoginFromIntentActivityTest {


    @Test
    fun `confirmationText should be visible in AppConfirmIdentity Requests`() {
        createAndStartActivity<CheckLoginFromIntentActivity>(bundleForAppConfirmIdentityRequest())
        verifyConfirmationUIVisibility(ViewMatchers.Visibility.VISIBLE)
    }

    @Test
    fun `confirmationText should be hidden in any other type of Requests`() {
        createAndStartActivity<CheckLoginFromIntentActivity>(bundleForAppEnrolRequest())
        verifyConfirmationUIVisibility(ViewMatchers.Visibility.GONE)

    }

    private fun verifyConfirmationUIVisibility(expectedVisibility: ViewMatchers.Visibility) {
        onView(withId(R.id.confirmationSent)).check(
            matches(withEffectiveVisibility(expectedVisibility))
        )
        onView(withId(R.id.redirectingBack)).check(
            matches(withEffectiveVisibility(expectedVisibility))
        )

    }

    private fun bundleForAppConfirmIdentityRequest() = Bundle().apply {
        putParcelable(
            IAppRequest.BUNDLE_KEY,
            AppConfirmationConfirmIdentityRequestModuleApi(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                GUID1,
                GUID2
            )
        )
    }

    private fun bundleForAppEnrolRequest() = Bundle().apply {
        putParcelable(
            IAppRequest.BUNDLE_KEY,
            AppEnrolRequestModuleApi(
                DEFAULT_PROJECT_ID,
                DEFAULT_USER_ID,
                DEFAULT_MODULE_ID,
                DEFAULT_METADATA
            )
        )
    }
}
