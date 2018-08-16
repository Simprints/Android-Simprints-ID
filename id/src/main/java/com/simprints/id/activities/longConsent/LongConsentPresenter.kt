package com.simprints.id.activities.longConsent

import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import javax.inject.Inject

class LongConsentPresenter(val view: LongConsentContract.View) : LongConsentContract.Presenter {

    @Inject
    lateinit var longConsentManager: LongConsentManager
    @Inject
    lateinit var preferences: PreferencesManager

    override fun start() {

        doAsync {

            val selectedLanguage: String = preferences.language.let {
                if (it.isBlank()) "en" else it
            }

            if (longConsentManager.checkIfLongConsentExists(selectedLanguage)) {
                val longConsentText = longConsentManager.getLongConsentText(selectedLanguage)
                uiThread { view.setLongConsentText(longConsentText) }
            }

        }

    }
}
