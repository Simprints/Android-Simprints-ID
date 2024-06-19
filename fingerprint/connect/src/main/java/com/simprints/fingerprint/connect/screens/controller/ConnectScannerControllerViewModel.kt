package com.simprints.fingerprint.connect.screens.controller

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ConnectScannerControllerViewModel @Inject constructor() : ViewModel() {
    var isInitialized = false
}