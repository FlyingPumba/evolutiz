// Copyright (C) 2011 Free Software Foundation FSF
//
// This file is part of Addi.
//
// Addi is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or (at
// your option) any later version.
//
// Addi is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Addi. If not, see <http://www.gnu.org/licenses/>.

package com.addi;

import java.io.BufferedReader;

import android.util.Log;
import android.view.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ThreadGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import com.addi.session.TermSession;
//import java.lang.*;

import com.addi.R;
import com.addi.core.interpreter.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent; 
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Addi extends AddiBase {

	private static final int REQUEST_CODE_ADDI_EDIT = 1;
	private static final int REQUEST_CODE_PICK_FILE_TO_OPEN = 4;
	private static final int REQUEST_CODE_BROWSER_DIRECTORY_TO_CREATE = 5;

	private ArrayAdapter<String> _mOutArrayAdapter;
	private ListView _mOutView;
	private Interpreter _interpreter;
	private String _mResults = "";
	private String _prevCmd = "";	
	private boolean _blockExecute = false;
	private String _command;
	private Activity _act;
	private Vector<String> _oldCommands = new Vector<String>();  
	private int _oldCommandIndex = -1;
	private String _partialCommand;
	protected String _addiEditString;
	private ArrayList<String> _listLabels;
	String _version = new String();
	public int _oldStartSelection;
	public int _oldEndSelection;
	public boolean _oldSelectionSaved = false;
	public int _startSelection;
	public int _endSelection;
	public boolean _selectionSaved = false;
	public boolean _selectionForwarded = false;
	public int _oldVisibility;	
	private TermSession _termSession = null;
	private String partialLine = "";
	private boolean interpreterReady = false;
	private ProgressDialog myPd_ring;
	private boolean copyingFailed = false;

	// Need handler for callbacks to the UI thread
	public final Handler _mHandler = new Handler() {
		public void handleMessage(Message msg) { 
			if (msg.getData().getString("text").startsWith("STARTUPADDIEDITWITH=")) {
				_addiEditString = msg.getData().getString("text").substring(20);
				Intent addiEditIntent = new Intent(Addi.this, AddiEdit.class);
				addiEditIntent.putExtra("fileName", msg.getData().getString("text").substring(20)); // key/value pair, where key needs current package prefix.
				startActivityForResult(addiEditIntent,1); 
			} else if (msg.getData().getString("text").startsWith("STARTUPADDIPLOTWITH=")) {
				Intent addiPlotIntent = new Intent();
				addiPlotIntent.setClassName("com.addiPlot", "com.addiPlot.addiPlot");
				addiPlotIntent.putExtra("plotData", msg.getData().getString("text").substring(20)); // key/value pair, where key needs current package prefix.
				try {
					startActivity(addiPlotIntent);
				} catch (ActivityNotFoundException e) {
					_mOutArrayAdapter.add(getString(R.string.error_no_addiplot));
				}
			} else if (msg.getData().getString("text").startsWith("PROMPTTOINSTALL=")) {
				String packageName = "com." + msg.getData().getString("text").substring(16);
				Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id="+packageName));
				startActivity(goToMarket);
			} else if (msg.getData().getString("text").startsWith("QUITADDI")) {
				finish();
			} else if (msg.getData().getString("text").startsWith("CLEARADDITERMINAL")) {
				_mOutArrayAdapter.clear();
			} else if (msg.getData().getString("text").startsWith("PRINTADDIVERSION")) {
				_mOutArrayAdapter.add(getString(R.string.addi_version) + _version);
			} else {
				_mOutArrayAdapter.add(msg.getData().getString("text"));
			}
		};
	};

	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (requestCode == REQUEST_CODE_ADDI_EDIT) {
			if (resultCode == AddiEdit.RESULT_CODE_ERROR) { 
				_mOutArrayAdapter.add(getString(R.string.error_addi_edit));
			} else if (resultCode == AddiEdit.RESULT_CODE_SAVE) {
				_mOutArrayAdapter.add(getString(R.string.edit_save));
			} else if (resultCode == AddiEdit.RESULT_CODE_SAVE_RUN) {
				_mOutArrayAdapter.add(getString(R.string.edit_save_run));
				int lastIndx = _addiEditString.lastIndexOf("/");
				String dir = _addiEditString.substring(0, lastIndx);
				String script = _addiEditString.substring(lastIndx+1);
				script = script.substring(0, script.length()-2);
				executeCmd("cd(\"" + dir + "\"); " + script,true);
			} else if (resultCode == AddiEdit.RESULT_CODE_QUIT) {
				_mOutArrayAdapter.add(getString(R.string.edit_quit));
			}
		}

		// Results from OI File Manager

		// Opens the selected file
		else if(requestCode == REQUEST_CODE_PICK_FILE_TO_OPEN){
			Uri fileUri = (data!=null?(Uri)data.getData():null);
			if(fileUri != null){ // Everything went well => edit the file
				String filePath = fileUri.getPath();
				Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
				executeCmd("edit " + filePath, true);
			}
			else{ // Error occurred
				Toast.makeText(this, "No file found.", Toast.LENGTH_LONG).show();
			}
		}

		// Ask for the filename, then create it.
		else if(requestCode == REQUEST_CODE_BROWSER_DIRECTORY_TO_CREATE){
			final Uri directoryUri = (data!=null?(Uri)data.getData():null);
			if(directoryUri != null){

				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("M-file name");
				alert.setMessage("Filename must end with \".m\"");
				final EditText input = new EditText(this);
				alert.setView(input);

				alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String fileName = input.getText().toString();
						String filePath = directoryUri.getPath();
						executeCmd("edit " + filePath + "/" + fileName, true);
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// TODO
					}
				});

				alert.show();
			}
			else {
				Toast.makeText(this, "No emplacement found.", Toast.LENGTH_LONG).show();
			}

		}
	}

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateResultsInUi();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.main);

		super.onCreate(savedInstanceState);

		_interpreter = new Interpreter(true);
		Interpreter.setCacheDir(getCacheDir());

		_mOutView = (ListView)findViewById(R.id.out);

		try {
			PackageInfo pi = getPackageManager().getPackageInfo("com.addi", 0);
			_version = pi.versionName;     // this is the line Eclipse complains
		}
		catch (PackageManager.NameNotFoundException e) {
			// eat error, for testing
			_version = "?";
		}

		_listLabels = new ArrayList<String>();
		try {
			String fileName4 = "addiListView";	
			FileInputStream input4 = openFileInput(fileName4);
			InputStreamReader input4reader = new InputStreamReader(input4);
			BufferedReader buffreader4 = new BufferedReader(input4reader);
			String line4;
			_listLabels.clear();
			while (( line4 = buffreader4.readLine()) != null) {
				_listLabels.add(line4);
			}
			input4.close();
			input4 = openFileInput(fileName4);

			String fileName5 = "addiVersion";	
			FileInputStream input5 = openFileInput(fileName5);
			InputStreamReader input5reader = new InputStreamReader(input5);
			BufferedReader buffreader5 = new BufferedReader(input5reader);

			String savedVersion = buffreader5.readLine();
			if (!savedVersion.startsWith(_version)) {
				_listLabels.add("** Welcome to Addi " + _version + " **");
				executeCmd("startup;",false);
			}
			input5.close();

		} catch (IOException e) {
			_listLabels.add("** Welcome to Addi " + _version + " **");
			executeCmd("startup;",false);
		}

		_mOutArrayAdapter = new ArrayAdapter<String>(this, R.layout.message, _listLabels);
		_mOutView.setAdapter(_mOutArrayAdapter);
		_mOutView.setDividerHeight(0);
		_mOutView.setDivider(new ColorDrawable(0x00FFFFFF));
		_mOutView.setFocusable(false);   
		_mOutView.setFocusableInTouchMode(false);
		_mOutView.setClickable(false);
		_mOutView.setDescendantFocusability(393216);
		_mOutView.setFooterDividersEnabled(false);
		_mOutView.setHeaderDividersEnabled(false);
		_mOutView.setChoiceMode(0);

		_mCmdEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						dpadDown();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						dpadUp();
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
						String command = _mCmdEditText.getText().toString();
						executeCmd(command,true);
						return true;
					}
				}
				return false;
			}

		});

		try
		{    	
			String fileName = "addiVariables";

			//create streams
			FileInputStream input = openFileInput(fileName);

			_interpreter.globals.getLocalVariables().loadVariablesOnCreate(input);

			String fileName2 = "addiPaths";	

			FileInputStream input2 = openFileInput(fileName2);

			int length = input2.available();

			byte buffer[] = new byte[(int)length];

			input2.read(buffer);

			String dirStr = new String(buffer);

			File dir = new File(dirStr);

			if (dir.isDirectory()) {
				_interpreter.globals.setWorkingDirectory(dir);
			}

			input2.close();

			String fileName3 = "addiCommands";	

			FileInputStream input3 = openFileInput(fileName3);

			InputStreamReader input3reader = new InputStreamReader(input3);
			BufferedReader buffreader = new BufferedReader(input3reader);

			String line;

			_oldCommands.clear();
			while (( line = buffreader.readLine()) != null) {
				_oldCommands.add(line);
			}

			input3.close();

		}
		catch(java.io.IOException except)
		{
		}

		Calendar c = Calendar.getInstance();  
		int seconds = c.get(Calendar.SECOND);
		int minutes = c.get(Calendar.MINUTE);
		int hours = c.get(Calendar.HOUR_OF_DAY);
		int days = c.get(Calendar.DAY_OF_YEAR);
		int offset = c.get(Calendar.ZONE_OFFSET);
		int year = c.get(Calendar.YEAR);
		
		int absoluteHour = (158*24+20+4) - (days*24+hours-(offset/(1000*60*60)));

		if ((year == 2012) && (absoluteHour > 0)) {
			CharSequence text = "There is an ongoing kickstarter campaign to raise money for Addi and Addiplot development. This will allow Addi and Addiplot to become substantially better in MANY regards.  Click \"Take me there\" to get more details.\nYou have " + Integer.toString(absoluteHour/24) + " days and " + Integer.toString(absoluteHour % 24) + " hours left to make a difference.";
			AlertDialog.Builder builder = new AlertDialog.Builder(this); 
			builder.setTitle("Please Support Addi");
			builder.setMessage(text);
			builder.setNegativeButton("Maybe later", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setPositiveButton("Take me there", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Uri uri = Uri.parse( "http://www.kickstarter.com/projects/6438588/sombreros-for-the-android-world" );
					startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}

	}

	public void processString(String newTermOut) {
		String modifiedTermOut = partialLine + newTermOut;
		int incompleteLine;
		String lines[] = modifiedTermOut.split("\\r?\\n");
		if ((lines.length > 0) && (modifiedTermOut.length() > 0)) {
			if ((modifiedTermOut.charAt(modifiedTermOut.length()-1) == '\n') || (modifiedTermOut.charAt(modifiedTermOut.length()-1) == '\r')) {
				incompleteLine = 0;
				partialLine = "";
			} else {
				incompleteLine = 1;
				partialLine = lines[lines.length-1];
			}
			for (int lineNum = 0; lineNum < lines.length - incompleteLine; lineNum++) {
				if (lines[lineNum].startsWith("STARTUPADDIEDITWITH=")) {
					_addiEditString = lines[lineNum].substring(20);
					Intent addiEditIntent = new Intent(Addi.this, AddiEdit.class);
					addiEditIntent.putExtra("fileName", lines[lineNum].substring(20));
					startActivityForResult(addiEditIntent,1);
				} else {
					_mOutArrayAdapter.add(lines[lineNum]);
				}
			}
		}

	}

	public void dpadDown() {
		if (_oldCommandIndex == 0) {
			_oldCommandIndex=_oldCommandIndex-1;
			_mCmdEditText.setText(_partialCommand);
			_mCmdEditText.setSelection(_partialCommand.length());
		} else if (_oldCommandIndex != -1){
			_oldCommandIndex=_oldCommandIndex-1;
			_mCmdEditText.setText(_oldCommands.get(_oldCommandIndex));
			_mCmdEditText.setSelection(_oldCommands.get(_oldCommandIndex).length());
		}
	}

	public void dpadUp() {
		if ((_oldCommandIndex+1) < _oldCommands.size()) {
			if (_oldCommandIndex == -1) {
				_partialCommand = _mCmdEditText.getText().toString();
				_oldCommandIndex = 0;
				_mCmdEditText.setText(_oldCommands.get(_oldCommandIndex));
				_mCmdEditText.setSelection(_oldCommands.get(_oldCommandIndex).length());
			} else {
				_oldCommandIndex = _oldCommandIndex+1;
				_mCmdEditText.setText(_oldCommands.get(_oldCommandIndex));
				_mCmdEditText.setSelection(_oldCommands.get(_oldCommandIndex).length());
			}
		}		
	}

	@Override
	public void handleBackButton() {
		saveOffEverything();
	}

	private void saveOffEverything() {
		try
		{    
			String fileName4 = "addiListView";	
			OutputStreamWriter out4 = new OutputStreamWriter(openFileOutput(fileName4, MODE_PRIVATE));
			int startIndex = 0;
			if (_listLabels.size() > 100) {
				startIndex = _listLabels.size() - 100;
			}
			for (int lineLoop = startIndex; lineLoop < _listLabels.size(); lineLoop++) {
				out4.write(_listLabels.get(lineLoop));
				out4.write("\n");
			}
			out4.close();

			String fileName5 = "addiVersion";	
			OutputStreamWriter out5 = new OutputStreamWriter(openFileOutput(fileName5, MODE_PRIVATE));
			out5.write(_version);
			out5.close();

			String fileName3 = "addiCommands";	

			OutputStreamWriter out3 = new OutputStreamWriter(openFileOutput(fileName3, MODE_PRIVATE));

			for (int lineLoop = 0; lineLoop < _oldCommands.size(); lineLoop++) {
				out3.write(_oldCommands.get(lineLoop));
				out3.write("\n");
			}

			out3.close();

			String fileName = "addiVariables";

			//create streams
			FileOutputStream output = openFileOutput(fileName, MODE_PRIVATE);

			_interpreter.globals.getLocalVariables().saveVariablesOnPause(output);

			String fileName2 = "addiPaths";	

			FileOutputStream output2 = openFileOutput(fileName2, MODE_PRIVATE);

			output2.write(_interpreter.globals.getWorkingDirectory().getAbsolutePath().getBytes());

			output2.close();

		}
		catch(java.io.IOException except)
		{
		}
	}


	/** Called when the activity is put into background. */
	@Override
	public void onPause() {
		super.onPause();
		saveOffEverything();
	}

	private void updateResultsInUi() {
		// Back in the UI thread -- update our UI elements based on the data in mResults
		if (_mResults.equals("PARSER: CCX: continue") == false) {
			_mOutArrayAdapter.add(_mResults);
			_prevCmd = "";
		} 
		_blockExecute = false;
	}

	public void executeCmd(final String command, boolean displayCommand) {

		if (_sharedPrefs.getBoolean("use_octave_interp", false)) {
			if (command.equals("startup") && (displayCommand == false)) {
				// do nothing
			} else {
				if (interpreterReady == false) {
					copyFileOrDir("tmp");
					String fileName = "mFileUnpacked";	
					FileInputStream input = null;
					try {
						input = openFileInput(fileName);
						InputStreamReader inputReader = new InputStreamReader(input);
						BufferedReader buffReader = new BufferedReader(inputReader);

						String mFileVersion = "";
						try {
							mFileVersion = buffReader.readLine();
							if (mFileVersion.equals("1")) {
								interpreterReady = true;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}	
				}
				if (interpreterReady == false) {
					myPd_ring=ProgressDialog.show(Addi.this, "Preparing Interpreter", "This is the first time you have used this version of the experimental interpreter.\nUnpacking .m files to a usable location.\nThis takes several minutes, feel free to hit the home button and use another app for a while.  Hit enter again when this is complete.\n", true);
					myPd_ring.setCancelable(false);
					new Thread(new Runnable() {  
						@Override
						public void run() {
							try
							{
								copyFileOrDir("share");
								if (copyingFailed == false) {
									String fileName = "mFileUnpacked";	
									OutputStreamWriter out = new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE));
									out.write("1\n");
									out.flush();
									out.close();
								}
							}catch(Exception e){}
							interpreterReady = true;
							myPd_ring.dismiss();
						}
					}).start();
				} else {
					if (_termSession == null) {
						_termSession = new TermSession(this);
						_termSession.updateSize(1024, 1024);
					}
					_oldCommands.add(0, command);
					if (_oldCommands.size() == 100) {
						_oldCommands.remove(99);
					}
					_oldCommandIndex = -1;
					_termSession.write(command + "\n");
					_mCmdEditText.setText("");
				}
			}
		} else {

			if (_blockExecute == false) {
				if (displayCommand) {
					_mOutArrayAdapter.add(">>  " + command);
					_oldCommands.add(0, command);
					if (_oldCommands.size() == 100) {
						_oldCommands.remove(99);
					}
				}
				_oldCommandIndex = -1;

				_blockExecute = true;

				_command = command;
				_act = this;

				// Fire off a thread to do some work that we shouldn't do directly in the UI thread
				ThreadGroup threadGroup = new ThreadGroup("executeCmdGroup");
				Thread t = new Thread(threadGroup, mRunThread, "executeCmd", 16*1024*1024) {};
				t.start();

				_mCmdEditText.setText("");
			}

		}

	}

	// Create runnable for thread run
	final Runnable mRunThread = new Runnable() {
		public void run() {
			_mResults = _interpreter.executeExpression(_prevCmd + _command + "\n",_act,_mHandler);
			_prevCmd = _prevCmd + _command  + "\n";
			_mHandler.post(mUpdateResults);
		}
	};

	@Override
	public void handleEnter() {
		String command = _mCmdEditText.getText().toString();
		executeCmd(command,true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mainMenuOpenMFile: 
			onOIFileManagerOptionsItemSelected(REQUEST_CODE_PICK_FILE_TO_OPEN, "Choose file to open");
			break;
		case R.id.mainMenuCreateMFile:
			onOIFileManagerOptionsItemSelected(REQUEST_CODE_BROWSER_DIRECTORY_TO_CREATE, "Choose directory");
			break;
		case R.id.mainMenuPreferences:
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		}
		return true;
	}

	private void onOIFileManagerOptionsItemSelected(int REQUEST_CODE, String titleString){

		Intent openOIFileManager = new Intent("org.openintents.action.PICK_FILE");
		openOIFileManager.putExtra("org.openintents.extra.TITLE", titleString);

		try{
			startActivityForResult(openOIFileManager, REQUEST_CODE);
		} catch (ActivityNotFoundException e){
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setMessage("You need OI File Manager to continue. Go to market?");
			alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					String packageName = "org.openintents.filemanager";
					Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id="+packageName));
					startActivity(goToMarket);
				}
			});

			alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface arg0, int arg1) {
					Toast.makeText(getApplicationContext(), (CharSequence)"Please use command line : \"edit yourfile.m\"", Toast.LENGTH_LONG).show();
				}
			});

			alertbox.show();
		}
	}

	private void copyFileOrDir(String path) {
		AssetManager assetManager = this.getAssets();
		String assets[] = null;
		try {
			assets = assetManager.list(path);
			if (assets.length == 0) {
				copyFile(path);
			} else {
				String fullPath = "/data/data/" + this.getPackageName() + "/" + path;
				File dir = new File(fullPath);
				if (!dir.exists())
					dir.mkdir();
				for (int i = 0; i < assets.length; ++i) {
					copyFileOrDir(path + "/" + assets[i]);
				}
			}
		} catch (IOException ex) {
			Log.e("tag", "I/O Exception", ex);
		}
	}

	private void copyFile(String filename) {
		AssetManager assetManager = this.getAssets();

		InputStream in = null;
		OutputStream out = null;
		try {
			Log.i("tag", "starting to copy" + filename);
			in = assetManager.open(filename);
			String newFileName = "/data/data/" + this.getPackageName() + "/" + filename;
			newFileName = newFileName.replace("startswithunderscore", "");
			out = new FileOutputStream(newFileName);

			byte[] buffer = new byte[1024];
			int read;

			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			Log.i("tag", "finished copying" + filename);
		} catch (Exception e) {
			Log.e("tag", e.getMessage());
			copyingFailed = true;
		}

	}


}