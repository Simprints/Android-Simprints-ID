package com.simprints.infra.protection.polyprotect

import com.simprints.infra.protection.auxiliary.AuxDataFactory
import org.junit.Ignore
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue
import kotlin.time.toDuration

@Ignore("This is a basic setup to check how efficient is the template encoding implementation")
class PolyProtectTemplateEncoderBenchmarkTest {

    private val auxDataFactory = AuxDataFactory()
    private val subject = TemplateEncoder()

    @Test
    fun `running PolyProtect on list of Double`() {
        val auxData = auxDataFactory.createAuxData(
            arraySize = 7,
            coefficientBounds = 100,
        )
        val random = Random

        val results = mutableListOf<Duration>()
        repeat(10) {
            val template = List(512) { random.nextDouble() - 0.5 } // Typical template values are in [-0.5;0.5]
            val v = measureTimedValue { subject.encodeTemplate(template, auxData) }
            results.add(v.duration)
        }

        println("Done ${results.size} runs")
        results.forEachIndexed { i, result -> println("${i + 1}: $result") }
        println("------")
        val average = results.sumOf { it.inWholeNanoseconds }.let { it / results.size }.let { it.toDuration(DurationUnit.NANOSECONDS) }
        println("Average: $average")
    }

    @Test
    fun `running PolyProtect on FloatArray`() {
        val auxData = auxDataFactory.createAuxData(
            arraySize = 7,
            coefficientBounds = 100,
        )
        val random = Random

        val results = mutableListOf<Duration>()
        repeat(10) {
            val template = FloatArray(512) { random.nextFloat() - 0.5f } // Typical template values are in [-0.5;0.5]
            val v = measureTimedValue { subject.encodeTemplate(template, auxData) }
            results.add(v.duration)
        }

        println("Done ${results.size} runs")
        results.forEachIndexed { i, result -> println("${i + 1}: $result") }
        println("------")
        val average = results.sumOf { it.inWholeNanoseconds }.let { it / results.size }.let { it.toDuration(DurationUnit.NANOSECONDS) }
        println("Average: $average")
    }
}
