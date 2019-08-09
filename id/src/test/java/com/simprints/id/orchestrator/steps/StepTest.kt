package com.simprints.id.orchestrator.steps

import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.testtools.common.syntax.mock
import org.junit.Test

class StepTest {

    @Test
    fun resultSet_stepShouldUpdateTheState() {
        val step = Step(0, "someActivityClassName", "bundle_key", mock(), ONGOING)
        step.result = mock()
        assertThat(step.status).isEqualTo(COMPLETED)
    }
}
