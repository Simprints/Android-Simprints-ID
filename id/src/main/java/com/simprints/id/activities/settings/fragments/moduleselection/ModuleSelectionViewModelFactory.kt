package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.services.sync.events.master.EventSyncManager

class ModuleSelectionViewModelFactory(
    private val moduleRepository: ModuleRepository,
    private val eventSyncManager: EventSyncManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ModuleSelectionViewModel::class.java)) {
            ModuleSelectionViewModel(moduleRepository, eventSyncManager) as T
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
    }

}
