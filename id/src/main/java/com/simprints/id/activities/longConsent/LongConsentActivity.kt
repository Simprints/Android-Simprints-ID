package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.consent.LongConsentManager
import kotlinx.android.synthetic.main.activity_long_consent.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import javax.inject.Inject


class LongConsentActivity : AppCompatActivity(), LongConsentContract.View {

    @Inject
    lateinit var longConsentManager: LongConsentManager

    override lateinit var viewPresenter: LongConsentContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_long_consent)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = LongConsentPresenter(this)
        viewPresenter.start()

        doAsync {
            val shitTonOfText = longConsentManager.getLongConsentText(longConsentManager.languages[0])
            uiThread {
                longConsent_TextView.text = shitTonOfText.subSequence(0, 15000)
                longConsent_ScrollView.requestLayout()
            }
        }
    }
}
