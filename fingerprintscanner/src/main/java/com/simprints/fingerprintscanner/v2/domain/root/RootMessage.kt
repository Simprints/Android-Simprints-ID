package com.simprints.fingerprintscanner.v2.domain.root


sealed class RootMessage(val rootMessageType: RootMessageType) {

    fun getBytes(): ByteArray =
        RootMessageProtocol.buildMessageBytes(rootMessageType, getDataBytes())

    open fun getDataBytes() = byteArrayOf()
}

abstract class RootCommand(rootMessageType: RootMessageType) : RootMessage(rootMessageType)

abstract class RootResponse(rootMessageType: RootMessageType) : RootMessage(rootMessageType)
