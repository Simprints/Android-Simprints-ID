package com.simprints.feature.externalcredential.screens.select

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.screens.controller.LayoutRepository

class LayoutConfigBottomSheetDialog(
    context: Context,
    private val initialConfig: LayoutConfig,
    private val onDismissed: (LayoutConfig) -> Unit = {}
) : BottomSheetDialog(context) {

    private var config = initialConfig.copy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_layout_config, null)
        setContentView(view)

        setupLayoutStyleSection(view)
        setupTitleSection(view)
        setupButtonSection(view)

        setOnDismissListener {
            onDismissed(config)
        }

        val bottomSheet = delegate.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
    }

    private fun setupLayoutStyleSection(view: View) {
        val gridCard = view.findViewById<MaterialCardView>(R.id.cardGrid)
        val verticalCard = view.findViewById<MaterialCardView>(R.id.cardVertical)
        val strokeWidth = 12
        fun updateSelection() {
            gridCard.isSelected = config.layoutStyle == LayoutStyle.Grid
            verticalCard.isSelected = config.layoutStyle == LayoutStyle.Vertical
        }

        gridCard.strokeWidth = if (config.layoutStyle == LayoutStyle.Grid) 16 else 0
        gridCard.setOnClickListener {
            config = config.copy(layoutStyle = LayoutStyle.Grid)
            gridCard.strokeWidth = strokeWidth
            verticalCard.strokeWidth = 0
            updateSelection()
        }
        verticalCard.strokeWidth = if (config.layoutStyle == LayoutStyle.Vertical) 16 else 0
        verticalCard.setOnClickListener {
            config = config.copy(layoutStyle = LayoutStyle.Vertical)
            gridCard.strokeWidth = 0
            verticalCard.strokeWidth = strokeWidth
            updateSelection()
        }

        updateSelection()
    }

    private fun setupTitleSection(view: View) {
        setupTitleField(
            view = view,
            options = listOf("Scan external credential", "Select document to scan", "Add patient document"),
            titleEditId = R.id.topScreenTitleEdit,
            checkboxId = null,
            suggestionsContainerId = R.id.topScreenTitleSuggestions,
            currentText = config.screenTitle,
            isVisible = true
        ) { newText, visible ->
            config = config.copy(screenTitle = newText)
        }

        setupTitleField(
            view = view,
            options = listOf("Freestyle", "Non-card"),
            titleEditId = R.id.topTitleEdit,
            checkboxId = R.id.topTitleCheckbox,
            suggestionsContainerId = R.id.topTitleSuggestions,
            currentText = config.topTitle,
            isVisible = config.isTopTitleVisible
        ) { newText, visible ->
            config = config.copy(topTitle = newText, isTopTitleVisible = visible)
        }

        setupTitleField(
            view = view,
            options = listOf("Predefined", "ID cards", "Documents"),
            titleEditId = R.id.bottomTitleEdit,
            checkboxId = R.id.bottomTitleCheckbox,
            suggestionsContainerId = R.id.bottomTitleSuggestions,
            currentText = config.bottomTitle,
            isVisible = config.isBottomTitleVisible
        ) { newText, visible ->
            config = config.copy(bottomTitle = newText, isBottomTitleVisible = visible)
        }
    }

    private fun setupTitleField(
        view: View,
        options: List<String>,
        titleEditId: Int,
        checkboxId: Int?,
        suggestionsContainerId: Int,
        currentText: String?,
        isVisible: Boolean,
        onUpdate: (String, Boolean) -> Unit
    ) {
        val editText = view.findViewById<EditText>(titleEditId)
        val checkBox = checkboxId?.let { view.findViewById<CheckBox>(it) }
        val container = view.findViewById<LinearLayout>(suggestionsContainerId)

        editText.setText(currentText)
        checkBox?.isChecked = isVisible

        val chips = getChips(options, context) {
            editText.setText(it)
            onUpdate(it, checkBox?.isChecked ?: true)
        }.onEach { container.addView(it) }

        editText.doAfterTextChanged {
            onUpdate(it.toString(), checkBox?.isChecked ?: true)
        }
        val color = ContextCompat.getColor(
            context,
            if (isVisible) com.simprints.infra.resources.R.color.simprints_text_grey
            else com.simprints.infra.resources.R.color.simprints_grey_light
        )
        val chipColor = ContextCompat.getColor(
            context,
            if (isVisible) com.simprints.infra.resources.R.color.simprints_blue
            else com.simprints.infra.resources.R.color.simprints_grey_light
        )

        editText.setTextColor(color)
        chips.forEach { it.chipBackgroundColor = ColorStateList.valueOf(chipColor) }

        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            val color = ContextCompat.getColor(
                context,
                if (isChecked) com.simprints.infra.resources.R.color.simprints_text_grey
                else com.simprints.infra.resources.R.color.simprints_grey_light
            )
            val chipColor = ContextCompat.getColor(
                context,
                if (isChecked) com.simprints.infra.resources.R.color.simprints_blue
                else com.simprints.infra.resources.R.color.simprints_grey_light
            )

            editText.setTextColor(color)
            chips.forEach { it.chipBackgroundColor = ColorStateList.valueOf(chipColor) }
            onUpdate(editText.text.toString(), checkBox?.isChecked ?: true)
        }
    }

    private fun setupButtonSection(view: View) {
        setupButtonEdit(
            view,
            currentText = config.uiText.qrText,
            listOf("Scan QR code", "QR Code", "Open QR code scanner"),
            R.id.scanQrText,
            R.id.chipsQrText
        ) { text ->
            config = config.copy(uiText = config.uiText.copy(qrText = text))
        }
        setupButtonEdit(
            view,
            currentText = config.uiText.anyDocText,
            listOf("Any document", "Scan other doc", "Other"),
            R.id.anyDoc,
            R.id.chipsAnyDocText
        ) { text ->
            config = config.copy(uiText = config.uiText.copy(anyDocText = text))
        }
        setupButtonEdit(
            view,
            currentText = config.uiText.ghanaCardText,
            listOf("\uD83C\uDDEC\uD83C\uDDED Ghana ID Card", "Scan ID card"),
            R.id.ghanaIdText,
            R.id.chipsGhanaCardText
        ) { text ->
            config = config.copy(uiText = config.uiText.copy(ghanaCardText = text))
        }
        setupButtonEdit(
            view, currentText = config.uiText.nhisCardText,
            listOf("\uD83C\uDFE5 Ghana NHIS Card", "Scan NHIS card"), R.id.nhisCardText, R.id.chipsNhisCardText
        ) { text ->
            config = config.copy(uiText = config.uiText.copy(nhisCardText = text))
        }
    }

    private fun setupButtonEdit(
        view: View,
        currentText: String,
        options: List<String>,
        editId: Int,
        chipsContainerId: Int,
        onUpdate: (String) -> Unit
    ) {
        val editText = view.findViewById<EditText>(editId)
        val container = view.findViewById<LinearLayout>(chipsContainerId)

        editText.setText(currentText)
        getChips(options, context) {
            editText.setText(it)
            onUpdate(it)
        }.onEach { chip -> container.addView(chip) }

        editText.doAfterTextChanged {
            onUpdate(it.toString())
        }
    }

    private fun getChips(options: List<String>, context: Context, onClick: (String) -> Unit): List<Chip> {
        return options.map { option ->
            val chip = Chip(context).apply {
                this.text = option
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_blue))
                setTextColor(ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_text_white))
                isClickable = true
                isCheckable = false
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
                ).toInt()
                layoutParams = params
                chipStrokeWidth = 0f
                chipStrokeColor = null
                isCloseIconVisible = false
                setOnClickListener {
                    onClick(option)
                }
            }
            chip
        }
    }
}
