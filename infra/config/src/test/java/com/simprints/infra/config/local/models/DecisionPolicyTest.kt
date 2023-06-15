package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.testtools.decisionPolicy
import com.simprints.infra.config.testtools.protoDecisionPolicy
import org.junit.Test

class DecisionPolicyTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(protoDecisionPolicy.toDomain()).isEqualTo(decisionPolicy)
        assertThat(decisionPolicy.toProto()).isEqualTo(protoDecisionPolicy)
    }
}
