package com.example.pasca.yugiohcart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MyCollectionActivity extends AppCompatActivity {

	private MySQLiteHandler handler;
	private List<Card> myCollection, myHelp;
	private RecyclerView recyclerView;
	private CartAdapter cartAdapter;

	private TextView priceTV, quantityTV;
	private PopupWindow popupWindow;
	private int position = 0;

	//PopupViewElements
	private EditText quantityET, priceET;
	private Spinner currencySP, conditionSP, raritySP;

	private static final String url = "http://yugiohprices.com/api/get_card_prices/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_cart);

		priceTV = (TextView) findViewById(R.id.cards_price);
		quantityTV = (TextView) findViewById(R.id.cards_quantity);

		handler = new MySQLiteHandler(getApplicationContext());

		recyclerView = (RecyclerView) findViewById(R.id.cart_RV);

		ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				final int position = viewHolder.getAdapterPosition();
				Card card = myCollection.get(position);
				myCollection.remove(position);
				cartAdapter.notifyItemRemoved(position);

				handler.deleteCard(card, handler.TABLE_COLLECTION);
				setToolbar();
				Toast.makeText(getApplicationContext(), "Card deleted", Toast.LENGTH_LONG).show();
			}
		};

		new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);


		//myCollection = handler.getAllCards(handler.TABLE_COLLECTION);
		//myHelp = new ArrayList<>();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean flag = preferences.getBoolean("show_prices", true);
		/*if (!flag){
			for (Iterator<Card> iterator = myCollection.iterator();iterator.hasNext();){
				Card aCard = iterator.next();
				aCard.setPrice(0.0);
				myHelp.add(aCard);
			}
			myCollection.clear();
			for (Card aCard : myHelp){
				myCollection.add(aCard);
			}
		}*/
		initializingArrays();

		setToolbar();

		cartAdapter = new CartAdapter(myCollection, new CustomItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {

				MyCollectionActivity.this.position = position;
				Card card = myCollection.get(position);

				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);

				View popupView = layoutInflater.inflate(R.layout.add_card_layout, null);

				popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 800);
				popupWindow.setFocusable(true);
				popupWindow.update();

				TextView cardTitleTV = (TextView) popupView.findViewById(R.id.card_title_tv);
				cardTitleTV.setText(card.getTitle());

				quantityET = (EditText) popupView.findViewById(R.id.quantity_tv);
				quantityET.setText(String.valueOf(card.getQuantity()));

				priceET = (EditText) popupView.findViewById(R.id.price_tv);
				priceET.setText(String.format("%.2f",card.getPrice()));
				priceET.setEnabled(false);

				currencySP = (Spinner) popupView.findViewById(R.id.currency_sp);
				List<String> curr = Arrays.asList(getResources().getStringArray(R.array.currency));
				currencySP.setSelection(curr.indexOf(card.getCurrency()));

				conditionSP = (Spinner) popupView.findViewById(R.id.condition_sp);
				List<String> cond = Arrays.asList(getResources().getStringArray(R.array.conditions));
				conditionSP.setSelection(cond.indexOf(card.getCondition()));

				raritySP = (Spinner) popupView.findViewById(R.id.rarity_sp);
				List<String> rar = Arrays.asList(getResources().getStringArray(R.array.rarities));
				raritySP.setSelection(rar.indexOf(card.getRarity()));


				Button updateBtn = (Button) popupView.findViewById(R.id.add_btn);
				updateBtn.setText("Update");

				RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cart_RV);

				popupView.findViewById(R.id.collection_add_btn).setVisibility(View.GONE);

				popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);

			}
		}, getApplicationContext());
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(cartAdapter);

		if(isOnline() && flag){
			int i = 0;
			for (Card card: myCollection){
				if (card.getPrice() == 0.0){
					setCardPrice(card, i);
				}
				i++;
			}
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		initializingArrays();
		cartAdapter.notifyDataSetChanged();
		setToolbar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.collection, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item){
		int id = item.getItemId();

		if (id == R.id.action_settings){

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void setToolbar(){

		double totalPrice = 0.00;
		double totalPriceE = 0.00;
		double totalPriceD = 0.00;

		String curKey = getString(R.string.pref_currency_key);
		String curValue = getString(R.string.pref_currency_def_value);
		String usdeurKey = getString(R.string.saved_usdeur);

		SharedPreferences sharedPreferences = getSharedPreferences("ab", MODE_PRIVATE);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String currSymbol = preferences.getString(curKey, curValue);
		double currency = Double.parseDouble(sharedPreferences.getString(usdeurKey, "1"));

		for (Card card : myCollection){
			if (card.getCurrency().equals("Euro")) {
				totalPriceE += card.getPrice() * card.getQuantity();
			}else totalPriceD += card.getPrice() * card.getQuantity();
		}

		if (totalPriceE > 0 || totalPriceD > 0) {
			if (currSymbol.equals("$")) {
				totalPriceE = totalPriceE / currency;

				totalPriceE = round(totalPriceE, 2);

			} else {
				totalPriceD = totalPriceD * currency;

				totalPriceD = round(totalPriceD, 2);
			}


			totalPrice = totalPriceE + totalPriceD;

			priceTV.setText("Price: " + String.format("%.2f", totalPrice) + currSymbol);
		}else if(totalPriceE == 0 && totalPriceD == 0){
			priceTV.setText("Price: " + "N/A");
		}
		quantityTV.setText("Total cards: " + handler.getOrderCount(handler.TABLE_COLLECTION));

	}

	public void addCard(View view){

		Card card = myCollection.get(position);

		int quantity = Integer.parseInt(quantityET.getText().toString());
		//double price = Double.parseDouble(priceET.getText().toString());
		String rarity = raritySP.getSelectedItem().toString();
		String currency = currencySP.getSelectedItem().toString();
		String condition = conditionSP.getSelectedItem().toString();

		myCollection.remove(card);

		card.setRarity(rarity);
		card.setCurrency(currency);
		//card.setPrice(price);
		card.setCondition(condition);
		card.setQuantity(quantity);

		myCollection.add(position, card);
		cartAdapter.notifyItemChanged(position);

		handler.updateCard(card, handler.TABLE_COLLECTION);

		popupWindow.dismiss();
	}

	public void dismissPopup(View view){
		popupWindow.dismiss();
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public boolean isOnline() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public void setCardPrice(final Card card, final int position){

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {

				try {
					ArrayList<Double> prices = new ArrayList<>();
					JSONObject jsonResponse = new JSONObject(response);
					if (jsonResponse.getString("status").equals("success")){
						JSONArray datas = jsonResponse.getJSONArray("data");
						for (int i = 0; i<datas.length();i++){
							JSONObject object = datas.getJSONObject(i);
							JSONObject priceData = object.getJSONObject("price_data");
							if (priceData.getString("status").equals("success")){
								JSONObject data = priceData.getJSONObject("data");
								JSONObject pricesObject = data.getJSONObject("prices");
								prices.add(pricesObject.getDouble("average"));

							}
						}
					}
					Collections.sort(prices);

					double price = prices.get(0);
					myCollection.remove(card);
					card.setPrice(price);
					myCollection.add(position, card);
					cartAdapter.notifyItemChanged(position);

					handler.updateCard(card, handler.TABLE_COLLECTION);
					setToolbar();
				} catch (JSONException e) {
					e.printStackTrace();
				}

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
				} else if (error instanceof TimeoutError) {
					Log.e("Volley Error", "Timeout Error");
				}
			}
		};

		StringRequest request = new StringRequest(Request.Method.GET, url+card.getTitle(),listener,errorListener);
		RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

		int socketTimeout = 30000;
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		request.setRetryPolicy(policy);

		queue.add(request);

	}

	public void initializingArrays(){
		List<Card> test = handler.getAllCards(handler.TABLE_COLLECTION);
		myHelp = new ArrayList<>();

		if (myCollection != null) {
			myCollection.clear();
		}else {
			myCollection = new ArrayList<>();
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean flag = preferences.getBoolean("show_prices", true);
		if (!flag){
			for (Iterator<Card> iterator = test.iterator();iterator.hasNext();){
				Card aCard = iterator.next();
				aCard.setPrice(0.0);
				myHelp.add(aCard);
			}
			for (Card aCard : myHelp){
				myCollection.add(aCard);
			}
		} else{
			for (Iterator<Card> iterator = test.iterator();iterator.hasNext();){
				Card aCard = iterator.next();
				myCollection.add(aCard);
			}
		}
	}
}
