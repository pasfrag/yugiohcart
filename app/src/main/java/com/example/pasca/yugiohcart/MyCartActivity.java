package com.example.pasca.yugiohcart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyCartActivity extends AppCompatActivity {

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
				Card card = myCart.get(position);
				myCart.remove(position);
				cartAdapter.notifyItemRemoved(position);

				handler.deleteCard(card);
				setToolbar();
			}
		};

		new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

		myCart = handler.getAllCards();

		setToolbar();

		cartAdapter = new CartAdapter(myCart, new CustomItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {

				MyCartActivity.this.position = position;
				Card card = myCart.get(position);

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

				popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);

			}
		});
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(cartAdapter);

	}

	public void setToolbar(){

		double totalPrice = 0.00;

		for (Card card : myCart){
			totalPrice += card.getPrice() * card.getQuantity();
		}

		quantityTV.setText("Number of cards: " + handler.getOrderCount());
		priceTV.setText("Total price: " + String.format("%.2f",totalPrice));

	}

	public void addCard(View view){

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

		handler.updateCard(card);

		popupWindow.dismiss();
	}

	public void dismissPopup(View view){
		popupWindow.dismiss();
	}


}
