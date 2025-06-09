package com.example.thoseTravelingAlong.android.bbdd;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;

import com.example.thoseTravelingAlong.android.modelo.Jugador;

import java.util.Date;

public class Modelo {

    //Hacer un writeable y un readeable
    public SQLiteDatabase getConnW(Context context) {
        ConexionSQLite conn = new ConexionSQLite(context);
        SQLiteDatabase db = conn.getWritableDatabase();
        return db;
    }

    public SQLiteDatabase getConnR(Context context) {
        ConexionSQLite conn = new ConexionSQLite(context);
        SQLiteDatabase db = conn.getReadableDatabase();
        return db;
    }

    public int insertarJugador(Context context, Jugador jugador) {
        int res = 0;
        String sql = "INSERT INTO jugador (nom_usuario, password, correo_electronico, fecha_nacimiento) VALUES ('" + jugador.getNombreUsuario()
                +"', '"+ jugador.getPassword() + "', '" + jugador.getCorreoElectronico() + "', '" + jugador.getFechaNacimiento() + "');";

        SQLiteDatabase db = this.getConnW(context);
        try{
            db.execSQL(sql);
        }catch (Exception e){
            res = 1;
        }
        return res;
    }



    public int buscarJugadorEnBBDDPorNombre(Context context, Jugador jugador){
        int res = 0;
        //String sql = "SELECT (nom_usuario) FROM jugador WHERE nom_usuario = '" + jugador.getNombreUsuario() + "';";
        SQLiteDatabase db = this.getConnR(context);
        int numeroDeColumnas = 0;
        Cursor cursor = null;
        try{
            //db.execSQL(sql);
            String[] tablas = new String[]{"nom_usuario","password"};
            String selection = "nom_usuario = ? AND password = ?";
            String[] argSelection = {jugador.getNombreUsuario(),jugador.getPassword()};

            cursor = db.query("jugador",tablas,selection,argSelection,null,null,null);
            numeroDeColumnas = cursor.getCount();
            if(numeroDeColumnas != 1 || cursor == null){
                res = 1;
                return res;
            }
            cursor.moveToNext();
            String password = cursor.getString(1);
            Log.w("Contraseña del user = ", password);
            if (!password.equals(jugador.getPassword())){
                res = 2;
                return res;
            }
        }catch (Exception e){
            //Toast.makeText(context, "Ha ocurrido un error a la hora de insertar el jugador", Toast.LENGTH_SHORT).show();
            res = 6;
            return res;
        }
        cursor.close();
        return res;
    }
    public int buscarJugadorEnBBDDPorCorreo(Context context, Jugador jugador){
        int res = 0;
        //String sql = "SELECT (correo_electronico) FROM jugador WHERE nom_usuario = '" + jugador.getCorreoElectronico() + "';";
        SQLiteDatabase db = this.getConnR(context);
        int numeroDeColumnas = 0;
        Cursor cursor = null;
        try{
            //db.execSQL(sql);
            String[] tablas = new String[]{"correo_electronico", "password"};
            String selection = "correo_electronico = ? AND password = ?";
            String[] argSelection = {jugador.getNombreUsuario(),jugador.getPassword()};

            cursor = db.query("jugador",tablas,selection,argSelection,null,null,null);
            numeroDeColumnas = cursor.getCount();
            if(numeroDeColumnas != 1 || cursor == null){
                res = 1;
                return res;
            }
            cursor.moveToNext();
            String password = cursor.getString(1);
            Log.w("Contraseña del user = ", password);
            if (!password.equals(jugador.getPassword())){
                res = 2;
                return res;
            }
            numeroDeColumnas = cursor.getCount();
        }catch (Exception e){
            //Toast.makeText(context, "Ha ocurrido un error a la hora de insertar el jugador", Toast.LENGTH_SHORT).show();
            res = 6;
            return res;
        }
        cursor.close();
        return res;
    }
    public String conseguirCorreoUser(Context context,String nomUsuario){
        String res = "";
        SQLiteDatabase db = this.getConnR(context);
        Cursor cursor = null;
        try {
            //db.execSQL(sql);
            String[] tablas = new String[]{"correo_electronico"};
            String selection = "nom_usuario = ?";
            String[] argSelection = {nomUsuario};

            cursor = db.query("jugador", tablas, selection, argSelection, null, null, null);
            cursor.moveToNext();
            res = cursor.getString(0);
        }catch (Exception e){
            res = "error";
        }
        return res;
    }
    public int guardarDatosUsuariosFinDelDia(Context context, String correoElc, Date fechaRegistro, int pasosDados, int numMonedas){
        int res = 0;
        SQLiteDatabase db = this.getConnW(context);
        String sql = "INSERT INTO pasos_andados_por_jugador (correo_electronico, registro_fecha, pasos_dados, numero_monedas) VALUES ('" + correoElc
            + "', '" + fechaRegistro + "', '" + pasosDados + "', '" + numMonedas + "');";
        Log.e("Prueba bbdd", sql);
        try{
            db.execSQL(sql);
        }catch (Exception e){
            res = 1;
        }
        return res;
    }

    public int guardarDatosJuegoPescaFinDelDia(Context context, String correoElc, Date fechaRegistro, int pecesCapturados){
        int res = 0;
        SQLiteDatabase db = this.getConnW(context);
        String sql = "INSERT INTO juego_pesca (correo_electronico, registro_fecha, num_peces_capturados) VALUES ('" + correoElc
            + "', '" + fechaRegistro + "', '" + pecesCapturados + "');";
        Log.e("Prueba bbdd", sql);
        try{
            db.execSQL(sql);
        }catch (Exception e){
            res = 1;
        }
        return res;
    }

    public Object[] cargarInformacionJugador(Context context, String correoUser, Date fecha) {
        Object[] res = new Object[0];
        SQLiteDatabase db = this.getConnR(context);
        Cursor cursor = null;
        try {
            //db.execSQL(sql);
            String[] tablas = new String[]{"pasos_dados", "numero_monedas"};
            String selection = "correo_electronico = ? AND registro_fecha = ?";
            String[] argSelection = {correoUser, fecha.toString()};

            cursor = db.query("pasos_andados_por_jugador", tablas, selection, argSelection, null, null, null);
            cursor.moveToNext();
            int pasosDados = cursor.getInt(0);
            int numeroMonedas = cursor.getInt(1);
            res = new Object[]{pasosDados,numeroMonedas};
        }catch (Exception e){
            res = new Object[]{-1};
        }


        return res;
    }

    public int cargarInformacionJugadorJuegoPesca(Context context, String correoUser, Date fecha) {
        int res = 0;
        SQLiteDatabase db = this.getConnR(context);
        Cursor cursor = null;
        try {
            //db.execSQL(sql);
            String[] tablas = new String[]{"num_peces_capturados"};
            String selection = "correo_electronico = ? AND registro_fecha = ?";
            String[] argSelection = {correoUser, fecha.toString()};

            cursor = db.query("juego_pesca", tablas, selection, argSelection, null, null, null);
            cursor.moveToNext();
            int numeroPeces = cursor.getInt(0);
            res = numeroPeces;
        }catch (Exception e){
            res = -1;
        }
        return res;
    }

    public String hayRegistroUser(Context context,String nomUsuario, Date fecha){
        String res = "N";
        SQLiteDatabase db = this.getConnR(context);
        Cursor cursor = null;
        try {
            //db.execSQL(sql);
            String[] tablas = new String[]{"correo_electronico"};
            String selection = "correo_electronico = ? AND registro_fecha = ?";
            String[] argSelection = {nomUsuario, fecha.toString()};

            cursor = db.query("pasos_andados_por_jugador", tablas, selection, argSelection, null, null, null);
            res = cursor.moveToNext() ? "S" : "N";
        }catch (Exception e){
            res = "error";
        }
        return res;
    }

    public String hayRegistroPesca(Context context,String nomUsuario, Date fecha){
        String res = "N";
        SQLiteDatabase db = this.getConnR(context);
        Cursor cursor = null;
        try {
            //db.execSQL(sql);
            String[] tablas = new String[]{"correo_electronico"};
            String selection = "correo_electronico = ? AND registro_fecha = ?";
            String[] argSelection = {nomUsuario, fecha.toString()};

            cursor = db.query("juego_pesca", tablas, selection, argSelection, null, null, null);
            res = cursor.moveToNext() ? "S" : "N";
        }catch (Exception e){
            res = "error";
        }
        return res;
    }

    public int updateInfoJugador(Context context, String correoElectronico, int pasosDados, int numMonedas, Date fecha){
        int res = 0;
        SQLiteDatabase db = this.getConnW(context);
        String sql = "UPDATE pasos_andados_por_jugador SET pasos_dados = '" + pasosDados +"', numero_monedas = '" + numMonedas
            +"' WHERE correo_electronico = '" + correoElectronico + "' AND registro_fecha = '"+ fecha + "';";
        try{
            db.execSQL(sql);
        }catch (Exception e){
            res = 1;
        }
        return res;
    }

    public int updateInfoPesca(Context context, String correoElectronico, int numPeces, Date fecha){
        int res = 0;
        SQLiteDatabase db = this.getConnW(context);
        String sql = "UPDATE juego_pesca SET num_peces_capturados = '" + numPeces +"' WHERE correo_electronico = '" + correoElectronico + "' AND registro_fecha = '"+ fecha + "';";
        try{
            db.execSQL(sql);
        }catch (Exception e){
            res = 1;
        }
        return res;
    }
}
