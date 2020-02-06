package com.simprints.id.activities.dashboard.cards.daily_activity.displayer

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.simprints.core.tools.extentions.nand
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import kotlinx.android.synthetic.main.activity_dashboard_card_daily_activity.view.*

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
            setTitle()
            setEnrolmentsCount(dailyActivityState.enrolments)
            setIdentificationsCount(dailyActivityState.identifications)
            setVerificationsCount(dailyActivityState.verifications)
            setDividers(dailyActivityState)
        }
    }

    private fun View.setTitle() {
        val date = timeHelper.getCurrentDateAsString()
        val text = androidResourcesHelper.getString(R.string.dashboard_card_activity, arrayOf(date))
        findViewById<TextView>(R.id.dashboard_daily_activity_card_title).text = text
    }

    private fun View.setEnrolmentsCount(enrolmentsCount: Int) {
        val enrolmentsGroup = findViewById<Group>(R.id.group_enrolments)

        if (enrolmentsCount > 0) {
            findViewById<TextView>(
                R.id.dashboard_daily_activity_card_enrolments_count
            ).text = "$enrolmentsCount"

            val labelText = androidResourcesHelper.getQuantityString(
                R.plurals.dashboard_card_enrolments,
                enrolmentsCount
            )

            findViewById<TextView>(R.id.enrolments_label).text = labelText
            enrolmentsGroup.visibility = VISIBLE
        } else {
            enrolmentsGroup.visibility = GONE
        }
    }

    private fun View.setIdentificationsCount(identificationsCount: Int) {
        val identificationsGroup = findViewById<Group>(R.id.group_identifications)

        if (identificationsCount > 0) {
            findViewById<TextView>(
                R.id.dashboard_daily_activity_card_identifications_count
            ).text = "$identificationsCount"

            val labelText = androidResourcesHelper.getQuantityString(
                R.plurals.dashboard_card_identifications,
                identificationsCount
            )

            findViewById<TextView>(R.id.identifications_label).text = labelText

            identificationsGroup.visibility = VISIBLE
        } else {
            identificationsGroup.visibility = GONE
        }
    }

    private fun View.setVerificationsCount(verificationsCount: Int) {
        val verificationsGroup = findViewById<Group>(R.id.group_verifications)

        if (verificationsCount > 0) {
            findViewById<TextView>(
                R.id.dashboard_daily_activity_card_verifications_count
            ).text = "$verificationsCount"

            val labelText = androidResourcesHelper.getQuantityString(
                R.plurals.dashboard_card_verifications,
                verificationsCount
            )

            findViewById<TextView>(R.id.verifications_label).text = labelText

            verificationsGroup.visibility = VISIBLE
        } else {
            verificationsGroup.visibility = GONE
        }
    }

    private fun View.setDividers(dailyActivityState: DashboardDailyActivityState) {
        val enrolmentsDivider = findViewById<View>(R.id.divider_enrolments)
        val identificationsDivider = findViewById<View>(R.id.divider_identifications)

        val shouldHideDividers = dailyActivityState.shouldHideDividers()

        if (shouldHideDividers) {
            divider_enrolments.visibility = GONE
            divider_identifications.visibility = GONE
        } else {
            setEnrolmentsDividerVisibility(enrolmentsDivider, dailyActivityState)
            setIdentificationsDividerVisibility(identificationsDivider, dailyActivityState)
        }
    }

    private fun DashboardDailyActivityState.hasEnrolments() = enrolments > 0

    private fun DashboardDailyActivityState.hasIdentifications() = identifications > 0

    private fun DashboardDailyActivityState.hasVerifications() = verifications > 0

    private fun setEnrolmentsDividerVisibility(
        divider: View,
        dailyActivityState: DashboardDailyActivityState
    ) {
        val shouldShowEnrolmentsDivider = dailyActivityState.hasEnrolments()
            && (dailyActivityState.hasIdentifications()
            || dailyActivityState.hasVerifications())

        divider.visibility = if (shouldShowEnrolmentsDivider)
            VISIBLE
        else
            GONE
    }

    private fun setIdentificationsDividerVisibility(
        divider: View,
        dailyActivityState: DashboardDailyActivityState
    ) {
        val shouldShowIdentificationsDivider = dailyActivityState.hasIdentifications()
            && dailyActivityState.hasVerifications()

        divider.visibility = if (shouldShowIdentificationsDivider)
            VISIBLE
        else
            GONE
    }

    /**
     * The dividers will be hidden ONLY if exactly one of the
     * 3 conditions (hasEnrolments, hasIdentifications, hasVerifications) is true
     */
    private fun DashboardDailyActivityState.shouldHideDividers(): Boolean {
        return hasEnrolments().nand(hasIdentifications())
            && hasEnrolments().nand(hasVerifications())
            && hasIdentifications().nand(hasVerifications())
    }

}
