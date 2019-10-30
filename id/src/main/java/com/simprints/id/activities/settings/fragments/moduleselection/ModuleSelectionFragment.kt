package com.simprints.id.activities.settings.fragments.moduleselection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionTracker
import com.simprints.id.moduleselection.ModuleSelectionCallback
import com.simprints.id.moduleselection.model.Module
import kotlinx.android.synthetic.main.fragment_module_selection.*

class ModuleSelectionFragment private constructor()
    : Fragment(), ModuleSelectionCallback, ModuleSelectionTracker {

    private val adapter by lazy { ModuleAdapter(tracker = this) }

    private lateinit var viewModel: ModuleViewModel

    private var modules = emptyList<Module>()
    private var selectedModulesCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView.queryHint = getString(R.string.hint_search_modules)
        rvModules.adapter = adapter
        viewModel = ViewModelProviders.of(this).get(ModuleViewModel::class.java)
        fetchData()
    }

    override fun noModulesSelected() {
        Log.d("TEST_ALAN", "No modules")
    }

    override fun tooManyModulesSelected() {
        Log.d("TEST_ALAN", "Too many")
    }

    override fun onSelectionStateChanged(module: Module) {
        modules.find { it.name == module.name }?.isSelected = module.isSelected
        selectedModulesCount = modules.count { it.isSelected }
        txtNoModulesSelected.visibility = if (selectedModulesCount == 0) VISIBLE else GONE
        updateSelectedModules()
    }

    private fun fetchData() {
        getAvailableModules()
        getSelectedModules()
    }

    private fun getAvailableModules() {
        viewModel.getAvailableModules().observe(this, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules)
        })
    }

    private fun getSelectedModules() {
        viewModel.getSelectedModules().observe(this, Observer { selectedModules ->
            modules.forEach { module ->
                txtNoModulesSelected.visibility = GONE
                module.isSelected = selectedModules.any { it.name == module.name }
            }
        })
    }

    private fun updateSelectedModules() {
        viewModel.setSelectedModules(modules.filter { it.isSelected })
    }

    companion object {
        fun getInstance(): ModuleSelectionFragment = lazy { ModuleSelectionFragment() }.value
    }

}
