package com.simprints.id.activities.requestLogin

import android.os.Bundle
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.extensions.packageVersionName
import kotlinx.android.synthetic.main.activity_front.*
import javax.inject.Inject

open class RequestLoginActivity : BaseSplitActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager

    lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        app = application as Application
        title = getString(R.string.requestLogin_title)

        setContentView(R.layout.activity_front)
        setTextInLayout()

        initSimprintsIdVersionTextView(packageVersionName)
    }

    private fun setTextInLayout() {
        libSimprintsVersionTextView.text = getString(R.string.libsimprints_label)
        simprintsIdVersionTextView.text = getString(R.string.simprints_label)
        requestLogin.text = getString(R.string.requestLogin_message)
    }

    private fun initSimprintsIdVersionTextView(simprintsIdVersion: String) {
        val simprintsIdVersionString = String.format(getString(R.string.front_simprintsId_version), simprintsIdVersion)
        simprintsIdVersionTextView.text = simprintsIdVersionString
    }
}
