package org.xbase.android.utils;

import android.app.Application;

import java.util.Locale;
import java.util.regex.Pattern;

public class Storage implements Comparable<Storage> {
    // ===========================================================
    // Constants
    // ===========================================================

    public static final int TYPE_SECONDARY       = 0;
    public static final int TYPE_PRIMARY         = 1;
    //
    public static final int STATUS_AVAILABLE     = 1;
    public static final int STATUS_NOT_AVAILABLE = 2;
    public static final int STATUS_REMOVED       = 3;

    // ===========================================================
    // Fields
    // ===========================================================

    private int             mID;
    private String          mPath;
    private int             mType;
    private long            mTotalBytes;
    private long            mAvailableBytes;
    private long            mFreeBytes;
    private boolean         mReadOnly;
    private int             mStatus;

    // ===========================================================
    // Constructors
    // ===========================================================

    public Storage() {
        this.mReadOnly = false;
        this.mStatus = STATUS_AVAILABLE;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public String getPath() {
        return mPath;
    }

    public void setPath(String pPath) {
        this.mPath = pPath;
    }

    public int getType() {
        return mType;
    }

    public void setType(int pType) {
        this.mType = pType;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    public void setTotalBytes(long pTotalBytes) {
        this.mTotalBytes = pTotalBytes;
    }

    public long getAvailableBytes() {
        return mAvailableBytes;
    }

    public void setAvailableBytes(long pAvailableBytes) {
        this.mAvailableBytes = pAvailableBytes;
    }

    public long getFreeBytes() {
        return mFreeBytes;
    }

    public void setFreeBytes(long pFreeBytes) {
        this.mFreeBytes = pFreeBytes;
    }

    public boolean isReadOnly() {
        return mReadOnly;
    }

    public void setReadOnly(boolean pReadOnly) {
        this.mReadOnly = pReadOnly;
    }

    public int getID() {
        return mID;
    }

    public void setID(int pID) {
        this.mID = pID;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int pStatus) {
        this.mStatus = pStatus;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        content.append(getName()).append(" (").append(this.mType == TYPE_PRIMARY ? "Primary" : "Secondary")
                .append(") ");
        content.append(this.mPath).append(" readonly(").append(mReadOnly).append(") Total(");
        content.append(FileBasicUtils.getHumanReadableSize(mTotalBytes, false)).append(") Free(");
        content.append(FileBasicUtils.getHumanReadableSize(mFreeBytes, false)).append(")");
        return content.toString();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public String getName() {
        Application application = null;
//        if (ApplicationImpl.getInstance() != null) {
//            application = ApplicationImpl.getInstance().getApplication();
//        }
        if (application != null) {
            if (this.mType == TYPE_PRIMARY) {
                return "Local";
            } else {
                String pattern_sdcard = ".*[((Sd)|(SD)|(sd))[c|C].*|((sd)|(Sd))]$";
                String pattern_usb = ".*((usb)|(Usb)|(USB)|(sd.[0-9])|(Sd.[0-9])|(SD.[0-9])).*";
                if (Pattern.matches(pattern_usb, mPath)) {
                    return String.format(Locale.getDefault(), "USB_" + mID);
                } else if (Pattern.matches(pattern_sdcard, mPath)) {
                    return String.format(Locale.getDefault(), "SD_" + mID);
                }
            }
            return String.format(Locale.getDefault(), "SD_" + mID);
        }
        return "";
    }

    @Override
    public int compareTo(Storage arg0) {
        if (this.mType == TYPE_SECONDARY) {
            return -1;
        } else if (arg0.getType() == TYPE_SECONDARY) {
            return 1;
        }
        return 0;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
