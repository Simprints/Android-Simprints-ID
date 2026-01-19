package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.step.StepParams

enum class ModalitySdkType : StepParams {
    RANK_ONE,
    SIM_FACE,
    SECUGEN_SIM_MATCHER,
    NEC, ;

    fun isFace() = this == RANK_ONE || this == SIM_FACE

    fun modality() = when (this) {
        RANK_ONE, SIM_FACE -> Modality.FACE
        SECUGEN_SIM_MATCHER, NEC -> Modality.FINGERPRINT
    }
}
