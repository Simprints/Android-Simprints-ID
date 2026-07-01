package com.simprints.face.capture.usecases

import com.simprints.face.infra.biosdkresolver.RocV3BioSdk
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.experimental
import javax.inject.Inject

class GetSpoofCheckConfigurationUseCase @Inject constructor(
    private val rocV3BioSdk: RocV3BioSdk,
) {
    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        bioSdk: ModalitySdkType,
    ): FaceConfiguration.SpoofCheckConfiguration {
        // Only available for ROC V3
        if (bioSdk != ModalitySdkType.RANK_ONE) {
            return FaceConfiguration.SpoofCheckConfiguration.DISABLED
        }
        val faceSdkConfig = projectConfiguration.face?.getSdkConfiguration(bioSdk)
        if (faceSdkConfig == null || faceSdkConfig.version != rocV3BioSdk.version()) {
            return FaceConfiguration.SpoofCheckConfiguration.DISABLED
        }

        // TODO use the Face SDK configuration instead
        return projectConfiguration.experimental().let {
            FaceConfiguration.SpoofCheckConfiguration(
                mode = it.spoofCheckMode,
                threshold = it.spoofCheckThreshold,
            )
        }
    }
}
