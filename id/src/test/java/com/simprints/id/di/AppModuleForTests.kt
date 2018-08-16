package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.services.scheduledSync.ScheduledSyncManager
import com.simprints.id.shared.AppModuleForAnyTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.DependencyRule.RealRule
import com.simprints.id.shared.setupFakeKeyStore

open class AppModuleForTests(app: Application,
                             override var localDbManagerRule: DependencyRule = RealRule,
                             override var remoteDbManagerRule: DependencyRule = RealRule,
                             override var dbManagerRule: DependencyRule = RealRule,
                             override var secureDataManagerRule: DependencyRule = RealRule,
                             override var dataManagerRule: DependencyRule = RealRule,
                             override var loginInfoManagerRule: DependencyRule = RealRule,
                             override var analyticsManagerRule: DependencyRule = RealRule,
                             override var bluetoothComponentAdapterRule: DependencyRule = RealRule,
                             override var scheduledSyncManagerRule: DependencyRule = RealRule)
    : AppModuleForAnyTests(app, localDbManagerRule, remoteDbManagerRule, dbManagerRule, secureDataManagerRule, dataManagerRule, loginInfoManagerRule, analyticsManagerRule, bluetoothComponentAdapterRule) {

    override fun provideKeystoreManager(): KeystoreManager = setupFakeKeyStore()
}
