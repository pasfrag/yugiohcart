package com.example.pasca.yugiohcart;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pasca on 8/11/2017.
 */

public class MyArrayAdapter<T> extends ArrayAdapter {

	private Context context;
	private int resource;
	private List<String> items, tempItems, suggestions;

	public MyArrayAdapter(Context context, int resource, List<String> items){
		super(context, resource, 0, items);

		this.context = context;
		this.resource = resource;
		this.items = items;
		tempItems = new ArrayList<String>(items);
		suggestions = new ArrayList<String>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(resource, parent, false);
		}

		String item = items.get(position);

		if (item != null && view instanceof TextView){
			((TextView) view).setText(item);
		}

		return view;
	}

	@NonNull
	public Filter getFilter(){
		return filter;
	}

	Filter filter = new Filter() {
		@Override
		public CharSequence convertResultToString(Object resultValue) {
			return (String) resultValue;
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (constraint != null){
				suggestions.clear();
				for (String name : tempItems){
					if (name.toLowerCase().contains(constraint.toString().toLowerCase())){
						suggestions.add(name);
					}
				}
				FilterResults filterResults = new FilterResults();
				filterResults.values = suggestions;
				filterResults.count = suggestions.size();

				return filterResults;
			} else {
				return new FilterResults();
			}
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			List<String> filterList = (ArrayList<String>) results.values;
			if (results != null && results.count>0){
				clear();
				addAll(filterList);
//				for (String item : filterList){
//					add(item);
//					notifyDataSetChanged();
//				}
				notifyDataSetChanged();
			}
		}
	};

}
