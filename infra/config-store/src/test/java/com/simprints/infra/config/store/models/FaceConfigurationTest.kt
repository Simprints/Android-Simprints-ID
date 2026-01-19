package com.simprints.infra.config.store.models

import com.google.common.truth.Truth
import org.junit.Test

class FaceConfigurationTest {
    @Test
    fun `should retrieve Rank One configuration when RANK_ONE is requested `() {
        val faceConfiguration = createConfiguration()
        Truth
            .assertThat(faceConfiguration.getSdkConfiguration(ModalitySdkType.RANK_ONE))
            .isEqualTo(faceConfiguration.rankOne)
    }

    @Test
    fun `should retrieve SimFace configuration when SIM_FACE is requested `() {
        val faceConfiguration = createConfiguration()
        Truth
            .assertThat(faceConfiguration.getSdkConfiguration(ModalitySdkType.SIM_FACE))
            .isEqualTo(faceConfiguration.simFace)
    }

    private fun createConfiguration(): FaceConfiguration = FaceConfiguration(
        allowedSDKs = listOf(ModalitySdkType.RANK_ONE),
        rankOne = FaceConfiguration.FaceSdkConfiguration(
            nbOfImagesToCapture = 2,
            qualityThreshold = 0.5f,
            imageSavingStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
            decisionPolicy = DecisionPolicy(20, 50, 100),
            version = "1",
        ),
        simFace = FaceConfiguration.FaceSdkConfiguration(
            nbOfImagesToCapture = 3,
            qualityThreshold = 0.1f,
            imageSavingStrategy = FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN,
            decisionPolicy = DecisionPolicy(20, 50, 100),
            version = "14",
        ),
    )
}
