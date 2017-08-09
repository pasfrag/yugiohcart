package com.example.pasca.yugiohcart;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button searchCardButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchCardButton = (Button) findViewById(R.id.search_card_button);
        searchCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSearchCardButton(v);
            }
        });
    }


    //This method creates the activity that shows search card area
    void onClickSearchCardButton(View v){

        Context context = this;

        Class destinyClass = SearchCardActivity.class;

        Intent startSearchCardIntent = new Intent(context, destinyClass);

        startActivity(startSearchCardIntent);
    }
}
