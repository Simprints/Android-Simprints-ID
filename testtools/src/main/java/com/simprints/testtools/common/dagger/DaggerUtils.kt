package com.simprints.testtools.common.dagger

import kotlin.reflect.full.createType
import kotlin.reflect.full.functions

fun injectClassFromComponent(component: Any, clazz: Any) {

    // Go through all the functions of the AppComponent and try to find the one corresponding to the clazz
    val injectFunction = component::class.functions.find { function ->

        // These inject KFunctions take two parameters, the first is the AppComponent, the second is the injectee
        function.parameters.size == 2 && function.parameters.last().type == clazz::class.createType()

    } ?: throw NoSuchMethodError("Forgot to add inject method to ${component::class.createType()} for ${clazz::class.createType()}")

    injectFunction.call(component, clazz)
}
