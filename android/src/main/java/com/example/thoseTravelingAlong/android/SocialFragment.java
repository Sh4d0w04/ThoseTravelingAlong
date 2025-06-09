package com.example.thoseTravelingAlong.android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.adaptador.AdaptadorComp;
import com.example.thoseTravelingAlong.android.bbdd.CompeticionFirebase;
import com.example.thoseTravelingAlong.android.modelo.Jugador;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SocialFragment extends Fragment {
    private TextView tvNombreUsuario, tvNumMonedas;
    private ListView lvListaComp;
    private SharedPreferences infoUser;
    private List<String> listaCorreos;
    private final String URL_APP = "https://those-traveling-along-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<CompeticionFirebase> listaCompeticiones;
    private List<Jugador> jugadores;
    private Button btCrearCom, btCancelar, btnIniciarCom;
    private TextInputLayout etCorreo1, etCorreo2;
    private Dialog dialogoCrearComp;
    private ValueEventListener listener;
    private AdaptadorComp adaptadorComp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        hideSystemUI();
        return inflater.inflate(R.layout.social_fragment, container, false);
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
        database = FirebaseDatabase.getInstance(URL_APP);
        databaseReference = database.getReference("competiciones");
        hideSystemUI();
        leerCompeticionFireBase();
        crearEInstanciarDialogoCrearComp();
        jugadores = new ArrayList<>();
        listaCorreos = new ArrayList<>();
        infoUser = requireContext().getSharedPreferences("Info_User", Context.MODE_PRIVATE);
        tvNombreUsuario = view.findViewById(R.id.tvNombreUserSocial);
        tvNumMonedas = view.findViewById(R.id.tvMonedasGanadasSF);
        lvListaComp = view.findViewById(R.id.lvComp);

        adaptadorComp = new AdaptadorComp(getContext(),R.layout.listado_competiciones_persona,jugadores);
        lvListaComp.setAdapter(adaptadorComp);
        btnIniciarCom = view.findViewById(R.id.btnIniciarNuevComp);

        tvNombreUsuario.setText(infoUser.getString("nombre_de_usuario", "Nombre"));
        tvNumMonedas.setText(Integer.toString(infoUser.getInt("numero_de_monedas", 0)));

        btnIniciarCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoCrearComp.show();
            }
        });


        super.onViewCreated(view, savedInstanceState);
    }

    private void guardarCompeticionFireBase(List<String> listaCorreos) {
        DatabaseReference nuevaDatabaseReference = databaseReference.push();
        CompeticionFirebase competicionFirebase = new CompeticionFirebase(listaCorreos, generarDate7Dias(new Date()));
        nuevaDatabaseReference.setValue(competicionFirebase);
        Log.e("Prueba Firebase", "si");
    }

    private void leerCompeticionFireBase(){
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCompeticiones = new ArrayList<>();
                if(snapshot.exists()){
                    for (DataSnapshot competicionSnapshot : snapshot.getChildren()){
                        String numComp = competicionSnapshot.getKey();
                        CompeticionFirebase competicionFirebase = competicionSnapshot.getValue(CompeticionFirebase.class);
                        if(competicionFirebase != null){
                            competicionFirebase.setNumComp(numComp);
                            listaCompeticiones.add(competicionFirebase);
                        }
                    }
                    String correoUser = infoUser.getString("Nom_User","");
                    for(CompeticionFirebase competicionFirebaseList: listaCompeticiones){
                        String email1 = competicionFirebaseList.getEmail1();
                        String email2 = competicionFirebaseList.getEmail2();
                        String email3 = competicionFirebaseList.getEmail3();

                        if(correoUser.equals(email1) || correoUser.equals(email2) || correoUser.equals(email3)){
                            anadirJugador(email1,competicionFirebaseList.getNumComp());
                            anadirJugador(email2,competicionFirebaseList.getNumComp());
                            anadirJugador(email3,competicionFirebaseList.getNumComp());
                        }
                    }
                    adaptadorComp.notifyDataSetChanged();
                }else{
                    Log.e("FireBase","no hay tablas");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FireBase","Error al leer todas las competiciones" + error.getMessage());
            }
        };
        databaseReference.addValueEventListener(listener);

    }



    private void crearEInstanciarDialogoCrearComp() {
        dialogoCrearComp = new Dialog(getContext());
        dialogoCrearComp.setContentView(R.layout.crear_competicion);
        dialogoCrearComp.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogoCrearComp.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.custom_dialog_box));
        dialogoCrearComp.setCancelable(false);

        btCrearCom = dialogoCrearComp.findViewById(R.id.btnCrearComp);
        btCancelar = dialogoCrearComp.findViewById(R.id.btnCancelarComp);
        etCorreo1 = dialogoCrearComp.findViewById(R.id.edTxtCorreoJ1);
        etCorreo2 = dialogoCrearComp.findViewById(R.id.edTxtCorreoJ2);

        btCrearCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listaCorreos = new ArrayList<>();
                String correo1 = "";
                String correo2 = "";
                String correo3 = infoUser.getString("Nom_User", "");
                if (!etCorreo1.getEditText().getText().toString().trim().isEmpty()) {
                    correo1 = etCorreo1.getEditText().getText().toString();
                    if (correo1.equals(correo3)) {
                        Toast.makeText(getContext(), "El correo no puede ser el tuyo", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if ((correo1.contains("@gmail.com") || correo1.contains("@hotmail.com") || correo1.contains("@yahoo.es")) && correo1.length() >= 10) {
                            listaCorreos.add(correo1);
                        } else {
                            Toast.makeText(getContext(), "El correo introducido no es valido", Toast.LENGTH_SHORT).show();
                        }

                    }
                    if (!etCorreo2.getEditText().getText().toString().trim().isEmpty()) {
                        correo2 = etCorreo2.getEditText().getText().toString();
                        if (correo2.equals(correo3) || correo2.equals(correo1)) {
                            Toast.makeText(getContext(), "El correo no puede ser el tuyo o el ya escrito", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if ((correo2.contains("@gmail.com") || correo2.contains("@hotmail.com") || correo2.contains("@yahoo.es")) && correo2.length() >= 10) {
                                listaCorreos.add(correo2);
                            } else {
                                Toast.makeText(getContext(), "El correo introducido no es valido", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    listaCorreos.add(correo3);
                    guardarCompeticionFireBase(listaCorreos);
                } else {
                    Toast.makeText(getContext(), "Tiene que introducir el primer correo", Toast.LENGTH_SHORT).show();
                }
                dialogoCrearComp.dismiss();
            }
        });

        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCorreo1.getEditText().setText("");
                etCorreo2.getEditText().setText("");
                dialogoCrearComp.dismiss();
            }
        });
    }

    private Date generarDate7Dias(Date date) {
        Date fecha = date;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 7);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        fecha = cal.getTime();
        return fecha;
    }

    private void anadirJugador(String correo, String numComp){
        if(correo != null){
            if(!correo.trim().isEmpty()){
                String correoUser = infoUser.getString("Nom_User","");
                if(!correo.equals(correoUser)) {
                    Jugador jugador = new Jugador(correo);
                    jugador.setNumComp(numComp);
                    jugadores.add(jugador);
                }
            }
        }
    }

    private void enviarMensajeComp(String correo, String nomUsuario){
        String asunto = "El usuario " + nomUsuario + "te invita a una competición!";
        String mensaje = "Te apetece andar?, quieres divertirte un rato capturando peces?, el usuario "
            + nomUsuario + "quiere ver quien de vosotros puede conseguir más pasos y peces!"
            +"\nA que esperas!, empieza ya a jugar a ThoseTravelingAlong!!"
            +"\nSi todavia no tienes instalada la app, puedes descargartela desde le siguiente enlace:"
            +"\n";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL,
            new String[]{correo});
        intent.putExtra(Intent.EXTRA_SUBJECT,asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Elije un cliente de correo:"));
    }
}
