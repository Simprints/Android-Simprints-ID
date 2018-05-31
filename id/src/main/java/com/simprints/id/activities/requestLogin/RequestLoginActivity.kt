package com.simprints.id.activities.requestLogin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.tools.LanguageHelper
import kotlinx.android.synthetic.main.activity_front.*
import javax.inject.Inject

open class RequestLoginActivity : AppCompatActivity() {

    @Inject lateinit var dataManager: DataManager

    lateinit var app: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        app = application as Application

        LanguageHelper.setLanguage(this, dataManager.language)
        setContentView(R.layout.activity_front)

        initSimprintsIdVersionTextView(dataManager.appVersionName)
        initLibSimprintsVersionTextView(dataManager.libVersionName)
    }

    private fun initSimprintsIdVersionTextView(simprintsIdVersion: String) {
        val simprintsIdVersionString = String.format(getString(R.string.front_simprintsId_version), simprintsIdVersion)
        simprintsIdVersionTextView.text = simprintsIdVersionString
    }

    private fun initLibSimprintsVersionTextView(libSimprintsVersion: String) {
        val libSimprintsVersionString = String.format(getString(R.string.front_libSimprints_version), libSimprintsVersion)
        libSimprintsVersionTextView.text = libSimprintsVersionString
    }
}
