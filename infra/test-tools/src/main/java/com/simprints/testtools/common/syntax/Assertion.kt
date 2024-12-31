package com.simprints.testtools.common.syntax
import org.junit.Assert

inline fun <reified T : Throwable> assertThrows(executable: () -> Unit): T {
    try {
        executable()
    } catch (exception: Throwable) {
        when (exception) {
            is T -> return exception
            else -> throw(exception)
        }
    }
    failTest("Expected an ${T::class.java.simpleName} to be thrown")
}

inline fun <reified T : Throwable> assertThrows(
    throwable: T,
    executable: () -> Unit,
): T {
    val thrown = assertThrows<T>(executable)
    Assert.assertEquals(throwable, thrown)
    return thrown
}

fun failTest(message: String?): Nothing {
    Assert.fail(message)
    throw Exception(message)
}
