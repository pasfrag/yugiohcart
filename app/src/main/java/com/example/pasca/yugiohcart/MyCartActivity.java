package com.example.pasca.yugiohcart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MyCartActivity extends AppCompatActivity {

	private MySQLiteHandler handler;
	private List<Card> myCart;
	private RecyclerView recyclerView;
	private CartAdapter cartAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_cart);

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
			}
		};

		new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

		myCart = handler.getAllCards();

		cartAdapter = new CartAdapter(myCart);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(cartAdapter);

	}


}
