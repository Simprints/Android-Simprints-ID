//package com.simprints.id.data.db.event.remote.events.callback
//
//import com.google.common.truth.Truth.assertThat
//import com.simprints.id.data.db.event.domain.events.callback.ErrorCallbackEvent
//import org.junit.Test

// StopShip: to fix once the event remote data source is sorted
//class ApiErrorCallbackTest {
//
//    @Test
//    fun differentProjectIdSignedIn_fromDomainToApi() {
//        val domain = ErrorCallbackEvent.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
//        val api = ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
//
//        assertThat(domain.fromDomainToApi()).isEqualTo(api)
//    }
//
//    @Test
//    fun differentUserIdSignedIn_fromDomainToApi() {
//        val domain = ErrorCallbackEvent.Reason.DIFFERENT_USER_ID_SIGNED_IN
//        val api = ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
//
//        assertThat(domain.fromDomainToApi()).isEqualTo(api)
//    }
//
//    @Test
//    fun guidNotFoundOnline_fromDomainToApi() {
//        val domain = ErrorCallbackEvent.Reason.GUID_NOT_FOUND_ONLINE
//        val api = ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
//
//        assertThat(domain.fromDomainToApi()).isEqualTo(api)
//    }
//
//    @Test
//    fun unexpectedError_fromDomainToApi() {
//        val domain = ErrorCallbackEvent.Reason.UNEXPECTED_ERROR
//        val api = ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
//
//        assertThat(domain.fromDomainToApi()).isEqualTo(api)
//    }
//
//    @Test
//    fun bluetoothNotSupported_fromDomainToApi() {
//        val domain = ErrorCallbackEvent.Reason.BLUETOOTH_NOT_SUPPORTED
//        val api = ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
//
//        assertThat(domain.fromDomainToApi()).isEqualTo(api)
//    }
//
//    @Test
//    fun loginNotComplete_fromDomainToApi() {
//        val domain = ErrorCallbackEvent.Reason.LOGIN_NOT_COMPLETE
//        val api = ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE
//
//        assertThat(domain.fromDomainToApi()).isEqualTo(api)
//    }
//
//}
