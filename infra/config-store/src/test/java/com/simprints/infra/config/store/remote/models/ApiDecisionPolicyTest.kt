package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.apiDecisionPolicy
import com.simprints.infra.config.store.testtools.decisionPolicy
import org.junit.Test

class ApiDecisionPolicyTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(apiDecisionPolicy.toDomain()).isEqualTo(decisionPolicy)
    }
}
