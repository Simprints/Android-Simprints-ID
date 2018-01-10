package com.simprints.id.domain.callout

import android.content.Intent
import com.simprints.id.exceptions.unsafe.MissingCalloutParameterError


class CalloutParameters(parameters: Set<CalloutParameter>): Iterable<CalloutParameter> {

    private val index = parameters
        .map { param -> param.key to param }
        .toMap()

    companion object {

        val Intent?.calloutParameters: CalloutParameters
            get() = CalloutParameters(this.extrasSet())

        private fun Intent?.extrasSet(): Set<CalloutParameter> =
            this.extrasKeys()
                .map { key -> CalloutParameter(key, this?.extras?.get(key)) }
                .toSet()

        private fun Intent?.extrasKeys(): Set<String> =
            this?.extras
                ?.keySet()
                ?: setOf()

    }

    operator fun get(key: String): CalloutParameter =
        index[key] ?: throw MissingCalloutParameterError()

    fun getOrDefault(key: String, defaultValue: Any?): CalloutParameter =
        index[key] ?: CalloutParameter(key, defaultValue)

    operator fun contains(key: String): Boolean =
        key in index

    override fun iterator(): Iterator<CalloutParameter> =
        index.values.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CalloutParameters

        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        return index.hashCode()
    }

}
