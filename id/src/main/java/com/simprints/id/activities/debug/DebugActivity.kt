package com.simprints.id.activities.debug

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.id.Application
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.databinding.ActivityDebugBinding
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.config.RemoteConfigScheduler
import com.simprints.id.services.config.RemoteConfigSchedulerImpl
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState.*
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


class DebugActivity : BaseSplitActivity() {

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var loginManager: LoginManager
    lateinit var securityStateScheduler: SecurityStateScheduler
    @Inject
    lateinit var dbEventDownSyncOperationStateDao: DbEventDownSyncOperationStateDao

    @Inject
    lateinit var securityStateRepository: SecurityStateRepository

    @Inject
    lateinit var securityStateProcessor: SecurityStateProcessor

    @Inject
    lateinit var eventLocalDataSource: EventLocalDataSource

    @Inject
    lateinit var subjectRepository: SubjectRepository

    @Inject
    lateinit var dispatcher: DispatcherProvider

    private val binding by viewBinding(ActivityDebugBinding::inflate)

    private val wm: WorkManager
        get() = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)

        setContentView(binding.root)

        eventSyncManager.getLastSyncState().observe(this) { state ->
            val states =
                (state.downSyncWorkersInfo.map { it.state } + state.upSyncWorkersInfo.map { it.state })
            val message =
                "${state.syncId.takeLast(5)} - " +
                    "${states.toDebugActivitySyncState().name} - " +
                    "${state.progress}/${state.total}"

            val ssb = SpannableStringBuilder(
                coloredText(
                    "\n$message",
                    Color.parseColor(getRandomColor())
                )
            )

            binding.logs.append(ssb)
        }

        binding.syncSchedule.setOnClickListener {
            eventSyncManager.scheduleSync()
        }

        binding.syncDevice.setOnClickListener {
            securityStateScheduler.getSecurityStateCheck()
        }

        binding.syncStart.setOnClickListener {
            eventSyncManager.sync()
        }

        binding.syncStop.setOnClickListener {
            eventSyncManager.stop()
        }

        binding.cleanAll.setOnClickListener {
            lifecycleScope.launch {
                withContext(dispatcher.io()) {
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
                withContext(dispatcher.main()) {
                    val logStringBuilder = StringBuilder()
                    logStringBuilder.append("\nSubjects ${subjectRepository.count()}")

                    val events = eventLocalDataSource.loadAll().toList().groupBy { it.type }
                    events.forEach {
                        logStringBuilder.append("\n${it.key} ${it.value.size}")
                    }

                    binding.logs.text = logStringBuilder.toString()
                }
            }
        }

        binding.syncConfig.setOnClickListener {
            binding.logs.append("\nGetting Configs from BFSID")
            runBlocking {
                configManager.refreshProjectConfiguration(loginManager.signedInProjectId)
            }
            binding.logs.append("\nGot Configs from BFSID")
        }

        wm.getWorkInfosForUniqueWorkLiveData(RemoteConfigSchedulerImpl.WORK_NAME_ONE_TIME)
            .observe(this) { workInfos ->
                binding.logs.append(
                    workInfos.joinToString("", "\n") { workInfo ->
                        "${workInfo.id.toString().take(5)} - ${workInfo.state}"
                    }
                )
            }
    }

    private fun getRandomColor(): String =
        arrayOf("red", "black", "purple", "green", "blue").random()

    private fun coloredText(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        try {
            spannableString.setSpan(
                ForegroundColorSpan(color), 0,
                text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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
