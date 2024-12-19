package com.simprints.infra.security.keyprovider

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.BuildSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

internal class EncryptedSharedPreferencesBuilderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    @BuildSdk private val buildSdk: Int,
    private val masterKeyProvider: MasterKeyProvider,
    private val preferencesProvider: EncryptedSharedPreferencesProvider,
) : EncryptedSharedPreferencesBuilder {
    private val masterKeyAlias: String
        get() = masterKeyProvider.provideMasterKey()

    override fun buildEncryptedSharedPreferences(filename: String): SharedPreferences = try {
        preferencesProvider.provideEncryptedSharedPreferences(
            filename = filename,
            masterKeyAlias = masterKeyAlias,
        )
    } catch (e: Exception) {
        // Workaround for CORE-2568
        // Sometimes the master key has invalid tag (zero), and in such cases the process can be
        // recovered by physically removing the shared preferences file and trying to create
        // it once again
        Simber.e("Unable to create encrypted shared preferences", e)
        deleteEncryptedSharedPreferences(filename = filename)
        preferencesProvider.provideEncryptedSharedPreferences(
            filename = filename,
            masterKeyAlias = masterKeyAlias,
        )
    }

    @SuppressLint("NewApi")
    private fun deleteEncryptedSharedPreferences(filename: String) {
        try {
            if (buildSdk >= Build.VERSION_CODES.N) {
                ctx.deleteSharedPreferences(filename)
            } else {
                // Shared preferences stored in 'data/data/%package%/shared_prefs/%filename%.xml'
                val prefPath = ctx.applicationInfo.dataDir + "/shared_prefs/"
                val name = "$filename.xml"
                File(prefPath, name).run {
                    if (exists()) delete()
                }
            }
        } catch (e: Exception) {
            Simber.e("Unable to delete encrypted shared preferences", e)
        }
    }
}
