package com.example.thoseTravelingAlong.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.bbdd.Modelo;
import com.example.thoseTravelingAlong.android.modelo.CompeticionFirebase;
import com.example.thoseTravelingAlong.android.modelo.Jugador;
import com.example.thoseTravelingAlong.android.modelo.RegistrosFirebase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompeticionFragment extends Fragment {
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private final String URL_APP = "https://those-traveling-along-default-rtdb.europe-west1.firebasedatabase.app";
    private ValueEventListener listener;

    private TextView tvNombreUserComp, tvNumMonedas;
    private List<RegistrosFirebase> registrosFirebase;

    private LineChart lineChart;
    private BarChart barChart;
    private List<Jugador> jugadores;
    private SharedPreferences infoUser;
    private Modelo modelo;
    private int numeroMonedasCom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        hideSystemUI();
        return inflater.inflate(R.layout.competicion_fragment, container, false);
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
        registrosFirebase = new ArrayList<>();
        jugadores = new ArrayList<>();
        numeroMonedasCom = 0;
        database = FirebaseDatabase.getInstance(URL_APP);
        databaseReference = database.getReference("registros");

        hideSystemUI();
        leerCompeticionFireBase();
        infoUser = requireContext().getSharedPreferences("Info_User", Context.MODE_PRIVATE);
        tvNombreUserComp = view.findViewById(R.id.tvNombreUserComp);
        tvNumMonedas = view.findViewById(R.id.tvMonedasGanadasComp);


        tvNombreUserComp.setText(infoUser.getString("nombre_comp", "Nombre"));
        tvNumMonedas.setText(Integer.toString(infoUser.getInt("numero_de_monedas_comp", 0)));

        lineChart = view.findViewById(R.id.lineChartComp);
        barChart = view.findViewById(R.id.barChartComp);


        super.onViewCreated(view, savedInstanceState);
    }

    private void leerCompeticionFireBase() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    registrosFirebase = new ArrayList<>();
                    jugadores = new ArrayList<>();
                    for (DataSnapshot competicionSnapshot : snapshot.getChildren()) {
                        RegistrosFirebase registroFirebase = competicionSnapshot.getValue(RegistrosFirebase.class);
                        if (registroFirebase != null) {
                            registrosFirebase.add(registroFirebase);
                        }
                    }
                    cargarInfoBarChart(registrosFirebase);
                    cargarInfoLineChart(registrosFirebase);
                    infoUser.edit().putInt("numero_de_monedas_comp", numeroMonedasCom).apply();
                    tvNumMonedas.setText(Integer.toString(numeroMonedasCom));
                } else {
                    Log.e("FireBase", "no hay tablas");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FireBase", "Error al leer todas las competiciones" + error.getMessage());
            }
        };
        databaseReference.addValueEventListener(listener);

    }

    private void cargarInfoBarChart(List<RegistrosFirebase> registrosFirebase) {
        ArrayList<BarEntry> entriesBarChart = new ArrayList<>();
        int numeroDeEntrada = 0;
        for (RegistrosFirebase rf : registrosFirebase) {
            String correoCom = infoUser.getString("nombre_comp", "Nombre");
            if (correoCom.equals(rf.getCorreo())) {
                int numeroPasos = rf.getPasosDia();
                numeroMonedasCom = rf.getNumMonedas();
                entriesBarChart.add(new BarEntry(numeroDeEntrada, numeroPasos));
                numeroDeEntrada++;
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

    private void cargarInfoLineChart(List<RegistrosFirebase> registrosFirebase) {
        int numeroDeEntrada = 0;
        List<Entry> entriesLineChart = new ArrayList<>();
        for (RegistrosFirebase rf : registrosFirebase) {
            String correoCom = infoUser.getString("nombre_comp", "Nombre");
            if (correoCom.equals(rf.getCorreo())) {
                int numeroPasos = rf.getPasosDia();
                numeroMonedasCom = rf.getNumMonedas();
                entriesLineChart.add(new Entry(numeroDeEntrada, numeroPasos));
                numeroDeEntrada++;
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


}
