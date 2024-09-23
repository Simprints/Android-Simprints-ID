package com.simprints.feature.exitform

import androidx.annotation.StringRes
import com.simprints.feature.exitform.config.ExitFormConfiguration
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.feature.exitform.screen.ExitFormFragmentArgs

data class ExitFormConfigurationBuilder(
    var title: String? = null,
    @StringRes var titleRes: Int? = null,
    var backButton: String? = null,
    @StringRes var backButtonRes: Int? = null,
    var visibleOptions: Set<ExitFormOption> = defaultOptions(),
)

/**
 * Set of default refusal options
 */
fun defaultOptions() = setOf(
    ExitFormOption.ReligiousConcerns,
    ExitFormOption.DataConcerns,
    ExitFormOption.NoPermission,
    ExitFormOption.AppNotWorking,
    ExitFormOption.PersonNotPresent,
    ExitFormOption.TooYoung,
    ExitFormOption.WrongAgeGroupSelected,
    ExitFormOption.UncooperativeChild,
    ExitFormOption.Other,
)

/**
 * Same as default list, but AppNotWorking substituted with ScannerNotWorking
 */
fun scannerOptions() = setOf(
    ExitFormOption.ReligiousConcerns,
    ExitFormOption.DataConcerns,
    ExitFormOption.NoPermission,
    ExitFormOption.ScannerNotWorking,
    ExitFormOption.PersonNotPresent,
    ExitFormOption.TooYoung,
    ExitFormOption.WrongAgeGroupSelected,
    ExitFormOption.UncooperativeChild,
    ExitFormOption.Other,
)

/**
 * Configuration builder for exit form screen:
 *
 * ```
 * title - Pre-formatted title string
 * titleRes - Title string resource (used if title is null)
 * backButton - Pre-formatted text for "Go back" button in the form
 * backButtonRes - String resource for "Go back" button (used if text is null)
 * visibleOptions - Set of visible form option items, default - all items are visible.
 * ```
 */
fun exitFormConfiguration(block: ExitFormConfigurationBuilder.() -> Unit) =
    ExitFormConfigurationBuilder().apply(block)

fun ExitFormConfigurationBuilder.toArgs() = ExitFormFragmentArgs(ExitFormConfiguration(
    title = this.title,
    titleRes = this.titleRes,
    backButton = this.backButton,
    backButtonRes = this.backButtonRes,
    visibleOptions = this.visibleOptions,
)).toBundle()
