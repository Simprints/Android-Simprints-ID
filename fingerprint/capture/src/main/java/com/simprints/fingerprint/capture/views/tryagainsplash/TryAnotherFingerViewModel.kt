package com.simprints.fingerprint.capture.views.tryagainsplash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TryAnotherFingerViewModel @Inject constructor() : ViewModel() {
    val dismiss: LiveData<Boolean> get() = _dismiss
    private val _dismiss = MutableLiveData(false)

    init {
        viewModelScope.launch {
            delay(FingerprintCaptureViewModel.TRY_DIFFERENT_FINGER_SPLASH_DELAY)
            _dismiss.value = true
        }
    }
}
