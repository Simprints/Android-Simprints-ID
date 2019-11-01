package com.simprints.id.tools.utils

interface QueryFilter<T> {
    fun getFilteredList(items: List<T>, query: String?): List<T>
}
