/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.0
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package io.rankone.rocsdk.embedded;

public class roc_embedded_landmark {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public roc_embedded_landmark(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(roc_embedded_landmark obj) {
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
        rocJNI.delete_roc_embedded_landmark(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setX(float value) {
    rocJNI.roc_embedded_landmark_x_set(swigCPtr, this, value);
  }

  public float getX() {
    return rocJNI.roc_embedded_landmark_x_get(swigCPtr, this);
  }

  public void setY(float value) {
    rocJNI.roc_embedded_landmark_y_set(swigCPtr, this, value);
  }

  public float getY() {
    return rocJNI.roc_embedded_landmark_y_get(swigCPtr, this);
  }

  public roc_embedded_landmark() {
    this(rocJNI.new_roc_embedded_landmark(), true);
  }

}
