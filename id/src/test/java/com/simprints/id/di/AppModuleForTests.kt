package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.shared.AppModuleForAnyTests
import com.simprints.id.shared.MockRule
import com.simprints.id.shared.MockRule.REAL
import com.simprints.id.shared.setupFakeKeyStore

open class AppModuleForTests(app: Application,
                             override var localDbManagerRule: MockRule = REAL,
                             override var remoteDbManagerRule: MockRule = REAL,
                             override var dbManagerRule: MockRule = REAL,
                             override var secureDataManagerRule: MockRule = REAL,
                             override var dataManagerRule: MockRule = REAL,
                             override var loginInfoManagerRule: MockRule = REAL,
                             override var analyticsManagerRule: MockRule = REAL)
    : AppModuleForAnyTests(app, localDbManagerRule, remoteDbManagerRule, dbManagerRule, secureDataManagerRule, dataManagerRule, loginInfoManagerRule, analyticsManagerRule) {

    override fun provideKeystoreManager(): KeystoreManager = setupFakeKeyStore()
}
