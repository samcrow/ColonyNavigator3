package org.samcrow.updater;

import java.io.FileNotFoundException;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

/**
 * Provides an interface for asynchronously managing a download
 * 
 * @author samcrow
 * 
 */
public class Download {

	/**
	 * Possibilities for the status of the download
	 */
	public static enum Status {
		Pending,
		Running,
		Successful,
		Failed,
	}
	/**
	 * Possible reasons for failure
	 */
	public static enum FailureReason {
		CannotResume,
		DeviceNotFound,
		FileAlreadyExists,
		FileError,
		HttpError,
		InsufficientSpace,
		TooManyRedirects,
		UnexpectedHttpCode,
		Unknown,
	}
	
	/**
	 * An interface for something that can be notified when the status of a download changed
	 */
	public static interface DownloadStatusListener {
		/**
		 * Called after the state changes to Status.Failed
		 */
		public void onDownloadFailed();
		/**
		 * Called after the state changes to Status.Running
		 */
		public void onDownloadStarted();
		/**
		 * Called after the state changes to State.Successful
		 */
		public void onDownloadSucceeded();
		/**
		 * Called when the number of bytes downloaded has changed
		 */
		public void onProgressChanged();
	}
	
	/**
	 * The time in milliseconds to wait between status checks
	 */
	private static final int CHECK_DELAY_MS = 100;
	
	private DownloadManager downloadManager;

	private static class InternalState {
		public long downloadId;

		public int bytesDownloaded;
		public int totalBytes;

		public int statusCode = DownloadManager.STATUS_PENDING;
		public int reasonCode;
	}

	/**
	 * The state of the download
	 */
	private final InternalState state = new InternalState();
	
	private DownloadStatusListener listener;
	
	/**
	 * The Handler used to schedule status checks
	 */
	private Handler handler;

	public Download(Context context) {
		downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		
		handler = new Handler();
	}

	/**
	 * Starts downloading a file from the given remote address to be saved in
	 * the default location.
	 * 
	 * @param remoteAddress
	 *            The address to download the file from
	 */
	public void start(Uri remoteAddress) {
		start(remoteAddress, null);
	}

	/**
	 * Starts downloading a file from the given remote address to be saved at
	 * the specified local address.
	 * 
	 * @param remoteAddress
	 *            The address to download the file from
	 * @param downloadLocation
	 *            The local location to save the file to
	 */
	public void start(Uri remoteAddress, Uri downloadLocation) {
		
		DownloadManager.Request request = new DownloadManager.Request(remoteAddress);
		
		if(downloadLocation != null) {
			request.setDestinationUri(downloadLocation);
		}
		
		state.downloadId = downloadManager.enqueue(request);
		
		startStatusChecks();
	}
	
	/**
	 * Returns the download ID that can be used to identify this system
	 * @return
	 * 
	 * @see #attachToExistingDownload()
	 */
	public long getDownloadId() {
		return state.downloadId;
	}
	
	/**
	 * Attaches this instance to an existing download
	 * that is in progress
	 * @param id The ID of the download, as returned by {@link #getDownloadId()}
	 */
	public void attachToExistingDownload(long id) {
		state.downloadId = id;
		
		startStatusChecks();
	}

	public void setStatusListener(DownloadStatusListener newListener) {
		listener = newListener;
	}
	
	/**
	 * Checks if the download has finished
	 * 
	 * @return true if the download has finished, otherwise false
	 */
	public boolean isFinished() {
		return state.statusCode == DownloadManager.STATUS_SUCCESSFUL;
	}
	
	/**
	 * Returns the total number of bytes that will be read from the server
	 * to download this file
	 * @see #getDownloadedBytes()
	 * @see #getProgressRatio();
	 * @return
	 */
	public int getTotalBytes() {
		return state.totalBytes;
	}
	/**
	 * Returns the number of bytes that have already been read.
	 * This is never greater than the value returned by {@link #getTotalBytes()}.
	 * @see #getTotalBytes()
	 * @see #getProgressRatio();
	 * @return
	 */
	public int getDownloadedBytes() {
		return state.bytesDownloaded;
	}
	/**
	 * Returns a ratio from 0 to 1 representing the progress
	 * of the download.
	 * @see #getTotalBytes()
	 * @see #getDownloadedBytes();
	 * @return
	 */
	public double getProgressRatio() {
		return state.bytesDownloaded / (double) state.totalBytes;
	}

	/**
	 * Returns the URI of the local downloaded file, if the download has
	 * completed
	 * 
	 * @return A URI that can be used to access the downloaded file
	 * @throws IllegalStateException
	 *             If the download is not finished
	 */
	public Uri getDownloadedFileUri() {
		checkDownloadFinished();
		return downloadManager.getUriForDownloadedFile(state.downloadId);
	}

	/**
	 * Returns the MIME type of the downloaded file, if the download has
	 * competed.
	 * 
	 * @return The MIME type of the file
	 * @throws IllegalStateException
	 *             If the download is not finished
	 * 
	 */
	public String getMimeType() {
		checkDownloadFinished();
		return downloadManager.getMimeTypeForDownloadedFile(state.downloadId);
	}
	
	/**
	 * Opens the downloaded file
	 * @return Something that can be used to access the downloaded file
	 */
	public ParcelFileDescriptor open() {
		checkDownloadFinished();
		try {
			return downloadManager.openDownloadedFile(state.downloadId);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("The download file does not exist", e);
		}
	}
	
	/**
	 * Cancels the download and removes the downloaded file
	 */
	public void cancel() {
		downloadManager.remove(state.downloadId);
	}
	/**
	 * Removes a previously downloaded file
	 */
	public void removeDownloadedFile() {
		downloadManager.remove(state.downloadId);
	}
	
	/**
	 * Returns the current status of the download
	 * @see #getFailureReason() can be used to get more information on a Failed status
	 * @return
	 */
	public Status getStatus() {
		
		switch(state.statusCode) {
		case DownloadManager.STATUS_PENDING:
			return Status.Pending;
		case DownloadManager.STATUS_RUNNING:
			return Status.Running;
		case DownloadManager.STATUS_SUCCESSFUL:
			return Status.Successful;
		case DownloadManager.STATUS_FAILED:
			return Status.Failed;
		default:
			throw new IllegalStateException("DownloadManager provided an invalid status code value: " + state.statusCode);
		}
	}
	
	/**
	 * Returns the reason for a failure.
	 * If the current status is not Status.Failed,
	 * the return value is undefined.
	 * @see #getStatus();
	 * @return
	 */
	public FailureReason getFailureReason() {
		
		switch(state.reasonCode) {
		case DownloadManager.ERROR_CANNOT_RESUME:
			return FailureReason.CannotResume;
		case DownloadManager.ERROR_DEVICE_NOT_FOUND:
			return FailureReason.DeviceNotFound;
		case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
			return FailureReason.FileAlreadyExists;
		case DownloadManager.ERROR_FILE_ERROR:
			return FailureReason.FileError;
		case DownloadManager.ERROR_HTTP_DATA_ERROR:
			return FailureReason.HttpError;
		case DownloadManager.ERROR_INSUFFICIENT_SPACE:
			return FailureReason.InsufficientSpace;
		case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
			return FailureReason.TooManyRedirects;
		case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
			return FailureReason.UnexpectedHttpCode;
		
		default:
			return FailureReason.Unknown;
		}
	}
	
	private final Runnable stateRefresher = new Runnable() {
		@Override
		public void run() {
			refreshState();
			handler.postDelayed(stateRefresher, CHECK_DELAY_MS);
		}
	};
	
	private void startStatusChecks() {
		stateRefresher.run();
	}
	
	private void pauseStatusChecks() {
		handler.removeCallbacks(stateRefresher);
	}

	/**
	 * Gets the state of the download from the download manager and saves it in
	 * State
	 */
	private void refreshState() {
		Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(state.downloadId));
		if(cursor.getCount() > 0) {
			// Move to row 0
			cursor.moveToNext();
			
			// Get progress
			final int newBytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
			final int newTotalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
			// Check for progress change
			final boolean progressChanged = !( newBytesDownloaded == state.bytesDownloaded && newTotalBytes == state.totalBytes );
			// Set state
			state.bytesDownloaded = newBytesDownloaded;
			state.totalBytes = newTotalBytes;
			// Notify listener
			if(progressChanged && listener != null) {
				listener.onProgressChanged();
			}
			
			final int newStatusCode = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			final int oldStatusCode = state.statusCode;
			state.statusCode = newStatusCode;
			if(newStatusCode != oldStatusCode && listener != null) {
				switch(newStatusCode) {
				case DownloadManager.STATUS_RUNNING:
					listener.onDownloadStarted();
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					// Stop status updates, because the status has changed
					pauseStatusChecks();
					listener.onDownloadSucceeded();
					break;
				case DownloadManager.STATUS_FAILED:
					listener.onDownloadFailed();
					break;
				}
			}
			state.reasonCode = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
		}
		
		cursor.close();
	}

	/**
	 * Checks that the download state is DownloadManager.STATUS_SUCCESSFUL, and
	 * throws an IllegalStateException otherwise.
	 */
	private void checkDownloadFinished() {
		if (state.statusCode != DownloadManager.STATUS_SUCCESSFUL) {
			throw new IllegalStateException(
					"The local URI of a downloaded file cannot be accessed until the download is finished");
		}
	}
}
