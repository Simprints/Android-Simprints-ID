package com.simprints.id.shared

import com.simprints.id.Application
import com.simprints.id.di.AppComponent

interface DaggerForTests {

    var testAppComponent: AppComponent
    var app: Application

    fun initComponent()
}
