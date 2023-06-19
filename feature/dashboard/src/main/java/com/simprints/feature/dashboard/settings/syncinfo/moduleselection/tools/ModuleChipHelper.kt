package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import android.content.Context
import android.text.TextUtils
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.infra.resources.R as IDR

internal class ModuleChipHelper(
    private val context: Context,
    private val listener: ChipClickListener
) {

    fun addModuleChip(parent: ChipGroup, module: Module) {
        parent.addView(createChipForModule(module))
    }

    fun removeModuleChip(parent: ChipGroup, module: Module) {
        parent.removeView(parent.findViewWithTag<Chip>(module.name))
    }

    fun findSelectedModuleNames(parent: ChipGroup): List<String> {
        return parent.children.filterIsInstance<Chip>().mapNotNull { it.tag as? String }.toList()
    }

    private fun createChipForModule(module: Module): Chip {
        val chipDrawable = createChipDrawable()

        return Chip(context).apply {
            setChipDrawable(chipDrawable)
            text = module.name
            tag = module.name
            isCheckable = false
            ellipsize = TextUtils.TruncateAt.END
            setOnCloseIconClickListener {
                listener.onChipClick(module)
            }
        }
    }

    private fun createChipDrawable(): ChipDrawable {
        return ChipDrawable.createFromResource(context, R.xml.module_selection_chip).apply {
            setTextAppearanceResource(IDR.style.SimprintsStyle_TextView_Chip)
            val cornerSize =
                context.resources.getDimension(IDR.dimen.chip_corner_size_module_selection)
            shapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCorners(CornerFamily.CUT, cornerSize).build()
        }
    }
}
