package com.example.pasca.yugiohcart;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
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
	private Button searchBT, cartBT;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.pb);
		searchBT = (Button) findViewById(R.id.search_card_button);
		cartBT = (Button) findViewById(R.id.my_cart_button);

		handler = new MySQLiteHandler(this);

		if (handler.getCardCount() == 0 && isOnline()){
			new PopulateDatabaseTask().execute();
		}else if (handler.getCardCount() == 0 && !isOnline()){
			Toast.makeText(this, "You must be online to get all card names", Toast.LENGTH_LONG).show();
			//InternetReceiver internetReceiver = new InternetReceiver();

		}else if (handler.getCardCount() != 0){
			progressBar.setVisibility(View.GONE);
			searchBT.setVisibility(View.VISIBLE);
			cartBT.setVisibility(View.VISIBLE);
		}
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

	private class PopulateDatabaseTask extends AsyncTask<Void,Void,Void>{

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

				for (String card : cardNames) {

					values.put(handler.COLUMN_TITLE, card);

					database.insert(handler.TABLE_CARDS, null, values);
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
			searchBT.setVisibility(View.VISIBLE);
			cartBT.setVisibility(View.VISIBLE);
		}
	}

	private class InternetReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras()!=null){
				final ConnectivityManager connectivityManager =
						(ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
				final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info.isConnected()){

					Toast.makeText(getApplicationContext(), "You are online now", Toast.LENGTH_LONG).show();
					this.abortBroadcast();

				}
			}
		}
	}

}
