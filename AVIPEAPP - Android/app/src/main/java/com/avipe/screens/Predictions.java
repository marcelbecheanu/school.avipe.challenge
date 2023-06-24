package com.avipe.screens;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.avipe.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Predictions extends Fragment {


    private View screen;

    public Predictions() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        screen = inflater.inflate(R.layout.fragment_predictions, container, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(screen.getContext());
        String token = sharedPref.getString("token", "");

        try{
            RequestQueue queue = Volley.newRequestQueue(screen.getContext());
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    getResources().getString(R.string.url)+"stats?token="+token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject object = new JSONObject(response);
                                JSONArray places = object.getJSONArray("places");

                                LinearLayout view = screen.findViewById(R.id.linear);
                                for (int i = 0; i < places.length(); i++) {
                                    AnyChartView anyChartView = new AnyChartView(screen.getContext());
                                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.FILL_PARENT, screen.getResources().getDimensionPixelSize(R.dimen.button_size)*2);
                                    anyChartView.setLayoutParams(params);

                                    Cartesian cartesian = AnyChart.line();
                                    cartesian.animation(true);
                                    cartesian.padding(10d, 20d, 5d, 20d);
                                    cartesian.crosshair().enabled(true);
                                    cartesian.crosshair()
                                            .yLabel(true)
                                            .yStroke((Stroke) null, null, null, (String) null, (String) null);
                                    cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
                                    cartesian.title(places.getJSONObject(i).getString("name"));

                                    cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

                                    List<DataEntry> seriesData = new ArrayList<>();

                                    JSONArray data = places.getJSONObject(i).getJSONArray("data");
                                    for (int j = 0; j < data.length(); j++) {
                                        seriesData.add(new CustomDataEntry(data.getJSONObject(j).getString("date"), Integer.parseInt(data.getJSONObject(j).getString("temperature")), Integer.parseInt(data.getJSONObject(j).getString("humidity")), Integer.parseInt(data.getJSONObject(j).getString("uv"))));
                                    }
                                    Set set = Set.instantiate();
                                    set.data(seriesData);
                                    Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
                                    Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
                                    Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");
                                    Line series1 = cartesian.line(series1Mapping);
                                    series1.name("Temperature");
                                    series1.hovered().markers().enabled(true);
                                    series1.hovered().markers()
                                            .type(MarkerType.CIRCLE)
                                            .size(4d);
                                    series1.tooltip()
                                            .position("right")
                                            .anchor(Anchor.LEFT_CENTER)
                                            .offsetX(5d)
                                            .offsetY(5d);
                                    Line series2 = cartesian.line(series2Mapping);
                                    series2.name("Humidity");
                                    series2.hovered().markers().enabled(true);
                                    series2.hovered().markers()
                                            .type(MarkerType.CIRCLE)
                                            .size(4d);
                                    series2.tooltip()
                                            .position("right")
                                            .anchor(Anchor.LEFT_CENTER)
                                            .offsetX(5d)
                                            .offsetY(5d);
                                    Line series3 = cartesian.line(series3Mapping);
                                    series3.name("UV");
                                    series3.hovered().markers().enabled(true);
                                    series3.hovered().markers()
                                            .type(MarkerType.CIRCLE)
                                            .size(4d);
                                    series3.tooltip()
                                            .position("right")
                                            .anchor(Anchor.LEFT_CENTER)
                                            .offsetX(5d)
                                            .offsetY(5d);
                                    cartesian.legend().enabled(true);
                                    cartesian.legend().fontSize(13d);
                                    cartesian.legend().padding(0d, 0d, 10d, 0d);
                                    anyChartView.setChart(cartesian);
                                    view.addView(anyChartView);
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
        return screen;
    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2, Number value3) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
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