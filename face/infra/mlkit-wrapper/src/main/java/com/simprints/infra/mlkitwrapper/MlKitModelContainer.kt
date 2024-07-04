package com.simprints.infra.mlkitwrapper

import com.simprints.infra.mlkitwrapper.model.MlKitModel
import javax.inject.Inject
import javax.inject.Singleton

// TODO PoC - global store for the initialised ML kit model
@Singleton
class MlKitModelContainer @Inject constructor() {
    lateinit var matcher: String
    lateinit var templateFormat: String
    lateinit var mlKitModel: MlKitModel
}
