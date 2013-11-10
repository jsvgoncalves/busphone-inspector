package org.fe.up.joao.busphoneinspector.helper;


import java.lang.reflect.Method;

import org.fe.up.joao.busphoneinspector.R;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ComService extends AsyncTask<String, String, String> {
	
	public static String serverURL = "http://busphone-service.herokuapp.com/";
	ProgressDialog dialog;
	String methodName;
	Object object;
	boolean showProgress;
	
	@Override
	protected void onPreExecute(){}
	
	public ComService(String url, Object object, String methodName, boolean showProgress) {
		Log.v("mylog", url );
		String full_url = serverURL + url;
		this.methodName = methodName;
		this.object = object;
		this.execute(full_url);
		this.showProgress = showProgress;
		//set message of the dialog
		if (showProgress) {
			dialog = new ProgressDialog((Context) object);
	        dialog.setMessage(((Context) object).getString(R.string.fetching_data));
	        dialog.setCancelable(false);
	        dialog.show();
		}
        super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
	    return ComHelper.httpGet(params);
	}
	
	@Override
	protected void onPostExecute (String result){
//		System.out.println(result);
		Log.e("mylog", "result " + result);
		JSONObject json = JSONHelper.string2JSON(result);
//		String status = JSONHelper.getValue(json, "status");
		if (showProgress) {
			dialog.dismiss();
		}
		try {
			Method method = object.getClass().getMethod(methodName, String.class);
			method.invoke(object, result);
//			callback.invoke(object, json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}