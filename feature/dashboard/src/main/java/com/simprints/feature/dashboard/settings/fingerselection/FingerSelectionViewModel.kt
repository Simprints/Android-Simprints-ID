package com.simprints.feature.dashboard.settings.fingerselection

import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SETTINGS
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class FingerSelectionViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : BaseMviViewModel<FingerSelectionUiState, FingerSelectionUiAction, FingerSelectionUiEffect>(FingerSelectionUiState()) {
    override suspend fun handleAction(action: FingerSelectionUiAction) {
        when (action) {
            FingerSelectionUiAction.Load -> load()
        }
    }

    private suspend fun load() {
        val fingerSelections = buildList {
            configRepository.getProjectConfiguration().fingerprint?.secugenSimMatcher?.fingersToCapture?.let {
                add(FingerSelectionSection("SimMatcher", it.toFingerSelectionItems()))
            }
            configRepository.getProjectConfiguration().fingerprint?.nec?.fingersToCapture?.let {
                add(FingerSelectionSection("NEC", it.toFingerSelectionItems()))
            }
        }
        reduce { it.copy(sections = fingerSelections) }
    }

    // Unknown fingers (a type this app version does not know maps to NONE) are skipped and logged; the section stays, possibly empty.
    private fun List<TemplateIdentifier>.toFingerSelectionItems(): List<FingerSelectionItem> {
        val unknownCount = count { it == TemplateIdentifier.NONE }
        if (unknownCount > 0) {
            Simber.w(
                "Skipped $unknownCount unknown finger(s) in finger selection",
                IllegalArgumentException("Unknown finger in the fingerprint configuration"),
                tag = SETTINGS,
            )
        }
        return filter { it != TemplateIdentifier.NONE }
            .groupingBy { it }
            .eachCount()
            .map { (finger, quantity) -> FingerSelectionItem(finger, quantity) }
    }
}

internal data class FingerSelectionSection(
    val sdkName: String,
    val items: List<FingerSelectionItem>,
)

internal data class FingerSelectionItem(
    val finger: TemplateIdentifier,
    val quantity: Int,
)
