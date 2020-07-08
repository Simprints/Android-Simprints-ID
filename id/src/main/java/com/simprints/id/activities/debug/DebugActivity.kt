package com.simprints.id.activities.debug

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.subjects_sync.down.local.SubjectsDownSyncOperationLocalDataSource
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState.*
import kotlinx.android.synthetic.main.activity_debug.*
import javax.inject.Inject


class DebugActivity : BaseSplitActivity() {

    @Inject lateinit var subjectsSyncManager: SubjectsSyncManager
    @Inject lateinit var subjectsDownSyncOperationLocalDataSource: SubjectsDownSyncOperationLocalDataSource

    private val wm: WorkManager
        get() = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(R.layout.activity_debug)

        subjectsSyncManager.getLastSyncState().observe(this, Observer {
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
            subjectsSyncManager.scheduleSync()
        }

        syncStart.setOnClickListener {
            subjectsSyncManager.sync()
        }

        syncStop.setOnClickListener {
            subjectsSyncManager.stop()
        }

        cleanAll.setOnClickListener {
            subjectsSyncManager.cancelScheduledSync()
            subjectsSyncManager.stop()
            wm.pruneWork()

            subjectsDownSyncOperationLocalDataSource.deleteAll()
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

    enum class DebugActivitySyncState {
        RUNNING,
        NOT_RUNNING,
        CONNECTING,
        SUCCESS,
        FAILED
    }
}
