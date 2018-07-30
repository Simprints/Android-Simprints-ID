package com.simprints.id.data.prefs.preferenceType

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.serializers.Serializer

class RemoteConfigComplexPreference<T : Any> (val prefs: ImprovedSharedPreferences,
                                              private val remoteConfig: FirebaseRemoteConfig,
                                              remoteDefaultsMap: MutableMap<String, Any>,
                                              private val key: String,
                                              defValue: T,
                                              private val serializer: Serializer<T>) {


}
