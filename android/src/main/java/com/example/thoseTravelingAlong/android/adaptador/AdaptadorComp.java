package com.example.thoseTravelingAlong.android.adaptador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.modelo.Jugador;

import java.util.List;

public class AdaptadorComp extends ArrayAdapter<Jugador> {
    private List<Jugador> listComp;
    private Context context;

    public AdaptadorComp(@NonNull Context context, int resource, @NonNull List<Jugador> objects) {
        super(context, resource, objects);
        this.listComp = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.listado_competiciones_persona, null);
        }

        Jugador jugadorCom = listComp.get(position);
        TextView tvNombreUser = view.findViewById(R.id.tvNomUsuario);
        tvNombreUser.setText(jugadorCom.getCorreoElectronico());
        TextView tvCodigoComp = view.findViewById(R.id.tvCodigoComp);
        tvCodigoComp.setText(jugadorCom.getNumComp());
        return view;
    }
}
