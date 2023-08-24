package com.simprints.feature.clientapi.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ClientApiViewModel @Inject constructor(
    private val intentMapper: IntentToActionMapper,
    private val clientSessionManager: ClientSessionManager,
) : ViewModel() {

    val proceedWithAction: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _proceedWithAction
    private val _proceedWithAction = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()


    val showAlert: LiveData<LiveDataEventWithContent<ClientApiError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<ClientApiError>>()

    fun handleIntent(action: String, extras: Map<String, Any>) {
        viewModelScope.launch { validateActionAndProceed(action, extras) }
    }

    private suspend fun validateActionAndProceed(action: String, extras: Map<String, Any>) = try {
        val action = intentMapper(action, extras)
        clientSessionManager.reportUnknownExtras(action.unknownExtras)

        // TODO proceed processing action
        _proceedWithAction.send(action) // TODO replace with user flow builder

    } catch (validationException: InvalidRequestException) {
        Simber.e(validationException)
        clientSessionManager.addInvalidIntentEvent(action, extras)
        _showAlert.send(validationException.error)
    }

}
