package com.example.thoseTravelingAlong.android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.bbdd.Modelo;
import com.example.thoseTravelingAlong.android.modelo.Jugador;
import com.example.thoseTravelingAlong.android.modelo.RegistrosFirebase;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InicioSesionActivity extends AppCompatActivity {
    private TextView tvIniciar, tvCrearCuenta, tvIniciarSesion;
    private Button btnIniciarSesionDialog, btnCancelarDialog;
    private Dialog dialogoInicioSesion, dialogoCrearCuenta;

    private TextInputLayout edTxtNombreUsuarioIni, edTxtContrasenaIni;
    private TextInputLayout edTxtNombreUsuarioCrear, edTxtContrasenaCrear, edTxtConfirmarContrasenaCrear, edTxtCorreo, edTxtFechaNaciminetoCrear;

    private SharedPreferences infoUsuario;
    private Modelo modelo = new Modelo();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        crearEInstanciarDialogoInicioSesion();
        crearEInstanciarDialogoCrearCuenta();

        infoUsuario = getSharedPreferences("Info_User", Context.MODE_PRIVATE);

        tvIniciar = findViewById(R.id.tvIniciar);
        tvIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoInicioSesion.show();
            }
        });

    }

    private void crearEInstanciarDialogoInicioSesion() {
        dialogoInicioSesion = new Dialog(InicioSesionActivity.this);
        dialogoInicioSesion.setContentView(R.layout.inicio_sesion);
        dialogoInicioSesion.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogoInicioSesion.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        dialogoInicioSesion.setCancelable(false);

        btnIniciarSesionDialog = dialogoInicioSesion.findViewById(R.id.btnIniciarSesion);
        btnCancelarDialog = dialogoInicioSesion.findViewById(R.id.btnCancelar);
        tvCrearCuenta = dialogoInicioSesion.findViewById(R.id.tvCrearCuenta);

        edTxtNombreUsuarioIni = dialogoInicioSesion.findViewById(R.id.edTxtNombreUsuarioIni);
        edTxtContrasenaIni = dialogoInicioSesion.findViewById(R.id.edTxtContrasenaIni);


        btnIniciarSesionDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int res = iniciarSesionEnBBDD();
                switch (res) {
                    case 0:
                        Toast.makeText(InicioSesionActivity.this, "Cuenta iniciada", Toast.LENGTH_SHORT).show();
                        dialogoInicioSesion.dismiss();
                        infoUsuario.edit().putString("nombre_de_usuario",edTxtNombreUsuarioIni.getEditText().getText().toString()).apply();
                        String correoUser = modelo.conseguirCorreoUser(InicioSesionActivity.this, edTxtNombreUsuarioIni.getEditText().getText().toString());
                        if (correoUser.equals("error")) {
                            correoUser = edTxtNombreUsuarioIni.getEditText().getText().toString();
                        }
                        String correoAntiguo = infoUsuario.getString("Nom_User", correoUser);
                        if (!correoAntiguo.equals(correoUser)) {
                            Date fechaActual = generarDate();;

                            int pasosDados = infoUsuario.getInt("ultimos_pasos_guardados", 0);
                            int numMonedas = infoUsuario.getInt("numero_de_monedas", 0);
                            int numPeces = infoUsuario.getInt("num_peces_capturados", 0);


                            String hayRegistroJugador = modelo.hayRegistroUser(InicioSesionActivity.this, correoAntiguo, fechaActual);
                            String hayRegistroPesca = modelo.hayRegistroPesca(InicioSesionActivity.this, correoAntiguo, fechaActual);
                            Object[] infoJugador = {pasosDados, numMonedas};
                            int numeroPeces = numPeces;
                            if (hayRegistroJugador.equals("S") || hayRegistroPesca.equals("S")) {
                                infoJugador = modelo.cargarInformacionJugador(InicioSesionActivity.this, correoUser, fechaActual);
                                numeroPeces = modelo.cargarInformacionJugadorJuegoPesca(InicioSesionActivity.this, correoUser, fechaActual);

                                int res2 =modelo.updateInfoJugador(InicioSesionActivity.this, correoAntiguo, pasosDados, numMonedas, fechaActual);
                                int res3 =modelo.updateInfoPesca(InicioSesionActivity.this, correoAntiguo, numPeces, fechaActual);
                            } else if (hayRegistroJugador.equals("N") || hayRegistroPesca.equals("N")) {
                                int resUsr = modelo.guardarDatosUsuariosFinDelDia(InicioSesionActivity.this,correoAntiguo,fechaActual,pasosDados,numMonedas);
                                if(resUsr == 0){
                                    int resPez = modelo.guardarDatosJuegoPescaFinDelDia(InicioSesionActivity.this,correoAntiguo,fechaActual,numPeces);
                                    if(resPez != 0)
                                        Toast.makeText(InicioSesionActivity.this, "Error al guardar en la tabla peces", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(InicioSesionActivity.this, "Error al guardar en la tabla user", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if ((int) infoJugador[0] != -1 && numeroPeces != -1) {
                                infoUsuario.edit().putInt("ultimos_pasos_guardados", (int) infoJugador[0]).apply();
                                infoUsuario.edit().putInt("numero_de_monedas", (int) infoJugador[1]).apply();
                                infoUsuario.edit().putInt("num_peces_capturados", numeroPeces).apply();
                            } else {
                                if ((int) infoJugador[0] == -1) {
                                    Toast.makeText(InicioSesionActivity.this, "Error al cargar los datos antiguos User", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(InicioSesionActivity.this, "Error al cargar los datos antiguos Peces", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        infoUsuario.edit().putString("Nom_User", correoUser).apply();
                        startActivity(new Intent(InicioSesionActivity.this, ControladorActivity.class));
                        break;
                    case 1:
                        Toast.makeText(InicioSesionActivity.this, "El Nombre de Usuario o Correo Electronico no se ha encontrado", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(InicioSesionActivity.this, "La contraseña introducida no es correcta", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(InicioSesionActivity.this, "Rellene el campo \"Nombre de Usuario\"", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(InicioSesionActivity.this, "Rellene el campo \"Contraseña\"", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(InicioSesionActivity.this, "Error en la BBDD", Toast.LENGTH_SHORT).show();
                }
                borrarDatosIniciarSesion();
            }
        });

        btnCancelarDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarDatosIniciarSesion();
                dialogoInicioSesion.dismiss();
                Toast.makeText(InicioSesionActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        tvCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarDatosIniciarSesion();
                dialogoInicioSesion.dismiss();
                dialogoCrearCuenta.show();
            }
        });
    }

    private void crearEInstanciarDialogoCrearCuenta() {
        dialogoCrearCuenta = new Dialog(InicioSesionActivity.this);
        dialogoCrearCuenta.setContentView(R.layout.crear_cuenta);
        dialogoCrearCuenta.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogoCrearCuenta.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        dialogoCrearCuenta.setCancelable(false);

        btnIniciarSesionDialog = dialogoCrearCuenta.findViewById(R.id.btnIniciarSesion);
        btnCancelarDialog = dialogoCrearCuenta.findViewById(R.id.btnCancelar);
        tvIniciarSesion = dialogoCrearCuenta.findViewById(R.id.tvIniciarSesion);

        edTxtNombreUsuarioCrear = dialogoCrearCuenta.findViewById(R.id.edTxtNombreUsuario);
        edTxtContrasenaCrear = dialogoCrearCuenta.findViewById(R.id.edTxtContrasena);
        edTxtConfirmarContrasenaCrear = dialogoCrearCuenta.findViewById(R.id.edTxtConfirmarContrasena);
        edTxtCorreo = dialogoCrearCuenta.findViewById(R.id.edTxtCorreo);
        edTxtFechaNaciminetoCrear = dialogoCrearCuenta.findViewById(R.id.edTxtFechaNacimiento);

        btnIniciarSesionDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int res = 0;
                res = comprobarPassword();
                if (res == 0) {
                    res = comprobarCorreoElectronico();
                    if (res == 0) {
                        res = crearJugadorNuevo();
                    }
                }
                switch (res) {
                    case 0:
                        dialogoCrearCuenta.dismiss();
                        Toast.makeText(InicioSesionActivity.this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();

                        String correoUser = edTxtCorreo.getEditText().getText().toString();
                        String correoAntiguo = infoUsuario.getString("Nom_User", correoUser);
                        if (!correoAntiguo.equals(correoUser)) {
                            Date fechaActual = generarDate();

                            int pasosDados = infoUsuario.getInt("ultimos_pasos_guardados", 0);
                            int numMonedas = infoUsuario.getInt("numero_de_monedas", 0);
                            int numPeces = infoUsuario.getInt("num_peces_capturados", 0);

                            String hayRegistroJugador = modelo.hayRegistroUser(InicioSesionActivity.this, correoAntiguo, fechaActual);
                            String hayRegistroPesca = modelo.hayRegistroPesca(InicioSesionActivity.this, correoAntiguo, fechaActual);

                            if (hayRegistroJugador.equals("N") && hayRegistroPesca.equals("N")) {
                                int res2 =modelo.guardarDatosUsuariosFinDelDia(InicioSesionActivity.this, correoAntiguo, fechaActual, pasosDados, numMonedas);
                                int res3 = modelo.guardarDatosJuegoPescaFinDelDia(InicioSesionActivity.this, correoAntiguo, fechaActual, numPeces);
                            } else if (hayRegistroJugador.equals("S") || hayRegistroPesca.equals("S")) {
                                int res4 =modelo.updateInfoJugador(InicioSesionActivity.this, correoAntiguo, pasosDados, numMonedas, fechaActual);
                                int res5 =modelo.updateInfoPesca(InicioSesionActivity.this, correoAntiguo, numPeces, fechaActual);
                            }
                        }

                        infoUsuario.edit().putString("Nom_User", edTxtCorreo.getEditText().getText().toString()).apply();
                        startActivity(new Intent(InicioSesionActivity.this, ControladorActivity.class));
                        break;
                    case 1:
                        Toast.makeText(InicioSesionActivity.this, "El Nombre de Usuario o Correo Electrónico introducido ya existe", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(InicioSesionActivity.this, "La contraseña introducida no coincide", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(InicioSesionActivity.this, "La contraseña tiene que ser minimo de 8 digitos", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(InicioSesionActivity.this, "Rellene el campo \"Nombre de Usuario\"", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(InicioSesionActivity.this, "Rellene el campo \"Contraseña\"", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(InicioSesionActivity.this, "Rellene el campo \"Correo Electrónico\"", Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(InicioSesionActivity.this, "La fecha introducida no es valida", Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(InicioSesionActivity.this, "El correo electrónico no es valido", Toast.LENGTH_SHORT).show();
                        break;
                }
                borrarDatosCrearCuenta();
            }
        });

        btnCancelarDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoCrearCuenta.dismiss();
                borrarDatosCrearCuenta();
                Toast.makeText(InicioSesionActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
        tvIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarDatosCrearCuenta();
                dialogoCrearCuenta.dismiss();
                dialogoInicioSesion.show();
            }
        });

    }

    private int iniciarSesionEnBBDD() {
        int res = 0;
        String nombreUser = edTxtNombreUsuarioIni.getEditText().getText().toString().trim();
        if (nombreUser == null || nombreUser.isEmpty()) {
            res = 3;
            return res;
        }
        String password = edTxtContrasenaIni.getEditText().getText().toString().trim();
        if (password == null || password.isEmpty()) {
            res = 4;
            return res;
        }
        Jugador jugadorIniciar = new Jugador(nombreUser, password);
        res = modelo.buscarJugadorEnBBDDPorNombre(InicioSesionActivity.this, jugadorIniciar);
        if (res != 0) {
            jugadorIniciar.setCorreoElectronico(nombreUser);
            res = modelo.buscarJugadorEnBBDDPorCorreo(InicioSesionActivity.this, jugadorIniciar);
        }
        return res;
    }

    private int crearJugadorNuevo() {
        int res = 0;
        String nombUsuario = edTxtNombreUsuarioCrear.getEditText().getText().toString();
        if (nombUsuario == null || nombUsuario.isEmpty()) {
            res = 4;
            return res;
        }

        String password = edTxtContrasenaCrear.getEditText().getText().toString();
        if (password == null || password.isEmpty()) {
            res = 5;
            return res;
        }

        String correoElectronico = edTxtCorreo.getEditText().getText().toString();
        if (correoElectronico == null || correoElectronico.isEmpty()) {
            res = 6;
            return res;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaNacimiento = null;
        try {
            fechaNacimiento = sdf.parse(edTxtFechaNaciminetoCrear.getEditText().getText().toString());
        } catch (ParseException pe) {
            res = 7;
            return res;
        }
        Jugador nuevoJugador = new Jugador(nombUsuario, password, correoElectronico, fechaNacimiento);
        Log.w("Info jugador", nuevoJugador.toString());

        res = modelo.insertarJugador(InicioSesionActivity.this, nuevoJugador);
        if(res == 0){
            Date fecha = generarDate();
            res = modelo.guardarDatosUsuariosFinDelDia(InicioSesionActivity.this,correoElectronico,fecha,0,5);
            if(res == 0) {
                modelo.guardarDatosJuegoPescaFinDelDia(InicioSesionActivity.this, correoElectronico, fecha,0);
            }
        }
        return res;
    }

    private int comprobarPassword() {
        int res = 2;
        String password = edTxtContrasenaCrear.getEditText().getText().toString();
        String confirmPassword = edTxtConfirmarContrasenaCrear.getEditText().getText().toString();

        if (password.equals(confirmPassword)) {
            if (password.length() < 8) {
                res = 3;
                return res;
            }
            res = 0;
        }
        return res;
    }

    private int comprobarCorreoElectronico() {
        int res = 8;
        String correoElectronico = edTxtCorreo.getEditText().getText().toString();
        if ((correoElectronico.contains("@gmail.com") || correoElectronico.contains("@hotmail.com") || correoElectronico.contains("@yahoo.es")) && correoElectronico.length() >= 10) {
            res = 0;
        }
        return res;
    }

    private Date generarDate(){
        Date date = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.HOUR_OF_DAY) > 0) {
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        date = cal.getTime();
        return date;
    }

    private void borrarDatosCrearCuenta(){
        edTxtNombreUsuarioCrear.getEditText().setText("");
        edTxtContrasenaCrear.getEditText().setText("");
        edTxtConfirmarContrasenaCrear.getEditText().setText("");
        edTxtCorreo.getEditText().setText("");
        edTxtFechaNaciminetoCrear.getEditText().setText("");
    }

    private void borrarDatosIniciarSesion(){
        edTxtNombreUsuarioIni.getEditText().setText("");
        edTxtContrasenaIni.getEditText().setText("");
    }
}
