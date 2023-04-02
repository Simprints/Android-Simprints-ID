package com.simprints.id.orchestrator.cache

import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.id.di.EncryptedSharedPreferences
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.cache.HotCacheImpl.AppRequestWrapper.Companion.CREATOR
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.ParcelableConverter.marshall
import com.simprints.id.tools.ParcelableConverter.unmarshall
import com.simprints.id.tools.extensions.save
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class HotCacheImpl @Inject constructor(
    @EncryptedSharedPreferences private val sharedPrefs: SharedPreferences,
    private val stepEncoder: StepEncoder
) : HotCache {

    override var appRequest: AppRequest
        set(value) {
            saveInSharedPrefs {
                val appRequestBytes = marshall(AppRequestWrapper(value))
                it.putString(KEY_APP_REQUEST, EncodingUtilsImpl.byteArrayToBase64(appRequestBytes))
            }
        }
        get() = with(sharedPrefs) {
            getString(KEY_APP_REQUEST, null)?.let {
                unmarshall(EncodingUtilsImpl.base64ToBytes(it), CREATOR).appRequest
            } ?: throw IllegalStateException("No AppRequest stored in HotCache")
        }

    override fun save(steps: List<Step>) {
        val encodedSteps = steps.map { stepEncoder.encode(it) }
        saveInSharedPrefs {
            it.putString(KEY_STEPS, JsonHelper.toJson(encodedSteps))
        }
    }

    override fun load(): List<Step> = try {
        val stepsJson = sharedPrefs.getString(KEY_STEPS, "")
        val encodedSteps = JsonHelper.fromJson<List<String>>(stepsJson !!)
        encodedSteps.map { stepEncoder.decode(it) }
    } catch (e: Throwable) {
        emptyList()
    }

    override fun clearSteps() {
        saveInSharedPrefs { it.remove(KEY_STEPS) }
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
                    val appRequest =
                        source.readParcelable<AppRequest>(AppRequest::class.java.classLoader)!!
                    return AppRequestWrapper(appRequest)
                }

                override fun newArray(size: Int): Array<AppRequestWrapper?> = arrayOfNulls(size)
            }
        }
    }
}
