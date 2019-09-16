package com.simprints.id.orchestrator.steps

import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.testtools.common.syntax.mock
import org.junit.Test

class StepTest {

    @Test
    fun resultSet_stepShouldUpdateTheState() {
        val resultOk = 0
        val step = Step(resultOk, "someActivityClassName", "bundle_key", mock(), mock(), ONGOING)
        assertThat(step.getStatus()).isEqualTo(COMPLETED)
    }
}
