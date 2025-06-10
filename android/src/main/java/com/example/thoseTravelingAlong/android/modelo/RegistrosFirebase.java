package com.example.thoseTravelingAlong.android.modelo;

public class RegistrosFirebase {
    private int pasosDia;
    private int numMonedas;
    private String correo;

    public RegistrosFirebase() {
    }

    public RegistrosFirebase(String correo, int numMonedas, int pasosDia) {
        this.correo = correo;
        this.numMonedas = numMonedas;
        this.pasosDia = pasosDia;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getNumMonedas() {
        return numMonedas;
    }

    public void setNumMonedas(int numMonedas) {
        this.numMonedas = numMonedas;
    }

    public int getPasosDia() {
        return pasosDia;
    }

    public void setPasosDia(int pasosDia) {
        this.pasosDia = pasosDia;
    }
}
