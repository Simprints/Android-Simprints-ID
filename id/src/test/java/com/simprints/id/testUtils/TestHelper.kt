package com.simprints.id.testUtils

import junit.framework.Assert.fail
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

inline fun <reified T> mock(): T =
        Mockito.mock(T::class.java)

fun <T> whenever(methodCall: T): OngoingStubbing<T> =
        Mockito.`when`(methodCall)

fun <T> verifyOnlyInteraction(mock: T, methodCall: T.() -> Any?) {
    Mockito.verify(mock).methodCall()
    Mockito.verifyNoMoreInteractions(mock)
}

fun <T> verifyOnlyInteractions(mock: T, vararg methodCalls: T.() -> Any?) {
    for (methodCall in methodCalls) {
        Mockito.verify(mock).methodCall()
    }
    Mockito.verifyNoMoreInteractions(mock)
}

/**
 * Junit 5 has a nice assertThrows method
 * (http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/Assertions.html#assertThrows-java.lang.Class-org.junit.jupiter.api.function.Executable-)
 * This is a placeholder until we migrate from JUnit 4 to Junit 5
 */
inline fun <reified T: Throwable> assertThrows(executable: () -> Unit) {
    try {
        executable()
        fail("Expected an ${T::class.java.simpleName} to be thrown")
    } catch(exception: Throwable) {
        when (exception) {
            is T -> {}
            else -> throw(exception)
        }
    }
}

