package com.example.pasca.yugiohcart;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CardDetailsFragment extends Fragment {


	private ImageView cardImage;
	/*private TextView ;*/

	private String cardName, cardText, cardType,cardFamily, atk, def, level, property;

	public CardDetailsFragment() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_card_details, container, false);
	}

}
