package com.avipe.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Profile extends Fragment {


    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private int gender;
    private String token;
    private View screen;

    private EditText name;
    private EditText email;
    private EditText password;

    private TextView errorName;
    private TextView errorEmail;
    private TextView errorPassword;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        screen = inflater.inflate(R.layout.fragment_profile, container, false);;

        name = screen.findViewById(R.id.nameProfile);
        email = screen.findViewById(R.id.email4);
        password = screen.findViewById(R.id.password4);

        errorName = screen.findViewById(R.id.nameError6);
        errorEmail = screen.findViewById(R.id.emailError5);
        errorPassword = screen.findViewById(R.id.passwordError4);

        errorName.setVisibility(View.INVISIBLE);
        errorEmail.setVisibility(View.INVISIBLE);
        errorPassword.setVisibility(View.INVISIBLE);


        ConstraintLayout masculine = screen.findViewById(R.id.genderMasculine);
        ConstraintLayout femele = screen.findViewById(R.id.genderFemele);

        gender = 1;
        masculine.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(), R.color.bg_color_purple));
        femele.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(), R.color.colorEditText));

        masculine.setOnClickListener(e -> {
            gender = 1;
            masculine.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(),R.color.bg_color_purple));
            femele.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(),R.color.colorEditText));
        });

        screen.findViewById(R.id.genderFemele).setOnClickListener(e -> {
            gender = 2;
            femele.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(),R.color.bg_color_purple));
            masculine.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(),R.color.colorEditText));
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(screen.getContext());
        token = sharedPref.getString("token", "");

        gettingData();
        screen.findViewById(R.id.update).setOnClickListener(e -> {
            errorName.setVisibility(View.INVISIBLE);
            errorEmail.setVisibility(View.INVISIBLE);
            errorPassword.setVisibility(View.INVISIBLE);



            try{
                RequestQueue queue = Volley.newRequestQueue(screen.getContext());
                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        getResources().getString(R.string.url)+"profile?token="+token,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                //TODO FRIST UPDATE THE TOKEN!!!
                                try{
                                    JSONObject o = new JSONObject(response);
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(screen.getContext());
                                    SharedPreferences.Editor edit = sharedPref.edit();
                                    edit.putString("token", o.getString("token"));
                                    edit.commit();

                                    Intent mainIntent = new Intent(MainScreen.instance, MainScreen.class);
                                    MainScreen.instance.startActivity(mainIntent);
                                    MainScreen.instance.finish();
                                }catch (Exception ex){
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

                                    System.out.println(data);

                                    JSONObject json = new JSONObject(data);
                                    JSONArray errors = json.getJSONArray("errors");

                                    if(errors.length() > 0){
                                        for (int i = 0; i < errors.length(); i++){

                                            if(errors.getJSONObject(i).getString("param").toLowerCase().contains("email")){
                                                errorEmail.setText(getResources().getString(R.string.registerscreen_msg_invalid_email));
                                                errorEmail.setVisibility(View.VISIBLE);
                                            }

                                            if(errors.getJSONObject(i).getString("param").toLowerCase().contains("emailrepeat")){
                                                errorEmail.setText(getResources().getString(R.string.registerscreen_msg_invalid_email_inuse));
                                                errorEmail.setVisibility(View.VISIBLE);
                                            }


                                            if(errors.getJSONObject(i).getString("param").toLowerCase().contains("name")) {
                                                errorName.setText(getResources().getString(R.string.registerscreen_msg_invalid_name));
                                                errorName.setVisibility(View.VISIBLE);
                                            }

                                            if(errors.getJSONObject(i).getString("param").toLowerCase().contains("password")){
                                                errorPassword.setText(getResources().getString(R.string.registerscreen_msg_invalid_password));
                                                errorPassword.setVisibility(View.VISIBLE);
                                            }
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

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email.getText().toString());
                        params.put("password", password.getText().toString());
                        params.put("name", name.getText().toString());
                        params.put("gender", gender+ "");
                        return params;
                    }

                };
                queue.add(stringRequest);
            }catch (Exception error) {
                showToast("CLIENT ERROR");
            }
        });
        return screen;
    }


    private void gettingData(){
        try{
            RequestQueue queue = Volley.newRequestQueue(screen.getContext());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"profile/?token="+token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);
                                name.setText(object.getString("name"));
                                email.setText(object.getString("email"));

                                gender = object.getInt("gender");

                                ConstraintLayout masculine = screen.findViewById(R.id.genderMasculine);
                                ConstraintLayout femele = screen.findViewById(R.id.genderFemele);


                                if(gender == 1){
                                    masculine.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(), R.color.bg_color_purple));
                                    femele.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(), R.color.colorEditText));
                                }else if(gender == 2){
                                    masculine.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(), R.color.colorEditText));
                                    femele.setBackgroundTintList(ContextCompat.getColorStateList(screen.getContext(), R.color.bg_color_purple));
                                }




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

}