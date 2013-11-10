package org.fe.up.joao.busphoneinspector.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class V {

	public static String busLineNumber;
	public static String busID;
	public static HashMap<String, Ticket> tickets = new HashMap<String, Ticket>();
	
	public static void parsetickets(ArrayList<String> tickets_str) {
		JSONObject json;
		tickets.clear();
		for (String ticketStr : tickets_str) {
			json = JSONHelper.string2JSON(ticketStr);
			try {
				// Convert date from ISO format to milliseconds
				// Then convert milliseconds to "X minutes/hours/days ago"
				long dateUsedMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
						Locale.getDefault()).parse(json.getString("date_used")).getTime();
				String dateUsedTimeSince = DateUtils.getRelativeTimeSpanString(dateUsedMillis).toString();
				Ticket t = new Ticket(json.getString("id"),
					json.getString("ticket_type"),
					json.getString("uuid"),
					json.getString("bus_id"),
					dateUsedTimeSince);
				tickets.put(json.getString("id"), t);
				Log.v("mylog","Ticket OK. Used " + dateUsedTimeSince);
			} catch (JSONException e) {
				Log.v("mylog","Invalid JSON while retrieving tickets!");
			} catch (ParseException e) {
				Log.v("mylog","Invalid DATE_USED while retrieving tickets!");
			}
		}
	}
	
}
