package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.shared.AppModuleForAnyTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.DependencyRule.*

open class AppModuleForAndroidTests(app: Application,
                                    override var localDbManagerRule: DependencyRule = RealRule(),
                                    override var remoteDbManagerRule: DependencyRule = RealRule(),
                                    override var dbManagerRule: DependencyRule = RealRule(),
                                    override var secureDataManagerRule: DependencyRule = RealRule(),
                                    override var dataManagerRule: DependencyRule = RealRule(),
                                    override var loginInfoManagerRule: DependencyRule = RealRule(),
                                    override var randomGeneratorRule: DependencyRule = RealRule(),
                                    override var analyticsManagerRule: DependencyRule = RealRule(),
                                    override var bluetoothComponentAdapterRule: DependencyRule = RealRule())
    : AppModuleForAnyTests(app, localDbManagerRule, remoteDbManagerRule, dbManagerRule, secureDataManagerRule, dataManagerRule, loginInfoManagerRule, randomGeneratorRule, analyticsManagerRule, bluetoothComponentAdapterRule)
