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


public class MyArrayAdapter<T> extends ArrayAdapter {

	/*The custom string array adapter that let's people search cards more easily*/

	private Context context;
	private int resource;
	private List<String> items, tempItems, suggestions;

	public MyArrayAdapter(Context context, int resource, List<String> items){
		super(context, resource, 0, items);

		this.context = context;
		this.resource = resource;
		this.items = items;
		tempItems = new ArrayList<>(items);
		suggestions = new ArrayList<>();
	}

	@NonNull
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

		//Better filtering letting users search a name more easily
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (constraint != null){
				suggestions.clear();
				String[] constraintArray;
				constraintArray = constraint.toString().split(" ");
				Boolean flag = true;
				for (String name : tempItems){
					String addedName = name;
					for (String con : constraintArray) {
						if (name.toLowerCase().contains(con.toLowerCase())){
							name = name.toLowerCase().replace(con.toLowerCase(), "");
						}else {
							flag = false;
						}
					}
					if (flag){
						suggestions.add(addedName);
					}
					flag = true;
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
				notifyDataSetChanged();
			}
		}
	};

}
