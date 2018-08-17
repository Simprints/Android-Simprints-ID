package com.simprints.id.activities.longConsent

import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import javax.inject.Inject

class LongConsentPresenter(val view: LongConsentContract.View,
                           component: AppComponent) : LongConsentContract.Presenter {

    @Inject
    lateinit var longConsentManager: LongConsentManager
    @Inject
    lateinit var preferences: PreferencesManager

    companion object {
        private const val ENGLISH_LANGUAGE_CODE = "en"
    }

    init {
        component.inject(this)
    }

    override fun start() {

        val selectedLanguage: String = preferences.language.let {
            if (it.isBlank()) ENGLISH_LANGUAGE_CODE else it
        }

        if (longConsentManager.checkIfLongConsentExists(selectedLanguage)) {
            val longConsentText = longConsentManager.getLongConsentText(selectedLanguage)
            view.setLongConsentText(longConsentText)
        } else
            view.setDefaultLongConsent()

        view.showProgressBar = false
    }

}

