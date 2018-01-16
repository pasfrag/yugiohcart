package com.example.pasca.yugiohcart;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class SearchCardActivity extends AppCompatActivity {

    private AutoCompleteTextView searchCardET;
    private MyArrayAdapter<String> cardAdapter;
	private boolean first;
	private PopupWindow popupWindow;
	private RelativeLayout relativeLayout;
	private MySQLiteHandler handler;
	private List<String> cardNameList;

	//PopupViewElements
	private EditText quantityET, priceET;
	private Spinner currencySP, conditionSP, raritySP;
	private Button cartBT, collectionBT;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_card);

		relativeLayout = (RelativeLayout) findViewById(R.id.activity_search_card);

		first = true;

		handler = new MySQLiteHandler(getApplicationContext());

		cardNameList = handler.getAllCardNames();

        searchCardET = (AutoCompleteTextView) findViewById(R.id.search_card);

		cardAdapter = new MyArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cardNameList);
		searchCardET.setAdapter(cardAdapter);
		cardAdapter.notifyDataSetChanged();

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

	private void makeAToast(String string){
		Toast toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
		toast.show();
	}

	public void addToCart(View view){
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View popupView = layoutInflater.inflate(R.layout.add_card_layout, null);

		popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 650);
		popupWindow.setFocusable(true);
		popupWindow.update();

		TextView cardTitleTV = (TextView) popupView.findViewById(R.id.card_title_tv);
		cardTitleTV.setText(searchCardET.getText().toString());
		quantityET = (EditText) popupView.findViewById(R.id.quantity_tv);
		priceET = (EditText) popupView.findViewById(R.id.price_tv);
		currencySP = (Spinner) popupView.findViewById(R.id.currency_sp);
		conditionSP = (Spinner) popupView.findViewById(R.id.condition_sp);
		raritySP = (Spinner) popupView.findViewById(R.id.rarity_sp);
		cartBT = (Button) popupView.findViewById(R.id.add_btn);
		collectionBT = (Button) popupView.findViewById(R.id.collection_add_btn);

		popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);

	}

	public void dismissPopup(View view){

		popupWindow.dismiss();

	}

	public void addCard(View view){

		String tableName = handler.TABLE_CART;
		String added = "cart!";

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

		if (view == collectionBT){
			price = 0.0;
			tableName = handler.TABLE_COLLECTION;
			added = "collection!";
		}

		Card card = new Card(quantity, title, rarity, type, condition, currency, price);

		if (!(rarity.equals("C") || rarity.equals("SR") || rarity.equals("R"))){
			makeAToast("RARITY WHORE!!!");
		}

		makeAToast("Card added to your " + added);
		handler.addACard(card, tableName);
		popupWindow.dismiss();
	}

}
