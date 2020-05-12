package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Base64.decode
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.cache.HotCacheImpl.AppRequestWrapper.Companion.CREATOR
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter
import com.simprints.id.tools.ParcelableConverter.unmarshall
import com.simprints.id.tools.ParcelableConverter.marshall
import com.simprints.id.tools.extensions.getMap
import com.simprints.id.tools.extensions.putMap
import com.simprints.id.tools.extensions.save
import kotlinx.android.parcel.Parcelize

class HotCacheImpl(private val sharedPrefs: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    override var appRequest: AppRequest
        set(value) {
            saveInSharedPrefs {
                val appRequestBytes = marshall(AppRequestWrapper(value))
                it.putString(KEY_APP_REQUEST, encodeToString(appRequestBytes, DEFAULT))
            }
        }
        get() = with(sharedPrefs) {
            getString(KEY_APP_REQUEST, null)?.let {
                unmarshall(decode(it, DEFAULT), CREATOR).appRequest
            } ?: throw IllegalStateException("No AppRequest stored in HotCache")
        }

    private val memoryCache
        get() = LinkedHashMap(sharedPrefs.getMap(KEY_STEPS, emptyMap()))

    override fun save(step: Step) {
        stepEncoder.encode(step).also { encodedStep ->
            saveEncodedStep(step.id, encodedStep)
        }
    }

    override fun load(): List<Step> {
        return memoryCache.values.map {
            stepEncoder.decode(it)
        }
    }

    override fun clearSteps() {
        saveInSharedPrefs { it.putMap(KEY_STEPS, emptyMap()) }
    }

    private fun saveEncodedStep(id: String, encodedStep: String) {
        val cache = memoryCache
        cache[id] = encodedStep
        saveInSharedPrefs { it.putMap(KEY_STEPS, cache) }
    }

    private fun saveInSharedPrefs(transaction: (SharedPreferences.Editor) -> Unit) {
        with(sharedPrefs) {
            save(transaction)
        }
    }

    private companion object {
        const val KEY_STEPS = "steps"
        const val KEY_APP_REQUEST = "appRequest"
    }

    /**
     * It wraps an AppRequest class, so CREATOR can use `readParcelable` to unmarshall
     * the AppRequest as Parcel
     */
    @Parcelize
    class AppRequestWrapper(val appRequest: AppRequest) : Parcelable {
        companion object {
            val CREATOR = object : Parcelable.Creator<AppRequestWrapper> {
                override fun createFromParcel(source: Parcel): AppRequestWrapper {
                    val appRequest = source.readParcelable<AppRequest>(AppRequest::class.java.classLoader)!!
                    return AppRequestWrapper(appRequest)
                }

                override fun newArray(size: Int): Array<AppRequestWrapper?> = arrayOfNulls(size)
            }
        }
    }
}
