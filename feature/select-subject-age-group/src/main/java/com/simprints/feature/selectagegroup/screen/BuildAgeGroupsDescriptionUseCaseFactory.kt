package com.simprints.feature.selectagegroup.screen

import android.content.Context
import com.simprints.infra.config.store.ConfigRepository
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

internal class BuildAgeGroupsDescriptionUseCaseFactory @Inject constructor(
    private val configurationRepo: ConfigRepository,
    @ActivityContext private val context: Context,
) {
    fun create() = BuildAgeGroupsDescriptionUseCase(configurationRepo, context)
}
