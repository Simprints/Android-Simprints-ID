package com.simprints.feature.selectagegroup.screen

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.sortedUniqueAgeGroups
import javax.inject.Inject

internal class BuildAgeGroupsUseCase @Inject constructor(
    private val configurationRepo: ConfigRepository,
) {

    /**
     * Builds a list of age groups for display it reads the allowed age ranges
     * from the configuration and adds a 0-<first age range> and <last age range>-above
     * if they are not present
     */
    suspend operator fun invoke(): List<AgeGroup> {
        return configurationRepo.getProjectConfiguration().sortedUniqueAgeGroups()
    }

}
