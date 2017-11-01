package financeapp.example.trafficparrot.com.mobilefinanceapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadPrice();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadPrice();
    }

    private void reloadPrice() {
        new RetrievePriceTask().execute();
    }

    private double parseStockQuoteLastPrice(String markitStockQuoteJson) throws JSONException {
        return new JSONObject(markitStockQuoteJson).getDouble("LastPrice");
    }

    private String markitStockQuoteForApple()  {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("accept-encoding", "identity")
                .url("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=AAPL")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "ERROR:" + response.code();
            }
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    class RetrievePriceTask extends AsyncTask<String, Void, Double> {
        private Exception exception;

        protected Double doInBackground(String... urls) {
            try {
                return parseStockQuoteLastPrice(markitStockQuoteForApple());
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Double price) {
            TextView applePrice = findViewById(R.id.apple_price);
            if (exception != null) {
                applePrice.setText("ERROR: " + exception.getMessage());
                applePrice.setTextColor(getResources().getColor(R.color.red));
            } else {
                if (price < 10) {
                    applePrice.setText(String.valueOf(price) + "!!!");
                    applePrice.setTextColor(getResources().getColor(R.color.red));
                } else {
                    applePrice.setText(String.valueOf(price));
                    applePrice.setTextColor(getResources().getColor(R.color.green));
                }
            }
        }
    }

}
