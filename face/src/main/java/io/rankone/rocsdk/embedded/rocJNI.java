/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.0
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package io.rankone.rocsdk.embedded;

public class rocJNI {
  public final static native byte[] cdata(long jarg1, int jarg2);
  public final static native void memmove(long jarg1, byte[] jarg2);
  public final static native String roc_preinitialize_android(java.lang.Object jarg1);
  public final static native long new_float();
  public final static native long copy_float(float jarg1);
  public final static native void delete_float(long jarg1);
  public final static native void float_assign(long jarg1, float jarg2);
  public final static native float float_value(long jarg1);
  public final static native long new_size_t();
  public final static native long copy_size_t(long jarg1);
  public final static native void delete_size_t(long jarg1);
  public final static native void size_t_assign(long jarg1, long jarg2);
  public final static native long size_t_value(long jarg1);
  public final static native long new_roc_similarity();
  public final static native long copy_roc_similarity(float jarg1);
  public final static native void delete_roc_similarity(long jarg1);
  public final static native void roc_similarity_assign(long jarg1, float jarg2);
  public final static native float roc_similarity_value(long jarg1);
  public final static native long new_roc_string();
  public final static native long copy_roc_string(String jarg1);
  public final static native void delete_roc_string(long jarg1);
  public final static native void roc_string_assign(long jarg1, String jarg2);
  public final static native String roc_string_value(long jarg1);
  public final static native long new_roc_buffer();
  public final static native long copy_roc_buffer(long jarg1);
  public final static native void delete_roc_buffer(long jarg1);
  public final static native void roc_buffer_assign(long jarg1, long jarg2);
  public final static native long roc_buffer_value(long jarg1);
  public final static native long new_roc_time();
  public final static native long copy_roc_time(java.math.BigInteger jarg1);
  public final static native void delete_roc_time(long jarg1);
  public final static native void roc_time_assign(long jarg1, java.math.BigInteger jarg2);
  public final static native java.math.BigInteger roc_time_value(long jarg1);
  public final static native long new_roc_algorithm_options();
  public final static native long copy_roc_algorithm_options(int jarg1);
  public final static native void delete_roc_algorithm_options(long jarg1);
  public final static native void roc_algorithm_options_assign(long jarg1, int jarg2);
  public final static native int roc_algorithm_options_value(long jarg1);
  public final static native long new_uint8_t_array(int jarg1);
  public final static native void delete_uint8_t_array(long jarg1);
  public final static native short uint8_t_array_getitem(long jarg1, int jarg2);
  public final static native void uint8_t_array_setitem(long jarg1, int jarg2, short jarg3);
  public final static native long new_roc_template_array(int jarg1);
  public final static native void delete_roc_template_array(long jarg1, roc_template jarg1_);
  public final static native long roc_template_array_getitem(long jarg1, roc_template jarg1_, int jarg2);
  public final static native void roc_template_array_setitem(long jarg1, roc_template jarg1_, int jarg2, long jarg3, roc_template jarg3_);
  public final static native long new_roc_similarity_array(int jarg1);
  public final static native void delete_roc_similarity_array(long jarg1);
  public final static native float roc_similarity_array_getitem(long jarg1, int jarg2);
  public final static native void roc_similarity_array_setitem(long jarg1, int jarg2, float jarg3);
  public final static native long new_roc_person_id_array(int jarg1);
  public final static native void delete_roc_person_id_array(long jarg1, roc_uuid jarg1_);
  public final static native long roc_person_id_array_getitem(long jarg1, roc_uuid jarg1_, int jarg2);
  public final static native void roc_person_id_array_setitem(long jarg1, roc_uuid jarg1_, int jarg2, long jarg3, roc_uuid jarg3_);
  public final static native long new_roc_detection_array(int jarg1);
  public final static native void delete_roc_detection_array(long jarg1, roc_detection jarg1_);
  public final static native long roc_detection_array_getitem(long jarg1, roc_detection jarg1_, int jarg2);
  public final static native void roc_detection_array_setitem(long jarg1, roc_detection jarg1_, int jarg2, long jarg3, roc_detection jarg3_);
  public final static native long new_roc_embedded_landmark_array(int jarg1);
  public final static native void delete_roc_embedded_landmark_array(long jarg1, roc_embedded_landmark jarg1_);
  public final static native long roc_embedded_landmark_array_getitem(long jarg1, roc_embedded_landmark jarg1_, int jarg2);
  public final static native void roc_embedded_landmark_array_setitem(long jarg1, roc_embedded_landmark jarg1_, int jarg2, long jarg3, roc_embedded_landmark jarg3_);
  public final static native int ROC_VERSION_MAJOR_get();
  public final static native int ROC_VERSION_MINOR_get();
  public final static native int ROC_VERSION_PATCH_get();
  public final static native String ROC_VERSION_STRING_get();
  public final static native int roc_version_major();
  public final static native int roc_version_minor();
  public final static native int roc_version_patch();
  public final static native String roc_version_string();
  public final static native String ROC_COPYRIGHT_get();
  public final static native String roc_copyright();
  public final static native void roc_ensure(String jarg1);
  public final static native String roc_set_logging(boolean jarg1, String jarg2, long jarg3);
  public final static native boolean roc_log(String jarg1);
  public final static native void roc_uuid_data_set(long jarg1, roc_uuid jarg1_, long jarg2);
  public final static native long roc_uuid_data_get(long jarg1, roc_uuid jarg1_);
  public final static native long new_roc_uuid();
  public final static native void delete_roc_uuid(long jarg1);
  public final static native void roc_uuid_set(long jarg1, roc_uuid jarg1_, byte[] jarg2);
  public final static native void roc_uuid_set_int(long jarg1, roc_uuid jarg1_, java.math.BigInteger jarg2);
  public final static native long roc_uuid_get_int(java.math.BigInteger jarg1);
  public final static native java.math.BigInteger roc_uuid_to_int(long jarg1, roc_uuid jarg1_);
  public final static native void roc_uuid_set_null(long jarg1, roc_uuid jarg1_);
  public final static native long roc_uuid_get_null();
  public final static native boolean roc_uuid_is_null(long jarg1, roc_uuid jarg1_);
  public final static native boolean roc_uuid_is_equal(long jarg1, roc_uuid jarg1_, long jarg2, roc_uuid jarg2_);
  public final static native boolean roc_uuid_is_less_than(long jarg1, roc_uuid jarg1_, long jarg2, roc_uuid jarg2_);
  public final static native void roc_hash_data_set(long jarg1, roc_hash jarg1_, long jarg2);
  public final static native long roc_hash_data_get(long jarg1, roc_hash jarg1_);
  public final static native long new_roc_hash();
  public final static native void delete_roc_hash(long jarg1);
  public final static native void roc_hash_set(long jarg1, roc_hash jarg1_, byte[] jarg2);
  public final static native void roc_hash_set_null(long jarg1, roc_hash jarg1_);
  public final static native boolean roc_hash_is_null(long jarg1, roc_hash jarg1_);
  public final static native boolean roc_hash_is_equal(long jarg1, roc_hash jarg1_, long jarg2, roc_hash jarg2_);
  public final static native boolean roc_hash_is_less_than(long jarg1, roc_hash jarg1_, long jarg2, roc_hash jarg2_);
  public final static native long roc_uuid_to_hash(long jarg1, roc_uuid jarg1_);
  public final static native long roc_hash_to_uuid(long jarg1, roc_hash jarg1_);
  public final static native int ROC_GRAY8_get();
  public final static native int ROC_BGR24_get();
  public final static native void roc_image_data_set(long jarg1, roc_image jarg1_, long jarg2);
  public final static native long roc_image_data_get(long jarg1, roc_image jarg1_);
  public final static native void roc_image_width_set(long jarg1, roc_image jarg1_, long jarg2);
  public final static native long roc_image_width_get(long jarg1, roc_image jarg1_);
  public final static native void roc_image_height_set(long jarg1, roc_image jarg1_, long jarg2);
  public final static native long roc_image_height_get(long jarg1, roc_image jarg1_);
  public final static native void roc_image_step_set(long jarg1, roc_image jarg1_, long jarg2);
  public final static native long roc_image_step_get(long jarg1, roc_image jarg1_);
  public final static native void roc_image_color_space_set(long jarg1, roc_image jarg1_, int jarg2);
  public final static native int roc_image_color_space_get(long jarg1, roc_image jarg1_);
  public final static native void roc_image_media_id_set(long jarg1, roc_image jarg1_, long jarg2, roc_hash jarg2_);
  public final static native long roc_image_media_id_get(long jarg1, roc_image jarg1_);
  public final static native void roc_image_timestamp_set(long jarg1, roc_image jarg1_, java.math.BigInteger jarg2);
  public final static native java.math.BigInteger roc_image_timestamp_get(long jarg1, roc_image jarg1_);
  public final static native long new_roc_image();
  public final static native void delete_roc_image(long jarg1);
  public final static native String roc_new_image(long jarg1, long jarg2, long jarg3, int jarg4, long jarg5, roc_hash jarg5_, java.math.BigInteger jarg6, byte[] jarg7, long jarg8, roc_image jarg8_);
  public final static native String roc_copy_image(long jarg1, roc_image jarg1_, long jarg2, roc_image jarg2_);
  public final static native String roc_rotate(long jarg1, roc_image jarg1_, int jarg2);
  public final static native String roc_swap_channels(long jarg1, roc_image jarg1_);
  public final static native String roc_bgr2gray(long jarg1, roc_image jarg1_, long jarg2, roc_image jarg2_);
  public final static native String roc_to_rgba(long jarg1, roc_image jarg1_, byte[] jarg2);
  public final static native String roc_from_rgba(byte[] jarg1, long jarg2, long jarg3, long jarg4, long jarg5, roc_image jarg5_);
  public final static native String roc_from_bgra(byte[] jarg1, long jarg2, long jarg3, long jarg4, long jarg5, roc_image jarg5_);
  public final static native String roc_from_yuv(byte[] jarg1, byte[] jarg2, byte[] jarg3, long jarg4, long jarg5, long jarg6, long jarg7, long jarg8, long jarg9, roc_image jarg9_);
  public final static native String roc_read_ppm(String jarg1, long jarg2, roc_image jarg2_);
  public final static native String roc_write_ppm(String jarg1, long jarg2, roc_image jarg2_);
  public final static native String roc_free_image(long jarg1, roc_image jarg1_);
  public final static native String roc_set_string(String jarg1, long jarg2);
  public final static native String roc_free_string(long jarg1);
  public final static native String roc_free_buffer(long jarg1);
  public final static native int ROC_POSE_FRONTAL_get();
  public final static native int ROC_POSE_LEFT_PROFILE_get();
  public final static native int ROC_POSE_RIGHT_PROFILE_get();
  public final static native int ROC_POSE_NUM_POSES_get();
  public final static native int ROC_POSE_PERIOCULAR_get();
  public final static native String roc_pose_to_string(long jarg1);
  public final static native void roc_detection_x_set(long jarg1, roc_detection jarg1_, float jarg2);
  public final static native float roc_detection_x_get(long jarg1, roc_detection jarg1_);
  public final static native void roc_detection_y_set(long jarg1, roc_detection jarg1_, float jarg2);
  public final static native float roc_detection_y_get(long jarg1, roc_detection jarg1_);
  public final static native void roc_detection_width_set(long jarg1, roc_detection jarg1_, float jarg2);
  public final static native float roc_detection_width_get(long jarg1, roc_detection jarg1_);
  public final static native void roc_detection_height_set(long jarg1, roc_detection jarg1_, float jarg2);
  public final static native float roc_detection_height_get(long jarg1, roc_detection jarg1_);
  public final static native void roc_detection_rotation_set(long jarg1, roc_detection jarg1_, float jarg2);
  public final static native float roc_detection_rotation_get(long jarg1, roc_detection jarg1_);
  public final static native void roc_detection_confidence_set(long jarg1, roc_detection jarg1_, float jarg2);
  public final static native float roc_detection_confidence_get(long jarg1, roc_detection jarg1_);
  public final static native void roc_detection_pose_set(long jarg1, roc_detection jarg1_, long jarg2);
  public final static native long roc_detection_pose_get(long jarg1, roc_detection jarg1_);
  public final static native long new_roc_detection();
  public final static native void delete_roc_detection(long jarg1);
  public final static native String roc_landmarks_to_detection(float jarg1, float jarg2, float jarg3, float jarg4, float jarg5, float jarg6, long jarg7, roc_detection jarg7_);
  public final static native String roc_adaptive_minimum_size(long jarg1, roc_image jarg1_, float jarg2, long jarg3, long jarg4);
  public final static native int ROC_FRONTAL_get();
  public final static native int ROC_FULL_get();
  public final static native int ROC_PERIOCULAR_get();
  public final static native int ROC_MANUAL_get();
  public final static native int ROC_ROLL_get();
  public final static native int ROC_FR_get();
  public final static native int ROC_FR_FAST_get();
  public final static native int ROC_FR_PERIOCULAR_get();
  public final static native int ROC_TATTOO_get();
  public final static native int ROC_PAD_LOGO_get();
  public final static native int ROC_PITCHYAW_get();
  public final static native int ROC_DEMOGRAPHICS_get();
  public final static native int ROC_LANDMARKS_get();
  public final static native int ROC_LIPS_get();
  public final static native int ROC_THUMBNAIL_get();
  public final static native int ROC_INVALID_get();
  public final static native int ROC_SPOOF_FF_get();
  public final static native int ROC_SPOOF_AF_get();
  public final static native int ROC_SERIAL_get();
  public final static native int ROC_TEMPLATE_VERSION_get();
  public final static native int ROC_FR_COMPATIBILITY_VERSION_get();
  public final static native int ROC_TATTOO_COMPATIBILITY_VERSION_get();
  public final static native int ROC_TEMPLATE_VERSION_MASK_get();
  public final static native String roc_algorithm_option_to_string(int jarg1, long jarg2);
  public final static native String roc_algorithm_option_from_string(String jarg1, long jarg2);
  public final static native String roc_check_template_version(long jarg1);
  public final static native java.math.BigInteger ROC_NO_TIMESTAMP_get();
  public final static native long ROC_FR_FV_SIZE_get();
  public final static native long ROC_FR_FAST_FV_SIZE_get();
  public final static native long ROC_FR_PERIOCULAR_FV_SIZE_get();
  public final static native long ROC_TATTOO_FV_SIZE_get();
  public final static native void roc_template_algorithm_id_set(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_template_algorithm_id_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_detection_set(long jarg1, roc_template jarg1_, long jarg2, roc_detection jarg2_);
  public final static native long roc_template_detection_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_template_id_set(long jarg1, roc_template jarg1_, long jarg2, roc_uuid jarg2_);
  public final static native long roc_template_template_id_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_person_id_set(long jarg1, roc_template jarg1_, long jarg2, roc_uuid jarg2_);
  public final static native long roc_template_person_id_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_timestamp_set(long jarg1, roc_template jarg1_, java.math.BigInteger jarg2);
  public final static native java.math.BigInteger roc_template_timestamp_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_media_id_set(long jarg1, roc_template jarg1_, long jarg2, roc_hash jarg2_);
  public final static native long roc_template_media_id_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_md_size_set(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_template_md_size_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_fv_size_set(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_template_fv_size_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_tn_size_set(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_template_tn_size_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_md_set(long jarg1, roc_template jarg1_, String jarg2);
  public final static native String roc_template_md_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_fv_set(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_template_fv_get(long jarg1, roc_template jarg1_);
  public final static native void roc_template_tn_set(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_template_tn_get(long jarg1, roc_template jarg1_);
  public final static native long new_roc_template();
  public final static native void delete_roc_template(long jarg1);
  public final static native long roc_template_header_size_get();
  public final static native String roc_free_template(long jarg1, roc_template jarg1_);
  public final static native String roc_copy_template(long jarg1, roc_template jarg1_, long jarg2, roc_template jarg2_);
  public final static native String roc_flatten(long jarg1, roc_template jarg1_, byte[] jarg2);
  public final static native String roc_unflatten(byte[] jarg1, long jarg2, roc_template jarg2_);
  public final static native String roc_flattened_bytes(long jarg1, roc_template jarg1_, long jarg2);
  public final static native long roc_cast(long jarg1);
  public final static native float ROC_MAX_SIMILARITY_get();
  public final static native float ROC_MIN_SIMILARITY_get();
  public final static native float ROC_INVALID_SIMILARITY_get();
  public final static native String roc_fuse(long jarg1, long jarg2, long jarg3);
  public final static native String roc_enable_openmp(boolean jarg1);
  public final static native int ROC_NOT_SUPPORTED_get();
  public final static native int ROC_SUCCESS_get();
  public final static native int ROC_ERROR_LICENSE_get();
  public final static native int ROC_ERROR_MALLOC_get();
  public final static native int ROC_ERROR_COLORSPACE_get();
  public final static native int roc_embedded_initialize(String jarg1);
  public final static native int roc_embedded_finalize();
  public final static native String roc_embedded_error_to_string(int jarg1);
  public final static native int roc_embedded_detect_faces(long jarg1, roc_image jarg1_, long jarg2, int jarg3, float jarg4, long jarg5, long jarg6, roc_detection jarg6_);
  public final static native void roc_embedded_landmark_x_set(long jarg1, roc_embedded_landmark jarg1_, float jarg2);
  public final static native float roc_embedded_landmark_x_get(long jarg1, roc_embedded_landmark jarg1_);
  public final static native void roc_embedded_landmark_y_set(long jarg1, roc_embedded_landmark jarg1_, float jarg2);
  public final static native float roc_embedded_landmark_y_get(long jarg1, roc_embedded_landmark jarg1_);
  public final static native long new_roc_embedded_landmark();
  public final static native void delete_roc_embedded_landmark(long jarg1);
  public final static native int roc_embedded_landmark_face(long jarg1, roc_image jarg1_, long jarg2, roc_detection jarg2_, long jarg3, roc_embedded_landmark jarg3_, long jarg4, roc_embedded_landmark jarg4_, long jarg5, roc_embedded_landmark jarg5_, long jarg6, roc_embedded_landmark jarg6_, long jarg7, long jarg8);
  public final static native int roc_embedded_liveness(long jarg1, roc_image jarg1_, long jarg2, roc_embedded_landmark jarg2_, boolean jarg3, long jarg4);
  public final static native void roc_embedded_gender_female_set(long jarg1, roc_embedded_gender jarg1_, float jarg2);
  public final static native float roc_embedded_gender_female_get(long jarg1, roc_embedded_gender jarg1_);
  public final static native void roc_embedded_gender_male_set(long jarg1, roc_embedded_gender jarg1_, float jarg2);
  public final static native float roc_embedded_gender_male_get(long jarg1, roc_embedded_gender jarg1_);
  public final static native long new_roc_embedded_gender();
  public final static native void delete_roc_embedded_gender(long jarg1);
  public final static native void roc_embedded_geographic_origin_african_set(long jarg1, roc_embedded_geographic_origin jarg1_, float jarg2);
  public final static native float roc_embedded_geographic_origin_african_get(long jarg1, roc_embedded_geographic_origin jarg1_);
  public final static native void roc_embedded_geographic_origin_european_set(long jarg1, roc_embedded_geographic_origin jarg1_, float jarg2);
  public final static native float roc_embedded_geographic_origin_european_get(long jarg1, roc_embedded_geographic_origin jarg1_);
  public final static native void roc_embedded_geographic_origin_east_asian_set(long jarg1, roc_embedded_geographic_origin jarg1_, float jarg2);
  public final static native float roc_embedded_geographic_origin_east_asian_get(long jarg1, roc_embedded_geographic_origin jarg1_);
  public final static native void roc_embedded_geographic_origin_south_asian_set(long jarg1, roc_embedded_geographic_origin jarg1_, float jarg2);
  public final static native float roc_embedded_geographic_origin_south_asian_get(long jarg1, roc_embedded_geographic_origin jarg1_);
  public final static native long new_roc_embedded_geographic_origin();
  public final static native void delete_roc_embedded_geographic_origin(long jarg1);
  public final static native void roc_embedded_glasses_none_set(long jarg1, roc_embedded_glasses jarg1_, float jarg2);
  public final static native float roc_embedded_glasses_none_get(long jarg1, roc_embedded_glasses jarg1_);
  public final static native void roc_embedded_glasses_sun_set(long jarg1, roc_embedded_glasses jarg1_, float jarg2);
  public final static native float roc_embedded_glasses_sun_get(long jarg1, roc_embedded_glasses jarg1_);
  public final static native void roc_embedded_glasses_eye_set(long jarg1, roc_embedded_glasses jarg1_, float jarg2);
  public final static native float roc_embedded_glasses_eye_get(long jarg1, roc_embedded_glasses jarg1_);
  public final static native long new_roc_embedded_glasses();
  public final static native void delete_roc_embedded_glasses(long jarg1);
  public final static native int roc_embedded_represent_face(long jarg1, roc_image jarg1_, long jarg2, roc_detection jarg2_, long jarg3, roc_embedded_landmark jarg3_, long jarg4, roc_embedded_landmark jarg4_, long jarg5, roc_embedded_landmark jarg5_, long jarg6, long jarg7, long jarg8, long jarg9, roc_embedded_gender jarg9_, long jarg10, roc_embedded_geographic_origin jarg10_, long jarg11, roc_embedded_glasses jarg11_);
  public final static native float roc_embedded_compare_templates(long jarg1, long jarg2, long jarg3, long jarg4, long jarg5);
  public final static native void roc_embedded_array_element_size_set(long jarg1, roc_embedded_array jarg1_, int jarg2);
  public final static native int roc_embedded_array_element_size_get(long jarg1, roc_embedded_array jarg1_);
  public final static native void roc_embedded_array_size_set(long jarg1, roc_embedded_array jarg1_, int jarg2);
  public final static native int roc_embedded_array_size_get(long jarg1, roc_embedded_array jarg1_);
  public final static native void roc_embedded_array_data_set(long jarg1, roc_embedded_array jarg1_, String jarg2);
  public final static native String roc_embedded_array_data_get(long jarg1, roc_embedded_array jarg1_);
  public final static native long new_roc_embedded_array();
  public final static native void delete_roc_embedded_array(long jarg1);
  public final static native void roc_embedded_array_initialize(long jarg1, roc_embedded_array jarg1_, int jarg2);
  public final static native int roc_embedded_array_append(long jarg1, roc_embedded_array jarg1_, long jarg2);
  public final static native void roc_embedded_array_get(long jarg1, roc_embedded_array jarg1_, int jarg2, long jarg3);
  public final static native void roc_embedded_array_free(long jarg1, roc_embedded_array jarg1_);
  public final static native long roc_embedded_checksum(long jarg1, int jarg2);
}
