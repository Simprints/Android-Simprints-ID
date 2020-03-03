package com.simprints.id.tools.extensions

import io.mockk.ConstantAnswer
import io.mockk.MockKStubScope
import io.mockk.Runs

infix fun MockKStubScope<Unit, Unit>.just(runs: Runs) = answers(ConstantAnswer(Unit))
