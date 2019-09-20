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
        val step = Step(
            requestCode = resultOk,
            activityName = "someActivityClassName",
            bundleKey = "bundle_key",
            request = mock(),
            result = mock(),
            status = ONGOING
        )
        assertThat(step.getStatus()).isEqualTo(COMPLETED)
    }
}
