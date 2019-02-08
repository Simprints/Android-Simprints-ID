package com.simprints.testframework.unit.reactive

import org.junit.Before

interface RxJavaTest {

    @Before
    fun setupClass() {
        rescheduleMainThreadForRxJava()
    }
}
