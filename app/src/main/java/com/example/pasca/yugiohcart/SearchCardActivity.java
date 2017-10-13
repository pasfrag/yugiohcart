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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
	private PopupWindow popupWindow;
	private RelativeLayout relativeLayout;
	private MySQLiteHandler handler;
	//private ResponseListenerClass responseListenerClass;

	//PopupViewElements
	private EditText quantityET, priceET;
	private Spinner currencySP, conditionSP, raritySP;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_card);

		//responseListenerClass = new ResponseListenerClass(this);

		relativeLayout = (RelativeLayout) findViewById(R.id.activity_search_card);

		cardNames = new String[9000];

		first = true;

		handler = new MySQLiteHandler(getApplicationContext());

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

	public void addToCart(View view){
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View popupView = layoutInflater.inflate(R.layout.add_card_layout, null);

		popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 800);
		popupWindow.setFocusable(true);
		popupWindow.update();

		TextView cardTitleTV = (TextView) popupView.findViewById(R.id.card_title_tv);
		cardTitleTV.setText(searchCardET.getText().toString());
		quantityET = (EditText) popupView.findViewById(R.id.quantity_tv);
		priceET = (EditText) popupView.findViewById(R.id.price_tv);
		currencySP = (Spinner) popupView.findViewById(R.id.currency_sp);
		conditionSP = (Spinner) popupView.findViewById(R.id.condition_sp);
		raritySP = (Spinner) popupView.findViewById(R.id.rarity_sp);

		//popupWindow.setAnimationStyle();

		popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);

	}

	public void dismissPopup(View view){

		popupWindow.dismiss();

	}

	public void addCard(View view){

		String title = searchCardET.getText().toString();
		int quantity = Integer.parseInt(quantityET.getText().toString());
		Double price = Double.parseDouble(priceET.getText().toString());
		String currency = currencySP.getSelectedItem().toString();
		String condition = conditionSP.getSelectedItem().toString();
		String rarity = raritySP.getSelectedItem().toString();

		TextView typeTV = (TextView) findViewById(R.id.card_type_TV);
		String type = typeTV.getText().toString();

		if(type.equals("monster")) {

			TextView monsterTypeTV = (TextView) findViewById(R.id.type_TV);
			type = monsterTypeTV.getText().toString();

		}

		Card card = new Card(quantity, title, rarity, type, condition, currency, price);

		if (!(rarity.equals("C") || rarity.equals("SR"))){
			makeAToast("RARITY WHORE!!!");
		}

		handler.addACard(card);
		popupWindow.dismiss();
	}

}
