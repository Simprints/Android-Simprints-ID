package com.simprints.id.activities.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import kotlinx.android.synthetic.main.activity_debug.*
import javax.inject.Inject

class DebugActivity : AppCompatActivity() {

    @Inject lateinit var downSyncManager: DownSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(R.layout.activity_debug)


        downSyncManager.lastSyncState.observe(this, Observer {
            logs.text = "${logs.text}\n${it?.syncId} - ${it?.state}"
        })

        syncSchedule.setOnClickListener {
            downSyncManager.scheduleSync()
        }

        syncStart.setOnClickListener {
            downSyncManager.sync()
        }

        syncStop.setOnClickListener {
            downSyncManager.stop()
        }
    }
}
