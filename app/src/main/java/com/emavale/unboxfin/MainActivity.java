package com.emavale.unboxfin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    private SeekBar ageBar;
    private EditText agenumber;
    private Button agenext;

     //Optional parameters


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ageBar = (SeekBar) findViewById(R.id.ageBar);
        agenumber = (EditText) findViewById(R.id.ageNumber);
        agenext = (Button) findViewById(R.id.age_btn_next);

        ageBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                agenumber.setText(""+ progress+"");
                Intent toTickerSelector_Intent = new Intent(MainActivity.this, SelectTicker.class);
                toTickerSelector_Intent.putExtra("age", progress);
                agenext.setVisibility(View.VISIBLE);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        agenext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toTickerSelector_Intent = new Intent(MainActivity.this, SelectTicker.class);
                startActivity(toTickerSelector_Intent);
            }
        });
    }
}