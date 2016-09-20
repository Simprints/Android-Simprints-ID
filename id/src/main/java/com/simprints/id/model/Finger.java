package com.simprints.id.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.simprints.id.R;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libmatcher.sourceafis.templates.Template;
import com.simprints.libsimprints.FingerIdentifier;


public class Finger implements Parcelable, Comparable<Finger>{

    public final static int NB_OF_FINGERS = 10;
    public static final Parcelable.Creator<Finger> CREATOR
            = new Parcelable.Creator<Finger>() {
        public Finger createFromParcel(Parcel in) {
            return new Finger(in);
        }

        public Finger[] newArray(int size) {
            return new Finger[size];
        }
    };
    private FingerIdentifier id;
    private boolean isActive;
    private Status status;
    private Fingerprint template;
    private boolean lastFinger;
    private int priority;

    public Finger(@NonNull FingerIdentifier id, boolean isActive, boolean isLastFinger, int priority) {
        this.id = id;
        this.isActive = isActive;
        this.status = Status.NOT_COLLECTED;
        this.template = null;
        this.lastFinger = isLastFinger;
        this.priority = priority;
    }


    private Finger(Parcel in) {
        id = FingerIdentifier.values()[in.readInt()];
        isActive = in.readInt() == 1;
        status = Status.values()[in.readInt()];
        if (in.readInt() == 1) {
            template = in.readParcelable(Fingerprint.class.getClassLoader());
        } else {
            template = null;
        }
    }

    public FingerIdentifier getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Fingerprint getTemplate() {
        return template;
    }

    public void setTemplate(Fingerprint template) {
        this.template = template;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id.ordinal());
        out.writeInt(isActive ? 1 : 0);
        out.writeInt(status.ordinal());
        out.writeInt(template != null ? 1 : 0);
        if (template != null) {
            out.writeParcelable(template, 0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Finger finger = (Finger) o;

        return id.equals(finger.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Finger other) {
        return this.priority - other.priority;
    }

    public boolean isLastFinger() {
        return lastFinger;
    }

    public void setLastFinger(boolean lastFinger) {
        this.lastFinger = lastFinger;
    }

    public int getPriority() {
        return priority;
    }

    public enum Status {
        NOT_COLLECTED(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
                R.string.scan_label, Color.WHITE, Color.GRAY, R.string.empty, Color.WHITE,
                R.string.please_scan, Color.GRAY),
        COLLECTING(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
                R.string.cancel_button, Color.WHITE, Color.BLUE, R.string.empty, Color.WHITE,
                R.string.scanning, Color.GRAY),
        GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
                R.string.good_scan_message, Color.WHITE, Color.argb(255, 0, 204, 0),
                R.string.good_scan_message, Color.GREEN, R.string.good_scan_direction, Color.GRAY),
        RESCAN_GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
                R.string.rescan_label_question, Color.WHITE, Color.argb(255, 0, 204, 0),
                R.string.good_scan_message, Color.GREEN, R.string.good_scan_direction, Color.GRAY),
        BAD_SCAN(R.drawable.ic_alert_selected, R.drawable.ic_alert_deselected,
                R.string.rescan_label, Color.WHITE, Color.argb(255, 204, 0, 0),
                R.string.poor_scan_message, Color.RED, R.string.poor_scan_direction, Color.GRAY);

        private int dotSelectedDrawableId;
        private int dotDeselectedDrawableId;
        private int buttonTextId;
        private int buttonTextColor;
        private int buttonBgColor;
        private int textResult;
        private int textResultColor;
        private int textDirection;
        private int textDirectionColor;

        Status(int selectedDrawableId, int deselectedDrawableId, int textId,
               int textColor, int bgColor, int result, int resultColor, int direction,
               int directionColor) {
            this.dotSelectedDrawableId = selectedDrawableId;
            this.dotDeselectedDrawableId = deselectedDrawableId;
            this.buttonTextId = textId;
            this.buttonTextColor = textColor;
            this.buttonBgColor = bgColor;
            this.textResult = result;
            this.textResultColor = resultColor;
            this.textDirection = direction;
            this.textDirectionColor = directionColor;
        }

        public int getDrawableId(boolean selected) {
            return selected ? dotSelectedDrawableId : dotDeselectedDrawableId;
        }

        public int getButtonTextId() {
            return buttonTextId;
        }

        public int getButtonTextColor() {
            return buttonTextColor;
        }

        public int getButtonBgColor() {
            return buttonBgColor;
        }

        public int getTextResult() {
            return textResult;
        }

        public int getTextResultColor() {
            return textResultColor;
        }

        public int getTextDirection() {
            return textDirection;
        }

        public int getTextDirectionColor() {
            return textDirectionColor;
        }
    }
}
