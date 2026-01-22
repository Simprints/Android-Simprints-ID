package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.Modality
import kotlinx.serialization.Serializable

@Serializable
enum class ModalitySdkType {
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
