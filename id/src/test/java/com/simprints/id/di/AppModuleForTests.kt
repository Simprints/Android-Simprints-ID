package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.shared.AppModuleForAnyTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.DependencyRule.MockRule
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
                             override var sessionEventsManagerRule: DependencyRule = RealRule,
                             override var sessionEventsLocalDbManagerRule: DependencyRule = MockRule, //Roboletric doesn't support Realm
                             override var scheduledPeopleSyncManagerRule: DependencyRule = RealRule,
                             override var scheduledSessionsSyncManagerRule: DependencyRule = RealRule,
                             override var simNetworkUtilsRule: DependencyRule = RealRule,
                             override var secureApiInterfaceRule: DependencyRule = RealRule,
                             override var longConsentManagerRule: DependencyRule = RealRule,
                             override var scannerManagerRule: DependencyRule = RealRule,
                             override var peopleUpSyncMasterRule: DependencyRule = RealRule,
                             override var syncStatusDatabaseRule: DependencyRule = RealRule,
                             override var syncScopesBuilderRule: DependencyRule = RealRule,
                             override var countTaskRule: DependencyRule = RealRule)
    : AppModuleForAnyTests(
    app,
    localDbManagerRule = localDbManagerRule,
    remoteDbManagerRule = remoteDbManagerRule,
    dbManagerRule = dbManagerRule,
    secureDataManagerRule = secureDataManagerRule,
    dataManagerRule = dataManagerRule,
    loginInfoManagerRule = loginInfoManagerRule,
    analyticsManagerRule = analyticsManagerRule,
    bluetoothComponentAdapterRule = bluetoothComponentAdapterRule,
    sessionEventsManagerRule = sessionEventsManagerRule,
    sessionEventsLocalDbManagerRule = sessionEventsLocalDbManagerRule,
    scheduledPeopleSyncManagerRule = scheduledPeopleSyncManagerRule,
    scheduledSessionsSyncManagerRule = scheduledSessionsSyncManagerRule,
    simNetworkUtilsRule = simNetworkUtilsRule,
    secureApiInterfaceRule = secureApiInterfaceRule,
    longConsentManagerRule = longConsentManagerRule,
    scannerManagerRule = scannerManagerRule,
    peopleUpSyncMasterRule = peopleUpSyncMasterRule,
    syncStatusDatabaseRule = syncStatusDatabaseRule,
    syncScopesBuilderRule = syncScopesBuilderRule,
    countTaskRule = countTaskRule) {

    override fun provideKeystoreManager(): KeystoreManager = setupFakeKeyStore()
}
