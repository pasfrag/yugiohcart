package com.example.pasca.yugiohcart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import java.util.Arrays;
import java.util.List;

public class MyCartActivity extends AppCompatActivity {
	/*The activity that is responsible for showing the users cart*/

	private MySQLiteHandler handler;
	private List<Card> myCart;
	private RecyclerView recyclerView;
	private CartAdapter cartAdapter;

	private TextView priceTV, quantityTV;
	private PopupWindow popupWindow;
	private int position = 0;

	//PopupViewElements
	private EditText quantityET, priceET;
	private Spinner currencySP, conditionSP, raritySP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_cart);

		priceTV = findViewById(R.id.cards_price);
		quantityTV = findViewById(R.id.cards_quantity);

		handler = new MySQLiteHandler(getApplicationContext());

		recyclerView = findViewById(R.id.cart_RV);

		//Delete on swipe left or right
		ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				final int position = viewHolder.getAdapterPosition();
				Card card = myCart.get(position);
				myCart.remove(position);
				cartAdapter.notifyItemRemoved(position);

				handler.deleteCard(card, MySQLiteHandler.TABLE_CART);
				setToolbar();
				Toast.makeText(getApplicationContext(), "Card deleted", Toast.LENGTH_LONG).show();
			}
		};

		new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

		myCart = handler.getAllCards(MySQLiteHandler.TABLE_CART);

		setToolbar();

		//Shows each card in a row
		cartAdapter = new CartAdapter(myCart, new CustomItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {

				MyCartActivity.this.position = position;
				Card card = myCart.get(position);

				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);

				View popupView = layoutInflater.inflate(R.layout.add_card_layout, null);

				popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 650);
				popupWindow.setFocusable(true);
				popupWindow.update();

				TextView cardTitleTV = popupView.findViewById(R.id.card_title_tv);
				cardTitleTV.setText(card.getTitle());

				quantityET = popupView.findViewById(R.id.quantity_tv);
				quantityET.setText(String.valueOf(card.getQuantity()));

				priceET = popupView.findViewById(R.id.price_tv);
				priceET.setText(String.format("%.2f",card.getPrice()));

				currencySP = popupView.findViewById(R.id.currency_sp);
				List<String> curr = Arrays.asList(getResources().getStringArray(R.array.currency));
				currencySP.setSelection(curr.indexOf(card.getCurrency()));

				conditionSP = popupView.findViewById(R.id.condition_sp);
				List<String> cond = Arrays.asList(getResources().getStringArray(R.array.conditions));
				conditionSP.setSelection(cond.indexOf(card.getCondition()));

				raritySP = popupView.findViewById(R.id.rarity_sp);
				List<String> rar = Arrays.asList(getResources().getStringArray(R.array.rarities));
				raritySP.setSelection(rar.indexOf(card.getRarity()));


				Button updateBtn = popupView.findViewById(R.id.add_btn);
				updateBtn.setText("Update");

				RecyclerView recyclerView = findViewById(R.id.cart_RV);

				popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);

			}
		}, getApplicationContext());
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(cartAdapter);

	}

	@Override
	protected void onRestart() {
		super.onRestart();
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

	//Sets the toolbar that shows the total amounts of cards and their price
	public void setToolbar(){

		double totalPrice;
		double totalPriceE = 0.00;
		double totalPriceD = 0.00;

		String curKey = getString(R.string.pref_currency_key);
		String curValue = getString(R.string.pref_currency_def_value);
		String usdeurKey = getString(R.string.saved_usdeur);

		SharedPreferences sharedPreferences = getSharedPreferences("ab", MODE_PRIVATE);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		String currSymbol = preferences.getString(curKey, curValue);
		double currency = Double.parseDouble(sharedPreferences.getString(usdeurKey, "1"));

		for (Card card : myCart){
			if (card.getCurrency().equals("Euro")) {
				totalPriceE += card.getPrice() * card.getQuantity();
			}else totalPriceD += card.getPrice() * card.getQuantity();
		}

		if (currSymbol.equals("$")){
			totalPriceE = totalPriceE / currency;

			totalPriceE = round(totalPriceE, 2);

		}else {
			totalPriceD = totalPriceD * currency;

			totalPriceD = round(totalPriceD, 2);
		}

		totalPrice = totalPriceE + totalPriceD;

		quantityTV.setText("Total cards: " + handler.getOrderCount(MySQLiteHandler.TABLE_CART));
		priceTV.setText("Price: " + String.format("%.2f",totalPrice) + currSymbol);

	}

	//Updates the cards data (e.g. quantity) and saves them to the database
	public void add(View view){

		Card card = myCart.get(position);

		int quantity = Integer.parseInt(quantityET.getText().toString());
		double price = Double.parseDouble(priceET.getText().toString());
		String rarity = raritySP.getSelectedItem().toString();
		String currency = currencySP.getSelectedItem().toString();
		String condition = conditionSP.getSelectedItem().toString();

		myCart.remove(card);

		card.setRarity(rarity);
		card.setCurrency(currency);
		card.setPrice(price);
		card.setCondition(condition);
		card.setQuantity(quantity);

		myCart.add(position, card);
		cartAdapter.notifyItemChanged(position);

		handler.updateCard(card, MySQLiteHandler.TABLE_CART);
		setToolbar();

		popupWindow.dismiss();
	}

	public void dismissPopup(View view){
		popupWindow.dismiss();
	}

	//Rounds the price to the second significant
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
