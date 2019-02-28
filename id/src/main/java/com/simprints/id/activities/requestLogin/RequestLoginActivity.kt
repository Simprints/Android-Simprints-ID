package com.simprints.id.activities.requestLogin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.extensions.packageVersionName
import kotlinx.android.synthetic.main.activity_front.*
import javax.inject.Inject

open class RequestLoginActivity : AppCompatActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager

    lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        app = application as Application

        LanguageHelper.setLanguage(this, preferencesManager.language)
        setContentView(R.layout.activity_front)

        initSimprintsIdVersionTextView(packageVersionName)
    }

    private fun initSimprintsIdVersionTextView(simprintsIdVersion: String) {
        val simprintsIdVersionString = String.format(getString(R.string.front_simprintsId_version), simprintsIdVersion)
        simprintsIdVersionTextView.text = simprintsIdVersionString
    }
}
