package com.simprints.id.di

import com.simprints.id.Application
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.shared.AppModuleForAnyTests
import com.simprints.id.shared.setupFakeKeyStore

open class AppModuleForTests(app: Application,
                             override var localDbManagerSpy: Boolean? = null,
                             override var remoteDbManagerSpy: Boolean? = null,
                             override var dbManagerSpy: Boolean? = null,
                             override val secureDataManagerSpy: Boolean? = null,
                             override var dataManagerSpy: Boolean? = null,
                             override var loginInfoManagerSpy: Boolean? = null,
                             override var analyticsManagerSpy: Boolean? = null)
    : AppModuleForAnyTests(app, localDbManagerSpy, remoteDbManagerSpy, dbManagerSpy, secureDataManagerSpy, dataManagerSpy, loginInfoManagerSpy, analyticsManagerSpy) {

    override fun provideKeystoreManager(): KeystoreManager = setupFakeKeyStore()
}
