package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.MutableLiveData
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools.ModuleQueryFilter

internal class ModuleSelectionQueryListener(
    private val modules: List<Module>,
) : TextWatcher {
    val searchResults = MutableLiveData<List<Module>>()

    private val queryFilter = ModuleQueryFilter()

    override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int,
    ) {
    }

    override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int,
    ) {
    }

    override fun afterTextChanged(s: Editable?) {
        val newText = s?.toString().orEmpty()
        searchResults.value = queryFilter.getFilteredList(modules, newText)
    }
}
