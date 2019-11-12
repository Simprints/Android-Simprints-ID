package com.simprints.id.activities.settings.fragments.moduleselection.tools

import android.content.Context
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.simprints.id.R
import com.simprints.id.moduleselection.model.Module
import org.jetbrains.anko.dimen

class ModuleChipHelper(private val context: Context, private val listener: ChipClickListener) {

    fun createChipForModule(module: Module): Chip {
        val chipDrawable = createChipDrawable()

        return Chip(context).apply {
            setChipDrawable(chipDrawable)
            text = module.name
            isCheckable = false
            setOnCloseIconClickListener {
                (it as Chip).isSelected = true
                listener.onChipClick(module)
            }
        }
    }

    fun findSelectedChip(parent: ChipGroup): Chip? {
        return parent.children.filterIsInstance<Chip>().find { it.isSelected }
    }

    private fun createChipDrawable(): ChipDrawable {
        return ChipDrawable.createFromResource(context, R.xml.module_selection_chip).apply {
            setTextAppearanceResource(R.style.SimprintsStyle_TextView_White)
            shapeAppearanceModel = ShapeAppearanceModel().also { shapeAppearanceModel ->
                val cornerSize = context.dimen(R.dimen.chip_corner_size_module_selection)
                shapeAppearanceModel.setAllCorners(CornerFamily.CUT, cornerSize)
            }
        }
    }

}
