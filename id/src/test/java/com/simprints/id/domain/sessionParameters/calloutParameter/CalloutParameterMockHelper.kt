package com.simprints.id.domain.sessionParameters.calloutParameter

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever


fun mockTypeParameter(action: CalloutAction): TypeParameter {
    val typeParameter = mock<TypeParameter>()
    whenever(typeParameter.value).thenReturn(action)
    return typeParameter
}

inline fun <T: Any, reified V: CalloutParameter<T>> mockInvalidCalloutParameter(value: T, error: InvalidCalloutError): V {
    val calloutParameter = mockValidCalloutParameter<T, V>(value)
    whenever(calloutParameter.validate()).thenThrow(error)
    return calloutParameter
}

inline fun <T: Any, reified V: CalloutParameter<T>> mockValidCalloutParameter(value: T): V {
    val calloutParameter = mock<V>()
    whenever(calloutParameter.value).thenReturn(value)
    return calloutParameter
}
