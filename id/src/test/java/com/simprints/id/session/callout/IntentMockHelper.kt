package com.simprints.id.session.callout

import android.content.Intent
import android.os.Bundle
import com.simprints.testframework.common.syntax.mock
import com.simprints.testframework.common.syntax.whenever

fun mockIntent(action: String?, vararg params: Pair<String, Any>): Intent {
    val intent = mockIntent(*params)
    whenever(intent.action).thenReturn(action)
    return intent
}

fun mockIntent(vararg params: Pair<String, Any?>): Intent {
    val intent = mock<Intent>()
    val mockBundle = mockBundle(*params)
    whenever(intent.extras).thenReturn(mockBundle)
    return intent
}

private fun mockBundle(vararg params: Pair<String, Any?>): Bundle {
    val bundle = mock<Bundle>()
    for ((key, value) in params) {
        whenever(bundle.get(key)).thenReturn(value)
    }
    val keys = params.map { (key, _) -> key }.toSet()
    whenever(bundle.keySet()).thenReturn(keys)
    return bundle
}
