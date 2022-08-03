package com.fish47.navbuttonslocktile.tile;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.fish47.navbuttonslocktile.base.NavButtonsLock;

public class NavButtonsLockTileService extends TileService {

    private ObserverImpl mObserver = null;

    @Override
    public void onClick() {
        Context ctx = getApplicationContext();
        boolean locked = NavButtonsLock.getValue(ctx);
        NavButtonsLock.setValue(ctx, !locked);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        mObserver = new ObserverImpl();
        mObserver.observe();
        updateTileState();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        getContentResolver().unregisterContentObserver(mObserver);
        mObserver = null;
    }

    private void updateTileState() {
        boolean locked = NavButtonsLock.getValue(getApplicationContext());
        Tile tile = getQsTile();
        tile.setState(locked ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private final class ObserverImpl extends ContentObserver {

        public ObserverImpl() {
            super(null);
        }

        public void observe() {
            ContentResolver resolver = getContentResolver();
            resolver.registerContentObserver(NavButtonsLock.getUri(), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateTileState();
        }
    }
}
