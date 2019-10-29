package com.simprints.id.activities.settings.fragments.moduleselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.moduleselection.ModuleSelectionCallback
import com.simprints.id.moduleselection.model.Module
import kotlinx.android.synthetic.main.fragment_module_selection.*

class ModuleSelectionFragment private constructor(): Fragment(), ModuleSelectionCallback {

    private val adapter by lazy { ModuleAdapter() }

    private lateinit var viewModel: ModuleViewModel

    private var modules = emptyList<Module>()

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
        // TODO: show toast
    }

    override fun tooManyModulesSelected() {
        // TODO: show toast
    }

    override fun onSuccess() {
        // TODO: close
    }

    fun updateSelectedModules() {
        viewModel.setSelectedModules(modules.filter { it.isSelected })
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
            modules = modules.map { module ->
                module.apply {
                    isSelected = selectedModules.any { it.name == name }
                }
            }
        })
    }

    companion object {
        fun getInstance(): ModuleSelectionFragment = lazy { ModuleSelectionFragment() }.value
    }

}
