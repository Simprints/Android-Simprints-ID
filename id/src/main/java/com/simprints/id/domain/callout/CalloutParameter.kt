package com.simprints.id.domain.callout

import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterTypeError
import kotlin.reflect.KClass

class CalloutParameter(val key: String, val value: Any?) {

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> castAs(kClass: KClass<T>): T =
        if (kClass.isInstance(value)) {
            value as T
        } else {
            throw InvalidCalloutParameterTypeError.forClasses(kClass, value.kClassOrNull())
        }

    private fun Any?.kClassOrNull() =
        if (this != null) {
            this::class
        } else {
            null
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CalloutParameter

        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

}
