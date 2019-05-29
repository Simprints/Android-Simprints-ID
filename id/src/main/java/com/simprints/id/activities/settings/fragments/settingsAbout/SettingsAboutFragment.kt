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
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning


class SettingsAboutFragment : PreferenceFragment(), SettingsAboutContract.View {

    override lateinit var packageVersionName: String
    override lateinit var viewPresenter: SettingsAboutContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_app_details)
        setHasOptionsMenu(true)

        val component = (activity.application as Application).component
        packageVersionName = activity.packageVersionName

        viewPresenter = SettingsAboutPresenter(this, component)
        viewPresenter.start()
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
        getString(R.string.preference_logout_key)

    override fun getKeyForSyncAndSearchConfigurationPreference(): String =
        getString(R.string.preference_sync_and_search_key)

    override fun getKeyForAppVersionPreference(): String =
        getString(R.string.preference_app_version_key)

    override fun getKeyForScannerVersionPreference(): String =
        getString(R.string.preference_scanner_version_key)

    override fun getKeyForDeviceIdPreference(): String =
        getString(R.string.preference_device_id_key)

    override fun showConfirmationDialogForLogout() {
        activity.runOnUiThreadIfStillRunning {
            buildConfirmationDialogForLogout().let {
                it.show()
            }
        }
    }

    internal fun buildConfirmationDialogForLogout(): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(R.string.confirmation_logout_title)
            .setMessage(R.string.confirmation_logout_message)
            .setPositiveButton(getString(R.string.logout)) { _, _ -> viewPresenter.logout() }
            .setNegativeButton(getString(R.string.confirmation_logout_cancel), null).create()

    override fun finishSettings() {
        activity.runOnUiThreadIfStillRunning {
            (activity as SettingsAboutActivity).finishActivityBecauseLogout()
        }
    }
}
