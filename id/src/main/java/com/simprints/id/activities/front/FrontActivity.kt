package com.simprints.id.activities.front

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.requestProjectCredentials.RequestProjectCredentialsActivity
import com.simprints.id.data.DataManager
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.PermissionManager
import com.simprints.id.tools.RemoteConfig
import kotlinx.android.synthetic.main.activity_front.*

class FrontActivity : AppCompatActivity(), FrontContract.View {

    private lateinit var frontPresenter: FrontContract.Presenter
    lateinit var app:Application
    lateinit var dataManager:DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as Application
        dataManager = app.dataManager

        LanguageHelper.setLanguage(this, dataManager.language)
        setContentView(R.layout.activity_front)
        RemoteConfig.init()

        initSimprintsIdVersionTextView(dataManager.appVersionName)
        initLibSimprintsVersionTextView(dataManager.libVersionName)

        PermissionManager.requestAllPermissions(this@FrontActivity, dataManager.callingPackage)

        initPresenter()
    }

    fun initPresenter(){
        frontPresenter = FrontPresenter(this, app.secureDataManager)
        frontPresenter.start()
    }
    override fun setPresenter(presenter: FrontContract.Presenter) {
        frontPresenter = presenter
    }

    override fun onResume() {
        super.onResume()
        frontPresenter.doSecurityChecks();
    }

    override fun openRequestAPIActivity(){
        overridePendingTransition(R.anim.slide_out_to_up, R.anim.stay)
        val intent = Intent(this, RequestProjectCredentialsActivity::class.java)
        startActivity(intent)
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
