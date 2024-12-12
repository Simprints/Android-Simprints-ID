package com.simprints.feature.dashboard.main.projectdetails

internal data class DashboardProjectState(
    val title: String = "",
    val lastUser: String = "",
    val lastScanner: String = "",
    val isLoaded: Boolean = false,
)
