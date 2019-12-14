package com.simprints.id.activities.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncDao
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import kotlinx.android.synthetic.main.activity_debug.*
import java.util.*
import javax.inject.Inject

class DebugActivity : AppCompatActivity() {

    @Inject lateinit var peopleSyncManager: PeopleSyncManager
    @Inject lateinit var peopleDownSyncDao: PeopleDownSyncDao

    private val wm: WorkManager
        get() = WorkManager.getInstance(this)


    var timeTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(R.layout.activity_debug)


        peopleSyncManager.getLastSyncState().observe(this, Observer {
            val states = (it.downSyncStates.map { it.state } + it.upSyncStates.map { it.state })

            logs.text =
                "${logs.text}\n" +
                    "${it.syncId?.takeLast(3)} - " +
                    "${states.toDebugActivitySyncState()?.name?.take(1)} - " +
                    "${it.progress}/${it.total}"
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

            peopleDownSyncDao.deleteAll()
        }
    }


    private fun List<WorkInfo.State>.toDebugActivitySyncState(): DebugActivitySyncState =
        when {
            isEmpty() -> DebugActivitySyncState.NOT_RUNNING
            this.any { it == WorkInfo.State.RUNNING } -> DebugActivitySyncState.RUNNING
            this.any { it == WorkInfo.State.ENQUEUED } -> DebugActivitySyncState.CONNECTING
            this.all { it == WorkInfo.State.SUCCEEDED } -> DebugActivitySyncState.SUCCESS
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
