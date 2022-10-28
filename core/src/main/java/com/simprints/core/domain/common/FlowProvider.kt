package com.simprints.core.domain.common

interface FlowProvider {

    enum class FlowType {
        ENROL,
        IDENTIFY,
        VERIFY
    }

    fun getCurrentFlow(): FlowType

}
