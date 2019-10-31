package com.simprints.id.tools.utils

interface QueryFilter<T> {
    fun filter(items: List<T>, query: String?, callback: SearchResultCallback? = null): List<T>

    interface SearchResultCallback {
        fun onNothingFound()
        fun onResultsFound()
    }

}
