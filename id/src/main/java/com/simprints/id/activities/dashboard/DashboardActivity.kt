package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var viewPresenter: DashboardContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val app = application as Application

        viewPresenter = DashboardPresenter(this)
        viewPresenter.start()

        dashboardButtonLogout.setOnClickListener {
            app.secureDataManager.cleanCredentials()
            startActivity(Intent(this, RequestLoginActivity::class.java))
            finish()
        }
    }

    override fun setPresenter(presenter: DashboardContract.Presenter) {
        viewPresenter = presenter
    }
}
