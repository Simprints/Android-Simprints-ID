package com.simprints.feature.dashboard.debug

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDebugBinding
import com.simprints.feature.dashboard.main.sync.EventSyncManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
internal class DebugFragment : Fragment(R.layout.fragment_debug) {

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var loginManager: LoginManager

    @Inject
    lateinit var securityStateScheduler: SecurityStateScheduler

    @Inject
    lateinit var eventLocalDataSource: EventLocalDataSource

    @Inject
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @Inject
    lateinit var dbEventDownSyncOperationStateDao: DbEventDownSyncOperationStateDao

    @Inject
    @DispatcherIO
    lateinit var dispatcher: CoroutineDispatcher

    private val binding by viewBinding(FragmentDebugBinding::bind)
    private val wm: WorkManager
        get() = WorkManager.getInstance(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventSyncManager.getLastSyncState().observe(viewLifecycleOwner) { state ->
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

        binding.syncStart.setOnClickListener {
            eventSyncManager.sync()
        }

        binding.syncStop.setOnClickListener {
            eventSyncManager.stop()
        }

        binding.syncSchedule.setOnClickListener {
            eventSyncManager.scheduleSync()
        }

        binding.syncConfig.setOnClickListener {
            binding.logs.append("\nGetting Configs from BFSID")
            lifecycleScope.launch {
                try {
                    configManager.refreshProjectConfiguration(loginManager.signedInProjectId)
                    binding.logs.append("\nGot Configs from BFSID")
                } catch (e: Exception) {
                    binding.logs.append("\nFailed to refresh the project configuration")
                }
            }
        }

        binding.syncDevice.setOnClickListener {
            securityStateScheduler.getSecurityStateCheck()
        }

        binding.printRoomDb.setOnClickListener {
            binding.logs.text = ""
            runBlocking {
                val logStringBuilder = StringBuilder()
                logStringBuilder.append("\nSubjects ${enrolmentRecordManager.count()}")

                val events = eventLocalDataSource.loadAll().toList().groupBy { it.type }
                events.forEach {
                    logStringBuilder.append("\n${it.key} ${it.value.size}")
                }

                binding.logs.text = logStringBuilder.toString()
            }
        }

        binding.cleanAll.setOnClickListener {
            lifecycleScope.launch(dispatcher) {
                eventSyncManager.cancelScheduledSync()
                eventSyncManager.stop()
                eventLocalDataSource.deleteAll()
                dbEventDownSyncOperationStateDao.deleteAll()
                enrolmentRecordManager.deleteAll()
                wm.pruneWork()
            }
        }
    }

    private fun getRandomColor(): String =
        arrayOf("red", "black", "purple", "green", "blue").random()

    private fun coloredText(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(color), 0,
            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun List<EventSyncWorkerState>.toDebugActivitySyncState(): DebugActivitySyncState =
        when {
            isEmpty() -> DebugActivitySyncState.NOT_RUNNING
            this.any { it is EventSyncWorkerState.Running } -> DebugActivitySyncState.RUNNING
            this.any { it is EventSyncWorkerState.Enqueued } -> DebugActivitySyncState.CONNECTING
            this.all { it is EventSyncWorkerState.Succeeded } -> DebugActivitySyncState.SUCCESS
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
