package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.tools.extentions.showToast
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.enablePreference
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import javax.inject.Inject

class SettingsAboutFragment : PreferenceFragmentCompat() {

    private lateinit var packageVersionName: String
    private lateinit var deviceId: String

    @Inject
    lateinit var recentEventsManager: RecentEventsPreferencesManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var settingsAboutViewModelFactory: SettingsAboutViewModelFactory
    lateinit var settingsAboutViewModel: SettingsAboutViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_app_details)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val component = (requireActivity().application as Application).component
        component.inject(this)

        settingsAboutViewModel = ViewModelProvider(this, settingsAboutViewModelFactory).get(SettingsAboutViewModel::class.java)
        setTextInLayout()
        setPreferenceListeners()

        packageVersionName = requireActivity().packageVersionName
        deviceId = requireActivity().deviceId

        loadPreferenceValuesAndBindThemToChangeListeners()
        enableSettingsBasedOnModalities(getScannerVersionPreference())
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadValueAndBindChangeListener(getSyncAndSearchConfigurationPreference())
        loadValueAndBindChangeListener(getAppVersionPreference())
        loadValueAndBindChangeListener(getScannerVersionPreference())
        loadValueAndBindChangeListener(getDeviceIdPreference())
        loadValueAndBindChangeListener(getLogoutPreference())
    }

    private fun setPreferenceListeners() {
        if (BuildConfig.DEBUG) {
            getDeviceIdPreference()?.setOnPreferenceClickListener {
                with(activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager) {
                    val clip = ClipData.newPlainText("deviceID", deviceId)
                    setPrimaryClip(clip)
                }

                context?.showToast("Your Device Id $deviceId was copied to the clipboard")

                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun setTextInLayout() {
        getAppVersionPreference()?.title = getString(R.string.preference_app_version_title)
        getDeviceIdPreference()?.title = getString(R.string.preference_device_id_title)
        getScannerVersionPreference()?.title = getString(R.string.preference_scanner_version_title)
        getSyncAndSearchConfigurationPreference()?.title = getString(R.string.preference_sync_and_search_title)
        getLogoutPreference()?.title = getString(R.string.preference_logout_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getSyncAndSearchConfigurationPreference(): Preference? =
        findPreference(getKeyForSyncAndSearchConfigurationPreference())

    private fun getAppVersionPreference(): Preference? =
        findPreference(getKeyForAppVersionPreference())

    private fun getScannerVersionPreference(): Preference? =
        findPreference(getKeyForScannerVersionPreference())

    private fun getDeviceIdPreference(): Preference? =
        findPreference(getKeyForDeviceIdPreference())

    private fun getLogoutPreference(): Preference? =
        findPreference(getKeyForLogoutPreference())

    private fun getKeyForLogoutPreference(): String =
        getString(R.string.preference_logout_key)

    private fun getKeyForSyncAndSearchConfigurationPreference(): String =
        getString(R.string.preference_sync_and_search_key)

    private fun getKeyForAppVersionPreference(): String =
        getString(R.string.preference_app_version_key)

    private fun getKeyForScannerVersionPreference(): String =
        getString(R.string.preference_scanner_version_key)

    private fun getKeyForDeviceIdPreference(): String =
        getString(R.string.preference_device_id_key)

    private fun showConfirmationDialogForLogout() {
        activity?.runOnUiThreadIfStillRunning {
            buildConfirmationDialogForLogout().show()
        }
    }

    private fun loadValueAndBindChangeListener(preference: Preference?) {
        when (preference?.key) {
            getKeyForSyncAndSearchConfigurationPreference() -> {
                loadSyncAndSearchConfigurationPreference(preference)
            }
            getKeyForAppVersionPreference() -> {
                loadAppVersionInPreference(preference, packageVersionName)
            }
            getKeyForScannerVersionPreference() -> {
                loadScannerVersionInPreference(preference)
            }
            getKeyForDeviceIdPreference() -> {
                loadDeviceIdInPreference(preference, deviceId)
            }
            getKeyForLogoutPreference() -> {
                preference.setOnPreferenceClickListener {
                    showConfirmationDialogForLogout()
                    true
                }
            }
        }
    }

    private fun enableSettingsBasedOnModalities(scannerVersionPref: Preference?) {
        preferencesManager.modalities.forEach {
            when (it) {
                Modality.FINGER -> enableFingerprintSettings(scannerVersionPref)
                Modality.FACE -> enableFaceSettings()
            }
        }
    }

    private fun enableFingerprintSettings(scannerVersionPref: Preference?) {
        scannerVersionPref?.enablePreference()
    }

    private fun enableFaceSettings() {
        // No face-specific settings yet
    }

    private fun loadSyncAndSearchConfigurationPreference(preference: Preference?) {
        preference?.summary = "${preferencesManager.syncGroup.lowerCaseCapitalized()} Sync" +
            " - ${preferencesManager.matchGroup.lowerCaseCapitalized()} Search"
    }

    private fun GROUP.lowerCaseCapitalized() = toString().toLowerCase().capitalize()

    private fun loadAppVersionInPreference(preference: Preference, packageVersionName: String) {
        preference.summary = packageVersionName
    }

    private fun loadScannerVersionInPreference(preference: Preference) {
        preference.summary = recentEventsManager.lastScannerVersion
    }

    private fun loadDeviceIdInPreference(preference: Preference, deviceId: String) {
        preference.summary = deviceId
    }

    internal fun buildConfirmationDialogForLogout(): AlertDialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirmation_logout_title))
            .setMessage(getString(R.string.confirmation_logout_message))
            .setPositiveButton(
                getString(R.string.logout)
            ) { _, _ ->
                settingsAboutViewModel.logout()
                finishSettings()
            }
            .setNegativeButton(
                getString(R.string.confirmation_logout_cancel), null
            ).create()

    private fun finishSettings() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsAboutActivity).finishActivityBecauseLogout()
        }
    }
}
