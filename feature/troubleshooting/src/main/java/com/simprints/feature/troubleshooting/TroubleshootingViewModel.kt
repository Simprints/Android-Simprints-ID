package com.simprints.feature.troubleshooting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class TroubleshootingViewModel @Inject constructor() : ViewModel() {
    private val _shouldOpenIntentDetails = MutableLiveData<LiveDataEventWithContent<String>>()
    val shouldOpenIntentDetails: LiveData<LiveDataEventWithContent<String>>
        get() = _shouldOpenIntentDetails

    fun openIntentDetails(string: String) {
        _shouldOpenIntentDetails.value = LiveDataEventWithContent(string)
    }
}
