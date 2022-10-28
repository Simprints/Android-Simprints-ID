package com.simprints.id.activities.settings.fragments.settingsAbout

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.extentions.showToast
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.settings.SettingsAboutActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.extensions.enablePreference
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR


@AndroidEntryPoint
class SettingsAboutFragment : PreferenceFragmentCompat() {

    @Inject
    @PackageVersionName
    lateinit var packageVersionName: String

    @Inject
    @DeviceID
    lateinit var deviceId: String

    private val confirmationDialogForLogout: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(IDR.string.confirmation_logout_title))
            .setMessage(getString(IDR.string.confirmation_logout_message))
            .setPositiveButton(
                getString(IDR.string.logout)
            ) { _, _ ->
                settingsAboutViewModel.logout()
                finishSettings()
            }
            .setNegativeButton(
                getString(IDR.string.confirmation_logout_cancel), null
            ).create()
    }

    private val settingsAboutViewModel: SettingsAboutViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_app_details)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setTextInLayout()
        setPreferenceListeners()

        loadPreferenceValuesAndBindThemToChangeListeners()

        settingsAboutViewModel.configuration.observe(this) {
            loadSyncAndSearchConfigurationPreference(it, getSyncAndSearchConfigurationPreference())
            enableSettingsBasedOnModalities(it, getScannerVersionPreference())
        }
        settingsAboutViewModel.recentUserActivity.observe(this) {
            loadScannerVersionInPreference(getScannerVersionPreference(), it)
        }
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadAppVersionInPreference(getAppVersionPreference(), packageVersionName)
        loadDeviceIdInPreference(getDeviceIdPreference(), deviceId)
        getLogoutPreference()?.setOnPreferenceClickListener {
            showConfirmationDialogForLogout()
            true
        }
    }

    private fun setPreferenceListeners() {
        if (BuildConfig.DEBUG_MODE) {
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
        getAppVersionPreference()?.title = getString(IDR.string.preference_app_version_title)
        getDeviceIdPreference()?.title = getString(IDR.string.preference_device_id_title)
        getScannerVersionPreference()?.title =
            getString(IDR.string.preference_scanner_version_title)
        getSyncAndSearchConfigurationPreference()?.title =
            getString(IDR.string.preference_sync_and_search_title)
        getLogoutPreference()?.title = getString(IDR.string.preference_logout_title)
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
        findPreference(getString(R.string.preference_sync_and_search_key))

    private fun getAppVersionPreference(): Preference? =
        findPreference(getString(R.string.preference_app_version_key))

    private fun getScannerVersionPreference(): Preference? =
        findPreference(getString(R.string.preference_scanner_version_key))

    private fun getDeviceIdPreference(): Preference? =
        findPreference(getString(R.string.preference_device_id_key))

    private fun getLogoutPreference(): Preference? =
        findPreference(getString(R.string.preference_logout_key))

    private fun showConfirmationDialogForLogout() {
        activity?.runOnUiThreadIfStillRunning {
            confirmationDialogForLogout.show()
        }
    }

    private fun enableSettingsBasedOnModalities(
        config: ProjectConfiguration,
        scannerVersionPref: Preference?
    ) {
        config.general.modalities.forEach {
            when (it) {
                GeneralConfiguration.Modality.FINGERPRINT -> enableFingerprintSettings(
                    scannerVersionPref
                )
                GeneralConfiguration.Modality.FACE -> enableFaceSettings()
            }
        }
    }

    private fun enableFingerprintSettings(scannerVersionPref: Preference?) {
        scannerVersionPref?.enablePreference()
    }

    private fun enableFaceSettings() {
        // No face-specific settings yet
    }

    private fun loadSyncAndSearchConfigurationPreference(
        config: ProjectConfiguration,
        preference: Preference?
    ) {
        preference?.summary =
            "${config.synchronization.down.partitionType.name.lowerCaseCapitalized()} Sync" +
                " - ${config.identification.poolType.name.lowerCaseCapitalized()} Search"
    }

    private fun String.lowerCaseCapitalized() =
        lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    private fun loadAppVersionInPreference(preference: Preference?, packageVersionName: String) {
        preference?.summary = packageVersionName
    }

    private fun loadScannerVersionInPreference(
        preference: Preference?,
        recentUserActivity: RecentUserActivity
    ) {
        preference?.summary = recentUserActivity.lastScannerVersion
    }

    private fun loadDeviceIdInPreference(preference: Preference?, deviceId: String) {
        preference?.summary = deviceId
    }

    private fun finishSettings() {
        activity?.runOnUiThreadIfStillRunning {
            (activity as SettingsAboutActivity).finishActivityBecauseLogout()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (confirmationDialogForLogout.isShowing) {
            confirmationDialogForLogout.dismiss()
        }
    }
}
