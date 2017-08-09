package com.example.pasca.yugiohcart;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pasca on 7/8/2017.
 */

public class ResponseListenerClass implements Response.Listener<String>, Response.ErrorListener {

	private SearchCardActivity parent;
	private String[] cardNames;

	public ResponseListenerClass(SearchCardActivity parent){
		this.parent = parent;
		this.cardNames = new String[9000];
	}

	public void onErrorResponse(VolleyError error) {
			if (error instanceof NetworkError) {
				Log.e("Volley Error", "Network error");
			} else if (error instanceof ServerError) {
				Log.e("Volley Error", "Server Error");
			} else if (error instanceof AuthFailureError) {
				Log.e("Volley Error", "Auth Failure Error");
			} else if (error instanceof ParseError) {
				Log.e("Volley Error", "Parse Error");
			} else if (error instanceof NoConnectionError) {
				Log.e("Volley Error", "No Connection Error");
			} else if (error instanceof TimeoutError) {
				Log.e("Volley Error", "Timeout Error");
			}

	}

	@Override
	public void onResponse(String response) {
		try {
			JSONObject JSONResponse = new JSONObject(response);
			JSONArray cardsJSON = JSONResponse.getJSONArray("items");

			cardNames = new String[cardsJSON.length()];
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cardsJSON.length(); i++) {
				JSONObject cardNameJSON = cardsJSON.getJSONObject(i);
				cardNames[i] = cardNameJSON.getString("title");
				sb.append(cardNames[i]).append(",");
			}

			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(parent.getBaseContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("cards_names", sb.toString());

			editor.apply();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void fetchData(){

		if (isOnline()) {

			CardRequest cardRequest = new CardRequest(this, this);
			RequestQueue queue = Volley.newRequestQueue(parent);

			int socketTimeout = 30000;
			RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			cardRequest.setRetryPolicy(policy);

			queue.add(cardRequest);

			//parent.adjustTheAdapter();
		}else {
			Toast toast = Toast.makeText(parent, "No network connection", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public String[] getCardNames(){
		return this.cardNames;
	}

	public boolean isOnline() {
		ConnectivityManager cm =
			(ConnectivityManager) parent.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
