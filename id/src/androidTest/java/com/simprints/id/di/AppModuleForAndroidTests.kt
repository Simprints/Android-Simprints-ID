package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.shared.AppModuleForAnyTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.DependencyRule.RealRule

open class AppModuleForAndroidTests(app: Application,
                                    override var localDbManagerRule: DependencyRule = RealRule,
                                    override var remoteDbManagerRule: DependencyRule = RealRule,
                                    override var dbManagerRule: DependencyRule = RealRule,
                                    override var secureDataManagerRule: DependencyRule = RealRule,
                                    override var dataManagerRule: DependencyRule = RealRule,
                                    override var loginInfoManagerRule: DependencyRule = RealRule,
                                    override var randomGeneratorRule: DependencyRule = RealRule,
                                    override var keystoreManagerRule: DependencyRule = RealRule,
                                    override var analyticsManagerRule: DependencyRule = RealRule,
                                    override var bluetoothComponentAdapterRule: DependencyRule = RealRule,
                                    override var sessionEventsManagerRule: DependencyRule = RealRule,
                                    override var sessionEventsLocalDbManagerRule: DependencyRule = RealRule,
                                    override var scheduledPeopleSyncManagerRule: DependencyRule = RealRule,
                                    override var settingsPreferencesManagerRule: DependencyRule = RealRule,
                                    override var scheduledSessionsSyncManagerRule: DependencyRule = RealRule) : AppModuleForAnyTests(
    app,
    localDbManagerRule,
    remoteDbManagerRule,
    dbManagerRule,
    secureDataManagerRule,
    dataManagerRule,
    loginInfoManagerRule,
    randomGeneratorRule,
    keystoreManagerRule,
    analyticsManagerRule,
    bluetoothComponentAdapterRule,
    sessionEventsManagerRule,
    sessionEventsLocalDbManagerRule,
    scheduledPeopleSyncManagerRule,
    settingsPreferencesManagerRule,
    scheduledSessionsSyncManagerRule)
