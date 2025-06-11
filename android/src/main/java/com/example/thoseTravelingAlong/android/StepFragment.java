package com.example.thoseTravelingAlong.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.bbdd.Modelo;
import com.example.thoseTravelingAlong.android.servicios.ServicioSensorPasos;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StepFragment extends Fragment implements SensorEventListener {
    private TextView tvStepCounter, tvKilometros, tvCalorias;
    private  int offset, numMonedas;
    private int ultimosPasosGuardados = -1;
    private int stepGoal = 5000;
    private float dinstanceKM, caloriasQuemadas;
    private Sensor stepCounterSensor;
    private SensorManager sensorManager;
    private ProgressBar progressBar;
    private SharedPreferences infoUsuario;
    ToggleButton tgServicio;
    private int pasosHastaMoneda = 1000;
    private LineChart lineChart;
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        hideSystemUI();
        return inflater.inflate(R.layout.step_fragment, container, false);
    }

    private void hideSystemUI() {
        View decorView = requireActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        hideSystemUI();
        infoUsuario = requireContext().getSharedPreferences("Info_User", Context.MODE_PRIVATE);

        tvStepCounter = view.findViewById(R.id.tvStepCounter);
        tvKilometros = view.findViewById(R.id.tvKilometrosRecorridos);
        tvCalorias = view.findViewById(R.id.tvCaloriasQuemadas);
        progressBar = view.findViewById(R.id.progressBar);
        tgServicio = view.findViewById(R.id.tbServicio);

        lineChart = view.findViewById(R.id.lineChart);
        barChart = view.findViewById(R.id.barChart);

        cargarInfoLineChart();
        cargarInfoBarChart();

        tgServicio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    getActivity().startService(new Intent(getContext(), ServicioSensorPasos.class));
                }else{
                    getActivity().stopService(new Intent(getContext(), ServicioSensorPasos.class));
                }
            }
        });

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this,stepCounterSensor,SensorManager.SENSOR_DELAY_NORMAL);
        progressBar.setMax(stepGoal);
        if (stepCounterSensor == null) {
            tvStepCounter.setText("El contador de pasos no funciona");
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Log.e("Pruebas Sensor", "esta registrado");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.e("Pruebas Sensor", "cambio de movimiento");
        int pasosActuales = 0;
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            pasosActuales = (int) event.values[0];
            int offsetSharedPreferences = infoUsuario.getInt("offset", 0);

            if (pasosActuales < offsetSharedPreferences) {
                offset = pasosActuales;
                infoUsuario.edit().putInt("offset", offset).apply();
            }else{
                if(offset != offsetSharedPreferences){
                    offset = offsetSharedPreferences;
                }
            }

            int pasosDadosHoy = pasosActuales - offset;
            ultimosPasosGuardados = infoUsuario.getInt("ultimos_pasos_guardados",0);
            if (pasosDadosHoy != ultimosPasosGuardados) {
                ultimosPasosGuardados = pasosDadosHoy;
                infoUsuario.edit().putInt("ultimos_pasos_guardados", ultimosPasosGuardados).apply();
            }

            int ultimoPasoHastaMoneda = infoUsuario.getInt("pasos_hasta_siguiente_moneda",0);
            if(pasosHastaMoneda <= ultimoPasoHastaMoneda){
                pasosHastaMoneda = ultimoPasoHastaMoneda;
            }else{
                infoUsuario.edit().putInt("pasos_hasta_siguiente_moneda",pasosHastaMoneda).apply();
            }

            if (pasosDadosHoy >= pasosHastaMoneda){
                int numMonedasGuardadas = infoUsuario.getInt("numero_de_monedas",0);
                if(numMonedas < numMonedasGuardadas){
                    numMonedas = numMonedasGuardadas;
                }
                numMonedas ++;
                pasosHastaMoneda += 1000;
                infoUsuario.edit().putInt("numero_de_monedas",numMonedas).apply();
                infoUsuario.edit().putInt("pasos_hasta_siguiente_moneda",pasosHastaMoneda).apply();
            }

            dinstanceKM = pasosDadosHoy * 0.754f / 1000;
            caloriasQuemadas = pasosDadosHoy * 0.04f;
            tvStepCounter.setText(Integer.toString(pasosDadosHoy));

            String km = String.valueOf(dinstanceKM);
            String cal = String.valueOf(caloriasQuemadas);
            tvKilometros.setText(String.format("%.2f", dinstanceKM) + " KM");
            tvCalorias.setText(String.format("%.2f",caloriasQuemadas) + " CAL");
            progressBar.setProgress(pasosDadosHoy);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void cargarInfoBarChart(){
        String correoUser = infoUsuario.getString("Nom_User","");
        ArrayList<Date> fechas = generarDate7Dias(new Date());
        Modelo modelo = new Modelo();
        ArrayList<BarEntry> entriesBarChart = new ArrayList<>();
        Object[] infoJugador = new Object[0];
        int numeroDeEntrada = 0;
        for(int i = 6; i >= 0; i--){
            infoJugador = modelo.cargarInformacionJugador(getContext(),correoUser,fechas.get(i));
            int numeroPasos = (int) infoJugador[0];
            if(numeroPasos != -1){
                entriesBarChart.add(new BarEntry(numeroDeEntrada,numeroPasos));
                numeroDeEntrada ++;
            }else{
                entriesBarChart.add(new BarEntry(numeroDeEntrada,0));
                numeroDeEntrada ++;
            }
        }
        BarDataSet barDataSet = new BarDataSet(entriesBarChart, "Pasos andados esta semana");
        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(10f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private void cargarInfoLineChart(){
        String correoUser = infoUsuario.getString("Nom_User","");
        ArrayList<Date> fechas = generarDate7Dias(new Date());
        Modelo modelo = new Modelo();
        ArrayList<Entry> entriesLineChart = new ArrayList<>();
        Object[] infoJugador = new Object[0];
        int numeroDeEntrada = 0;
        for(int i = 6; i >= 0; i--){
            infoJugador = modelo.cargarInformacionJugador(getContext(),correoUser,fechas.get(i));
            int numeroPasos = (int) infoJugador[0];
            if(numeroPasos != -1){
                entriesLineChart.add(new Entry(numeroDeEntrada,numeroPasos));
                numeroDeEntrada ++;
            }else{
                entriesLineChart.add(new Entry(numeroDeEntrada,0));
                numeroDeEntrada ++;
            }
        }
        LineDataSet lineDataSet = new LineDataSet(entriesLineChart, "Pasos andados esta semana");
        lineDataSet.setColors(ColorTemplate.LIBERTY_COLORS);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private ArrayList<Date> generarDate7Dias(Date date){
        ArrayList<Date> fechas = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i< 7; i++) {
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 1);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            date = cal.getTime();
            fechas.add(date);
        }


        return fechas;
    }
}
