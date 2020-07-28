package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.simprints.core.tools.extentions.showToast
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsAboutFragment : PreferenceFragment(), SettingsAboutContract.View {

    override lateinit var packageVersionName: String
    override lateinit var deviceId: String
    override lateinit var viewPresenter: SettingsAboutContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_app_details)
        setHasOptionsMenu(true)

        val component = (activity.application as Application).component
        component.inject(this)

        setTextInLayout()
        setPreferenceListeners()

        packageVersionName = activity.packageVersionName
        deviceId = activity.deviceId

        viewPresenter = SettingsAboutPresenter(this, component)
        viewPresenter.start()
    }

    private fun setPreferenceListeners() {
        if (BuildConfig.DEBUG) {
            getDeviceIdPreference().setOnPreferenceClickListener {
                with(activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager) {
                    val clip = ClipData.newPlainText("deviceID", deviceId)
                    setPrimaryClip(clip)
                }

                context.showToast("Your Device Id $deviceId was copied to the clipboard")

                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun setTextInLayout() {
        getAppVersionPreference().title = getString(R.string.preference_app_version_title)
        getDeviceIdPreference().title = getString(R.string.preference_device_id_title)
        getScannerVersionPreference().title = getString(R.string.preference_scanner_version_title)
        getSyncAndSearchConfigurationPreference().title = getString(R.string.preference_sync_and_search_title)
        getLogoutPreference().title = getString(R.string.preference_logout_title)
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
            buildConfirmationDialogForLogout().show()
        }
    }

    override fun enablePreference(preference: Preference) {
        preference.isEnabled = true
    }

    internal fun buildConfirmationDialogForLogout(): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.confirmation_logout_title))
            .setMessage(getString(R.string.confirmation_logout_message))
            .setPositiveButton(
                getString(R.string.logout)
            ) { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    viewPresenter.logout()
                }
            }
            .setNegativeButton(
                getString(R.string.confirmation_logout_cancel), null
            ).create()

    override fun finishSettings() {
        activity.runOnUiThreadIfStillRunning {
            (activity as SettingsAboutActivity).finishActivityBecauseLogout()
        }
    }
}
