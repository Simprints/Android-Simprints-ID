package com.simprints.id.activities.dashboard.cards.sync

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.AndroidResourcesHelperImpl
import com.simprints.id.tools.TimeHelperImpl
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DashboardSyncCardDisplayerImplTest {

    private lateinit var androidResHelper: AndroidResourcesHelper
    private lateinit var syncCardDisplayer: DashboardSyncCardDisplayer
    private lateinit var syncCardRootLayout: LinearLayout
    private lateinit var ctx: Context

    private val lastSyncTime by lazy {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        cal.time
    }

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        syncCardRootLayout = LinearLayout(ctx)
        androidResHelper = AndroidResourcesHelperImpl(ctx)
        syncCardDisplayer = DashboardSyncCardDisplayerImpl(androidResHelper, TimeHelperImpl())
        syncCardDisplayer.initRoot(syncCardRootLayout)
    }

    @Test
    fun syncCardDisplayer_defaultSyncState_shouldDisplayTheCorrectUI() {
        syncCardDisplayer.displayState(SyncDefault(lastSyncTime))
        syncCardRootLayout.assessDefaultStateSyncUI()
    }

    @Test
    fun syncCardDisplayer_failedSyncState_shouldDisplayTheCorrectUI() {
        syncCardDisplayer.displayState(SyncFailed(lastSyncTime))
        syncCardRootLayout.assessFailedStateSyncUI()
    }

    @Test
    fun syncCardDisplayer_tryAgainSyncState_shouldDisplayTheCorrectUI() {
        syncCardDisplayer.displayState(SyncTryAgain(lastSyncTime))
        syncCardRootLayout.assessTryAgainStateSyncUI()
    }

    @Test
    fun syncCardDisplayer_noModulesSyncState_shouldDisplayTheCorrectUI() {
        syncCardDisplayer.displayState(SyncNoModules(lastSyncTime))
        syncCardRootLayout.assessNoModulesStateSyncUI()
    }

    @Test
    fun syncCardDisplayer_offlineSyncState_shouldDisplayTheCorrectUI() {
        syncCardDisplayer.displayState(SyncOffline(lastSyncTime))
        syncCardRootLayout.assessOfflineStateSyncUI()
    }

    @Test
    fun syncCardDisplayer_progressSyncState_shouldDisplayTheCorrectUI() {
        val progressState = SyncProgress(lastSyncTime, 10, 100)
        syncCardDisplayer.displayState(progressState)
        syncCardRootLayout.assessProgressStateSyncUI(progressState)
    }

    @Test
    fun syncCardDisplayer_connectingSyncState_shouldDisplayTheCorrectUI() {
        val connectingState = SyncConnecting(lastSyncTime, 10, 100)
        syncCardDisplayer.displayState(connectingState)
        syncCardRootLayout.assessConnectingStateSyncUI(connectingState)
    }

    @Test
    fun syncCardDisplayer_completeSyncState_shouldDisplayTheCorrectUI() {
        syncCardDisplayer.displayState(SyncComplete(lastSyncTime))
        syncCardRootLayout.assessCompleteStateSyncUI()
    }

    @Test
    fun syncCardDisplayer_lastSyncEmpty_shouldHideLastSyncTime() {
        syncCardDisplayer.displayState(SyncComplete(null))
        val card = syncCardRootLayout.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        assertThat(cardContent.lastSyncTimeTextView().visibility).isEqualTo(GONE)
    }

    private fun LinearLayout.assessDefaultStateSyncUI() {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as LinearLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(defaultSyncButton()).isEqualTo(SYNC_CARD_DEFAULT_STATE_SYNC_BUTTON)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(3)
        }
    }

    private fun LinearLayout.assessFailedStateSyncUI() {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(failedMessage()).isEqualTo(SYNC_CARD_FAILED_STATE_MESSAGE)
            assertThat(failedIcon().visibility).isEqualTo(VISIBLE)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(4)
        }
    }

    private fun LinearLayout.assessTryAgainStateSyncUI() {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as LinearLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(tryAgainMessage()).isEqualTo(SYNC_CARD_TRY_AGAIN_STATE_MESSAGE)
            assertThat(tryAgainSyncButton().visibility).isEqualTo(VISIBLE)
            assertThat(tryAgainSyncButton().text).isEqualTo(SYNC_CARD_TRY_AGAIN_STATE_SYNC_BUTTON)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(4)
        }
    }

    private fun LinearLayout.assessNoModulesStateSyncUI() {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(noModulesMessage()).isEqualTo(SYNC_CARD_NO_MODULES_STATE_MESSAGE)
            assertThat(noModulesModulesButton().visibility).isEqualTo(VISIBLE)
            assertThat(noModulesModulesButton().text).isEqualTo(SYNC_CARD_NO_MODULES_STATE_BUTTON)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(4)
        }
    }

    private fun LinearLayout.assessOfflineStateSyncUI() {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(offlineMessage()).isEqualTo(SYNC_CARD_OFFLINE_STATE_MESSAGE)
            assertThat(offlineIcon().visibility).isEqualTo(VISIBLE)
            assertThat(offlineSettingsButton().visibility).isEqualTo(VISIBLE)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(5)
        }
    }

    private fun LinearLayout.assessProgressStateSyncUI(progressState: SyncProgress) {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        with(cardContent) {
            val percentage = (100 * (progressState.progress.toFloat() / (progressState.total ?: 0))).toInt()
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(progressMessage()).isEqualTo("$SYNC_CARD_PROGRESS_STATE_STATE_MESSAGE ${percentage}%")
            assertThat(progressConnectingProgressBar().visibility).isEqualTo(GONE)
            assertThat(progressSyncProgressBar().visibility).isEqualTo(VISIBLE)
            assertThat(progressSyncProgressBar().progress).isEqualTo(percentage)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(5)
        }
    }


    private fun LinearLayout.assessConnectingStateSyncUI(connectingState: SyncConnecting) {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(progressMessage()).isEqualTo(SYNC_CARD_CONNECTING_STATE_STATE_MESSAGE)
            assertThat(progressConnectingProgressBar().visibility).isEqualTo(VISIBLE)
            assertThat(progressSyncProgressBar().visibility).isEqualTo(VISIBLE)
            assertThat(progressSyncProgressBar().progress).isEqualTo((100 * (connectingState.progress.toFloat() / connectingState.total!!.toFloat())).toInt())
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(5)
        }
    }

    private fun LinearLayout.assessCompleteStateSyncUI() {
        assertThat(childCount).isEqualTo(1)
        val card = this.children.first() as CardView
        val cardContent = card.children.first() as ConstraintLayout
        with(cardContent) {
            assertThat(cardTitle()).isEqualTo(SYNC_CARD_TITLE)
            assertThat(progressMessage()).isEqualTo(SYNC_CARD_COMPLETE_STATE_STATE_MESSAGE)
            assertThat(progressConnectingProgressBar().visibility).isEqualTo(GONE)
            assertThat(progressSyncProgressBar().visibility).isEqualTo(VISIBLE)
            assertThat(progressSyncProgressBar().progress).isEqualTo(100)
            assessLastSyncTime()
            assertThat(cardContent.childCount).isEqualTo(5)
        }
    }

    private fun View.cardTitle() = this.findViewById<TextView>(R.id.dashboard_sync_card_title).text.toString()
    private fun View.lastSyncTimeTextView() = this.findViewById<TextView>(R.id.dashboard_sync_card_last_sync)
    private fun View.lastSyncTime() = lastSyncTimeTextView().text.toString()

    private fun View.defaultSyncButton() = this.findViewById<AppCompatButton>(R.id.dashboard_sync_card_default_state_sync_button).text.toString()

    private fun View.failedMessage() = this.findViewById<TextView>(R.id.dashboard_sync_card_failed_message).text.toString()
    private fun View.failedIcon() = this.findViewById<ImageView>(R.id.dashboard_sync_card_failed_icon)

    private fun View.tryAgainMessage() = this.findViewById<TextView>(R.id.dashboard_sync_card_try_again_message).text.toString()
    private fun View.tryAgainSyncButton() = this.findViewById<AppCompatButton>(R.id.dashboard_sync_card_try_again_sync_button)

    private fun View.noModulesMessage() = this.findViewById<TextView>(R.id.dashboard_sync_card_select_no_modules_message).text.toString()
    private fun View.noModulesModulesButton() = this.findViewById<AppCompatButton>(R.id.dashboard_sync_card_select_no_modules_button)

    private fun View.offlineMessage() = this.findViewById<TextView>(R.id.dashboard_sync_card_offline_message).text.toString()
    private fun View.offlineIcon() = this.findViewById<ImageView>(R.id.dashboard_sync_card_offline_icon)
    private fun View.offlineSettingsButton() = this.findViewById<AppCompatButton>(R.id.dashboard_sync_card_offline_button)

    private fun View.progressMessage() = this.findViewById<TextView>(R.id.dashboard_sync_card_progress_message).text.toString()
    private fun View.progressConnectingProgressBar() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_indeterminate_progress_bar)
    private fun View.progressSyncProgressBar() = this.findViewById<ProgressBar>(R.id.dashboard_sync_card_progress_sync_progress_bar)

    private fun View.assessLastSyncTime() {
        assertThat(lastSyncTimeTextView().visibility).isEqualTo(VISIBLE)
        assertThat(lastSyncTime()).isEqualTo(LAST_SYNC_TEXT)
    }

    companion object {
        const val SYNC_CARD_TITLE = "Sync status"
        const val SYNC_CARD_DEFAULT_STATE_SYNC_BUTTON = "SYNC NOW"
        const val LAST_SYNC_TEXT = "Last sync: Yesterday"
        const val SYNC_CARD_FAILED_STATE_MESSAGE = "Sync failed. Please contact your supervisor"
        const val SYNC_CARD_TRY_AGAIN_STATE_MESSAGE = "Sync incomplete"
        const val SYNC_CARD_TRY_AGAIN_STATE_SYNC_BUTTON = "TRY AGAIN"
        const val SYNC_CARD_NO_MODULES_STATE_BUTTON = "MODULES"
        const val SYNC_CARD_NO_MODULES_STATE_MESSAGE = "Please select modules to sync"
        const val SYNC_CARD_OFFLINE_STATE_MESSAGE = "Please turn on internet connection in settings"
        const val SYNC_CARD_CONNECTING_STATE_STATE_MESSAGE = "Connecting"
        const val SYNC_CARD_PROGRESS_STATE_STATE_MESSAGE = "Syncing…"
        const val SYNC_CARD_COMPLETE_STATE_STATE_MESSAGE = "Sync complete"
    }
}
