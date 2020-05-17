/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.0
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package io.rankone.rocsdk.embedded;

public class roc_template {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected roc_template(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(roc_template obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        rocJNI.delete_roc_template(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setAlgorithm_id(long value) {
    rocJNI.roc_template_algorithm_id_set(swigCPtr, this, value);
  }

  public long getAlgorithm_id() {
    return rocJNI.roc_template_algorithm_id_get(swigCPtr, this);
  }

  public void setTimestamp(long value) {
    rocJNI.roc_template_timestamp_set(swigCPtr, this, value);
  }

  public long getTimestamp() {
    return rocJNI.roc_template_timestamp_get(swigCPtr, this);
  }

  public void setPerson_id(long value) {
    rocJNI.roc_template_person_id_set(swigCPtr, this, value);
  }

  public long getPerson_id() {
    return rocJNI.roc_template_person_id_get(swigCPtr, this);
  }

  public void setDetection(roc_detection value) {
    rocJNI.roc_template_detection_set(swigCPtr, this, roc_detection.getCPtr(value), value);
  }

  public roc_detection getDetection() {
    long cPtr = rocJNI.roc_template_detection_get(swigCPtr, this);
    return (cPtr == 0) ? null : new roc_detection(cPtr, false);
  }

  public void setMd_size(long value) {
    rocJNI.roc_template_md_size_set(swigCPtr, this, value);
  }

  public long getMd_size() {
    return rocJNI.roc_template_md_size_get(swigCPtr, this);
  }

  public void setFv_size(long value) {
    rocJNI.roc_template_fv_size_set(swigCPtr, this, value);
  }

  public long getFv_size() {
    return rocJNI.roc_template_fv_size_get(swigCPtr, this);
  }

  public void setTn_size(long value) {
    rocJNI.roc_template_tn_size_set(swigCPtr, this, value);
  }

  public long getTn_size() {
    return rocJNI.roc_template_tn_size_get(swigCPtr, this);
  }

  public void setMd(String value) {
    rocJNI.roc_template_md_set(swigCPtr, this, value);
  }

  public String getMd() {
    return rocJNI.roc_template_md_get(swigCPtr, this);
  }

  public void setFv(SWIGTYPE_p_unsigned_char value) {
    rocJNI.roc_template_fv_set(swigCPtr, this, SWIGTYPE_p_unsigned_char.getCPtr(value));
  }

  public SWIGTYPE_p_unsigned_char getFv() {
    long cPtr = rocJNI.roc_template_fv_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_char(cPtr, false);
  }

  public void setTn(SWIGTYPE_p_unsigned_char value) {
    rocJNI.roc_template_tn_set(swigCPtr, this, SWIGTYPE_p_unsigned_char.getCPtr(value));
  }

  public SWIGTYPE_p_unsigned_char getTn() {
    long cPtr = rocJNI.roc_template_tn_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_char(cPtr, false);
  }

  public roc_template() {
    this(rocJNI.new_roc_template(), true);
  }

}
