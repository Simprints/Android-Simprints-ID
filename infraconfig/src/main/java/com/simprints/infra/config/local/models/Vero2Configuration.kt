package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.Vero2Configuration
import com.simprints.infra.config.exceptions.InvalidProtobufEnumException

internal fun Vero2Configuration.toProto(): ProtoVero2Configuration =
    ProtoVero2Configuration.newBuilder()
        .setImageSavingStrategy(imageSavingStrategy.toProto())
        .setCaptureStrategy(captureStrategy.toProto())
        .setDisplayLiveFeedback(displayLiveFeedback)
        .putAllFirmwareVersions(firmwareVersions.map { it.key to it.value.toProto() }.toMap())
        .build()

internal fun Vero2Configuration.ImageSavingStrategy.toProto(): ProtoVero2Configuration.ImageSavingStrategy =
    when (this) {
        Vero2Configuration.ImageSavingStrategy.NEVER -> ProtoVero2Configuration.ImageSavingStrategy.NEVER
        Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN -> ProtoVero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
        Vero2Configuration.ImageSavingStrategy.EAGER -> ProtoVero2Configuration.ImageSavingStrategy.EAGER
    }

internal fun Vero2Configuration.CaptureStrategy.toProto(): ProtoVero2Configuration.CaptureStrategy =
    when (this) {
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI
        Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI
    }

internal fun Vero2Configuration.Vero2FirmwareVersions.toProto(): ProtoVero2Configuration.Vero2FirmwareVersions =
    ProtoVero2Configuration.Vero2FirmwareVersions.newBuilder()
        .setCypress(cypress)
        .setStm(stm)
        .setUn20(un20)
        .build()

internal fun ProtoVero2Configuration.toDomain(): Vero2Configuration =
    Vero2Configuration(
        imageSavingStrategy.toDomain(),
        captureStrategy.toDomain(),
        displayLiveFeedback,
        firmwareVersionsMap.map { it.key to it.value.toDomain() }.toMap(),
    )

internal fun ProtoVero2Configuration.ImageSavingStrategy.toDomain(): Vero2Configuration.ImageSavingStrategy =
    when (this) {
        ProtoVero2Configuration.ImageSavingStrategy.NEVER -> Vero2Configuration.ImageSavingStrategy.NEVER
        ProtoVero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN -> Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
        ProtoVero2Configuration.ImageSavingStrategy.EAGER -> Vero2Configuration.ImageSavingStrategy.EAGER
        ProtoVero2Configuration.ImageSavingStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException(
            "invalid ImageSavingStrategy $name"
        )
    }

internal fun ProtoVero2Configuration.CaptureStrategy.toDomain(): Vero2Configuration.CaptureStrategy =
    when (this) {
        ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI
        ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
        ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI
        ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI
        ProtoVero2Configuration.CaptureStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid CaptureStrategy $name")
    }

internal fun ProtoVero2Configuration.Vero2FirmwareVersions.toDomain(): Vero2Configuration.Vero2FirmwareVersions =
    Vero2Configuration.Vero2FirmwareVersions(
        cypress,
        stm,
        un20,
    )
