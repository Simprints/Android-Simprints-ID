package com.simprints.feature.dashboard.tools

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider

fun testNavController(graph: Int, startDestination: Int? = null): TestNavHostController {
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    navController.setGraph(graph)
    startDestination?.also { navController.setCurrentDestination(it) }
    return navController
}
