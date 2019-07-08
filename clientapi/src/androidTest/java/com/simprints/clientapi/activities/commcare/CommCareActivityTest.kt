package com.simprints.clientapi.activities.commcare

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.clientapi.activities.robots.commCare
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.libsimprints.Constants.*
import com.simprints.testtools.android.BaseActivityTest
import com.simprints.testtools.common.syntax.verifyOnce
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.declareMock

@SmallTest
@RunWith(AndroidJUnit4::class)
class CommCareActivityTest : BaseActivityTest<CommCareActivity>(CommCareActivity::class), KoinTest {

    private val mod = module(override = true) {
        factory { (context: Context) ->
            CommCareGuidSelectionNotifier(context)
        }
    }

    private val guidSelectionNotifier by inject<OdkGuidSelectionNotifier> {
        parametersOf(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    override fun setUp() {
        super.setUp()
        startKoin { listOf(mod) }
        declareMock<CommCareGuidSelectionNotifier>()
    }

    @Test
    fun withConfirmIdentityIntent_shouldDisplayCorrectToastMessage() {
        commCare {
        } assert {
            verifyOnce(guidSelectionNotifier) {
                showMessage()
            }
        }
    }

    override fun intent(): Intent {
        return super.intent()
            .setAction(CommCarePresenter.ACTION_CONFIRM_IDENTITY)
            .putExtra(SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

}
