package com.example.pasca.yugiohcart;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {

	private List<Card> myCart;
	private CustomItemClickListener listener;
	private Context context;

	public CartAdapter(List<Card> myCart, CustomItemClickListener listener, Context context) {
		this.myCart = myCart;
		this.listener = listener;
		this.context = context;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.cart_list_layout, parent, false);
		final MyViewHolder viewHolder = new MyViewHolder(itemView);

		itemView.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				listener.onItemClick(v, viewHolder.getAdapterPosition());
			}
		});

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {

		String curKey = context.getString(R.string.pref_currency_key);
		String curValue = context.getString(R.string.pref_currency_def_value);
		String usdeurKey = context.getString(R.string.saved_usdeur);

		SharedPreferences preferences = context.getSharedPreferences("ab", Context.MODE_PRIVATE);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String currSymbol = prefs.getString(curKey, curValue);
		double currency = Double.parseDouble(preferences.getString(usdeurKey, "1"));

		Card card = myCart.get(position);
		int quantity = card.getQuantity();
		double price = card.getPrice();

		if (currSymbol.equals("$")){
			if (card.getCurrency().equals("Euro")){
				price = price / currency;

				price = round(price, 2);
			}
		}else {
			if (card.getCurrency().equals("Dollar")){
				price = price * currency;

				price = round(price, 2);
			}
		}

		double multiplePrice = price * (double) quantity;
		String condition = card.getCondition();

		holder.titleTV.setText(card.getTitle());
		holder.quantityTV.setText(Integer.toString(quantity) + " X");
		holder.conditionTV.setText("Condition: " + condition);
		holder.rarityTV.setText("Rarity: " + card.getRarity());
		if (price > 0.0) {
			holder.singlePriceTV.setText(String.format("%.2f", price) + currSymbol);
			holder.multiplePriceTV.setText(String.format("%.2f", multiplePrice) + currSymbol);
		}else if (price == 0.0){
			holder.singlePriceTV.setText("N/A");
			holder.multiplePriceTV.setText("N/A");
		}

	}

	@Override
	public int getItemCount() {
		return myCart.size();
	}

	public class MyViewHolder extends RecyclerView.ViewHolder{

		public TextView quantityTV, titleTV, rarityTV, conditionTV, singlePriceTV, multiplePriceTV;

		public MyViewHolder(View view){
			super(view);

			quantityTV = (TextView) view.findViewById(R.id.list_quantity);
			titleTV = (TextView) view.findViewById(R.id.list_title);
			rarityTV = (TextView) view.findViewById(R.id.list_rarity);
			conditionTV = (TextView) view.findViewById(R.id.list_condition);
			singlePriceTV = (TextView) view.findViewById(R.id.list_price_single);
			multiplePriceTV = (TextView) view.findViewById(R.id.list_price_multiple);
		}
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
