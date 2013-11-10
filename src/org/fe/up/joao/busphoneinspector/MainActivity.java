package org.fe.up.joao.busphoneinspector;

import java.util.ArrayList;

import org.fe.up.joao.busphoneinspector.helper.JSONHelper;
import org.fe.up.joao.busphoneinspector.helper.ComHelper;
import org.fe.up.joao.busphoneinspector.helper.ComService;
import org.fe.up.joao.busphoneinspector.helper.V;
import org.fe.up.joao.busphoneinspector.InspectorActivity;
import org.json.JSONObject;

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
	
	
	/**
	 * Handler for start inspection button
	 * @param v
	 */
	public void startInspection(View v) {
		String busPlate = ((EditText)findViewById(R.id.bus_plate_field)).getText().toString();
		
		if(!ComHelper.isOnline(getApplicationContext())){
			Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_LONG).show();
		}
		
		if (!busPlate.equals("")) {
			new ComService(
					//bus/b/:bus_id/
					"bus/b/" + busPlate, 
					MainActivity.this, 
					"getTicketsDone", 
					true);
		}
	}
	
	public void getTicketsDone(String result) {
		JSONObject json = JSONHelper.string2JSON(result);
		String status = JSONHelper.getValue(json, "status");
		if (status.equals("0")) {
			ArrayList<String> tickets = JSONHelper.getArray(json, "used_tickets");
			V.parsetickets(tickets);
			Intent intent = new Intent(this, InspectorActivity.class);
			startActivity(intent);
		} else {
			Toast.makeText(getApplicationContext(), "No tickets", Toast.LENGTH_LONG).show();
		}
	}
}
