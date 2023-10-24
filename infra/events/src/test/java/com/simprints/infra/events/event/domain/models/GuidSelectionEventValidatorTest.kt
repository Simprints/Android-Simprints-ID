//package com.simprints.eventsystem.event.domain.models
//
//import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
//import com.simprints.infra.events.domain.validators.GuidSelectionEventValidator
//import com.simprints.infra.events.domain.events.GuidSelectionEvent
//import com.simprints.infra.events.domain.events.callback.IdentificationCallbackEvent
//import com.simprints.id.exceptions.safe.session.validator.GuidSelectEventValidatorException
//import com.simprints.id.tools.time.TimeHelperImpl
//import com.simprints.testtools.common.syntax.assertThrows
//import org.junit.Test
//import java.util.*
// TOFIX
//class GuidSelectionEventValidatorTest {
//
//    val timeHelper = TimeHelperImpl()
//
//    @Test
//    fun validate_shouldRejectASessionWithMultipleGuidSelectionEvents() {
//        val session = createFakeOpenSession(timeHelper)
//        session.addEvent(GuidSelectionEvent(0, UUID.randomUUID().toString()))
//        session.addEvent(GuidSelectionEvent(0, UUID.randomUUID().toString()))
//
//        assertThrows<GuidSelectEventValidatorException> {
//            GuidSelectionEventValidator().validate(session)
//        }
//    }
//
//    @Test
//    fun validate_shouldRejectASessionWithNoIdentificationCallback() {
//        val session = createFakeOpenSession(timeHelper)
//        session.addEvent(GuidSelectionEvent(0, UUID.randomUUID().toString()))
//
//        assertThrows<GuidSelectEventValidatorException> {
//            GuidSelectionEventValidator().validate(session)
//        }
//    }
//
//    @Test
//    fun validate_shouldAcceptAValidSession() {
//        val session = createFakeOpenSession(timeHelper)
//        session.addEvent(IdentificationCallbackEvent(0, UUID.randomUUID().toString(), emptyList()))
//        session.addEvent(GuidSelectionEvent(0, UUID.randomUUID().toString()))
//
//        GuidSelectionEventValidator().validate(session)
//    }
//}
