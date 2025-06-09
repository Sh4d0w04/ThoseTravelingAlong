package com.example.thoseTravelingAlong.android.bbdd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ConexionSQLite extends SQLiteOpenHelper {
    static final String BD_NAME = "bdThoseTravelingAlong.db";
    static final int VERSION = 1;

    static final String TBL_JUGADOR = "CREATE TABLE IF NOT EXISTS jugador (correo_electronico VARHCAR(20) PRIMARY KEY,nom_usuario VARCHAR(12) UNIQUE, password VARCHAR(12), fecha_nacimiento DATE)";
    static final String TBL_PASOS_ANDADOS_POR_JUGADOR = "CREATE TABLE IF NOT EXISTS pasos_andados_por_jugador (correo_electronico VARHCAR(20), registro_fecha DATE, pasos_dados INTEGER, numero_monedas INTEGER,FOREIGN KEY(correo_electronico) REFERENCES jugador(correo_electronico))";
    static final String TBL_JUEGO_PESCA = "CREATE TABLE IF NOT EXISTS juego_pesca (correo_electronico VARHCAR(20), registro_fecha DATE, num_peces_capturados INTEGER,FOREIGN KEY(correo_electronico) REFERENCES jugador(correo_electronico))";
    public ConexionSQLite(@Nullable Context context) {
        super(context, BD_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        sqlDB.execSQL(TBL_JUGADOR);
        sqlDB.execSQL(TBL_PASOS_ANDADOS_POR_JUGADOR);
        sqlDB.execSQL(TBL_JUEGO_PESCA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
