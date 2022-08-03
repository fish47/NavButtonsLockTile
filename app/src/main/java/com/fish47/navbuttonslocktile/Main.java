package com.fish47.navbuttonslocktile;

import com.fish47.navbuttonslocktile.hook.NavButtonsLockHook;
import com.fish47.navbuttonslocktile.hook.SettingsPermissionHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {

    private static final String PKG_NAME_SYSTEM = "android";
    private static final String PKG_NAME_SETTINGS_PROVIDER = "com.android.providers.settings";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        if (PKG_NAME_SYSTEM.equals(packageName)) {
            NavButtonsLockHook.execute(lpparam.classLoader);
        } else if (PKG_NAME_SETTINGS_PROVIDER.equals(packageName)) {
            SettingsPermissionHook.execute(lpparam.classLoader);
        }
    }
}
