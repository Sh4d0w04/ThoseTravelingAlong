package com.example.thoseTravelingAlong.android.workManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.InicioSesionActivity;
import com.example.thoseTravelingAlong.android.StepFragment;
import com.example.thoseTravelingAlong.android.bbdd.Modelo;
import com.example.thoseTravelingAlong.android.modelo.RegistrosFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class BBDDWorker extends Worker {
    private SharedPreferences sharedPreferences;

    private FirebaseDatabase database;
    private static final String URL = "https://those-traveling-along-default-rtdb.europe-west1.firebasedatabase.app";
    private DatabaseReference databaseReference;

    public BBDDWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        database = FirebaseDatabase.getInstance(URL);
        databaseReference = database.getReference("registros");
        Modelo modelo = new Modelo();
        sharedPreferences = getApplicationContext().getSharedPreferences("Info_User", Context.MODE_PRIVATE);
        int ultimosPasosDados = sharedPreferences.getInt("ultimos_pasos_guardados", 0);
        int offset = (sharedPreferences.getInt("offset", 0)) + ultimosPasosDados;
        int monedasGanadas = sharedPreferences.getInt("numero_de_monedas", 0);
        int numPecesCapturados = sharedPreferences.getInt("num_peces_capturados", 0);
        String usuario = sharedPreferences.getString("Nom_User", "error");
        Date fechaActual = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaActual);
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        fechaActual = cal.getTime();
        String hayRegistroJugador = modelo.hayRegistroUser(getApplicationContext(), usuario, fechaActual);
        String hayRegistroPesca = modelo.hayRegistroPesca(getApplicationContext(), usuario, fechaActual);

        if (hayRegistroJugador.equals("N") && hayRegistroPesca.equals("N")) {
            modelo.guardarDatosUsuariosFinDelDia(getApplicationContext(), usuario, fechaActual, ultimosPasosDados, monedasGanadas);
            modelo.guardarDatosJuegoPescaFinDelDia(getApplicationContext(), usuario, fechaActual, numPecesCapturados);
        } else if (hayRegistroJugador.equals("S") || hayRegistroPesca.equals("S")) {
            modelo.updateInfoJugador(getApplicationContext(), usuario, ultimosPasosDados, monedasGanadas, fechaActual);
            modelo.updateInfoPesca(getApplicationContext(), usuario, numPecesCapturados, fechaActual);
        }
        sharedPreferences.edit().putInt("offset", offset).apply();
        sharedPreferences.edit().putInt("pasos_hasta_siguiente_moneda", 1000).apply();
        sharedPreferences.edit().putInt("ultimos_pasos_guardados", 0).apply();
        sharedPreferences.edit().putInt("num_peces_capturados", 0).apply();

        guardarRegistroFireBase(usuario,ultimosPasosDados,monedasGanadas);
        crearNotificacion(ultimosPasosDados, numPecesCapturados);

        return Result.success();
    }

    private void guardarRegistroFireBase(String correoUser, int pasosDados, int numMonedas) {
        DatabaseReference nuevaDatabaseReference = databaseReference.push();
        RegistrosFirebase registrosFirebase = new RegistrosFirebase(correoUser, numMonedas,pasosDados);
        nuevaDatabaseReference.setValue(registrosFirebase);

    }

    private void crearNotificacion(int ultimosPasosDados, int numPecesCapturados) {
        NotificationCompat.Builder mBuilder;

        NotificationManager mNotifyMgr = (NotificationManager)
            getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "notify_001", NotificationManager.IMPORTANCE_DEFAULT);
            mNotifyMgr.createNotificationChannel(channel);
        }
        int icono = R.mipmap.icono_tta_round;
        Intent i = new Intent(getApplicationContext(),
            StepFragment.class);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_MUTABLE);
        mBuilder = new
            NotificationCompat.Builder(getApplicationContext(), "default")
            .setContentIntent(pendingIntent)
            .setSmallIcon(icono)
            .setContentTitle("Información actividad usuario")
            .setContentText("Hoy has andado: " + ultimosPasosDados + " pasos, y has capturado " + numPecesCapturados + " peces!")
            .setVibrate(new long[]{100, 250, 100, 500})
            .setAutoCancel(true);
        mNotifyMgr.notify(1, mBuilder.build());
    }
}
