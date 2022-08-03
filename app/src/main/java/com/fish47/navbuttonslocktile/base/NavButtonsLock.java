package com.fish47.navbuttonslocktile.base;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;

public final class NavButtonsLock {

    private static final int VAL_LOCK = 1;
    private static final int VAL_UNLOCK = 0;
    private static final int VAL_DEFAULT = VAL_UNLOCK;
    public static final String KEY_NAME = "nav_buttons_lock";

    public static Uri getUri() {
        return Settings.Global.getUriFor(KEY_NAME);
    }

    public static void setValue(Context ctx, boolean lock) {
        int val = lock ? VAL_LOCK : VAL_UNLOCK;
        Settings.Global.putInt(ctx.getContentResolver(), KEY_NAME, val);
    }

    public static boolean getValue(Context ctx) {
        return (Settings.Global.getInt(ctx.getContentResolver(), KEY_NAME, VAL_DEFAULT) == VAL_LOCK);
    }
}
