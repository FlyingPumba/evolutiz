package edu.killerud.fileexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExplorerActivity extends ListActivity
{

	/*
	 * Holds the current directory File object and the file list for that
	 * directory
	 */
	private File mCurrentDirectory;
	private File[] mCurrentDirectoryList;

	/* The path TextView at the top of our screen */
	private TextView mPathOut;

	private static final String PATH_TO_ROOT = "/";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Set up our fields and load root */
		mPathOut = (TextView) findViewById(R.id.current_path);

		try
		{
			/*
			 * Load up our explorer
			 */
			navigateExplorer(PATH_TO_ROOT);
		} catch (IOException e)
		{
			Toast.makeText(getApplicationContext(), R.string.error,
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView parentList, View clickedView,
			int viewPosition, long viewId)
	{
		/*
		 * Here you can for instance get the position of the view and use it to
		 * fetch the file details of the file at that position in the directory
		 * File array. If the position is a directory, why don't navigate to it?
		 * 
		 * Go nuts!
		 */
	}

	/*
	 * Navigates the application to the given path, updating the UI in the
	 * process.
	 */
	private void navigateExplorer(String pathToNavigateTo) throws IOException
	{
		mCurrentDirectory = new File(pathToNavigateTo);
		mCurrentDirectoryList = mCurrentDirectory.listFiles();
		mPathOut.setText(mCurrentDirectory.getAbsolutePath());
		listDirectoryContent(getFileNames(mCurrentDirectoryList));
	}

	/*
	 * Populates the ListView with the file names of the File array for the
	 * current directory.
	 */
	private void listDirectoryContent(ArrayList<String> fileNames)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				android.R.id.text1, fileNames);
		setListAdapter(adapter);
	}

	/*
	 * Takes a File array and builds an arraylist consisting of strings with the
	 * file name of all files in the File array.
	 */
	private ArrayList<String> getFileNames(File[] fileList)
	{
		ArrayList<String> fileNames = new ArrayList<String>();
		if (fileList.length < 1)
		{
			fileNames.add("No files in directory");
			return fileNames;
		}

		for (int i = 0; i < fileList.length; i++)
		{
			fileNames.add(fileList[i].getName());
		}
		return fileNames;
	}

	/*
	 * Takes a File array and builds an arraylist consisting of strings with the
	 * abolute path to all files in the File array.
	 */
	private ArrayList<String> getFilePaths(File[] fileList)
	{
		ArrayList<String> filePaths = new ArrayList<String>();
		if (fileList.length < 1)
		{
			filePaths.add("/");
			return filePaths;
		}

		for (int i = 0; i < fileList.length; i++)
		{
			filePaths.add(fileList[i].getAbsolutePath());
		}
		return filePaths;
	}
}