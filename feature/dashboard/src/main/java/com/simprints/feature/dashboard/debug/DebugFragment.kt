package com.simprints.feature.dashboard.debug

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.simprints.core.DispatcherIO
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDebugBinding
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.usecase.SyncUseCase
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
internal class DebugFragment : Fragment(R.layout.fragment_debug) {
    @Inject
    lateinit var sync: SyncUseCase

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    @Inject
    lateinit var authStore: AuthStore

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @Inject
    @DispatcherIO
    lateinit var dispatcher: CoroutineDispatcher

    private val binding by viewBinding(FragmentDebugBinding::bind)
    private val wm: WorkManager
        get() = WorkManager.getInstance(requireContext())

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)

        sync(SyncCommands.ObserveOnly)
            .syncStatusFlow
            .map {
                it.eventSyncState
            }.asLiveData()
            .observe(viewLifecycleOwner) { state ->
                val states =
                    (state.downSyncWorkersInfo.map { it.state } + state.upSyncWorkersInfo.map { it.state })
                val message =
                    "${state.syncId.takeLast(5)} - " +
                        "${states.toDebugActivitySyncState().name} - " +
                        "${state.progress}/${state.total}"

                val ssb = SpannableStringBuilder(
                    coloredText(
                        "\n$message",
                        getRandomColor().toColorInt(),
                    ),
                )

                binding.logs.append(ssb)
            }

        binding.syncStart.setOnClickListener {
            sync(SyncCommands.OneTime.Events.start())
        }

        binding.syncStop.setOnClickListener {
            sync(SyncCommands.OneTime.Events.stop())
        }

        binding.syncSchedule.setOnClickListener {
            sync(SyncCommands.Schedule.Events.start())
        }

        binding.clearFirebaseToken.setOnClickListener {
            authStore.clearFirebaseToken()
            binding.logs.append("\nFirebase token deleted")
        }

        binding.printRoomDb.setOnClickListener {
            binding.logs.text = ""
            runBlocking {
                val logStringBuilder = StringBuilder()
                logStringBuilder.append("\nSubjects ${enrolmentRecordRepository.count()}")

                val events = eventRepository.getAllEvents().toList().groupBy { it.type }
                events.forEach {
                    logStringBuilder.append("\n${it.key} ${it.value.size}")
                }

                binding.logs.text = logStringBuilder.toString()
            }
        }

        binding.cleanAll.setOnClickListener {
            lifecycleScope.launch(dispatcher) {
                sync(SyncCommands.OneTime.Events.stop())
                sync(SyncCommands.Schedule.Events.stop())

                eventRepository.deleteAll()
                eventSyncManager.resetDownSyncInfo()
                enrolmentRecordRepository.deleteAll()
                wm.pruneWork()
            }
        }
    }

    private fun getRandomColor(): String = arrayOf("red", "black", "purple", "green", "blue").random()

    private fun coloredText(
        text: String,
        color: Int,
    ): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(color),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        return spannableString
    }

    private fun List<EventSyncWorkerState>.toDebugActivitySyncState(): DebugActivitySyncState = when {
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
        FAILED,
    }
}
