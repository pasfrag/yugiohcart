package com.example.pasca.yugiohcart;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

	private MySQLiteHandler handler;
	private ProgressBar progressBar;
	private Button searchBT, cartBT, retryBT;
	private TextView progressTV, cardDownloadedTV;
	private String cardNow;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.pb);
		searchBT = (Button) findViewById(R.id.search_card_button);
		cartBT = (Button) findViewById(R.id.my_cart_button);
		retryBT = (Button) findViewById(R.id.retry_main_btn);
		progressTV = (TextView) findViewById(R.id.progress_percentage);
		cardDownloadedTV = (TextView) findViewById(R.id.progress_card);

		handler = new MySQLiteHandler(this);

		if (handler.getCardCount() == 0 && isOnline()){
			new PopulateDatabaseTask().execute();
			getCurrency();
		}else if (handler.getCardCount() == 0 && !isOnline()){
			Toast.makeText(this, "You must be online to get all card names", Toast.LENGTH_LONG).show();
			progressBar.setVisibility(View.GONE);
			progressTV.setVisibility(View.GONE);
			cardDownloadedTV.setVisibility(View.GONE);
			retryBT.setVisibility(View.VISIBLE);

		}else if (handler.getCardCount() != 0){
			progressTV.setVisibility(View.GONE);
			progressBar.setVisibility(View.GONE);
			cardDownloadedTV.setVisibility(View.GONE);
			searchBT.setVisibility(View.VISIBLE);
			cartBT.setVisibility(View.VISIBLE);
			retryBT.setVisibility(View.GONE);
		}
    }

    public boolean onCreateOptionsMenu(Menu menu){

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.action_download){

			retryPopulatingDB(null);

		}else if (id == R.id.action_settings){

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;

		}else if (id == R.id.action_currency){
			if (isOnline()){
				getCurrency();
			}else {
				Toast.makeText(this, "You must be online to get latest currency", Toast.LENGTH_LONG).show();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	//This method creates the activity that shows search card area
    public void onClickSearchCardButton(View view){

        Context context = this;

        Class destinyClass = SearchCardActivity.class;

        Intent startSearchCardIntent = new Intent(context, destinyClass);

        startActivity(startSearchCardIntent);

    }

    public void onClickMyCartButton(View view){

        Context context = this;

        Class destinyClass = MyCartActivity.class;

        Intent startSearchCardIntent = new Intent(context, destinyClass);

        startActivity(startSearchCardIntent);

    }

	public boolean isOnline() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public void retryPopulatingDB(View view){
		if (isOnline()){

			handler.deleteAllCards();
			cartBT.setVisibility(View.GONE);
			searchBT.setVisibility(View.GONE);
			retryBT.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			progressTV.setVisibility(View.VISIBLE);
			cardDownloadedTV.setVisibility(View.VISIBLE);
			new PopulateDatabaseTask().execute();

			if (view!=null){
				getCurrency();
			}

		}else{
			Toast.makeText(this, "You must be online to get all card names", Toast.LENGTH_LONG).show();
		}
	}

	private class PopulateDatabaseTask extends AsyncTask<Void,Integer,Void>{

		@Override
		protected Void doInBackground(Void... params) {

			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String response = null;

			try {
				Uri uri = Uri.parse("http://yugioh.wikia.com/api/v1/Articles/List?category=TCG_cards&limit=9000&namespaces=0");
				URL url = new URL(uri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();
				InputStream stream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				if (stream == null) return null;

				reader = new BufferedReader(new InputStreamReader(stream));

				String line;
				while ((line = reader.readLine()) != null){
					buffer.append(line + "\n");
				}
				response = buffer.toString();

				ArrayList<String> cardNames;

				JSONObject JSONResponse = new JSONObject(response);
				JSONArray cardsJSON = JSONResponse.getJSONArray("items");

				cardNames = new ArrayList<>(cardsJSON.length());

				for (int i = 0; i < cardsJSON.length(); i++){

					JSONObject cardNameJSON = cardsJSON.getJSONObject(i);
					cardNames.add(cardNameJSON.getString("title"));

				}

				SQLiteDatabase database = handler.getWritableDatabase();
				ContentValues values = new ContentValues();

				int progress = 0;

				for (String card : cardNames) {

					cardNow = card;

					values.put(handler.COLUMN_TITLE, card);

					database.insert(handler.TABLE_CARDS, null, values);

					progress++;

					publishProgress(progress);
				}

				database.close();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}finally {

				if (urlConnection != null) {

					urlConnection.disconnect();

				}
				if (reader != null) {

					try {
						reader.close();
					} catch (final IOException e) {
						e.printStackTrace();
					}

				}
			}

			return null;
		}

		protected void onPostExecute(Void v){
			progressBar.setVisibility(View.GONE);
			progressTV.setVisibility(View.GONE);
			cardDownloadedTV.setVisibility(View.GONE);
			searchBT.setVisibility(View.VISIBLE);
			cartBT.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress[0]);

			progressBar.setProgress(progress[0]);

			int percentage = (progress[0]/100);
			progressTV.setText( String.valueOf(percentage) + "%");
			cardDownloadedTV.setText("Downloading card " + cardNow);
		}
	}

	public void getCurrency(){

		String url = "http://www.apilayer.net/api/live?access_key=5ef2d99463c96f522c3830ba0af61443&format=1";

		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {

				try {
					JSONObject json = new JSONObject(response);
					if (json.getBoolean("success")){
						JSONObject currencies = json.getJSONObject("quotes");
						double usdeur = currencies.getDouble("USDEUR");

						SharedPreferences preferences = getSharedPreferences("ab" ,Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();

						editor.putString(getString(R.string.saved_usdeur), String.valueOf(usdeur));
						editor.apply();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();

				editor.putString(getString(R.string.saved_usdeur), "1");
				editor.apply();
			}
		};

		StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener);
		RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

		int socketTimeout = 30000;
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		request.setRetryPolicy(policy);

		queue.add(request);

	}

}
