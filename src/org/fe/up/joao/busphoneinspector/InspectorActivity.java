package org.fe.up.joao.busphoneinspector;

import org.fe.up.joao.busphoneinspector.helper.CameraHelper;
import org.fe.up.joao.busphoneinspector.helper.Ticket;
import org.fe.up.joao.busphoneinspector.helper.V;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
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

	static {
		System.loadLibrary("iconv");
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String busMessage = getString(R.string.bus) + V.busID;

		setContentView(R.layout.activity_inspector);

		((TextView) findViewById(R.id.terminal_bus_label)).setText(busMessage);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		qrReader = new QRCodeReader(this);
		FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
		preview.addView(qrReader.mPreview);
	}

	public void onPause() {
		super.onPause();
		qrReader.releaseCamera();
	}
	
	/**
	 * Parses the data read from the QRCode and
	 * verifies is the ticket is in the list
	 * and if it is valid.
	 * @param data
	 */
	public void validate(String data) {
		Log.v("mylog", "Read symbol:" + data);
		String[] dataArray = data.split(";");
		String status = "";
		int color = 0;
		int imgID;
		
		if (dataArray.length != 2) {
			// Bad QRCode
			Log.v("mylog", "Read error");
			imgID = R.drawable.error;
			color = 0xFFFF8800;
			status = "Utilizador não coincide";
		} else {
			//Good QRCOde
			String userID = dataArray[0];
			String ticketID = dataArray[1];

			if (V.tickets.containsKey(ticketID)){
				// Ticket found
				Ticket ticket = V.tickets.get(ticketID);
				
				if (userID.equals(ticket.userID)) {
					// User is valid
					if (ticket.hasExpired) {
						// Ticket has expired!
						color = 0xFFCC0000;
						imgID = R.drawable.invalid;
					} else {
						// Ticket is valid!
						color = 0xFF669900;
						imgID = R.drawable.ok;
					}
					status = String.format(getString(R.string.ticket_status_ok), ticket.getPrettyDate());
				} else {
					// User doesn't match!
					imgID = R.drawable.invalid;
					color = 0xFF669900;
					status = "Utilizador não coincide";
				}
				
			} else {
				// Ticket not found
				imgID = R.drawable.invalid;
				color = 0xFF669900;
				status = "Bilhete não encontrado";
			}
		}
		
		Log.v("mylog", status);
		TextView statusView = ((TextView) findViewById(R.id.status_message));
		statusView.setText(status);
		statusView.setTextColor(color);
		((ImageView) findViewById(R.id.validationStatus)).setImageResource(imgID);
		((TextView) findViewById(R.id.status_message)).setText(status);

		
		// Set a delayed reset to the status message and image
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
		isPreviewing = previewing;
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
							InspectorActivity.this.validate(sym.getData());
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


}
