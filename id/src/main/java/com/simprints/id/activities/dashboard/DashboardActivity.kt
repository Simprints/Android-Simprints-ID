package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.secure.SecureDataManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import javax.inject.Inject

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    @Inject lateinit var secureDataManager: SecureDataManager

    private lateinit var viewPresenter: DashboardContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        Application.component.inject(this)

        viewPresenter = DashboardPresenter(this)
        viewPresenter.start()

        dashboardButtonLogout.setOnClickListener {
            secureDataManager.cleanCredentials()
            startActivity(Intent(this, RequestLoginActivity::class.java))
            finish()
        }
    }

    override fun setPresenter(presenter: DashboardContract.Presenter) {
        viewPresenter = presenter
    }
}
