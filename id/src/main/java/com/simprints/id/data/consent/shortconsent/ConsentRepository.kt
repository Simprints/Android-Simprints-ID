package com.simprints.id.data.consent.shortconsent

import androidx.lifecycle.LiveData
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

interface ConsentRepository {

    fun getGeneralConsentText(appRequest: AppRequest): LiveData<String>
    fun parentalConsentExists(): LiveData<Boolean>
    fun getParentalConsentText(appRequest: AppRequest): LiveData<String>
}
