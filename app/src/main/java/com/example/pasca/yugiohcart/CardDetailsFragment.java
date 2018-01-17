package com.example.pasca.yugiohcart;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CardDetailsFragment extends Fragment {


	private ImageView cardImage;
	private TextView titleTV, textTV, typeTV, cardTypeTV, attributeTV, statsTV, levelTV;
	private  String cardName;
	private Bitmap image;

	private static final String DATA_URL = "http://yugiohprices.com/api/card_data/";
	private static final String IMAGE_URL = "https://static-3.studiobebop.net/ygo_data/card_images/";

	public CardDetailsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_card_details, container, false);
		Bundle args = getArguments();
		cardName = args.getString("cardName", null);

		//Initialization of UI variables
		titleTV = rootView.findViewById(R.id.title_TV);
		textTV = rootView.findViewById(R.id.card_text_TV);
		typeTV = rootView.findViewById(R.id.type_TV);
		cardTypeTV = rootView.findViewById(R.id.card_type_TV);
		attributeTV = rootView.findViewById(R.id.attribute_TV);
		statsTV = rootView.findViewById(R.id.stats_TV);
		levelTV = rootView.findViewById(R.id.level_TV);
		cardImage = rootView.findViewById(R.id.card_image);

		//The implicit intent that shares the image
		cardImage.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				Intent shareImage = new Intent(Intent.ACTION_SEND);
				shareImage.setType("image/jpeg");
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
				File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");

				try {
					file.createNewFile();
					FileOutputStream outputStream = new FileOutputStream(file);
					outputStream.write(bytes.toByteArray());
				} catch (IOException e) {
					e.printStackTrace();
				}

				shareImage.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
				startActivity(Intent.createChooser(shareImage, "Share Image"));

				return true;
			}
		});

		populateData();

		return rootView;
	}

	/*Sets the cards data and image in the fragment*/
	private void populateData(){

		String cardNameNew = cardName.replace(" ","_");
		cardNameNew = cardNameNew.replace("-","_");
		cardNameNew = cardNameNew.replace("\"","_");
		cardName = cardName.replace(" ", "%20");

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

						SpannableString content = new SpannableString(data.getString("name"));
						content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
						titleTV.setText(content);
						textTV.setText(text);
						cardTypeTV.setText(data.getString("card_type"));
						if (data.getString("card_type").equals("monster")) {
							String type = "Monster type: " + data.getString("type");
							typeTV.setText(type);
							String attribute = data.getString("family");
							attribute = "Attribute: " + attribute.substring(0,1).toUpperCase() + attribute.substring(1);
							attributeTV.setText(attribute);
							String stats = "ATK/DEF: " + data.getString("atk") + "/" + data.getString("def");
							statsTV.setText(stats);
							String level = "Level: ";
							if (type.toLowerCase().contains("xyz")){
								level = "Rank: ";
							}
							level = level + data.getString("level");
							levelTV.setText(level);
						}else{
							typeTV.setVisibility(View.GONE);
							attributeTV.setVisibility(View.GONE);
							statsTV.setVisibility(View.GONE);
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

				titleTV.setVisibility(View.GONE);
				textTV.setText("There is no internet connection. You can \'t see any data.");
				cardTypeTV.setVisibility(View.GONE);
				typeTV.setVisibility(View.GONE);
				attributeTV.setVisibility(View.GONE);
				statsTV.setVisibility(View.GONE);
				levelTV.setVisibility(View.GONE);

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
						cardImage.buildDrawingCache();
						image = cardImage.getDrawingCache();
					}
				}, 0, 0, null,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						cardImage.setImageResource(R.drawable.image_load_error);
						image = ((BitmapDrawable) cardImage.getDrawable()).getBitmap();
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

	/*A volley requets class for the card data*/
	private class CardDataRequest extends StringRequest{

		public CardDataRequest( String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
			super(Method.GET, url, listener, errorListener);
		}

	}

}
