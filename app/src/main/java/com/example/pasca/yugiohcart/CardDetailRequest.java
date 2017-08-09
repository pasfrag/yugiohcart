package com.example.pasca.yugiohcart;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pasca on 8/8/2017.
 */

public class CardDetailRequest extends StringRequest {

	//private static final String YGO_PRICES_URL = "http://yugiohprices.com/api/card_data/";
	private Map<String, String> params;

	public CardDetailRequest( String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
		super(Method.GET, url, listener, errorListener);
		params = new HashMap<>();
	}

	public Map<String, String> getParams(){
		return params;
	}
}
