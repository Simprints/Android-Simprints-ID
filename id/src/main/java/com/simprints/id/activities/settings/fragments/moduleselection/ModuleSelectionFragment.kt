package com.simprints.id.activities.settings.fragments.moduleselection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionTracker
import com.simprints.id.moduleselection.ModuleSelectionCallback
import com.simprints.id.moduleselection.model.Module
import kotlinx.android.synthetic.main.fragment_module_selection.*

class ModuleSelectionFragment private constructor(
    private val applicationContext: Context
) : Fragment(),
    ModuleSelectionCallback,
    ModuleSelectionTracker,
    ModuleSelectionQueryListener.SearchResultCallback {

    private val adapter by lazy { ModuleAdapter(tracker = this) }

    private lateinit var viewModel: ModuleViewModel

    private var modules = emptyList<Module>()
    private var selectedModules = emptyList<Module>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvModules.adapter = adapter
        viewModel = ViewModelProviders.of(this).get(ModuleViewModel::class.java)
        fetchData()
    }

    override fun noModulesSelected() {
        Toast.makeText(
            applicationContext,
            R.string.settings_no_modules_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun tooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.settings_too_many_modules_toast, maxAllowed),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSelectionStateChanged(module: Module) {
        modules.find { it.name == module.name }?.isSelected = module.isSelected
        selectedModules = modules.filter { it.isSelected }
        configureNoModulesSelectedTextVisibility(selectedModules)
        updateSelectedModules()
    }

    override fun onNothingFound() {
        txtNoResults.visibility = VISIBLE
    }

    override fun onResultsFound() {
        txtNoResults.visibility = GONE
    }

    private fun fetchData() {
        getAvailableModules()
        getSelectedModules()
    }

    private fun getAvailableModules() {
        viewModel.getAvailableModules().observe(this, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules)
            searchView.setOnQueryTextListener(
                ModuleSelectionQueryListener(adapter, modules, searchResultCallback = this)
            )
        })
    }

    private fun getSelectedModules() {
        viewModel.getSelectedModules().observe(this, Observer { selectedModules ->
            modules.forEach { module ->
                configureNoModulesSelectedTextVisibility(selectedModules)
                module.isSelected = selectedModules.any { it.name == module.name }
            }
        })
    }

    private fun updateSelectedModules() {
        viewModel.setSelectedModules(modules.filter { it.isSelected })
    }

    private fun configureNoModulesSelectedTextVisibility(selectedModules: List<Module>) {
        txtNoModulesSelected.visibility = if (selectedModules.isEmpty()) VISIBLE else GONE
    }

    companion object {
        fun getInstance(applicationContext: Context): ModuleSelectionFragment = lazy {
            ModuleSelectionFragment(applicationContext)
        }.value
    }

}
