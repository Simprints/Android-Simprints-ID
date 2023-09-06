package com.simprints.testtools.unit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
open class BaseUnitTestConfig {

    open fun coroutinesMainThread() = apply {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

}
