package com.example.pasca.yugiohcart;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class CardRequest extends StringRequest{
	private static final String YGO_WIKIA_URL = "http://yugioh.wikia.com/api/v1/Articles/List?category=TCG_cards&limit=9000&namespaces=0";
	private Map<String, String> params;

	public CardRequest(Response.Listener<String> listener,
					   Response.ErrorListener errorListener){

		super(Method.GET, YGO_WIKIA_URL, listener, errorListener);
		params = new HashMap<>();

	}

	public Map<String, String> getParams(){
		return params;
	}
}
