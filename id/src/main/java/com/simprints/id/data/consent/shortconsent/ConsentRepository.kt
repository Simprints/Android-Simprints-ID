package com.simprints.id.data.consent.shortconsent

import androidx.lifecycle.LiveData
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest

interface ConsentRepository {

    fun getGeneralConsentText(askConsentRequest: AskConsentRequest): LiveData<String>
    fun parentalConsentExists(): LiveData<Boolean>
    fun getParentalConsentText(askConsentRequest: AskConsentRequest): LiveData<String>
}
