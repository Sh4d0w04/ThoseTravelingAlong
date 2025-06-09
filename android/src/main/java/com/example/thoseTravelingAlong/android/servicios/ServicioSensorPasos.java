package com.example.thoseTravelingAlong.android.servicios;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.StepFragment;

public class ServicioSensorPasos extends Service implements SensorEventListener {
    Sensor stepCounterSensor;
    SensorManager sensorManager;
    SharedPreferences infoUsuario;
    private int pasosHastaMoneda = 1000;
    int offset = -1;
    int numMonedas;

    int ultimosPasosGuardados = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int pasosActuales = 0;
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            pasosActuales = (int) event.values[0];
            infoUsuario = getSharedPreferences("Info_User", Context.MODE_PRIVATE);
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
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        crearNotificacion("Información del Servicio","La aplicación esta registrando sus pasos");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        crearNotificacion("Información del Servicio","La aplicacíon ha dejado de registrar tus pasos");
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void crearNotificacion(String titulo, String mensaje){
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
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setVibrate(new long[]{100, 250, 100, 500})
            .setAutoCancel(true);
        mNotifyMgr.notify(1, mBuilder.build());
    }
}
