package com.example.pasca.yugiohcart;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class CardDetailsFragment extends Fragment {


	private ImageView cardImage;
	//private String cardName, cardText, type, cardType,cardFamily, atk, def, level;
	private TextView titleTV, textTV, typeTV, cardTypeTV, familyTV, atkTV, defTV, levelTV;
	private  String cardName;

	private static final String DATA_URL = "http://yugiohprices.com/api/card_data/";
	private static final String IMAGE_URL = "https://static-3.studiobebop.net/ygo_data/card_images/";

	public CardDetailsFragment() {
	}

	/*@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Initialization of data variables
		cardName = null;
		cardText = null;
		type = null;
		cardType = null;
		cardFamily = null;
		atk = null;
		def = null;
		level = null;
	}*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_card_details, container, false);
		Bundle args = getArguments();
		cardName = args.getString("cardName", null);

		//Initialization of UI variables
		//cardImage = ;
		titleTV = (TextView) rootView.findViewById(R.id.title_TV);
		textTV = (TextView) rootView.findViewById(R.id.card_text_TV);
		typeTV = (TextView) rootView.findViewById(R.id.type_TV);
		cardTypeTV = (TextView) rootView.findViewById(R.id.card_type_TV);
		familyTV = (TextView) rootView.findViewById(R.id.family_TV);
		atkTV = (TextView) rootView.findViewById(R.id.attack_TV);
		defTV = (TextView) rootView.findViewById(R.id.defence_TV);
		levelTV = (TextView) rootView.findViewById(R.id.level_TV);
		cardImage = (ImageView) rootView.findViewById(R.id.card_image);

		populateData();

		return rootView;
	}

	private void populateData(){

		String cardNameNew = cardName.replace(" ","_");
		cardNameNew = cardNameNew.replace("-","_");

		Response.Listener listener = new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				try {
					JSONObject JSONResponse = new JSONObject(response);
					if (JSONResponse.getString("status").contentEquals("success")) {
						JSONObject data = JSONResponse.getJSONObject("data");

						String textToChange = data.getString("text");
						String text = textToChange.replaceAll("\\s+", " ");
						text = text.replaceAll("â\\u0097\\u008F", "\n \u2022");

						Log.e("Change text", textToChange);

						SpannableString content = new SpannableString(data.getString("name"));
						content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
						titleTV.setText(content);
						textTV.setText(text);
						cardTypeTV.setText(data.getString("card_type"));
						if (data.getString("card_type").equals("monster")) {
							typeTV.setText(data.getString("type"));
							familyTV.setText(data.getString("family"));
							atkTV.setText(data.getString("atk"));
							defTV.setText(data.getString("def"));
							levelTV.setText(data.getString("level"));
						}else{
							typeTV.setVisibility(View.GONE);
							familyTV.setVisibility(View.GONE);
							atkTV.setVisibility(View.GONE);
							defTV.setVisibility(View.GONE);
							levelTV.setVisibility(View.GONE);
						}

					}else {
						Log.e("Volley.Response","No data to retrieve");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (error instanceof NetworkError) {
					Log.e("Volley Error", "Network error");
				} else if (error instanceof ServerError) {
					Log.e("Volley Error", "Server Error");
				} else if (error instanceof AuthFailureError) {
					Log.e("Volley Error", "Auth Failure Error");
				} else if (error instanceof ParseError) {
					Log.e("Volley Error", "Parse Error");
				} else if (error instanceof NoConnectionError) {
					Log.e("Volley Error", "No Connection Error");
				} else if (error instanceof TimeoutError) {
					Log.e("Volley Error", "Timeout Error");
				}
			}
		};

		ImageRequest imageRequest = new ImageRequest(IMAGE_URL  + cardNameNew + ".jpg",
				new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap response) {
						cardImage.setImageBitmap(response);
					}
				}, 0, 0, null,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						cardImage.setImageResource(R.drawable.image_load_error);
					}
				}
		);

		CardDataRequest dataRequest = new CardDataRequest(DATA_URL + cardName, listener, errorListener);
		RequestQueue queue = Volley.newRequestQueue(this.getContext());

		int socketTimeout = 30000;
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		dataRequest.setRetryPolicy(policy);

		queue.add(dataRequest);
		queue.add(imageRequest);


	}

	private class CardDataRequest extends StringRequest{

		public CardDataRequest( String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
			super(Method.GET, url, listener, errorListener);
		}

	}

}
