#include "libyuv.h"
#include <jni.h>
#include <android/log.h>

#include <stddef.h>
#include <malloc.h>
#include <string.h>

#define ENV(methodName, args...)\
    (*env)->methodName(env, args)\

void computeYuvPlaneSizes(
        int *yWidth, int *yHeight, int *uvWidth, int *uvHeight,
        jint left, jint top, jint right, jint bottom, jint rotation
) {
    if (rotation % 180 == 0) {
        *yWidth = right - left;
        *yHeight = bottom - top;
    } else {
        *yWidth = bottom - top;
        *yHeight = right - left;
    }
    *uvWidth = *yWidth / 2;
    *uvHeight = *yHeight / 2;

}

void adaptRotationMode(RotationModeEnum *rotationMode, jint rotation) {
    if (rotation == 0 || rotation == 360) {
        *rotationMode = kRotate0;
    } else if (rotation == 90) {
        *rotationMode = kRotate90;
    } else if (rotation == 180) {
        *rotationMode = kRotate180;
    } else if (rotation == 270) {
        *rotationMode = kRotate270;
    } else {
        // TODO: throw
    }
}

static void crop_rotate_yuv_nv21(
        jint srcWidth, jint srcHeight, jbyte *srcBytes,
        jbyte *dstBytes,
        jint left, jint top, jint right, jint bottom,
        jint rotation) {

    int yWidth, yHeight, uvWidth, uvHeight;
    computeYuvPlaneSizes(&yWidth, &yHeight, &uvWidth, &uvHeight, left, top, right, bottom, rotation);
    RotationModeEnum rotationMode;
    adaptRotationMode(&rotationMode, rotation);

    uint8_t *intermediaryU = malloc(uvWidth * uvHeight * sizeof(uint8_t));
    uint8_t *intermediaryV = malloc(uvWidth * uvHeight * sizeof(uint8_t));

    ConvertToI420(
            (uint8_t *) srcBytes,
            (size_t) 0,
            (uint8_t *) dstBytes, yWidth,
            intermediaryU, uvWidth,
            intermediaryV, uvWidth,
            left, top,
            srcWidth, srcHeight, (right - left), (bottom - top),
            rotationMode, FOURCC_NV21
    );

    MergeUVPlane(
            intermediaryV, uvWidth, intermediaryU, uvWidth,
            (uint8_t *) dstBytes + yWidth * yHeight, 2 * uvWidth, uvWidth, uvHeight
    );

    free(intermediaryU);
    free(intermediaryV);
}


JNIEXPORT jbyteArray JNICALL Java_com_simprints_uicomponents_imageTools_LibYuvJni_cropRotateYuvNV21(
        JNIEnv *env,
        jobject obj,
        jint srcWidth, jint srcHeight, jbyteArray srcByteArray,
        jint left, jint top, jint right, jint bottom,
        jint rotation
) {
    jbyte *srcBytes = ENV(GetByteArrayElements, srcByteArray, NULL);

    jbyteArray dstByteArray = ENV(NewByteArray, (bottom - top) * (right - left) * 3 / 2);
    jbyte *dstBytes = ENV(GetByteArrayElements, dstByteArray, NULL);

    crop_rotate_yuv_nv21(
            srcWidth, srcHeight, srcBytes, dstBytes,
            left, top, right, bottom,
            rotation
    );

    ENV(ReleaseByteArrayElements, dstByteArray, dstBytes, 0);
    ENV(ReleaseByteArrayElements, srcByteArray, srcBytes, JNI_ABORT);
    return dstByteArray;
}
