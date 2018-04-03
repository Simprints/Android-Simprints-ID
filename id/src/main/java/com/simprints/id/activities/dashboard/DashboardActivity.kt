package com.simprints.id.activities.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.services.sync.SyncService
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    override lateinit var viewPresenter: DashboardContract.Presenter
    private val app: Application by lazy { application as Application }
    private val dataManager: DataManager by lazy { app.dataManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val syncClient = SyncService.getClient(this)
        viewPresenter = DashboardPresenter(this, syncClient, dataManager)

        initUI()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun onPause() {
        super.onPause()
        viewPresenter.pause()
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        dashboardButtonLogout.setOnClickListener {
            app.secureDataManager.cleanCredentials()
            app.dataManager.signOut()
            startActivity(Intent(this, RequestLoginActivity::class.java))
            finish()
        }

        dashboardButtonSync.setOnClickListener {
            viewPresenter.didUserWantToSyncBy(dataManager.syncGroup)
        }

        //FIXME: remove this code
        buttonAddPersonRealm.setOnClickListener {
            app.dbManager.getRealmInstance().executeTransaction { realm ->
                realm.copyToRealmOrUpdate(PeopleGeneratorUtils.getRandomPerson())
                val results = realm.where(rl_Person::class.java).findAll()
                textAddPersonRealm.text = "Count: ${results.count()}"
            }
        }
    }

    override fun showToast(messageRes: Int) {
        Toast.makeText(this,
            messageRes,
            Toast.LENGTH_LONG).show()
    }

    override fun getStringWithParams(stringRes: Int, currentValue: Int, maxValue: Int): String {
        return getString(stringRes, currentValue, maxValue)
    }

    override fun setSyncItem(enabled: Boolean, string: String, icon: Int) {
        dashboardButtonSync.visibility = View.GONE
        dashboardSyncState.isEnabled = enabled
        dashboardSyncStateText.text = string
        dashboardSyncStateIcon.setImageDrawable(ContextCompat.getDrawable(this, icon))
    }

    override fun launchAlertView(error: ALERT_TYPE) {
        this.launchAlert(error)
    }
}
