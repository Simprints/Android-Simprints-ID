package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.cache.HotCacheImpl.AppRequestWrapper.Companion.CREATOR
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter
import com.simprints.id.tools.extensions.getMap
import com.simprints.id.tools.extensions.putMap
import com.simprints.id.tools.extensions.save
import kotlinx.android.parcel.Parcelize

class HotCacheImpl(private val sharedPrefs: SharedPreferences,
                   private val stepEncoder: StepEncoder) : HotCache {

    override var appRequest: AppRequest
        set(value) {
            saveInSharedPrefs {
                val data = ParcelableConverter.marshall(AppRequestWrapper(value))
                val bytes = Base64.encodeToString(data, Base64.DEFAULT)
                it.putString(KEY_APP_REQUEST, bytes)
            }
        }
        get() = with(sharedPrefs) {
            this.getString(KEY_APP_REQUEST, null)?.let {
                ParcelableConverter.unmarshall(Base64.decode(it, Base64.DEFAULT), CREATOR).appRequest
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
     * It wraps the AppRequest, so we can use `readParcelable` to unmarshall
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
