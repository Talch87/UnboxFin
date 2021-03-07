package com.emavale.unboxfin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.icu.text.UnicodeSet;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TickerDetails extends AppCompatActivity {

    public TextView tickerlabel;
    public TextView tickeronly;
    public String ticker;
    public TextView details_sales;
    public TextView details_ebitda;
    public String revenues;
    public String ebitda;
    public LineChart price_chart;
    public String prices_api_token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticker_details);
        
        String selected_ticker = getIntent().getStringExtra("TICKER_SELECTED");
        tickerlabel = findViewById(R.id.Details_ticker_label);
        tickeronly = findViewById(R.id.Details_ticker_only);
        details_sales = findViewById(R.id.Details_Revenues);
        details_ebitda = findViewById(R.id.Details_EBITDA);
        prices_api_token = "6043dde5179441.94270114";

        //Price CHART
        price_chart = findViewById(R.id.price_chart);


        String ticker = selected_ticker.substring(0, selected_ticker.indexOf("-") - 1);
        tickeronly.setText(ticker);
        YahooTickerData(ticker);



    };



    private void YahooTickerData(String ticker){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/v2/get-summary?symbol=" + ticker + "&region=US";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject response_obj = new JSONObject(response);
                        revenues = response_obj.getJSONObject("financialData").getJSONObject("totalRevenue").getString("fmt");
                        ebitda = response_obj.getJSONObject("financialData").getJSONObject("ebitda").getString("fmt");
                        details_sales.setText(revenues);
                        details_ebitda.setText(ebitda);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> details_sales.setText("That didn't work!")


        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-rapidapi-key", "e0be66ae65mshf978aea8cd09be1p152050jsnfc62bd35e202");
                headers.put("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com");
                return headers;
            }

        };
        queue.add(stringRequest);

    };
    //End of YahooTickerData




    @RequiresApi(api = Build.VERSION_CODES.N)
    private void downloadPricesJson(String game_type, String timeframe) {
        String first_date = getCalculatedDate("dd-MM-yyyy", -1825);
        String last_date = getCalculatedDate("dd-MM-yyyy", -1);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://eodhistoricaldata.com/api/eod/MCD.US?api_token=OeAFFmMliFG5orCUuwAKQ8l4WWFQ67YX&period=d&order=a&from="+first_date+"&to="+last_date;
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject response_obj = new JSONObject(response);
                            revenues = response_obj.getJSONObject("financialData").getJSONObject("totalRevenue").getString("fmt");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } }, error -> {
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("x-rapidapi-key", "e0be66ae65mshf978aea8cd09be1p152050jsnfc62bd35e202");
                    headers.put("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com");
                    return headers;
                };

                public Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("interval", "1d");
                    params.put("symbol", "AMRN");
                    params.put("range", "1y");
                    params.put("region", "US");
                    return params;
                };
            };

            queue.add(stringRequest);
        } finally {};
    };
    //End of downloadPricesJson

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    };

};
//End of Class


