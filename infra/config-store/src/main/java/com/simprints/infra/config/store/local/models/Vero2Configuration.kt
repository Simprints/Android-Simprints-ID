package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode

internal fun Vero2Configuration.toProto(): ProtoVero2Configuration = ProtoVero2Configuration
    .newBuilder()
    .setQualityThreshold(qualityThreshold)
    .setImageSavingStrategy(imageSavingStrategy.toProto())
    .setCaptureStrategy(captureStrategy.toProto())
    .setLedsMode(ledsMode.toProto())
    .putAllFirmwareVersions(firmwareVersions.mapValues { it.value.toProto() })
    .build()

internal fun Vero2Configuration.ImageSavingStrategy.toProto(): ProtoVero2Configuration.ImageSavingStrategy = when (this) {
    Vero2Configuration.ImageSavingStrategy.NEVER -> ProtoVero2Configuration.ImageSavingStrategy.NEVER
    Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN -> ProtoVero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
    Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE -> ProtoVero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
    Vero2Configuration.ImageSavingStrategy.EAGER -> ProtoVero2Configuration.ImageSavingStrategy.EAGER
}

internal fun Vero2Configuration.CaptureStrategy.toProto(): ProtoVero2Configuration.CaptureStrategy = when (this) {
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI
}

internal fun LedsMode.toProto() = when (this) {
    LedsMode.BASIC -> ProtoVero2Configuration.LedsMode.BASIC
    LedsMode.LIVE_QUALITY_FEEDBACK -> ProtoVero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK
    LedsMode.VISUAL_SCAN_FEEDBACK -> ProtoVero2Configuration.LedsMode.VISUAL_SCAN_FEEDBACK
}

internal fun Vero2Configuration.Vero2FirmwareVersions.toProto(): ProtoVero2Configuration.Vero2FirmwareVersions =
    ProtoVero2Configuration.Vero2FirmwareVersions
        .newBuilder()
        .setCypress(cypress)
        .setStm(stm)
        .setUn20(un20)
        .build()

internal fun ProtoVero2Configuration.toDomain(): Vero2Configuration = Vero2Configuration(
    qualityThreshold,
    imageSavingStrategy.toDomain(),
    captureStrategy.toDomain(),
    ledsMode.toDomain(),
    firmwareVersionsMap.mapValues { it.value.toDomain() },
)

internal fun ProtoVero2Configuration.ImageSavingStrategy.toDomain(): Vero2Configuration.ImageSavingStrategy = when (this) {
    ProtoVero2Configuration.ImageSavingStrategy.NEVER -> Vero2Configuration.ImageSavingStrategy.NEVER
    ProtoVero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN -> Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
    ProtoVero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE -> Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
    ProtoVero2Configuration.ImageSavingStrategy.EAGER -> Vero2Configuration.ImageSavingStrategy.EAGER
    ProtoVero2Configuration.ImageSavingStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid ImageSavingStrategy $name",
    )
}

internal fun ProtoVero2Configuration.CaptureStrategy.toDomain(): Vero2Configuration.CaptureStrategy = when (this) {
    ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI
    ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI
    ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI
    ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI -> Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI
    ProtoVero2Configuration.CaptureStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid CaptureStrategy $name")
}

internal fun ProtoVero2Configuration.LedsMode.toDomain() = when (this) {
    ProtoVero2Configuration.LedsMode.BASIC -> LedsMode.BASIC
    ProtoVero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK -> LedsMode.LIVE_QUALITY_FEEDBACK
    ProtoVero2Configuration.LedsMode.VISUAL_SCAN_FEEDBACK -> LedsMode.VISUAL_SCAN_FEEDBACK
    ProtoVero2Configuration.LedsMode.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid LedsMode $name")
}

internal fun ProtoVero2Configuration.Vero2FirmwareVersions.toDomain(): Vero2Configuration.Vero2FirmwareVersions =
    Vero2Configuration.Vero2FirmwareVersions(
        cypress,
        stm,
        un20,
    )
