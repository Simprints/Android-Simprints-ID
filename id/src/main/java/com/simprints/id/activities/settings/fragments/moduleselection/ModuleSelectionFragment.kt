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
import com.google.android.material.chip.Chip
import com.simprints.id.R
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleSelectionListener
import com.simprints.id.moduleselection.model.Module
import kotlinx.android.synthetic.main.fragment_module_selection.*

class ModuleSelectionFragment(
    private val applicationContext: Context
) : Fragment(), ModuleSelectionListener {

    private val adapter by lazy { ModuleAdapter(listener = this) }

    private lateinit var viewModel: ModuleViewModel
    private lateinit var queryListener: ModuleSelectionQueryListener

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

    override fun onModuleSelected(module: Module) {
        updateSelectedModules()
        addChipForModule(module)
    }

    private fun fetchData() {
        viewModel.getModules().observe(this, Observer { modules ->
            this.modules = modules
            adapter.submitList(modules.filter { !it.isSelected })
            configureSearchView()
            observeSearchResults()
            configureNoModulesSelectedTextVisibility()
            displaySelectedModules()
        })
    }

    private fun configureSearchView() {
        queryListener = ModuleSelectionQueryListener(modules)
        searchView.setOnQueryTextListener(queryListener)
    }

    private fun displaySelectedModules() {
        modules.filter {
            it.isSelected && !selectedModules.contains(it)
        }.forEach { selectedModule ->
            addChipForModule(selectedModule)
        }
        selectedModules = modules.filter { it.isSelected }
    }

    private fun addChipForModule(selectedModule: Module) {
        val chip = Chip(requireContext()).apply {
            text = selectedModule.name
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                modules.first { module ->
                    module.name == selectedModule.name
                }.isSelected = false
                updateSelectedModules()
                chipGroup.removeView(it)
            }
        }

        chipGroup.addView(chip)
    }

    private fun observeSearchResults() {
        queryListener.searchResults.observe(this, Observer { searchResults ->
            adapter.submitList(searchResults)

            txtNoResults.visibility = if (searchResults.isEmpty()) VISIBLE else GONE
        })
    }

    private fun updateSelectedModules() {
        selectedModules = modules.filter { it.isSelected }
        val maxSelectedModules = viewModel.getMaxSelectedModules()
        when {
            selectedModules.isEmpty() -> noModulesSelected()

            selectedModules.size > maxSelectedModules -> tooManyModulesSelected(
                maxSelectedModules
            )

            else -> viewModel.updateModules(modules)
        }
    }

    private fun noModulesSelected() {
        Toast.makeText(
            applicationContext,
            R.string.settings_no_modules_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun tooManyModulesSelected(maxAllowed: Int) {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.settings_too_many_modules_toast, maxAllowed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun configureNoModulesSelectedTextVisibility() {
        txtNoModulesSelected.visibility = if (modules.none { it.isSelected }) VISIBLE else GONE
    }

}
