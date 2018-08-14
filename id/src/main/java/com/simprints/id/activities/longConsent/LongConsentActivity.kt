package com.simprints.id.activities.longConsent

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.consent.LongConsentManager
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

        try {
            val intentUrl = Intent(Intent.ACTION_VIEW)

            val fileUri = FileProvider.getUriForFile(this, getString(R.string.file_provider_authority),
                longConsentManager.getLongConsentUri(longConsentManager.languages[0]))

            intentUrl.setDataAndType(fileUri, "application/pdf")
            intentUrl.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intentUrl.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            startActivity(intentUrl)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No PDF Viewer Installed", Toast.LENGTH_LONG).show()
        }

    }
}
