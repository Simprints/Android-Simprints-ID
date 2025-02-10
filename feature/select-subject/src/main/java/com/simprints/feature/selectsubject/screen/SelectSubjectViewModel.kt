package com.simprints.feature.selectsubject.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SelectSubjectViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    val finish: LiveData<LiveDataEventWithContent<Boolean>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<Boolean>>()

    fun saveGuidSelection(
        projectId: String,
        subjectId: String,
    ) {
        if (authStore.isProjectIdSignedIn(projectId)) {
            saveSelectionEvent(subjectId)
        } else {
            _finish.send(false)
        }
    }

    private fun saveSelectionEvent(subjectId: String) = sessionCoroutineScope.launch {
        try {
            val event = GuidSelectionEvent(timeHelper.now(), subjectId)
            eventRepository.addOrUpdateEvent(event)

            Simber.i("Added Guid Selection Event", tag = SESSION)
            _finish.send(true)
        } catch (t: Throwable) {
            // It doesn't matter if it was an error, we always return a result
            Simber.e("Failed to save Guid Selection Event", t, tag = SESSION)
            _finish.send(false)
        }
    }
}
