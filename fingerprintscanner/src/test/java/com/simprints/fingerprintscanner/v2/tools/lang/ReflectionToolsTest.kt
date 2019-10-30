package com.simprints.fingerprintscanner.v2.tools.lang

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReflectionToolsTest {

    @Test
    fun isSubclass_forDirectSubclasses_succeedsCheck() {
        assertThat(isSubclass<TestSubclass, TestOpenMiddleClass>()).isTrue()
        assertThat(isSubclass<TestOpenMiddleClass, TestOpenBaseClass>()).isTrue()
    }

    @Test
    fun isSubclass_forIndirectSubclasses_succeedsCheck() {
        assertThat(isSubclass<TestSubclass, TestOpenBaseClass>()).isTrue()
    }

    @Test
    fun isSubclass_forNonSubclasses_failsCheck() {
        assertThat(isSubclass<TestNotSubclass, TestOpenBaseClass>()).isFalse()
        assertThat(isSubclass<TestOpenBaseClass, TestSubclass>()).isFalse()
    }

    @Test
    fun sealedClassWithObjectsOnly_retrieveObjects_returnsAllObjects() {
        assertThat(TestSealedClassObjectsOnly::class.objects())
            .containsExactlyElementsIn(listOf(
                TestSealedClassObjectsOnly.ObjectA,
                TestSealedClassObjectsOnly.ObjectB,
                TestSealedClassObjectsOnly.ObjectC
            ))
    }

    @Test
    fun sealedClassWithoutAnyObjects_retrieveObjects_returnsEmptyList() {
        assertThat(TestSealedClassNoObjects::class.objects())
            .isEmpty()
    }

    @Test
    fun sealedClassWithMixedObjectsAndClasses_retrieveObjects_returnsOnlyObjectsAndIgnoresClasses() {
        assertThat(TestSealedClassMixedClassesAndObjects::class.objects())
            .containsExactlyElementsIn(listOf(
                TestSealedClassMixedClassesAndObjects.ObjectA,
                TestSealedClassMixedClassesAndObjects.ObjectC,
                TestSealedClassMixedClassesAndObjects.ObjectE
            ))
    }

    private open class TestOpenBaseClass
    private open class TestOpenMiddleClass : TestOpenBaseClass()
    private class TestSubclass : TestOpenMiddleClass()
    private class TestNotSubclass

    private sealed class TestSealedClassObjectsOnly {
        object ObjectA : TestSealedClassObjectsOnly()
        object ObjectB : TestSealedClassObjectsOnly()
        object ObjectC : TestSealedClassObjectsOnly()
    }

    private sealed class TestSealedClassNoObjects {
        @Suppress("unused")
        class ClassA(val i: Int) : TestSealedClassNoObjects()

        @Suppress("unused")
        class ClassB(val i: Int) : TestSealedClassNoObjects()

        @Suppress("unused")
        class ClassC(val i: Int) : TestSealedClassNoObjects()
    }

    private sealed class TestSealedClassMixedClassesAndObjects {
        object ObjectA : TestSealedClassMixedClassesAndObjects()
        @Suppress("unused")
        class ObjectB(val i: Int) : TestSealedClassMixedClassesAndObjects()

        object ObjectC : TestSealedClassMixedClassesAndObjects()
        @Suppress("unused")
        class ClassD(val i: Int) : TestSealedClassMixedClassesAndObjects()

        object ObjectE : TestSealedClassMixedClassesAndObjects()
        @Suppress("unused")
        class ClassF(val i: Int) : TestSealedClassMixedClassesAndObjects()
    }
}
