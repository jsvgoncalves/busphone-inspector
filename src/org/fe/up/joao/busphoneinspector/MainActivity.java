package org.fe.up.joao.busphoneinspector;

import org.fe.up.joao.busphoneinspector.InspectorActivity;
import org.fe.up.joao.busphonevalidation.helper.ComHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void startInspection(View v) {
		String busPlate = ((EditText)findViewById(R.id.bus_plate_field)).getText().toString();
		if(!ComHelper.isOnline(getApplicationContext())){
			Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_LONG);
			toast.show();
		}
		if (busPlate != "") {
			(new GetTickets()).getBusTickets(busPlate);
			Intent intent = new Intent(this, InspectorActivity.class);
			intent.putExtra("bus_plate", busPlate);
			startActivity(intent);
		}
	}
	
	/**
	 * Gets all tickets from a bus
	 * that have been validated in the last hour.
	 *
	 */
	private class GetTickets extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			
			String url = params[0];
			String JSONstr = ComHelper.httpGet(url);
			
			
			
			return null;
		}
		
		public void getBusTickets(String busPlate){
			String url = ComHelper.serverURL + "bus/" + busPlate;
			this.execute(url);
		}
	}

	
}
