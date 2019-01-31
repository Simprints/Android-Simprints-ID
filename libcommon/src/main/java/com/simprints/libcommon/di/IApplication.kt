package com.simprints.libcommon.di

interface IApplication {

    var component: IAppComponent

    fun initApplication()
}
