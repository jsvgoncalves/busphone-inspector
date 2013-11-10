package org.fe.up.joao.busphoneinspector.helper;

import java.util.Locale;

import org.fe.up.joao.busphoneinspector.InspectorActivity;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

public class Ticket {

	public int id = -1;
	public int type = -1;
	public String uuid = "Undefined";
	public String userID;
	public long date_used = -1;
	public String bus_id = "Undefined";
	public boolean hasExpired = false;
	
	public final int expireTimeT1 = 15 * 60 * 1000; // 15 minutes
	public final int expireTimeT2 = 30 * 60 * 1000; // 15 minutes
	public final int expireTimeT3 = 60 * 60 * 1000; // 15 minutes

	public Ticket(String id, String ticket_type, String uuid, String userID,
			String bus_id, long date_used) {
		super();
		this.id = Integer.valueOf(id);
		this.type = Integer.valueOf(ticket_type);
		this.uuid = uuid;
		this.userID = userID;
		this.date_used = date_used;
		this.bus_id = bus_id;
		
		Time now = new Time();
		now.setToNow();
		long millis = now.toMillis(false) - date_used;
		long expireTime = 0;
		switch (this.type) {
		case 1:
			expireTime = expireTimeT1;
			break;
		case 2:
			expireTime = expireTimeT2;
			break;
		case 3:
			expireTime = expireTimeT3;
			break;

		default:
			break;
		} 
		
		if (millis > expireTime) {
			hasExpired = true;
		}
	}

	public String getPrettyDate() {
		String dateInEnglish = DateUtils.getRelativeTimeSpanString(date_used).toString();
		String dateInPortuguese = dateInEnglish.replaceAll("ago", "atrás");
		dateInPortuguese = dateInPortuguese.replaceAll("minutes", "minutos");
		dateInPortuguese = dateInPortuguese.replaceAll("hours", "horas");
		dateInPortuguese = dateInPortuguese.replaceAll("days", "dias");
		return dateInPortuguese;
	}
}
