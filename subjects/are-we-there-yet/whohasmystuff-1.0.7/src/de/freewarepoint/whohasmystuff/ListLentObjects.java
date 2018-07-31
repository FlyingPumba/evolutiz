package de.freewarepoint.whohasmystuff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import de.freewarepoint.whohasmystuff.database.DatabaseHelper;

public class ListLentObjects extends AbstractListIntent {

	@Override
	protected int getIntentTitle() {
		return R.string.app_name;
	}

	@Override
	protected int getEditAction() {
		return AddObject.ACTION_EDIT_LENT;
	}

	@Override
	protected Cursor getDisplayedObjects() {
		return mDbHelper.fetchLentObjects();
	}

    @Override
    protected boolean isMarkAsReturnedAvailable() {
        return true;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            // We have only one menu option
            case R.id.addButton:
                i = new Intent(this, AddObject.class);
                i.putExtra(AddObject.ACTION_TYPE, AddObject.ACTION_ADD);
                startActivityForResult(i, ACTION_ADD);
                break;
            case R.id.historyButton:
                i = new Intent(this, ShowHistory.class);
                startActivity(i);
                break;
            case R.id.exportButton:
                if (isExternalStorageWritable()) {
                    if (DatabaseHelper.existsBackupFile()) {
                        askForExportConfirmation();
                    }
                    else {
                        exportData();
                    }
                }
                break;
            case R.id.importButton:
                if (isExternalStorageReadable()) {
                    askForImportConfirmation();
                }
                break;
        }
        return true;
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.sd_card_error_title));
            alertDialog.setMessage(getString(R.string.sd_card_error_not_readable));
            alertDialog.show();
            return false;
        }
        else {
            return true;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.sd_card_error_title));
            alertDialog.setMessage(getString(R.string.sd_card_error_not_writeable));
            alertDialog.show();
            return false;
        }
        else {
            return true;
        }
    }

    private void askForExportConfirmation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(getString(R.string.database_export_title));
        dialog.setMessage(getString(R.string.database_export_message));
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                exportData();
            }
        }
        );
        dialog.setNegativeButton(android.R.string.no, null);
        dialog.show();
    }

    private void exportData() {
        if (DatabaseHelper.exportDatabaseToXML(mDbHelper)) {
            Toast.makeText(this, R.string.database_export_success, Toast.LENGTH_LONG).show();
        }
        else {
            showExportErrorDialog();
        }
    }

    private void askForImportConfirmation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(getString(R.string.database_import_title));
        dialog.setMessage(getString(R.string.database_import_message));
        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (DatabaseHelper.importDatabaseFromXML(mDbHelper)) {
                    fillData();
                }
                else {
                    showImportErrorDialog();
                }
            }
        }
        );
        dialog.setNegativeButton(android.R.string.no, null);
        dialog.show();
    }

    private void showImportErrorDialog() {
        showErrorDialog(getString(R.string.database_import_error));
    }

    private void showExportErrorDialog() {
        showErrorDialog(getString(R.string.database_export_error));
    }

    private void showErrorDialog(String message) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(getString(R.string.database_import_title));
        dialog.setMessage(message);
        dialog.setPositiveButton(android.R.string.yes, null);
        dialog.show();
    }


}
