package com.simprints.id.tools

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import junit.framework.TestCase.assertNotSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class RandomGeneratorTest {

    @Test
    fun differentRandomGeneratorsGenerateDifferentKeys() {

        val randomGenerator1 = RandomGeneratorImpl()
        val generatedKey1 = randomGenerator1.generateByteArray(64)

        val randomGenerator2 = RandomGeneratorImpl()
        val generatedKey2 = randomGenerator2.generateByteArray(64)

        assertNotSame(generatedKey1, generatedKey2)
    }
}
