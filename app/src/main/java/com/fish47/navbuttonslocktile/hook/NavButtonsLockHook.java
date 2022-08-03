package com.fish47.navbuttonslocktile.hook;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class NavButtonsLockHook {

    private static Context getContextField(Object obj, String name) {
        return (Context) XposedHelpers.getObjectField(obj, name);
    }

    public static void execute(ClassLoader cl) {
        HookState state = new HookState();

        XposedHelpers.findAndHookMethod(
                "com.android.server.power.PowerManagerService", cl,
                "systemReady",
                "com.android.internal.app.IAppOpsService",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object service = param.thisObject;
                        Context context = getContextField(service, "mContext");
                        Object observer = XposedHelpers.getObjectField(service, "mSettingsObserver");
                        Object buttonLight = XposedHelpers.getObjectField(service, "mButtonsLight");
                        state.setButtonLight(buttonLight);
                        state.registerObserver(context, observer);
                    }
                });

        XposedHelpers.findAndHookMethod(
                "com.android.server.power.PowerManagerService", cl,
                "updateSettingsLocked",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object service = param.thisObject;
                        ClassLoader cl = service.getClass().getClassLoader();
                        Context context = getContextField(service, "mContext");
                        boolean forceNavBar = XposedHelpers.getBooleanField(service, "mDevForceNavbar");
                        state.updateLockState(cl, context, forceNavBar);
                    }
                });

        XposedHelpers.findAndHookMethod(
                "com.android.server.power.PowerManagerService", cl,
                "updateUserActivitySummaryLocked", long.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        state.startUpdateButtonLight(param.thisObject);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        state.endUpdateButtonLight(param.thisObject);
                    }
                });

        XposedHelpers.findAndHookMethod(
                "com.android.server.policy.PhoneWindowManager", cl,
                "updateSettings",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        state.setHardwareCallBlocked(true);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        state.setHardwareCallBlocked(false);
                    }
                });

        XposedHelpers.findAndHookMethod(
                "lineageos.hardware.LineageHardwareManager", cl,
                "set", int.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // ignore all invocations from PhoneWindowManager
                        ClassLoader cl = param.thisObject.getClass().getClassLoader();
                        int feature = (int) param.args[0];
                        if (state.isHardwareCallBlocked(cl, feature))
                            param.setResult(false);
                    }
                });
    }
}
