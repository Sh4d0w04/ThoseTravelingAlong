package com.example.thoseTravelingAlong.android;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.example.thoseTravelingAlong.R;
import com.example.thoseTravelingAlong.android.alarmManager.AlarmaMediaNoche;
import com.example.thoseTravelingAlong.android.libgdxFragmentsGames.AndroidLauncher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ControladorActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
    BottomNavigationView bottomNavigationView;
    Fragment fragmentActual;
    int indiceActual = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controlador);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainerView, new StepFragment());
        ft.commit();
        bottomNavigationView = findViewById(R.id.navigation_view);
        Log.e("prueba", "llega aqui");
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int nuevoIndice = 0;
                Fragment selectedFragment = null;
                if (item.getTitle().equals("Social")) {
                    selectedFragment = new SocialFragment();

                } else if (item.getTitle().equals("Home")) {
                    nuevoIndice = 1;
                    selectedFragment = new AndroidLauncher();
                } else if (item.getTitle().equals("Pasos")) {
                    nuevoIndice = 2;
                    selectedFragment = new StepFragment();
                }

                if(selectedFragment != null && nuevoIndice != indiceActual){
                    boolean toRigth = nuevoIndice > indiceActual;
                    cargarFragment(selectedFragment, toRigth);
                    indiceActual = nuevoIndice;
                }

                return false;
            }
        });
        Log.e("prueba", "llega aqui2");
        AlarmaMediaNoche.organizarAlarmaMediaNoche(this);

    }

    private void cargarFragment(Fragment selectedFragment, boolean toRigth) {
        fragmentActual = selectedFragment;
        int animEntrar,animSalida;
        if(toRigth){
            animEntrar = R.anim.slide_in_derecha;
            animSalida = R.anim.slide_out_izquierda;
        }else{
            animEntrar = R.anim.slide_in_izquierda;
            animSalida = R.anim.slide_out_derecha;
        }


        getSupportFragmentManager().beginTransaction().setCustomAnimations(animEntrar,animSalida).replace(R.id.fragmentContainerView,selectedFragment).commit();
    }

    @Override
    public void exit() {

    }
}
