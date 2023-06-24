package com.avipe.screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.avipe.screens.presets.WarnComponent;
import com.avipe.screens.presets.addPresets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Notifications extends Fragment {

    private View screen;
    private String token;
    private Context context;

    public Notifications() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        screen = inflater.inflate(R.layout.fragment_notifications, container, false);
        context = screen.getContext();

        screen.findViewById(R.id.addPreset).setOnClickListener(e -> {
            Intent mainIntent = new Intent( screen.getContext(), addPresets.class);
            screen.getContext().startActivity(mainIntent);
            MainScreen.instance.finish();
        });


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(screen.getContext());
        token = sharedPref.getString("token", "");
        loadPresets();


        return screen;
    }

    public void loadPresets(){
        LinearLayout view = screen.findViewById(R.id.presets);
        LinearLayout warn = screen.findViewById(R.id.warns);
        view.setPadding(getResources().getDimensionPixelSize(R.dimen.row_padding), getResources().getDimensionPixelSize(R.dimen.row_padding)/2, getResources().getDimensionPixelSize(R.dimen.row_padding), getResources().getDimensionPixelSize(R.dimen.row_padding)/2);
        view.removeAllViews();
        warn.removeAllViews();
        try{
            RequestQueue queue = Volley.newRequestQueue(screen.getContext());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"notifications?token="+token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);
                                JSONArray array = object.getJSONArray("notifications");


                                for (int i = 0; i < array.length(); i++) {
                                    TextView text = new TextView(context);

                                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.FILL_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                                    params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.button_size)/30, 0, getResources().getDimensionPixelSize(R.dimen.button_size)/30);
                                    text.setLayoutParams(params);

                                    text.setPadding(getResources().getDimensionPixelSize(R.dimen.row_padding),getResources().getDimensionPixelSize(R.dimen.row_padding)/2,getResources().getDimensionPixelSize(R.dimen.row_padding),getResources().getDimensionPixelSize(R.dimen.row_padding)/2);
                                    text.setBackground(context.getDrawable(R.drawable.card_background_border));
                                    text.setText(array.getJSONObject(i).getString("placename") + " - " + array.getJSONObject(i).getString("variation") + " %");
                                    text.setTextSize(14);
                                    Typeface face = ResourcesCompat.getFont(context, R.font.poppins_bold);
                                    text.setTypeface(face);

                                    String id = array.getJSONObject(i).getString("uid");
                                    text.setOnClickListener(e -> {
                                        taskDelete(id);
                                    });
                                    view.addView(text);


                                    try{
                                        for (int j = array.getJSONObject(i).getJSONArray("data").length()-1; j >= 0; j--) {
                                             JSONObject o = array.getJSONObject(i).getJSONArray("data").getJSONObject(j);

                                            TextView text1 = new TextView(context);
                                            ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.FILL_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                                            params1.setMargins(0, getResources().getDimensionPixelSize(R.dimen.button_size)/30, 0, getResources().getDimensionPixelSize(R.dimen.button_size)/30);
                                            text1.setLayoutParams(params1);

                                            text1.setPadding(getResources().getDimensionPixelSize(R.dimen.row_padding),getResources().getDimensionPixelSize(R.dimen.row_padding)/2,getResources().getDimensionPixelSize(R.dimen.row_padding),getResources().getDimensionPixelSize(R.dimen.row_padding)/2);
                                            text1.setBackground(context.getDrawable(R.drawable.card_background_border));

                                            String[] time = o.getString("timestamp").split("\\.");
                                            text1.setText("" + time[0].replace("T", "\n"));


                                            JSONArray sensors = o.getJSONArray("data");

                                            if(sensors.length() > 0){
                                                text1.setText(text1.getText() + "\n" + "T: " + sensors.getJSONObject(0).getString("temperature") + "ºc H: " + sensors.getJSONObject(0).getString("humidity") + " %");

                                            }
                                            System.out.println(sensors.toString());
                                            String uid = o.getString("uid");

                                            text1.setTextSize(14);
                                            Typeface face1 = ResourcesCompat.getFont(context, R.font.poppins);
                                            text1.setTypeface(face1);
                                            text1.setOnClickListener(e -> {
                                                deleteWarn(uid, id);
                                            });

                                            warn.addView(text1);
                                        }
                                    }catch (Exception e){
                                        System.out.println(e.getMessage());
                                    }

                                }
                            }catch (JSONException e){
                                showToast("JSON ERROR FORMAT");
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

    private void deleteWarn(String uid, String notify){
        try{
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.DELETE,
                    getResources().getString(R.string.url)+"notifications/warn?token="+token+"&uid="+uid+"&notify="+notify,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            loadPresets();
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
                    return params;
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


    private void taskDelete(String uid){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");

        TextView text = new TextView(context);
        text.setText(context.getResources().getString(R.string.preset_remove_confirm));
        text.setPadding(60, 60, 60, 60);
        text.setTextSize(18);
        Typeface face = ResourcesCompat.getFont(context, R.font.poppins_bold);
        text.setTypeface(face);

        builder.setView(text);
        builder.setPositiveButton(context.getText(R.string.dashboard_place_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePreset(uid);
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


    private void deletePreset(String uid){
        try{
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.DELETE,
                    getResources().getString(R.string.url)+"notifications?token="+token+"&uid="+uid,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            loadPresets();
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
                    return params;
                }

            };
            queue.add(stringRequest);
        }catch (Exception error) {
            showToast("CLIENT ERROR");
        }
    }


}