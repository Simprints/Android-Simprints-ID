package com.simprints.fingerprintscanner.v2.incoming.common

import com.simprints.fingerprintscanner.v2.domain.Message
import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import java.nio.BufferUnderflowException

interface MessageParser<out R : Message> {

    /** @throws InvalidMessageException */
    fun parse(messageBytes: ByteArray): R

    fun handleExceptionDuringParsing(e: Throwable): Nothing =
        when (e) {
            is InvalidMessageException ->
                throw e
            is IndexOutOfBoundsException, is BufferUnderflowException ->
                throw InvalidMessageException("Incorrect number of bytes received parsing response in ${this::class.java.simpleName}", e)
            else ->
                throw InvalidMessageException("Unknown issue during parsing in ${this::class.java.simpleName}", e)
        }
}
