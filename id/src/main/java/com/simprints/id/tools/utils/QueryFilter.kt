package com.simprints.id.tools.utils

interface QueryFilter<T> {
    fun filter(items: List<T>, query: String?): List<T>
}
