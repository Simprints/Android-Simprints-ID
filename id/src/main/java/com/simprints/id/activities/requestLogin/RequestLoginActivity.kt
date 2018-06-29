package com.simprints.id.activities.requestLogin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.tools.LanguageHelper
import kotlinx.android.synthetic.main.activity_front.*

open class RequestLoginActivity : AppCompatActivity() {

    lateinit var app: Application
    lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as Application
        dataManager = app.dataManager

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
