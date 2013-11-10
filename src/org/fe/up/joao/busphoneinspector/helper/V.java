package org.fe.up.joao.busphoneinspector.helper;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;


public class V {

	public static String busLineNumber;
	public static String busID;
	public static ArrayList<Ticket> tickets = new ArrayList<Ticket>();
	
	public static void parsetickets(ArrayList<String> tickets_str) {
		JSONObject json;
		tickets.clear();
		for (String ticketStr : tickets_str) {
			json = JSONHelper.string2JSON(ticketStr);
			try {
				Ticket t = new Ticket(json.getString("id"),
						json.getString("ticket_type"),
						json.getString("uuid"),
						json.getString("created_at"),
						json.getString("updated_at"));
				tickets.add(t);
			} catch (JSONException e) {
				System.err.println(e.toString());
				System.err.println("Invalid JSON while retrieving tickets!");
			}
		}
	}
}
