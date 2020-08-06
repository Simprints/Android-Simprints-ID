package com.simprints.id.activities.debug

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.events_sync.down.local.EventDownSyncOperationLocalDataSource
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerState
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerState.*
import kotlinx.android.synthetic.main.activity_debug.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class DebugActivity : BaseSplitActivity() {

    @Inject lateinit var eventSyncManager: EventSyncManager
    @Inject lateinit var EventDownSyncOperationLocalDataSource: EventDownSyncOperationLocalDataSource
    @Inject lateinit var securityStateRepository: SecurityStateRepository
    @Inject lateinit var securityStateProcessor: SecurityStateProcessor

    private val wm: WorkManager
        get() = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(R.layout.activity_debug)

        eventSyncManager.getLastSyncState().observe(this, Observer {
            val states = (it.downSyncWorkersInfo.map { it.state } + it.upSyncWorkersInfo.map { it.state })
            val message =
                "${it.syncId.takeLast(3)} - " +
                "${states.toDebugActivitySyncState().name} - " +
                "${it.progress}/${it.total}"

            val ssb = SpannableStringBuilder(logs.text)
            ssb.append(coloredText(message + "\n", Color.parseColor(getRandomColor(it.syncId))))

            logs.setText(ssb, TextView.BufferType.SPANNABLE)
        })

        syncSchedule.setOnClickListener {
            eventSyncManager.scheduleSync()
        }

        syncStart.setOnClickListener {
            eventSyncManager.sync()
        }

        syncStop.setOnClickListener {
            eventSyncManager.stop()
        }

        cleanAll.setOnClickListener {
            eventSyncManager.cancelScheduledSync()
            eventSyncManager.stop()
            wm.pruneWork()

            EventDownSyncOperationLocalDataSource.deleteAll()
        }

        securityStateCompromised.setOnClickListener {
            setSecurityStatus(SecurityState.Status.COMPROMISED)
        }

        securityStateProjectEnded.setOnClickListener {
            setSecurityStatus(SecurityState.Status.PROJECT_ENDED)
        }
    }

    private fun getRandomColor(seed: String): String {
        val rnd = seed.toCharArray().sumBy { it.toInt() } % 4
        return arrayOf("red", "yellow", "green", "blue")[rnd]
    }

    private fun coloredText(text: String, color: Int): SpannableString? {
        val spannableString = SpannableString(text)
        try {
            spannableString.setSpan(ForegroundColorSpan(color), 0,
                text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } catch (e: Exception) {
        }
        return spannableString
    }

    private fun List<SubjectsSyncWorkerState>.toDebugActivitySyncState(): DebugActivitySyncState =
        when {
            isEmpty() -> DebugActivitySyncState.NOT_RUNNING
            this.any { it is Running } -> DebugActivitySyncState.RUNNING
            this.any { it is Enqueued } -> DebugActivitySyncState.CONNECTING
            this.all { it is Succeeded } -> DebugActivitySyncState.SUCCESS
            else -> DebugActivitySyncState.FAILED
        }

    private fun setSecurityStatus(status: SecurityState.Status) {
        lifecycleScope.launch {
            with(securityStateRepository.securityStatusChannel) {
                if (!isClosedForSend) {
                    send(status)
                    securityStateProcessor.processSecurityState(
                        SecurityState("device-id", status)
                    )
                }
            }
        }
    }

    enum class DebugActivitySyncState {
        RUNNING,
        NOT_RUNNING,
        CONNECTING,
        SUCCESS,
        FAILED
    }
}
