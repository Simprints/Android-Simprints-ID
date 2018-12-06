package com.simprints.id.activities.dashboard

import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModelHelper
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DashboardSyncCardViewModelHelperTest {

//    fun test(){
//
//        val helper = DashboardSyncCardViewModelHelper
//    }
}
