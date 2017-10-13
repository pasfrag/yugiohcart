package com.example.pasca.yugiohcart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {

	private List<Card> myCart;

	public CartAdapter(List<Card> myCart) {
		this.myCart = myCart;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.cart_list_layout, parent, false);
		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {

		Card card = myCart.get(position);
		int quantity = card.getQuantity();
		double price = card.getPrice();
		double multiplePrice = price * (double) quantity;
		String condition = card.getCondition();

		holder.titleTV.setText(card.getTitle());
		holder.quantityTV.setText(Integer.toString(quantity) + " X");
		holder.conditionTV.setText("Condition: " + condition);
		holder.rarityTV.setText("Rarity: " + card.getRarity());
		holder.singlePriceTV.setText(Double.toString(price) + "€");
		holder.multiplePriceTV.setText(Double.toString(multiplePrice) + "€");

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

}
