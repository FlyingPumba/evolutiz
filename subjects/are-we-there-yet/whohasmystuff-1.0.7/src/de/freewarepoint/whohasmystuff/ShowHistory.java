package de.freewarepoint.whohasmystuff;

import android.database.Cursor;

public class ShowHistory extends AbstractListIntent {

	@Override
	protected int getIntentTitle() {
		return R.string.history_title;
	}

	@Override
	protected int getEditAction() {
		return AddObject.ACTION_EDIT_RETURNED;
	}

	@Override
	protected Cursor getDisplayedObjects() {
		return mDbHelper.fetchReturnedObjects();
	}

    @Override
    protected boolean isMarkAsReturnedAvailable() {
        return false;
    }
}
