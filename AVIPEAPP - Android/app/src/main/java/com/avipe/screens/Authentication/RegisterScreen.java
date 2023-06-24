package com.avipe.screens.Authentication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.avipe.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RegisterScreen extends AppCompatActivity {

    private int gender = 0;


    // Inputs
    private EditText name;
    private EditText email;
    private EditText password;

    // Labels with errors
    private TextView errorEmail;
    private TextView errorPassword;
    private TextView errorName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.bg_color_purple));

        // Inputs
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        // Labels with errors
        errorEmail = findViewById(R.id.emailError);
        errorPassword = findViewById(R.id.passwordError);
        errorName = findViewById(R.id.nameError);


        //Hide errors
        errorName.setVisibility(View.INVISIBLE);
        errorEmail.setVisibility(View.INVISIBLE);
        errorPassword.setVisibility(View.INVISIBLE);




        ConstraintLayout masculine = findViewById(R.id.genderMasculine);
        ConstraintLayout femele = findViewById(R.id.genderFemele);

         // Set Default gender
        gender = 1;
        masculine.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_color_purple));
        femele.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorEditText));

        masculine.setOnClickListener(e -> {
            gender = 1;
            masculine.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.bg_color_purple));
            femele.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.colorEditText));

        });


        findViewById(R.id.genderFemele).setOnClickListener(e -> {
            gender = 2;
            femele.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.bg_color_purple));
            masculine.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.colorEditText));

        });


        findViewById(R.id.callLoginScreen).setOnClickListener(e -> onCallLoginScreen());
        findViewById(R.id.register).setOnClickListener(e -> {

            String name = this.name.getText().toString();
            String email = this.email.getText().toString();
            String password = this.password.getText().toString();
            String gender = this.gender + "";
            createAccountUser(name, email, password, gender);
        });
    }


    private void onCallLoginScreen(){
        finish();
    }

    private void createAccountUser(String name, String email, String password, String gender){
        try{
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    getResources().getString(R.string.url)+"auth",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Button register = findViewById(R.id.register);
                            register.setClickable(false);
                            showToastS(getResources().getString(R.string.registerscreen_created));
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1500);

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
                    params.put("email", email);
                    params.put("password", password);
                    params.put("name", name);
                    params.put("gender", gender);
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