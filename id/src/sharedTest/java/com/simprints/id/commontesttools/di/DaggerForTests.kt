package com.simprints.id.commontesttools.di

import com.simprints.id.Application
import com.simprints.id.di.AppComponent

interface DaggerForTests {

    var testAppComponent: AppComponent
    var app: Application

    fun initComponent()
}
