package com.simprints.id.activities.settings.syncinformation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TabHost
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.core.domain.common.GROUP
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCountAdapter
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.settings.canSyncToSimprints
import com.simprints.id.databinding.ActivitySyncInformationBinding
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import javax.inject.Inject

class SyncInformationActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: SyncInformationViewModelFactory

    @Inject
    lateinit var preferencesManager: IdPreferencesManager

    @Inject
    lateinit var eventSyncManager: EventSyncManager
    private val binding by viewBinding(ActivitySyncInformationBinding::inflate)

    private val moduleCountAdapterForSelected by lazy { ModuleCountAdapter() }

    private lateinit var viewModel: SyncInformationViewModel
    private lateinit var selectedModulesTabSpec: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        title = getString(R.string.title_activity_sync_information)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SyncInformationViewModel::class.java)

        setTextInLayout()
        enableModuleSelectionButtonAndTabsIfNecessary()
        setupAdapters()
        setupToolbar()
        setupModulesTabs()
        setupClickListeners()
        observeUi()
        setupRecordsCountCards()

        viewModel.fetchSyncInformation()
    }

    override fun onResume() {
        super.onResume()
        setFocusOnDefaultModulesTab()
    }

    private fun setTextInLayout() {
        binding.moduleSelectionButton.text =
            getString(R.string.select_modules_button_title)
        binding.recordsToUploadText.text =
            getString(R.string.sync_info_records_to_upload)
        binding.recordsToDownloadText.text =
            getString(R.string.sync_info_records_to_download)
        binding.recordsToDeleteText.text =
            getString(R.string.sync_info_records_to_delete)
        binding.totalRecordsOnDeviceText.text =
            getString(R.string.sync_info_total_records_on_device)
        binding.imagesToUploadText.text = getString(R.string.sync_info_images_to_upload)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sync_info_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.sync_redo -> {
                viewModel.resetFetchingSyncInformation()
                viewModel.fetchSyncInformation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setFocusOnDefaultModulesTab() {
        binding.modulesTabHost.setCurrentTabByTag(SELECTED_MODULES_TAB_TAG)
    }

    private fun enableModuleSelectionButtonAndTabsIfNecessary() {
        if (isModuleSyncAndModuleIdOptionsNotEmpty()) {
            binding.moduleSelectionButton.visibility = View.VISIBLE
            binding.modulesTabHost.visibility = View.VISIBLE
        } else {
            binding.moduleSelectionButton.visibility = View.GONE
            binding.modulesTabHost.visibility = View.GONE
        }
    }

    private fun setupAdapters() {
        with(binding.selectedModulesView) {
            adapter = moduleCountAdapterForSelected
            layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    private fun isModuleSyncAndModuleIdOptionsNotEmpty() =
        preferencesManager.moduleIdOptions.isNotEmpty() && preferencesManager.syncGroup == GROUP.MODULE

    private fun setupToolbar() {
        setSupportActionBar(binding.syncInfoToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupModulesTabs() {
        binding.modulesTabHost.setup()

        selectedModulesTabSpec = binding.modulesTabHost.newTabSpec(SELECTED_MODULES_TAB_TAG)
            .setIndicator(getString(R.string.sync_info_selected_modules))
            .setContent(R.id.selectedModulesView)

        binding.modulesTabHost.addTab(selectedModulesTabSpec)
    }

    private fun setupClickListeners() {
        binding.moduleSelectionButton.setOnClickListener {
            startActivity(Intent(this, ModuleSelectionActivity::class.java))
        }
    }

    private fun observeUi() {
        viewModel.recordsInLocal.observe(this, Observer {
            binding.totalRecordsCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.totalRecordsCount, binding.totalRecordsProgress)
        })

        viewModel.recordsToUpSync.observe(this, Observer {
            binding.recordsToUploadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToUploadCount, binding.recordsToUploadProgress)
        })

        viewModel.imagesToUpload.observe(this, Observer {
            binding.imagesToUploadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.imagesToUploadCount, binding.imagesToUploadProgress)
        })

        viewModel.recordsToDownSync.observe(this, Observer {
            binding.recordsToDownloadCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToDownloadCount, binding.recordsToDownloadProgress)
        })

        viewModel.recordsToDelete.observe(this, Observer {
            binding.recordsToDeleteCount.text = it?.toString() ?: ""
            setProgressBar(it, binding.recordsToDeleteCount, binding.recordsToDeleteProgress)
        })

        viewModel.moduleCounts.observe(this, Observer {
            it?.let {
                addTotalRowAndSubmitList(it, moduleCountAdapterForSelected)
            }
        })
    }

    private fun setProgressBar(value: Int?, tv: TextView, pb: ProgressBar) {
        if (value == null) {
            pb.visibility = View.VISIBLE
            tv.visibility = View.GONE
        } else {
            pb.visibility = View.GONE
            tv.visibility = View.VISIBLE
        }
    }

    private fun addTotalRowAndSubmitList(
        moduleCounts: List<ModuleCount>,
        moduleCountAdapter: ModuleCountAdapter
    ) {
        val moduleCountsArray = ArrayList<ModuleCount>().apply {
            addAll(moduleCounts)
        }

        val totalRecordsEntry =
            ModuleCount(getString(R.string.sync_info_total_records),
                moduleCounts.sumBy { it.count })
        moduleCountsArray.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

        moduleCountAdapter.submitList(moduleCountsArray)
    }

    private fun setupRecordsCountCards() {
        if (!isDownSyncAllowed()) {
            binding.recordsToDownloadCardView.visibility = View.GONE
            binding.recordsToDeleteCardView.visibility = View.GONE
        }

        if (!preferencesManager.canSyncToSimprints()) {
            binding.recordsToDownloadCardView.visibility = View.GONE
            binding.recordsToDeleteCardView.visibility = View.GONE
            binding.recordsToUploadCardView.visibility = View.GONE
            binding.imagesToUploadCardView.visibility = View.GONE
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
    }

    companion object {
        private const val SELECTED_MODULES_TAB_TAG = "SelectedModules"
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
