package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


class SettingsAboutFragment : PreferenceFragment(), SettingsAboutContract.View {

    override lateinit var packageVersionName: String
    override lateinit var deviceId: String
    override lateinit var viewPresenter: SettingsAboutContract.Presenter

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_app_details)
        setHasOptionsMenu(true)

        val component = (activity.application as Application).component
        component.inject(this)

        setTextInLayout()

        packageVersionName = activity.packageVersionName
        deviceId = activity.deviceId

        viewPresenter = SettingsAboutPresenter(this, component)
        viewPresenter.start()
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            getAppVersionPreference().title = getString(R.string.preference_app_version_title)
            getDeviceIdPreference().title = getString(R.string.preference_device_id_title)
            getScannerVersionPreference().title = getString(R.string.preference_scanner_version_title)
            getSyncAndSearchConfigurationPreference().title = getString(R.string.preference_sync_and_search_title)
            getLogoutPreference().title = getString(R.string.preference_logout_title)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSyncAndSearchConfigurationPreference(): Preference =
        findPreference(getKeyForSyncAndSearchConfigurationPreference())

    override fun getAppVersionPreference(): Preference =
        findPreference(getKeyForAppVersionPreference())

    override fun getScannerVersionPreference(): Preference =
        findPreference(getKeyForScannerVersionPreference())

    override fun getDeviceIdPreference(): Preference =
        findPreference(getKeyForDeviceIdPreference())

    override fun getLogoutPreference(): Preference =
        findPreference(getKeyForLogoutPreference())

    override fun getKeyForLogoutPreference(): String =
        androidResourcesHelper.getString(R.string.preference_logout_key)

    override fun getKeyForSyncAndSearchConfigurationPreference(): String =
        androidResourcesHelper.getString(R.string.preference_sync_and_search_key)

    override fun getKeyForAppVersionPreference(): String =
        androidResourcesHelper.getString(R.string.preference_app_version_key)

    override fun getKeyForScannerVersionPreference(): String =
        androidResourcesHelper.getString(R.string.preference_scanner_version_key)

    override fun getKeyForDeviceIdPreference(): String =
        androidResourcesHelper.getString(R.string.preference_device_id_key)

    override fun showConfirmationDialogForLogout() {
        activity.runOnUiThreadIfStillRunning {
            buildConfirmationDialogForLogout().show()
        }
    }

    internal fun buildConfirmationDialogForLogout(): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(androidResourcesHelper.getString(R.string.confirmation_logout_title))
            .setMessage(androidResourcesHelper.getString(R.string.confirmation_logout_message))
            .setPositiveButton(
                androidResourcesHelper.getString(R.string.logout)
            ) { _, _ ->
                lifecycleScope.launch {
                     viewPresenter.logout()
                }
            }
            .setNegativeButton(
                androidResourcesHelper.getString(R.string.confirmation_logout_cancel), null
            ).create()

    override fun finishSettings() {
        activity.runOnUiThreadIfStillRunning {
            (activity as SettingsAboutActivity).finishActivityBecauseLogout()
        }
    }
}
