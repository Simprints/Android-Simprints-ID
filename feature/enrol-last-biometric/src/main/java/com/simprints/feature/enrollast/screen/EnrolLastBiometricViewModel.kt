package com.simprints.feature.enrollast.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.infra.config.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EnrolLastBiometricViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
) : ViewModel() {

    val finish: LiveData<LiveDataEventWithContent<EnrolLastState>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<EnrolLastState>>()

}
