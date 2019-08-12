package com.example.stormy;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

import com.example.stormy.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_main);
        setupAttributionLink();
        requestData();
    }

    private void setupAttributionLink() {
        TextView attributionLabel = findViewById(R.id.attributionLabel);
        attributionLabel.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void requestData() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.error_network_unavailable), Toast.LENGTH_LONG).show();
            return;
        }

        String apiKey = BuildConfig.DarkSkyApiKey;
        double latitude = 40.795335;
        double longitude = -73.972640;
        String forecastURL = "https://api.darksky.net/forecast/"
                + apiKey
                + "/"
                + latitude
                + ","
                + longitude;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(forecastURL).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "IO Exception Caught: ", e);
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = Objects.requireNonNull(response.body()).string();
                Log.v(TAG, responseBody);
                if (response.isSuccessful()) {
                    try {
                        binding.setWeather(getCurrentWeather(responseBody));
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception Caught: ", e);
                    }
                } else {
                    alertUserAboutError();
                }
            }
        });
    }

    private CurrentWeather getCurrentWeather(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");
        return new CurrentWeather("New York, NY",
                currently.getString("icon"),
                currently.getLong("time"),
                currently.getDouble("temperature"),
                currently.getDouble("humidity"),
                currently.getDouble("precipProbability"),
                currently.getString("summary"),
                forecast.getString("timezone"));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager != null ? manager.getActiveNetworkInfo() : null;

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }

    public void refreshOnClick(View view) {
        requestData();
    }

    @BindingAdapter({"android:src"})
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }
}
