package org.fe.up.joao.busphoneinspector;

import org.fe.up.joao.busphonevalidation.helper.CameraHelper;
import org.fe.up.joao.busphonevalidation.helper.ComHelper;
import org.fe.up.joao.busphonevalidation.helper.V;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

public class InspectorActivity extends Activity
{
	QRCodeReader qrReader;
	private boolean isPreviewing = true;
	protected final int STATUS_DELAY_MILIS = 3000;

	static {
		System.loadLibrary("iconv");
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String busMessage = getString(R.string.bus) + " " + V.busLineNumber;

		setContentView(R.layout.activity_inspector);

		((TextView) findViewById(R.id.terminal_bus_label)).setText(busMessage);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		qrReader = new QRCodeReader(this);
		FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
		preview.addView(qrReader.mPreview);
	}

	public void onPause() {
		super.onPause();
		qrReader.releaseCamera();
	}

	/**
	 * Updates the image and message on the screen
	 * after the user validates the ticket
	 * @param statusCode
	 */
	protected void showValidationResult(int statusCode) {
		
		int imgID;
		String statusMsg;
		switch (statusCode) {
		case ValidateCode.VALID_CODE:
			imgID = R.drawable.ok;
			statusMsg = getString(R.string.validation_ok);
			break;
		case ValidateCode.READ_ERROR:
			imgID = R.drawable.error;
			statusMsg = getString(R.string.validation_error);
			break;
		case ValidateCode.INVALID_CODE:
			imgID = R.drawable.invalid;
			statusMsg = getString(R.string.validation_invalid);
			break;
		case ValidateCode.SERVER_ERROR:
			imgID = R.drawable.connection;
			statusMsg = getString(R.string.validation_connection);
			break;
		default:
			imgID = R.drawable.connection;
			statusMsg = getString(R.string.validation_connection);
			break;
		}
		
		((ImageView) findViewById(R.id.validationStatus)).setImageResource(imgID);
		((TextView) findViewById(R.id.status_message)).setText(statusMsg);
		
		
		final Handler resetStatus = new Handler();
		resetStatus.postDelayed(new Runnable() {
			public void run() {
				InspectorActivity.this.isPreviewing = true;
				((ImageView) findViewById(R.id.validationStatus)).setImageResource(R.drawable.instructions);;
				((TextView) findViewById(R.id.status_message)).setText(R.string.instructions);
				qrReader.mCamera.startPreview();
				qrReader.mCamera.setPreviewCallback(qrReader.getPreviewCallBack());
			}
		}, STATUS_DELAY_MILIS);
		
	}
	
	public void setPreviewing(boolean previewing){
		if (previewing) {
			isPreviewing = true;
		} else {
			
		}
	}
	
	public boolean isPreviewing(){
		return isPreviewing;
	}


	/**
	 * Implements everything needed
	 * to show the camera preview.
	 *
	 */
	private class QRCodeReader {

		protected Camera mCamera;
		protected CameraHelper mPreview;
//		protected Handler autoFocusHandler;
		protected AutoFocusCallback autoFocusCB;

		ImageScanner scanner;

		/**
		 * Constructor
		 * Instanciates camera stuff
		 */
		public QRCodeReader(Context context){
			PreviewCallback previewCb = getPreviewCallBack();
			
			mCamera = getCameraInstance();

			/* Instance barcode scanner */
			scanner = new ImageScanner();
			scanner.setConfig(0, Config.X_DENSITY, 3);
			scanner.setConfig(0, Config.Y_DENSITY, 3);
			mPreview = new CameraHelper(context, mCamera, previewCb, autoFocusCB);
		}

		public PreviewCallback getPreviewCallBack() {
			return new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					Camera.Parameters parameters = camera.getParameters();
					Size size = parameters.getPreviewSize();

					Image barcode = new Image(size.width, size.height, "Y800");
					barcode.setData(data);

					int result = scanner.scanImage(barcode);

					if (result != 0) {
						InspectorActivity.this.setPreviewing(false);
						mCamera.setPreviewCallback(null);
						mCamera.stopPreview();

						/**
						 * The scanned data is handled here!
						 * syms = list of symbols read.
						 */
						SymbolSet syms = scanner.getResults();
						for (Symbol sym : syms) {
							Log.v("MyLog", "Read symbol:" + sym.getData());
							(new ValidateCode()).validate(sym.getData());							
							
							return;
						}
					}
				}
			};
		}

		/**
		 * Called by activity to realease the camera for other apps
		 */
		private void releaseCamera() {
			if (mCamera != null) {
				InspectorActivity.this.setPreviewing(false);
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}

//		/**
//		 * Does auto focus.
//		 */
//		private Runnable doAutoFocus = new Runnable() {
//			public void run() {
//				if (TerminalActivity.this.isPreviewing())
//					mCamera.autoFocus(autoFocusCB);
//			}
//		};

		/** A safe way to get an instance of the Camera object. */
		public Camera getCameraInstance(){
			int cameraCount = 0;
			Camera cam = null;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();
			for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
				Camera.getCameraInfo( camIdx, cameraInfo );
				if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK  ) {
					try {
						cam = Camera.open( camIdx );
					} catch (RuntimeException e) {
						Log.v("MyLog", "Camera failed to open: " + e.getLocalizedMessage());
					}
				}
			}

			return cam;
		}
	}


	private class ValidateCode extends AsyncTask<String, String, Integer> {

		/**
		 * The code was read and validated on the server.
		 */
		public static final int VALID_CODE = 0;
		/**
		 * The code couldn't be read or the data format is not valid.
		 */
		public static final int READ_ERROR = 1;
		/**
		 * The code was read but was rejected by the server.
		 */
		public static final int INVALID_CODE = 2;
		/**
		 * Unexpected server response or server error.
		 */
		public static final int SERVER_ERROR = 3;

		@Override
		protected Integer doInBackground(String... params) {
			String url = params[0];
			try {
				JSONObject response = new JSONObject(ComHelper.httpGet(url));
				if (response.has("error")) {
					return Integer.valueOf(INVALID_CODE);
				} else {
					return Integer.valueOf(VALID_CODE);
				}
			} catch (JSONException e) {
				Log.v("MyTag", "Failed to Validate or unexpected JSON.");
				return Integer.valueOf(SERVER_ERROR);
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			InspectorActivity.this.showValidationResult(result);
		}

		/**
		 * Receives a String with data from a QRCode that
		 * should contain a valid ticket and sends it
		 * to the server for validation.
		 * The ticket should be accompanied by a
		 * userID.
		 * @param data
		 */
		public void validate(String data) {
			String[] dataArray = data.split(";");
			if (dataArray.length != 2) {
				/**
				 * Malformed QRCode
				 */
				InspectorActivity.this.showValidationResult(READ_ERROR);
				return;
			}
			String userID = dataArray[0];
			String ticketID = dataArray[1];
			// get 'bus/validate/:bus_id/:ticket_id/:user_id'
			String url = String.format(ComHelper.serverURL + "bus/validate/%s/%s/%s", V.busID, ticketID, userID);
			Log.v("MyLog", "Connecting to: " + url);
			this.execute(url);
		}

	}

}
