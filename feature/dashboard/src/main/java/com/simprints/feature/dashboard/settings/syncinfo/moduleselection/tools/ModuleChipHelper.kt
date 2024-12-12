package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import android.content.Context
import android.text.TextUtils
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module

internal class ModuleChipHelper(
    private val context: Context,
    private val listener: ChipClickListener,
) {
    fun addModuleChip(
        parent: ChipGroup,
        module: Module,
    ) {
        parent.addView(createChipForModule(module))
    }

    fun removeModuleChip(
        parent: ChipGroup,
        module: Module,
    ) {
        parent.removeView(parent.findViewWithTag<Chip>(module.name.value))
    }

    fun findSelectedModuleNames(parent: ChipGroup): List<String> = parent.children
        .filterIsInstance<Chip>()
        .mapNotNull {
            it.tag as? String
        }.toList()

    private fun createChipForModule(module: Module): Chip {
        val chipDrawable = createChipDrawable()

        return Chip(context).apply {
            setChipDrawable(chipDrawable)
            text = module.name.value
            tag = module.name.value
            isCheckable = false
            ellipsize = TextUtils.TruncateAt.END
            setOnCloseIconClickListener {
                listener.onChipClick(module)
            }
        }
    }

    private fun createChipDrawable(): ChipDrawable = ChipDrawable.createFromResource(context, R.xml.module_selection_chip)
}
