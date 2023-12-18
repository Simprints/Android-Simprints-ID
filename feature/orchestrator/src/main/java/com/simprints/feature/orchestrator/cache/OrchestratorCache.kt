package com.simprints.feature.orchestrator.cache

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.edit
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.feature.orchestrator.steps.SerializableMixin
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.security.SecurityManager
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OrchestratorCache @Inject constructor(
    securityManager: SecurityManager,
    private val encodingUtils: EncodingUtils,
    private val jsonHelper: JsonHelper,
    private val parcelableConverter: ParcelableConverter,
) {

    private val prefs = securityManager.buildEncryptedSharedPreferences(ORCHESTRATION_CACHE)

    var actionRequest: ActionRequest?
        set(value) {
            prefs.edit(commit = true) {
                if (value == null) {
                    remove(KEY_REQUEST)
                } else {
                    val requestBytes = parcelableConverter.marshall(ActionRequestWrapper(value))
                    putString(KEY_REQUEST, encodingUtils.byteArrayToBase64(requestBytes))
                }
            }
        }
        get() = prefs.getString(KEY_REQUEST, null)
            ?.let { encodingUtils.base64ToBytes(it) }
            ?.let { parcelableConverter.unmarshall(it, ActionRequestWrapper.CREATOR).request }


    var steps: List<Step>
        set(value) {
            prefs.edit(commit = true) {
                jsonHelper.addMixin(Serializable::class.java, SerializableMixin::class.java)
                putString(KEY_STEPS, jsonHelper.toJson(value))
            }
        }
        get() = prefs.getString(KEY_STEPS, null)
                ?.let {
                    jsonHelper.addMixin(Serializable::class.java, SerializableMixin::class.java)
                    jsonHelper.fromJson(
                        json = it,
                        module = stepsModule,
                        type = object : TypeReference<List<Step>>() {}
                    )
                }
                ?: emptyList()

    fun clearSteps() {
        prefs.edit(commit = true) {
            remove(KEY_STEPS)
        }
    }

    companion object {
        private const val ORCHESTRATION_CACHE = "ORCHESTRATOR_CACHE"

        private const val KEY_REQUEST = "actionRequest"
        private const val KEY_STEPS = "steps"

        private val stepsModule = SimpleModule().apply {
            addSerializer(Bundle::class.java, BundleSerializer())
            addDeserializer(Bundle::class.java, BundleDeserializer())
        }
    }

    //TODO: Use JSON instead of Parcelable
    @Parcelize
    internal class ActionRequestWrapper(val request: ActionRequest) : Parcelable {
        companion object {
            val CREATOR = object : Parcelable.Creator<ActionRequestWrapper> {
                override fun createFromParcel(source: Parcel): ActionRequestWrapper = ActionRequestWrapper(
                    source.readParcelable(ActionRequest::class.java.classLoader)!!
                )

                override fun newArray(size: Int): Array<ActionRequestWrapper?> = arrayOfNulls(size)
            }
        }
    }
}
