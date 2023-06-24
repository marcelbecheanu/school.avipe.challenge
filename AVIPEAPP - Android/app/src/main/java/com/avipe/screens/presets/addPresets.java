package com.avipe.screens.presets;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.avipe.R;
import com.avipe.screens.Authentication.LoginScreen;
import com.avipe.screens.MainScreen;
import com.avipe.screens.SliderScreen.SliderScreen;
import com.avipe.screens.SplashScreen;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class addPresets extends AppCompatActivity {

    private String token = "";
    private Context context = null;
    private HashMap<String, String> places = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_presets);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.background));

        context = this;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        token = sharedPref.getString("token", "");

        findViewById(R.id.back).setOnClickListener(e -> {
            Intent mainIntent = new Intent(context, MainScreen.class);
            MainScreen.instance.startActivity(mainIntent);
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    MainScreen.instance.setViewPager(2);
                }
            }, 300);
            finish();
        });

        TextView percent = findViewById(R.id.textView11);
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                percent.setText(String.valueOf(new Integer(i)) + " %");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        loadSpinner();
        findViewById(R.id.save).setOnClickListener(e-> {
            Spinner spinner = findViewById(R.id.spinner);
            int pointer = 0;
            for(String key : places.keySet()){
                if(places.get(key).equals(spinner.getSelectedItem().toString()) && pointer == spinner.getSelectedItemPosition()){
                   String id = key;
                   int percentAVG = seekBar.getProgress();
                   //TODO POST WITH THIS DATA
                    try{
                        RequestQueue queue = Volley.newRequestQueue(context);
                        StringRequest stringRequest = new StringRequest(
                                Request.Method.POST,
                                getResources().getString(R.string.url)+"notifications?token="+token,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Intent mainIntent = new Intent(context, MainScreen.class);
                                        MainScreen.instance.startActivity(mainIntent);
                                        new Handler().postDelayed(new Runnable(){
                                            @Override
                                            public void run() {
                                                MainScreen.instance.setViewPager(2);
                                            }
                                        }, 300);
                                        showToastS(context.getResources().getString(R.string.preset_added));
                                        finish();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        try {
                                            int status = error.networkResponse.statusCode;
                                            String data = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                            JSONObject json = new JSONObject(data);
                                            JSONArray errors = json.getJSONArray("errors");
                                            if(errors.length() > 0){
                                                for (int i = 0; i < errors.length(); i++ ){
                                                    showToast(errors.getJSONObject(i).getString("msg"));
                                                }
                                            }

                                        } catch (Exception e) {
                                            showToast("Internal Error Server");
                                        }
                                    }
                                }) {
                            @Override
                            public String getBodyContentType() {
                                return "application/x-www-form-urlencoded; charset=UTF-8";
                            }

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("place", id);
                                params.put("variation", percentAVG + "");
                                return params;
                            }

                        };
                        queue.add(stringRequest);
                    }catch (Exception error) {
                        showToast("CLIENT ERROR");
                    }

                }
                pointer += 1;
            }


        });

    }

    public void loadSpinner(){
        Spinner spinner = findViewById(R.id.spinner);
        try{
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"auth/profile?token="+token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);
                                JSONArray array = object.getJSONArray("places");

                                ArrayList<String> list = new ArrayList<>();
                                for (int i = 0; i < array.length(); i++) {
                                    places.put(array.getJSONObject(i).getString("uid"), array.getJSONObject(i).getString("place"));
                                    list.add(array.getJSONObject(i).getString("place"));
                                }

                                ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, list);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);

                            }catch (JSONException e){
                                showToastS("JSON ERROR FORMAT");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try{
                                int status = error.networkResponse.statusCode;
                                String data = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                JSONObject json = new JSONObject(data);
                                JSONArray errors = json.getJSONArray("errors");

                                if(errors.length() > 0){
                                    for (int i = 0; i < errors.length(); i++ ){
                                        showToast(errors.getJSONObject(i).getString("msg"));
                                    }
                                }
                            }catch (Exception e){
                                showToast("Internal Error Server");
                            }
                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }
            };
            queue.add(stringRequest);
        }catch (Exception error) {
            showToast("CLIENT ERROR");
        }
    }



    private void showToast(String error){
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout_id));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(error);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showToastS(String error){
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast_success,
                (ViewGroup) findViewById(R.id.custom_toast_layout_id));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(error);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

}