package org.samcrow.updater;

import java.io.File;

import org.samcrow.colonynavigator3.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * An activity that downloads an application package and then
 * starts another activity to install it
 * @author samcrow
 *
 */
public class UpdateActivity extends Activity {

	/**
	 * An action used to launch this activity to start a download.
	 * 
	 * If this intent is used, the URI of the intent must be the URL of the .apk file to download and install
	 */
	public static final String ACTION_START_DOWNLOAD = "org.samcrow.colonynavigator3.UpdateActivity.ACTION_START_DOWNLOAD";

	/**
	 * The key used in a saved instance bundle to store the ID of the download
	 * in progress
	 */
	private static final String BUNDLE_KEY_DOWNLOAD_ID = "org.samcrow.colonynavigator3.UpdateActivity.BUNDLE_KEY_DOWNLOAD_ID";
	private static final String BUNDLE_KEY_DOWNLOAD_LOCATION = "org.samcrow.colonynavigator3.UpdateActivity.BUNDLE_KEY_DOWNLOAD_LOCATION";
	
	private static final String TAG = "UpdateActivity";

	private Download download;
	
	/**
	 * The file URI describing the location of the file
	 */
	private Uri downloadLocation;
	
	private ProgressBar progressBar;
	
	private TextView statusTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		setTitle("Updating...");
		
		progressBar = (ProgressBar) findViewById(R.id.update_progress_bar);
		statusTextView = (TextView) findViewById(R.id.update_status_view);
		
		// Finish when the cancel button is pressed
		findViewById(R.id.update_cancel_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		download = new Download(this);
		
		download.setStatusListener(new Download.DownloadStatusListener() {

			@Override
			public void onDownloadFailed() {
				statusTextView.setText(stringForReason(download.getFailureReason()));
				showFatalErrorDialog(stringForReason(download.getFailureReason()));
			}

			@Override
			public void onDownloadStarted() {
				progressBar.setIndeterminate(false);
			}

			@Override
			public void onDownloadSucceeded() {
				UpdateActivity.this.onDownloadCompleted();
			}

			@Override
			public void onProgressChanged() {
				progressBar.setMax(download.getTotalBytes());
				progressBar.setProgress(download.getDownloadedBytes());
				statusTextView.setText("Downloading: " + (int)(100 * download.getProgressRatio()) +"%");
			}
			
		});

		// Check for the existence of a download ID from a previous invocation
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(BUNDLE_KEY_DOWNLOAD_ID)) {
			
			recallExistingDownload(savedInstanceState
					.getLong(BUNDLE_KEY_DOWNLOAD_ID), Uri.parse(savedInstanceState.getString(BUNDLE_KEY_DOWNLOAD_LOCATION)));
			
		} else if (getIntent().getAction().equals(ACTION_START_DOWNLOAD)) {
			
			final Uri downloadUri = getIntent().getData();
			setUpNewDownload(downloadUri);
			
		} else {
			throw new IllegalArgumentException(
					"UpdateActivity: Action must either be ACTION_START_DOWNLOAD, or the download ID must be set in the saved instance state.");
		}
	}
	
	/**
	 * Called when the download has completed successfully
	 */
	private void onDownloadCompleted() {
		progressBar.setIndeterminate(true);
		statusTextView.setText("Installing...");
		
		// Check that the file is of the right type
		final String mimeType = download.getMimeType();
		if(!mimeType.equals("application/vnd.android.package-archive")) {
			showFatalErrorDialog("The downloaded file does not have the expected MIME type");
			return;
		}
		
		// Perform the intent to install the application
		Intent promptInstall = new Intent(Intent.ACTION_VIEW)
	    	.setDataAndType(downloadLocation, "application/vnd.android.package-archive");
		startActivity(promptInstall); 
	}


	private void setUpNewDownload(Uri url) {
		
		Log.d(TAG, "Starting download: "+url.toString());
		
		// Put it in the cache dir, so that we have a known file URI corresponding to it
		final File cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		final File downloadLocationFile = new File(cacheDir, "update.apk");
		// Delete that file if it exists
		if(downloadLocationFile.exists()) {
			downloadLocationFile.delete();
		}
		// Set the download location URI
		downloadLocation = Uri.parse(downloadLocationFile.toURI().toString());
		
		download.start(url, downloadLocation);
	}
	

	private void recallExistingDownload(long id, Uri downloadLocation) {
		download.attachToExistingDownload(id);
		this.downloadLocation = downloadLocation;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the download ID
		outState.putLong(BUNDLE_KEY_DOWNLOAD_ID, download.getDownloadId());
		// Save the download destination folder
		outState.putString(BUNDLE_KEY_DOWNLOAD_LOCATION, downloadLocation.toString());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Cancel the download
		download.removeDownloadedFile();
	}

	/**
	 * Shows a dialog, and finishes this activity when the dialog closes
	 * @param text
	 */
	private void showFatalErrorDialog(String text) {
		new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage(text)
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// Exit this activity
					finish();
				}
			})
			.show();
	}
	
	private static String stringForReason(Download.FailureReason reason) {
		switch(reason) {
		case CannotResume:
			return "Could not resume";
		case DeviceNotFound:
			return "External storage device not found";
		case FileAlreadyExists:
			return "File already exists";
		case FileError:
			return "Unknown file error";
		case HttpError:
			return "HTTP error";
		case InsufficientSpace:
			return "Insufficient storage space";
		case TooManyRedirects:
			return "Too many redirects";
		case UnexpectedHttpCode:
			return "Unexpected HTTP code";
		
		default:
		return "Unknown";
		}
	}
}
