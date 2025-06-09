package com.example.thoseTravelingAlong.android.bbdd;

import java.util.Date;
import java.util.List;

public class CompeticionFirebase {
    public String email1;
    public String email2;
    public String email3;
    public Date fecha;

    public String numComp;

    public CompeticionFirebase(){

    }

    public CompeticionFirebase(List<String> listaCorreos, Date fechaFinCompeticion){
        if(listaCorreos!= null){
            if (!listaCorreos.isEmpty()) this.email1 = listaCorreos.get(0);
            if (listaCorreos.size() > 1) this.email2 = listaCorreos.get(1);
            if (listaCorreos.size() > 2) this.email3 = listaCorreos.get(2);
        }
        this.fecha = fechaFinCompeticion;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getEmail3() {
        return email3;
    }

    public void setEmail3(String email3) {
        this.email3 = email3;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNumComp() {
        return numComp;
    }

    public void setNumComp(String numComp) {
        this.numComp = numComp;
    }
}
