package com.simprints.id.tools.delegates

import kotlin.reflect.KProperty

/**
 * Note: LazyVar is largely inspired from Lazy
 */

/**
 * Represents a variable with lazy initialization.
 *
 * To create an instance of [LazyVar] use the [lazyVar] function.
 */
interface LazyVar<T> {
    /**
     * Gets the lazily initialized value of the current LazyVar instance.
     */
    var value: T

    /**
     * Allows to use instances of LazyVar for property delegation:
     * `var property: String by lazyVar { initializer }`
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    /**
     * Allows to use instances of LazyVar for property delegation:
     * `var property: String by lazyVar { initializer }`
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

}

/**
 * *** USE WITH CAUTION ***
 * This implementation of LazyVar has been found to be the source of many bugs involving
 * unintentional caching of state. These issues have been difficult to debug, as the debugger often
 * hangs when trying to step into code that uses lazyVar. In most cases the performance gain is
 * negligible, and often the native kotlin [lazy] or even var would be just fine.
 *
 * Creates a new instance of the [LazyVar] that uses the specified initialization function [initializer]
 * and the default thread-safety mode [LazyThreadSafetyMode.SYNCHRONIZED].
 *
 * If the initialization of a value throws an exception, it will attempt to reinitialize the value at next access.
 *
 * Note that the returned instance uses itself to synchronize on. Do not synchronize from external code on
 * the returned instance as it may cause accidental deadlock. Also this behavior can be changed in the future.
 */
fun <T> lazyVar(initializer: () -> T): LazyVar<T> = SynchronizedLazyVarImpl(initializer)

private object UNINITIALIZED_VALUE

private class SynchronizedLazyVarImpl<T>(initializer: () -> T, lock: Any? = null) : LazyVar<T> {

    private var initializer: (() -> T)? = initializer
    @Volatile private var _value: Any? = UNINITIALIZED_VALUE
    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this

    override var value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                }
                else {
                    val typedValue = initializer!!()
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }
        set(value) {
            synchronized(lock) {
                _value = value
                initializer = null
            }
        }
}
