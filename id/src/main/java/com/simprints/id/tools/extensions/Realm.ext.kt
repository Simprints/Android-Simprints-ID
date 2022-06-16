package com.simprints.id.tools.extensions

import io.realm.RealmList

inline fun <reified T> List<T>.toRealmList(): RealmList<T> =
    RealmList(*this.toTypedArray())
