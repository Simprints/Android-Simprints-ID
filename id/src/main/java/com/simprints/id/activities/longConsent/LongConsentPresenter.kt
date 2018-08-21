package com.simprints.id.activities.longConsent

import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import javax.inject.Inject

class LongConsentPresenter(val view: LongConsentContract.View,
                           component: AppComponent) : LongConsentContract.Presenter {

    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var preferences: PreferencesManager

    init {
        component.inject(this)
    }

    override fun start() {
        if (longConsentManager.checkIfLongConsentExists(preferences.language)) {
            val longConsentText = longConsentManager.getLongConsentText(preferences.language)
            view.setLongConsentText(longConsentText)
        } else {
            view.setDefaultLongConsent()
        }

        view.showProgressBar = false
    }

}
