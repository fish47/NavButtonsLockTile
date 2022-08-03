package com.fish47.navbuttonslocktile.hook;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.UserHandle;

import com.fish47.navbuttonslocktile.base.NavButtonsLock;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

final class HookState {

    private static final String TAG = "[ButtonLock] ";

    private boolean mButtonLockState = false;

    private boolean mButtonLockIsInitialized = false;

    private Boolean mSavedDevForceNavBarValue = null;

    private Object mButtonsLight = null;

    private final Uri mButtonLockUri = NavButtonsLock.getUri();

    private final ThreadLocal<Boolean> mIsHardwareCallBlocked = new ThreadLocal<>();

    public void registerObserver(Context ctx, Object observer) {
        ContentResolver resolver = ctx.getContentResolver();
        int userHandleAll = XposedHelpers.getStaticIntField(UserHandle.class, "USER_ALL");
        XposedHelpers.callMethod(resolver, "registerContentObserver",
                mButtonLockUri, false, observer, userHandleAll);
        XposedBridge.log(TAG + "observer registered");
    }

    public void setButtonLight(Object buttonLight) {
        mButtonsLight = buttonLight;
        XposedBridge.log(TAG + "find button light");
    }

    public void updateLockState(ClassLoader cl, Context ctx, boolean forceNavBar) {
        boolean isLockedNew = !forceNavBar && NavButtonsLock.getValue(ctx);
        if (mButtonLockIsInitialized && mButtonLockState == isLockedNew)
            return;

        mButtonLockState = isLockedNew;
        mButtonLockIsInitialized = true;

        Class<?> clz = XposedHelpers.findClass("lineageos.hardware.LineageHardwareManager", cl);
        int feature = XposedHelpers.getStaticIntField(clz, "FEATURE_KEY_DISABLE");
        Object manager = XposedHelpers.callStaticMethod(clz, "getInstance", ctx);
        boolean supported = (boolean) XposedHelpers.callMethod(manager, "isSupported", feature);
        if (!supported)
            return;

        XposedHelpers.callMethod(manager, "set", feature, mButtonLockState);
        if (isLockedNew && mButtonsLight != null)
            XposedHelpers.callMethod(mButtonsLight, "setBrightness", 0);
        XposedBridge.log(TAG + "lock state changed: " + mButtonLockState);
    }

    private static int getKeyDisableConstant(ClassLoader cl) {
        Class<?> clz = XposedHelpers.findClass("lineageos.hardware.LineageHardwareManager", cl);
        return XposedHelpers.getStaticIntField(clz, "FEATURE_KEY_DISABLE");
    }

    public void setHardwareCallBlocked(boolean blocked) {
        if (blocked)
            mIsHardwareCallBlocked.set(true);
        else
            mIsHardwareCallBlocked.remove();
    }

    public boolean isHardwareCallBlocked(ClassLoader cl, int feature) {
        return Boolean.TRUE.equals(mIsHardwareCallBlocked.get())
                && feature == getKeyDisableConstant(cl);
    }

    public void startUpdateButtonLight(Object powerManagerService) {
        mSavedDevForceNavBarValue = null;
        if (!mButtonLockIsInitialized || !mButtonLockState)
            return;

        // forced to set zero brightness if button is locked
        mSavedDevForceNavBarValue = XposedHelpers.getBooleanField(powerManagerService, "mDevForceNavbar");
        XposedHelpers.setBooleanField(powerManagerService, "mDevForceNavbar", true);
    }

    public void endUpdateButtonLight(Object powerManagerService) {
        if (mSavedDevForceNavBarValue == null)
            return;

        // restore value
        XposedHelpers.setBooleanField(powerManagerService, "mDevForceNavbar", mSavedDevForceNavBarValue);
        mSavedDevForceNavBarValue = null;
    }
}
