package com.fish47.navbuttonslocktile.hook;

import com.fish47.navbuttonslocktile.BuildConfig;
import com.fish47.navbuttonslocktile.base.NavButtonsLock;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SettingsPermissionHook {

    private static void findAndHookMethodBestMatch(ClassLoader cl, String clz, String method, XC_MethodHook hook) {
        Class<?> c = XposedHelpers.findClass(clz, cl);
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                XposedBridge.hookMethod(m, hook);
                break;
            }
        }
    }

    public static void execute(ClassLoader cl) {
        ThreadLocal<Boolean> skipPermissionCheck = new ThreadLocal<>();

        findAndHookMethodBestMatch(cl,
                "com.android.providers.settings.SettingsProvider",
                "mutateGlobalSetting",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        skipPermissionCheck.set(isButtonLockSetting(param));
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        skipPermissionCheck.remove();
                    }
                });

        findAndHookMethodBestMatch(cl,
                "com.android.providers.settings.SettingsProvider",
                "enforceWritePermission",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (Boolean.TRUE.equals(skipPermissionCheck.get()))
                            param.setResult(null);
                    }
                });
    }

    private static boolean isButtonLockSetting(XC_MethodHook.MethodHookParam param) {
        return NavButtonsLock.KEY_NAME.equals(param.args[0])
                && BuildConfig.APPLICATION_ID.equals(XposedHelpers.callMethod(param.thisObject, "getCallingPackage"));
    }
}
