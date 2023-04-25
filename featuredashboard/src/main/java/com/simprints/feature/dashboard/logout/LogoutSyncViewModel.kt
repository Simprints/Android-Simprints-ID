package com.simprints.feature.dashboard.logout

import androidx.lifecycle.ViewModel
import com.simprints.core.ExternalScope
import com.simprints.feature.dashboard.settings.about.SignerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutSyncViewModel @Inject constructor(
    private val signerManager: SignerManager,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    fun logout() {
        externalScope.launch { signerManager.signOut() }
    }
}
