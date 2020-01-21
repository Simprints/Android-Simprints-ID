package com.simprints.id.activities.debug

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncOperationLocalDataSource
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState.*
import kotlinx.android.synthetic.main.activity_debug.*
import javax.inject.Inject


class DebugActivity : AppCompatActivity() {

    @Inject lateinit var peopleSyncManager: PeopleSyncManager
    @Inject lateinit var peopleDownSyncOperationLocalDataSource: PeopleDownSyncOperationLocalDataSource

    private val wm: WorkManager
        get() = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(R.layout.activity_debug)

        peopleSyncManager.getLastSyncState().observe(this, Observer {
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
            peopleSyncManager.scheduleSync()
        }

        syncStart.setOnClickListener {
            peopleSyncManager.sync()
        }

        syncStop.setOnClickListener {
            peopleSyncManager.stop()
        }

        cleanAll.setOnClickListener {
            peopleSyncManager.cancelScheduledSync()
            peopleSyncManager.stop()
            wm.pruneWork()

            peopleDownSyncOperationLocalDataSource.deleteAll()
        }
    }

    private fun setTextAsHtml(message: String): CharSequence? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(message)
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

    private fun List<PeopleSyncWorkerState>.toDebugActivitySyncState(): DebugActivitySyncState =
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
