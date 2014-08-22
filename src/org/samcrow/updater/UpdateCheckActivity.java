package org.samcrow.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.samcrow.colonynavigator3.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

/**
 * Checks for updates.
 * 
 * When this activity is started, its Intent must have a data field that is the
 * URL of a path to a folder on a web server that contains a version.txt file.
 * 
 * @author samcrow
 * 
 */
public class UpdateCheckActivity extends Activity {
	
	private static final String TAG = "UpdateCheckActivity";

	private Download download;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_check);

		setTitle("Checking for updates...");
		
		// Finish the activity when the cancel button is pressed
		findViewById(R.id.update_check_cancel_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		// Verify that a URI has been provided
		final Uri uri = getIntent().getData();
		if (uri == null) {
			throw new IllegalArgumentException(
					"A URI must be provided in the UpdaterCheckActivity's intent data field");
		}

		// Start the check

		try {

			final URL folderUrl = new URL(uri.toString());
			final URL versionFileUrl = new URL(folderUrl.toString() + "/version.txt");
			
			// Find a location for the downloaded file
			final File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			final File downloadLocationFile = new File(downloadsDir, "version.txt");
			
			Log.d(TAG, "Starting version check with information file "+versionFileUrl);

			download = new Download(this);

			download.setStatusListener(new Download.DownloadStatusListener() {
				@Override
				public void onDownloadFailed() {
					showFatalDialog("Download failed", download.getFailureReason().toString());
				}

				@Override
				public void onDownloadStarted() {
				}

				@Override
				public void onDownloadSucceeded() {
					final Uri downloadLocation = download
							.getDownloadedFileUri();

					try {
						File infoFile = new File(new URI(downloadLocation
								.toString()));
						handleVersionFile(new FileInputStream(infoFile), folderUrl);

					} catch (URISyntaxException e) {
						showFatalDialog("URI Syntax Exception", e.getMessage());
					} catch (IOException e) {
						showFatalDialog("IO error", e.getMessage());
					} catch (ClassCastException e) {
						showFatalDialog("Invalid URL",
								"The update URL " + uri.toString()
										+ " is not a valid HTTP URL");
					} catch (Exception e) {
						showFatalDialog(e.getClass().getSimpleName(),
								e.getMessage());
					}

				}

				@Override
				public void onProgressChanged() {
					Log.d(TAG, "Download progress: "+download.getProgressRatio());
				}
			});

			download.start(Uri.parse(versionFileUrl.toString()), Uri.parse(downloadLocationFile.toURI().toString()));

		} catch (MalformedURLException e) {
			showFatalDialog("Invalid URL", "The update URI " + uri.toString()
					+ " is not a valid URL");
		}

	}

	private void showFatalDialog(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setNeutralButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// Close the activity
								finish();
							}
						}).show();
	}

	/**
	 * Handles a version file. Reads the file name and version from the provided
	 * stream, and then closes the stream.
	 * 
	 * @param stream
	 * @param folderUrl
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws NameNotFoundException
	 */
	private void handleVersionFile(InputStream stream, URL folderUrl)
			throws NumberFormatException, IOException, NameNotFoundException {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));

			// Expected format: .apk file name, \n, version code
			final String apkFileName = reader.readLine();
			final int newVersionCode = Integer.valueOf(reader.readLine());

			final int currentVersionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;

			if (newVersionCode > currentVersionCode) {
				// Do an update

				final URL updatePackageUrl = new URL(folderUrl + "/"
						+ apkFileName);

				final Intent intent = new Intent(UpdateActivity.ACTION_START_DOWNLOAD, Uri.parse(updatePackageUrl.toString()), this, UpdateActivity.class);
				intent.setData(Uri.parse(updatePackageUrl.toString()));

				startActivity(intent);

			} else {
				// Don't update
				showFatalDialog("No update available",
						"The latest version is already installed");
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (stream != null) {
					stream.close();
				}
			} catch (IOException ex) {
			}

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		download.cancel();
	}
	
	

}
