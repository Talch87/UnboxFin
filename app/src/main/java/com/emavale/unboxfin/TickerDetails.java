package com.emavale.unboxfin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import android.app.ProgressDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static com.emavale.unboxfin.R.color.cards_background;

public class TickerDetails extends AppCompatActivity {

    public TextView tickerlabel;
    public String ticker;
    public TextView details_sales;
    public TextView details_ebitda;
    public ScrollView generalscrollview;

    //News variables
    public String news_thumb;
    public String news_text;
    public String news_url;
    public JSONArray news_arr;
    public ScrollView news_scrollview;
    public LinearLayout news_linearlayout;
    public CardView cardview;
    public CardView cardNews;
    //End of News variables

    ///Trading Multipliers//////

     /*
            (+) Cash & Cash-Equivalents:
            (+) Equity Investments:
            (+) Other Non-Core Assets, Net:
            (+) Net Operating Losses:
            (-) Total Debt:
            (-) Preferred Stock:
            (-) Noncontrolling Interests:
            (-) Unfunded Pension Obligations:
            (-) Capital Leases:
            (-) Restructuring & Other Liabilities:

      */
    ///////////////////////


    public String revenues;
    public String ebitda;
    public Integer marketcap_ev;


    public LineChart price_chart;
    public List<Entry> prices_entries;
    public ArrayList<String> xLabel = new ArrayList<>();
    public JSONArray timestamps;
    public JSONArray close_prices;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticker_details);
        
        String selected_ticker = getIntent().getStringExtra("TICKER_SELECTED");
        tickerlabel = findViewById(R.id.Details_ticker_label);
        details_sales = findViewById(R.id.Details_Revenues);
        details_ebitda = findViewById(R.id.Details_EBITDA);

        cardNews = findViewById(R.id.cardNews);
        generalscrollview = findViewById(R.id.generalscrollview);

        //setup News layout
        news_scrollview = findViewById(R.id.newsscrollview);
        news_linearlayout = findViewById(R.id.newsLinearLayout);



        ticker = selected_ticker.substring(0, selected_ticker.indexOf("-") - 1);
        tickerlabel.setText(selected_ticker);
        YahooTickerData(ticker);


        //Price CHART
        price_chart = findViewById(R.id.price_chart);
        price_chart.setTouchEnabled(true);
        UpdatePriceChart(ticker,"1d","1y"); //update price chart
        YahooTickerNews(ticker);



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
                headers.put("useQueryString", "true");
                return headers;
            }

        };
        queue.add(stringRequest);

    };
    //End of YahooTickerData

    private void YahooTickerNews(String ticker){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://yahoo-finance-low-latency.p.rapidapi.com/v2/finance/news?symbols=" + ticker;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject response_obj = new JSONObject(response);
                        news_arr = response_obj.getJSONObject("Content").getJSONArray("result");
                        ArrayList<String> url_arr = new ArrayList<String>();
                        for (int i=0; i<Math.min(5,news_arr.length());i++){
                            news_thumb = news_arr.getJSONObject(i).getString("thumbnail");
                            news_text = asci_decoder(news_arr.getJSONObject(i).getString("title"));
                            news_url = news_arr.getJSONObject(i).getString("url");
                            url_arr.add(news_url);
                            //Card News
                            cardview = new CardView(this);
                            LinearLayout.LayoutParams card_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
                            cardview.setLayoutParams(card_params);
                            cardview.setRadius(15);
                            ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) cardview.getLayoutParams();
                            cardViewMarginParams.setMargins(0, 2, 0, 0);
                            cardview.requestLayout();
                            cardview.setCardBackgroundColor(ContextCompat.getColor(this,cards_background));
                            cardview.setId(i);
                            //Image thumb news
                            ImageView news_thumb_view = new ImageView(this);

                            LinearLayout.LayoutParams news_thumb_params = new LinearLayout.LayoutParams(280,180);
                            news_thumb_view.setLayoutParams(news_thumb_params);
                            ViewGroup.MarginLayoutParams thumbMarginParams = (ViewGroup.MarginLayoutParams) news_thumb_view.getLayoutParams();
                            thumbMarginParams.setMargins(0, 2, 2, 0);
                            Glide.with(TickerDetails.this).load(news_thumb).into(news_thumb_view);

                            //Text news
                            TextView news_text_view = new TextView(this);
                            news_text_view.setText(news_text);
                            LinearLayout.LayoutParams news_text_params = new LinearLayout.LayoutParams(600,180);
                            news_text_view.setLayoutParams(news_text_params);
                            ViewGroup.MarginLayoutParams textMarginparams = (ViewGroup.MarginLayoutParams) news_text_view.getLayoutParams();
                            textMarginparams.setMargins(10, 2, 0, 0);


                            LinearLayout in_card_layout = new LinearLayout(this);
                            in_card_layout.setOrientation(LinearLayout.HORIZONTAL);


                            //add views
                            news_linearlayout.addView(cardview);
                            cardview.addView(in_card_layout);
                            in_card_layout.addView(news_thumb_view);
                            in_card_layout.addView(news_text_view);








                        }
                        cardview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(url_arr.get(v.getId()));
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> details_sales.setText("That didn't work!")


        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-rapidapi-key", "e0be66ae65mshf978aea8cd09be1p152050jsnfc62bd35e202");
                headers.put("x-rapidapi-host", "yahoo-finance-low-latency.p.rapidapi.com");
                return headers;
            }

        };
        queue.add(stringRequest);

    };
    //End of YahooTickerNews

    private void YahooTickerFinancials(String ticker){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/v2/get-financials?symbol=" + ticker + "&region=US";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject response_obj = new JSONObject(response);
                        marketcap_ev = response_obj
                                        .getJSONObject("balanceSheetHistory")
                                        .getJSONArray("balanceSheetStatements")
                                        .getJSONObject(0)
                                        .getJSONObject("cash")
                                        .getInt("raw")
                                        +
                                        response_obj
                                        .getJSONObject("balanceSheetHistory")
                                        .getJSONArray("balanceSheetStatements")
                                        .getJSONObject(0)
                                        .getJSONObject("shortTermInvestments")
                                        .getInt("raw")
                                        +
                                response_obj
                                        .getJSONObject("balanceSheetHistory")
                                        .getJSONArray("balanceSheetStatements")
                                        .getJSONObject(0)
                                        .getJSONObject("commonStock")
                                        .getInt("raw");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("YahooFinancials","YahooTickerFinancials failed")
            ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-rapidapi-key", "e0be66ae65mshf978aea8cd09be1p152050jsnfc62bd35e202");
                headers.put("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com");
                headers.put("useQueryString", "true");
                return headers;
            }

        };
        queue.add(stringRequest);

    };
    //End of YahooTickerData


    public void UpdatePriceChart(String ticker, String interval, String range) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/v2/get-chart?interval="+interval+"&symbol="+ticker+"&range="+range+"&region=US";
        prices_entries = new ArrayList<>();
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject response_obj = new JSONObject(response);

                            timestamps = response_obj
                                    .getJSONObject("chart")
                                    .getJSONArray("result")
                                    .getJSONObject(0)
                                    .getJSONArray("timestamp");

                            close_prices = response_obj
                                    .getJSONObject("chart")
                                    .getJSONArray("result")
                                    .getJSONObject(0)
                                    .getJSONObject("indicators")
                                    .getJSONArray("quote")
                                    .getJSONObject(0)
                                    .getJSONArray("close");

                            for (int i=0; i<timestamps.length(); i++) {

                                try {
                                    xLabel.add(getDate(Long.parseLong(timestamps.getString(i))).toString());

                                    prices_entries.add(new Entry(i+1, (float) close_prices.getDouble(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            Log.d("prices entries", prices_entries.toString());
                            LineDataSet dataSet = new LineDataSet(prices_entries, "Price");
                            dataSet.setColor(Color.RED);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setDrawCircles(false);
                            LineData lineData = new LineData(dataSet);
                            lineData.setDrawValues(false);
                            lineData.setHighlightEnabled(true);


                            XAxis xAxis = price_chart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setValueFormatter(new ValueFormatter() {
                                                        @Override
                                                        public String getFormattedValue(float value) {
                                                            return xLabel.get((int)value);
                                                        }
                                                    });


                            price_chart.setData(lineData);
                            price_chart.getDescription().setEnabled(false);
                            price_chart.setElevation(2f);
                            price_chart.animateX(1000);
                            price_chart.setDrawMarkers(false);
                            price_chart.setDrawGridBackground(false);

                            Legend legend = price_chart.getLegend();
                            legend.setEnabled(false);
                            price_chart.invalidate();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } }, error -> {
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("x-rapidapi-key", "e0be66ae65mshf978aea8cd09be1p152050jsnfc62bd35e202");
                    headers.put("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com");
                    headers.put("useQueryString", "true");
                    return headers;
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



    public String asci_decoder(String str){
        str = str.replace("&#039;", "'");
        str = str.replace("&#39;", "'");
        return str;
    };

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

};
//End of Class


