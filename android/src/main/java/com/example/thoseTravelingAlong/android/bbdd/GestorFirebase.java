package com.example.thoseTravelingAlong.android.bbdd;

import android.content.Context;

import com.example.thoseTravelingAlong.android.modelo.CompeticionFirebase;
import com.example.thoseTravelingAlong.android.modelo.RegistrosFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

public class GestorFirebase {
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private Modelo modelo;

    public GestorFirebase(String url, String referencia){
        database = FirebaseDatabase.getInstance(url);
        databaseReference = database.getReference(referencia);
        modelo = new Modelo();
    }

    public void guardarRegistroFireBase(String correoUser, Date fecha, Context context) {
        DatabaseReference nuevaDatabaseReference = databaseReference.push();
        Object[] infoUser = modelo.cargarInformacionJugador(context,correoUser,fecha);
        RegistrosFirebase registrosFirebase = new RegistrosFirebase(correoUser,(int) infoUser[0], (int) infoUser[1]);
        nuevaDatabaseReference.setValue(registrosFirebase);
    }

    private void guardarCompeticionFireBase(List<String> listaCorreos, Date date) {
        DatabaseReference nuevaDatabaseReference = databaseReference.push();
        CompeticionFirebase competicionFirebase = new CompeticionFirebase(listaCorreos, date);
        nuevaDatabaseReference.setValue(competicionFirebase);
    }
}
