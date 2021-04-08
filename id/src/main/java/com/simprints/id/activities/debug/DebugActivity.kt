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
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.databinding.ActivityDebugBinding
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import javax.inject.Inject


class DebugActivity : BaseSplitActivity() {

    @Inject lateinit var eventSyncManager: EventSyncManager
    @Inject lateinit var dbEventDownSyncOperationStateDao: DbEventDownSyncOperationStateDao
    @Inject lateinit var securityStateRepository: SecurityStateRepository
    @Inject lateinit var securityStateProcessor: SecurityStateProcessor
    @Inject lateinit var eventLocalDataSource: EventLocalDataSource
    @Inject lateinit var subjectRepository: SubjectRepository

    private val binding by viewBinding(ActivityDebugBinding::inflate)

    private val wm: WorkManager
        get() = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(binding.root)

        eventSyncManager.getLastSyncState().observe(this, Observer {
            val states = (it.downSyncWorkersInfo.map { it.state } + it.upSyncWorkersInfo.map { it.state })
            val message =
                "${it.syncId.takeLast(3)} - " +
                    "${states.toDebugActivitySyncState().name} - " +
                    "${it.progress}/${it.total}"

            val ssb = SpannableStringBuilder(binding.logs.text)
            ssb.append(coloredText(message + "\n", Color.parseColor(getRandomColor(it.syncId))))

            binding.logs.setText(ssb, TextView.BufferType.SPANNABLE)
        })

        binding.syncSchedule.setOnClickListener {
            eventSyncManager.scheduleSync()
        }

        binding.syncStart.setOnClickListener {
            eventSyncManager.sync()
        }

        binding.syncStop.setOnClickListener {
            eventSyncManager.stop()
        }

        binding.cleanAll.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    eventSyncManager.cancelScheduledSync()
                    eventSyncManager.stop()
                    eventLocalDataSource.deleteAll()
                    dbEventDownSyncOperationStateDao.deleteAll()
                    subjectRepository.deleteAll()
                    wm.pruneWork()
                }
            }
        }

        binding.securityStateCompromised.setOnClickListener {
            setSecurityStatus(SecurityState.Status.COMPROMISED)
        }

        binding.securityStateProjectEnded.setOnClickListener {
            setSecurityStatus(SecurityState.Status.PROJECT_ENDED)
        }

        binding.printRoomDb.setOnClickListener {
            binding.logs.text = ""
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    val logStringBuilder = StringBuilder()
                    logStringBuilder.append("${binding.logs.text} ${subjectRepository.count()} \n")

                    val events = eventLocalDataSource.loadAll().toList().groupBy { it.type }
                    events.forEach {
                        logStringBuilder.append(" ${it.key} ${it.value.size} \n")
                    }

                    binding.logs.text = logStringBuilder.toString()
                }
            }
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

    private fun List<EventSyncWorkerState>.toDebugActivitySyncState(): DebugActivitySyncState =
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
