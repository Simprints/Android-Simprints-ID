package com.simprints.id.tools

import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import junit.framework.TestCase.assertNotSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class RandomGeneratorTest {

    @Before
    fun setUp() {
    }

    @Test
    fun differentRandomGeneratorsGenerateDifferentKeys() {

        val randomGenerator1 = RandomGeneratorImpl()
        val generatedKey1 = randomGenerator1.generateByteArray(64)

        val randomGenerator2 = RandomGeneratorImpl()
        val generatedKey2 = randomGenerator2.generateByteArray(64)

        assertNotSame(generatedKey1, generatedKey2)
    }
}
