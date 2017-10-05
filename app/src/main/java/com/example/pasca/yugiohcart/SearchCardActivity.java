package com.example.pasca.yugiohcart;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;

public class SearchCardActivity extends AppCompatActivity {

    private AutoCompleteTextView searchCardET;
    private ArrayAdapter<String> cardAdapter;
	private String[] cardNames;
	private boolean first;
	//private ResponseListenerClass responseListenerClass;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_card);

		//responseListenerClass = new ResponseListenerClass(this);

		cardNames = new String[9000];

		first = true;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SearchCardActivity.this);

        searchCardET = (AutoCompleteTextView) findViewById(R.id.search_card);

		cardAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
		searchCardET.setAdapter(cardAdapter);
		cardAdapter.notifyDataSetChanged();

		String cards = preferences.getString("cards_names", null);

		PopulateCardTask task = new PopulateCardTask();
		task.execute();

		searchCardET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				methodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

				String cardNameSelected = searchCardET.getText().toString();

				FragmentManager fragmentManager = getSupportFragmentManager();
				android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

				CardDetailsFragment fragment = new CardDetailsFragment();

				Bundle args = new Bundle();
				args.putString("cardName", cardNameSelected);
				fragment.setArguments(args);

				if (first){
					first = false;
					transaction
							.add(R.id.fragment_container, fragment)
							.commit();

				}else{
					transaction
							.replace(R.id.fragment_container, fragment)
							.addToBackStack(null)
							.commit();
				}

			}
		});

    }

	//The method that fills the adapters
	public void adjustTheAdapter(){
		if(cardAdapter != null) {
			cardAdapter.clear();
		}

		for (String cardName : cardNames) {
			cardAdapter.add(cardName);
			cardAdapter.notifyDataSetChanged();
		}

	}


	//A method to check network connection
	public boolean isOnline() {
		ConnectivityManager cm =
			(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public void fetchData(){

		if (isOnline()) {

			Response.Listener responseListener = new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					try {
						JSONObject JSONResponse = new JSONObject(response);
						JSONArray cardsJSON = JSONResponse.getJSONArray("items");


						Toast toasta = Toast.makeText(SearchCardActivity.this, cardsJSON.length() + "", Toast.LENGTH_SHORT);
						toasta.show();

						cardNames = new String[cardsJSON.length()];
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < cardsJSON.length(); i++) {
							JSONObject cardNameJSON = cardsJSON.getJSONObject(i);
							cardNames[i] = cardNameJSON.getString("title");
							sb.append(cardNames[i]).append(",");
						}

						SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SearchCardActivity.this);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("cards_names", sb.toString());

						editor.apply();

					} catch (JSONException e) {
						e.printStackTrace();
					}

					adjustTheAdapter();
				}
			};

			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
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
			};

			CardRequest cardRequest = new CardRequest(responseListener, errorListener);
			RequestQueue queue = Volley.newRequestQueue(SearchCardActivity.this);

			int socketTimeout = 30000;
			RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			cardRequest.setRetryPolicy(policy);

			queue.add(cardRequest);
		}else {
			Toast toast = Toast.makeText(SearchCardActivity.this, "No network connection", Toast.LENGTH_SHORT);
			toast.show();
		}
	}


	private class PopulateCardTask extends AsyncTask<String[], Void, Void>{

		public PopulateCardTask(){

		}

		@Override
		protected Void doInBackground(String[]... params) {

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SearchCardActivity.this);
			String cards = preferences.getString("cards_names", null);


			if(cards == null){
				fetchData();
				adjustTheAdapter();
			}
			else {
				cardNames = cards.split(",");
				adjustTheAdapter();
			}

//			if(cards == null){
//
//				responseListenerClass.fetchData();
//				cardNames = responseListenerClass.getCardNames();
//				adjustTheAdapter();
//			}
//			else {
//				cardNames = cards.split(",");
//				adjustTheAdapter();
//			}

			return null;
		}
	}

	private void makeAToast(String string){
		Toast toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
		toast.show();
	}

}
