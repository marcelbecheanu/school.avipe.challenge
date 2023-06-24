package com.avipe.screens.Authentication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.avipe.screens.MainScreen;
import com.avipe.screens.SliderScreen.SliderScreen;
import com.avipe.screens.SplashScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        EditText password = findViewById(R.id.password), email = findViewById(R.id.email);
        CheckBox saveAccount = findViewById(R.id.saveAccount);


        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.bg_color_purple));
        loadData(email, password, saveAccount);


        findViewById(R.id.callLoginScreen).setOnClickListener(e -> onCallRegisterScreen());

        findViewById(R.id.login).setOnClickListener(e -> {

            callLogin(email.getText().toString(), password.getText().toString(), saveAccount);

        });


    }

    private void onCallRegisterScreen(){
        Intent mainIntent = new Intent(LoginScreen.this, RegisterScreen.class);
        LoginScreen.this.startActivity(mainIntent);
    }

    public void loadData(EditText email, EditText password, CheckBox saveAccount){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginScreen.this);
        boolean saved = sharedPref.getBoolean("saved", false);
        saveAccount.setChecked(saved);
        if(saved){
            email.setText(sharedPref.getString("email",""));
            password.setText(sharedPref.getString("password",""));
        }
    }


    private void callLogin(String email, String password, CheckBox saveAccount){
        try{
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"auth?email="+email+"&password="+password,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);

                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginScreen.this);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("token", object.getString("token"));

                                editor.putBoolean("saved", saveAccount.isChecked());
                                if(saveAccount.isChecked()){
                                    editor.putString("email", email);
                                    editor.putString("password", password);
                                }else{
                                    editor.remove("email");
                                    editor.remove("password");
                                }
                                editor.commit();

                                showToastS(getResources().getString(R.string.lgoinsreen_authenticated));
                                Intent mainIntent = new Intent(LoginScreen.this, MainScreen.class);
                                LoginScreen.this.startActivity(mainIntent);
                                LoginScreen.this.finish();

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