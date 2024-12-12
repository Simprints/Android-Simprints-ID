package com.simprints.feature.troubleshooting.networking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.troubleshooting.adapter.TroubleshootingItemViewData
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
internal class NetworkingLogViewModel @Inject constructor(
    private val persistentLogger: PersistentLogger,
) : ViewModel() {

    private val _logs = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val logs: LiveData<List<TroubleshootingItemViewData>>
        get() = _logs

    fun collectData() {
        viewModelScope.launch {
            persistentLogger.get(LogEntryType.Network)
                .map {
                    TroubleshootingItemViewData(
                        title = it.title,
                        subtitle = Date(it.timestampMs).toString(),
                        body = it.body,
                    )
                }
                .ifEmpty { listOf(TroubleshootingItemViewData(title = "No data")) }
                .let { _logs.postValue(it) }
        }
    }
}
