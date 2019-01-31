package com.simprints.testframework.common.di

import com.simprints.libcommon.di.IAppComponent
import com.simprints.libcommon.di.IApplication

interface DaggerForTests {

    var testAppComponent: IAppComponent
    var app: IApplication

    fun initComponent()
}
