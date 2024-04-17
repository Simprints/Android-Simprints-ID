package com.simprints.feature.importsubject.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.infra.config.store.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ImportSubjectViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    val subjectState: LiveData<LiveDataEventWithContent<ImportSubjectState>>
        get() = _subjectState
    private var _subjectState = MutableLiveData<LiveDataEventWithContent<ImportSubjectState>>()
    private var fetchWasAttempted = false

    fun onViewCreated(projectId: String, subjectId: String) {
        if (!fetchWasAttempted) {
            importSubject(projectId, subjectId)
        }
    }

    fun importSubject(projectId: String, subjectId: String) = viewModelScope.launch {
        // TODO: Implement this method
        delay(3000)
        _subjectState.send(ImportSubjectState.Imported)
    }

}
