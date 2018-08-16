package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.LanguageHelper
import kotlinx.android.synthetic.main.activity_long_consent.*
import javax.inject.Inject


class LongConsentActivity : AppCompatActivity(), LongConsentContract.View {

    @Inject
    lateinit var preferences: PreferencesManager

    override lateinit var viewPresenter: LongConsentContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.setLanguage(this, preferences.language)
        setContentView(R.layout.activity_long_consent)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = LongConsentPresenter(this)
        viewPresenter.start()
    }

    override fun setLongConsentText(text: String) {
        longConsent_TextView.text = text
        longConsent_ScrollView.requestLayout()
    }

}
