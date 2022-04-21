package com.simprints.testtools.common.coroutines

import com.simprints.core.tools.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

class TestDispatcherProvider(private val testCoroutineRule: TestCoroutineRule): DispatcherProvider {
    override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

    override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

    override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

    override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
}
