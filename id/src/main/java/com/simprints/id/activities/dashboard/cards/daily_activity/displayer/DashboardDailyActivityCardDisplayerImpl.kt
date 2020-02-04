package com.simprints.id.activities.dashboard.cards.daily_activity.displayer

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper

class DashboardDailyActivityCardDisplayerImpl(
    private val timeHelper: TimeHelper,
    private val androidResourcesHelper: AndroidResourcesHelper
) : DashboardDailyActivityCardDisplayer {

    private lateinit var root: ViewGroup

    override fun initRoot(rootLayout: ViewGroup) {
        root = rootLayout
    }

    override fun displayDailyActivityState(dailyActivityState: DashboardDailyActivityState) {
        with(root) {
            if (dailyActivityState.hasNoActivity()) {
                visibility = GONE
            } else {
                setTitle()
                setEnrolmentsCount(dailyActivityState.enrolments)
                setIdentificationsCount(dailyActivityState.identifications)
                setVerificationsCount(dailyActivityState.verifications)
            }
        }
    }

    private fun View.setTitle() {
        val date = timeHelper.getCurrentDateAsString()
        val text = androidResourcesHelper.getString(R.string.dashboard_card_activity, arrayOf(date))
        findViewById<TextView>(R.id.dashboard_daily_activity_card_title).text = text
    }

    private fun View.setEnrolmentsCount(enrolmentsCount: Int) {
        findViewById<Group>(R.id.group_enrolments).visibility = VISIBLE
        findViewById<TextView>(
            R.id.dashboard_daily_activity_card_enrolments_count
        ).text = "$enrolmentsCount"
    }

    private fun View.setIdentificationsCount(identificationsCount: Int) {
        findViewById<Group>(R.id.group_identifications).visibility = VISIBLE
        findViewById<TextView>(
            R.id.dashboard_daily_activity_card_identifications_count
        ).text = "$identificationsCount"
    }

    private fun View.setVerificationsCount(verificationsCount: Int) {
        findViewById<Group>(R.id.group_verifications).visibility = VISIBLE
        findViewById<TextView>(
            R.id.dashboard_daily_activity_card_verifications_count
        ).text = "$verificationsCount"
    }

}
