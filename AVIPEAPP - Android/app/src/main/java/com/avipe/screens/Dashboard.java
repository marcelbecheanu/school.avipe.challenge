package com.avipe.screens;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.avipe.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends Fragment {

    public Dashboard() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private String token = "";
    private View screen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        screen = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //GETTING USER TOKEN
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(screen.getContext());
        token = sharedPref.getString("token", "");

        //Define add Place
        TextView addPlaceAction = screen.findViewById(R.id.addPlace);
        addPlaceAction.setOnClickListener(e -> registerPlace(screen.getContext()));

        loadPlaces();
        showStats();
        updateStats(null);


        return screen;
    }


    private void registerPlace(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.dashboard_place_add));

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(context.getText(R.string.dashboard_place_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String nameOfPlace = input.getText().toString();

                Intent mainIntent = new Intent(context, QrCodeReadPlace.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("nameOfPlace", nameOfPlace);
                mBundle.putString("token", token);
                mainIntent.putExtras(mBundle);

                startActivityForResult(mainIntent, 2000);

            }
        });

        builder.setNegativeButton(context.getText(R.string.dashboard_place_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private ConstraintLayout ButtonAction(Context context, String title){
        ConstraintLayout cache = new ConstraintLayout(context);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams( getResources().getDimensionPixelSize(R.dimen.button_size), ConstraintLayout.LayoutParams.MATCH_PARENT);
        params.setMarginEnd(20);
        cache.setLayoutParams(params);
        cache.setBackground(context.getDrawable(R.drawable.card_background_border));
        TextView text = new TextView(context);
        text.setWidth(0);
        text.setHeight(0);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setPadding(0, 10, 0,0);
        ConstraintLayout.LayoutParams serviceDescParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.FILL_PARENT, ConstraintLayout.LayoutParams.FILL_PARENT);
        text.setLayoutParams(serviceDescParams);
        text.setText(title);
        text.setTextSize(14);
        Typeface face = ResourcesCompat.getFont(context, R.font.poppins_bold);
        text.setTypeface(face);

        cache.addView(text);
        return cache;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == 2000 ) {
            loadPlaces();
        }
        if (requestCode == 100) {
            showStats();
        }
    }

    public void loadPlaces(){
        LinearLayout linear = screen.findViewById(R.id.placesAddLinear);

        //clear linear
        for(int i=0; i <= linear.getChildCount(); i++){
            if(linear.getChildCount() > 1){
                linear.removeViewAt(0);
            }
        }


        ConstraintLayout Global = ButtonAction(screen.getContext(), screen.getResources().getString(R.string.dashboard_options_Global));
        linear.addView(Global, 0);
        Global.setOnClickListener(e -> {
            updateStats(null);
        });

        try{
            RequestQueue queue = Volley.newRequestQueue(screen.getContext());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"auth/profile?token="+token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);

                                TextView name = screen.findViewById(R.id.nameUser);
                                name.setText(""+ object.getString("name"));

                                try {
                                    JSONArray array = object.getJSONArray("places");
                                    for(int i = 0; i < array.length(); i++){
                                        ConstraintLayout cache = ButtonAction(screen.getContext(), array.getJSONObject(i).getString("place"));
                                        String uid = array.getJSONObject(i).getString("uid");
                                        cache.setOnClickListener(e -> {
                                            updateStats(uid);
                                        });
                                        linear.addView(cache, 1);
                                    }
                                }catch (JSONException e){

                                }


                            }catch (JSONException e){
                                showToastS("JSON ERROR FORMAT - Places");
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

    private void updateStats(String uid){
        if(uid == null)
            uid = "";
        else
            uid = "/"+uid;

        //
        TextView temperature = screen.findViewById(R.id.temperature);
        TextView humidity = screen.findViewById(R.id.humidity);
        TextView soil1 = screen.findViewById(R.id.soil1);
        TextView soil2 = screen.findViewById(R.id.soil2);
        TextView soil3 = screen.findViewById(R.id.soil3);
        TextView soil4 = screen.findViewById(R.id.soil4);
        TextView wind = screen.findViewById(R.id.wind);
        TextView wind2 = screen.findViewById(R.id.wind2);
        TextView uv = screen.findViewById(R.id.uv);


        //Request informartion
        try{
            RequestQueue queue = Volley.newRequestQueue(screen.getContext());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"dashboard/place"+uid,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                 JSONObject object = new JSONObject(response);
                                temperature.setText("" + object.getString("temperature") + " ºC");
                                humidity.setText("" + object.getString("humidity") + " %");
                                soil1.setText("" + object.getString("soil1") + " %");
                                soil2.setText("" + object.getString("soil2") + " %");
                                soil3.setText("" + object.getString("soil3") + " %");
                                soil4.setText("" + object.getString("soil4") + " %");
                                wind.setText("" + object.getString("windSpeed") + " Km/h º");
                                wind2.setText("" + object.getString("windDirection") + "");
                                uv.setText(""+ object.getString("uv") + " %");
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
                (ViewGroup)  screen.findViewById(R.id.custom_toast_layout_id));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(error);

        Toast toast = new Toast(screen.getContext().getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showToastS(String error){
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_toast_success,
                (ViewGroup) screen.findViewById(R.id.custom_toast_layout_id));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(error);

        Toast toast = new Toast(screen.getContext().getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }


    private void showStats(){
        try{
            RequestQueue queue = Volley.newRequestQueue(screen.getContext());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"dashboard/diseases",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);
                                ConstraintLayout stats = screen.findViewById(R.id.stats);
                                JSONArray array = object.getJSONArray("data");

                                TextView aux = null;
                                ProgressBar auxProg = null;

                                AnyChartView anyChartView = screen.findViewById(R.id.any_chart_view);

                                Pie pie = AnyChart.pie();
                                List<DataEntry> data = new ArrayList<>();
                                for (int i = 0; i < array.length(); i++ ){
                                    JSONObject o = array.getJSONObject(i);
                                    data.add(new ValueDataEntry(o.getString("_id"), o.getInt("count")));
                                }

                                pie.data(data);
                                pie.title(screen.getResources().getString(R.string.dashboard_graph_title));
                                pie.labels().position("outside");
                                pie.legend().title().enabled(true);
                                pie.legend().title()
                                        .text("")
                                        .padding(0d, 0d, 10d, 0d);
                                pie.legend().title().enabled(false);

                                pie.legend()
                                        .position("center-bottom")
                                        .itemsLayout(LegendLayout.HORIZONTAL)
                                        .align(Align.CENTER);
                                anyChartView.setChart(pie);
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


}