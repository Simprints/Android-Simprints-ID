package com.simprints.id.orchestrator.cache

import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter

class StepEncoderImpl(private val keystoreManager: KeystoreManager) : StepEncoder {

    override fun encode(step: Step): String {
        val converter = ParcelableConverter(step)
        val string = String(converter.toBytes())
        converter.recycle()
        return keystoreManager.encryptString(string)
    }

    override fun decode(encodedStep: String?): Step? {
        return encodedStep?.let {
            val bytes = keystoreManager.decryptString(it).toByteArray()
            val converter = ParcelableConverter(bytes)
            val parcel = converter.getParcel()
            converter.recycle()
            Step.createFromParcel(parcel)
        }
    }

}
