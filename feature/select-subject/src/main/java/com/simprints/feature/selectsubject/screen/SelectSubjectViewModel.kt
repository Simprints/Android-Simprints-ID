package com.simprints.feature.selectsubject.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.selectsubject.model.ExternalCredentialSaveResult
import com.simprints.feature.selectsubject.model.SubjectIdSaveResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.external.credential.store.repository.ExternalCredentialRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ENROLMENT
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
    private val externalCredentialRepository: ExternalCredentialRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    val confirmIdentitySaveResultLiveData: LiveData<LiveDataEventWithContent<SubjectIdSaveResult>>
        get() = _confirmIdentitySaveResultLiveData
    private var _confirmIdentitySaveResultLiveData = MutableLiveData<LiveDataEventWithContent<SubjectIdSaveResult>>()

    val finish: LiveData<LiveDataEventWithContent<ExternalCredentialSaveResult>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<ExternalCredentialSaveResult>>()

    fun saveGuidSelection(
        projectId: String,
        subjectId: String,
        externalCredentialId: String?,
    ) {
        if (authStore.isProjectIdSignedIn(projectId)) {
            sessionCoroutineScope.launch {
                val isSubjectIdSaved = saveSelectionEvent(subjectId)
                val currentCredential = externalCredentialRepository.findBySubjectId(subjectId)
                val shouldDisplaySaveCredentialDialog = externalCredentialId != null
                    && (currentCredential == null || currentCredential.data != externalCredentialId)
                _confirmIdentitySaveResultLiveData.send(
                    SubjectIdSaveResult(
                        isSubjectIdSaved = isSubjectIdSaved,
                        shouldDisplaySaveCredentialDialog = shouldDisplaySaveCredentialDialog
                    )
                )
            }
        } else {
            _confirmIdentitySaveResultLiveData.send(SubjectIdSaveResult(isSubjectIdSaved = false, shouldDisplaySaveCredentialDialog = false))
        }
    }

    fun saveExternalCredential(
        externalCred: String,
        subjectId: String,
    ) = viewModelScope.launch {
        try {
            externalCredentialRepository.save(ExternalCredential(data = externalCred, subjectId = subjectId))
            _finish.send(ExternalCredentialSaveResult(isExternalCredentialIdSaved = true))
        } catch (t: Throwable) {
            Simber.e("External Credential Enrolment in 'Confirm Identity' failed", t, tag = ENROLMENT)
            _finish.send(ExternalCredentialSaveResult(isExternalCredentialIdSaved = false))
        }
    }

    private suspend fun saveExternalCredential(externalCredentialId: String?, subjectId: String): Boolean {
        try {
            if (externalCredentialId == null) return false
            externalCredentialRepository.save(ExternalCredential(data = externalCredentialId, subjectId = subjectId))
            return true
        } catch (t: Throwable) {
            Simber.e("Failed to save External Credential Id", t, tag = SESSION)
            return false
        }
    }

    private suspend fun saveSelectionEvent(subjectId: String): Boolean {
        try {
            val event = GuidSelectionEvent(timeHelper.now(), subjectId)
            eventRepository.addOrUpdateEvent(event)

            Simber.i("Added Guid Selection Event", tag = SESSION)
            return true
        } catch (t: Throwable) {
            // It doesn't matter if it was an error, we always return a result
            Simber.e("Failed to save Guid Selection Event", t, tag = SESSION)
            return false
        }
    }
}
