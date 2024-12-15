package com.simprints.fingerprint.infra.scanner.v2.incoming.common

import com.simprints.fingerprint.infra.scanner.v2.domain.Message
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import java.nio.BufferUnderflowException

/**
 * High level interface for parsing bytes into a [Message] of sub-type [R].
 * The bytes received are assumed to be a complete, single message in byte form.
 *
 * Any exceptions that occur during parsing are wrapped and transformed into
 * [InvalidMessageException]
 */
interface MessageParser<out R : Message> {
    /** @throws InvalidMessageException if message could not be successfully parsed */
    fun parse(messageBytes: ByteArray): R

    fun handleExceptionDuringParsing(e: Throwable): Nothing = when (e) {
        is InvalidMessageException ->
            throw e

        is IndexOutOfBoundsException, is BufferUnderflowException ->
            throw InvalidMessageException("Incorrect number of bytes received parsing response in ${this::class.java.simpleName}", e)

        else ->
            throw InvalidMessageException("Unknown issue during parsing in ${this::class.java.simpleName}", e)
    }
}
