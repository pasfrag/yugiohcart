package com.example.pasca.yugiohcart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {

	private List<Card> myCart;
	private CustomItemClickListener listener;

	public CartAdapter(List<Card> myCart, CustomItemClickListener listener) {
		this.myCart = myCart;
		this.listener = listener;
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

		Card card = myCart.get(position);
		int quantity = card.getQuantity();
		double price = card.getPrice();
		double multiplePrice = price * (double) quantity;
		String condition = card.getCondition();

		holder.titleTV.setText(card.getTitle());
		holder.quantityTV.setText(Integer.toString(quantity) + " X");
		holder.conditionTV.setText("Condition: " + condition);
		holder.rarityTV.setText("Rarity: " + card.getRarity());
		holder.singlePriceTV.setText(String.format("%.2f", price) + "€");
		holder.multiplePriceTV.setText(String.format("%.2f", multiplePrice) + "€");

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
