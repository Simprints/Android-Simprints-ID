package com.simprints.core.tools.extentions

import com.google.common.truth.Truth
import com.simprints.core.tools.extentions.FlowKtTest.FixtureGenerators.flowNPeople
import com.simprints.core.tools.extentions.FlowKtTest.FixtureGenerators.generateNPeople
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.measureVirtualTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import org.junit.Rule
import org.junit.Test
import java.util.*

class FlowKtTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val delay = 1L
    private val nPeople = 100
    private val people = generateNPeople(nPeople).toList()

    /**
     * This test should show that parallel processing is faster than sequential even if the time
     * to process something is as 1ms. 1ms over lots of operations add more time than the overhead
     * of creating the new flows in a different dispatcher.
     *
     * The test also uses virtual time to check the real time elapsed since it is running inside a
     * blockingTest block.
     */
    @Test
    fun `compare sequential vs parallel processing`() = testCoroutineRule.runBlockingTest {
        val mapTime = measureVirtualTimeMillis { people.map { delay(delay); it.id to it }.toMap() }

        val flowConcurrentTime = measureVirtualTimeMillis {
            val peopleMap = mutableMapOf<String, Person>()
            flowNPeople(nPeople)
                .concurrentMap(testCoroutineRule.testCoroutineDispatcher) { delay(delay); it.id to it }
                .collect { peopleMap += it }
        }

        println("MAP TIME $mapTime")
        println("FLOW CONCURRENT TIME $flowConcurrentTime")

        Truth.assertThat(flowConcurrentTime).isLessThan(mapTime)
    }

    private data class Person(val id: String, val template: String)

    private object FixtureGenerators {
        fun generatePerson() = generateSequence {
            Person(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        }

        fun generateNPeople(n: Int) = generatePerson().take(n)

        fun flowNPeople(n: Int) = generateNPeople(n).asFlow()
    }
}

