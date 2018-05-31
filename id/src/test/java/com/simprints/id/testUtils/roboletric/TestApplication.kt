package com.simprints.id.testUtils.roboletric

import com.simprints.id.Application

class TestApplication : Application() {

    override fun injectDependencies() {}
    override fun createComponent() {}
    override fun initModules() {
        //Prevent to call Application OnCreate. Because it does db.initialiseDb, it creates all dependencies
        //and we can't mock them any more.
    }
}
