package com.example.thoseTravelingAlong.fishingGameLibGDX.libGDXObjects;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class FishingBarActor extends Group {
    private final Image BARRA_FONDO;
    private final Image BARRA_FONDO_PROGRESO;
    private final Image BARRA_JUGADOR;
    private final Image PEZ;

    private float posicionPezY, velocidadPez;
    private float posicionJugadorY, velocidadJugador;
    private float progreso;
    private boolean isPressing = false;

    private final float ALTURA_MAX_BARRA = 110f;

    private final float ANCHO_BARRA_PESCAR = 70f;
    private final float LARGO_BARRA_PESCAR = 600f;
    private final float TAMANO_PEZ = 55f;

    private boolean isFishing = false;


    public FishingBarActor() {
        setSize(ANCHO_BARRA_PESCAR, LARGO_BARRA_PESCAR);
        setPosition(250, 600);

        BARRA_FONDO = new Image(new Texture("barra_pesca_fondo.png"));
        BARRA_FONDO.setSize(ANCHO_BARRA_PESCAR + 50, LARGO_BARRA_PESCAR);
        addActor(BARRA_FONDO);

        BARRA_FONDO_PROGRESO = new Image(new Texture("barra_fondo_progreso.png"));
        BARRA_FONDO_PROGRESO.setSize(40, LARGO_BARRA_PESCAR);
        BARRA_FONDO_PROGRESO.setX(ANCHO_BARRA_PESCAR + 50);
        addActor(BARRA_FONDO_PROGRESO);

        BARRA_JUGADOR = new Image(new Texture("barra_jugador.png"));
        BARRA_JUGADOR.setSize(ANCHO_BARRA_PESCAR, ALTURA_MAX_BARRA);
        BARRA_JUGADOR.setX(25);
        addActor(BARRA_JUGADOR);

        PEZ = new Image(new Texture("pez_barra.png"));
        PEZ.setSize(TAMANO_PEZ, TAMANO_PEZ);
        PEZ.setX(((ANCHO_BARRA_PESCAR + 40) - TAMANO_PEZ) / 2);
        addActor(PEZ);

        posicionPezY = (BARRA_JUGADOR.getHeight() - TAMANO_PEZ) / 2;
        posicionJugadorY = 0;
        progreso = 1.5f;

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float yMaxima = (LARGO_BARRA_PESCAR - 15) - ALTURA_MAX_BARRA;
        if (isPressing) {
            if (posicionJugadorY < yMaxima) {
                velocidadJugador += 350 * delta;
            } else {
                velocidadJugador -= 350 * delta;
            }
        } else {
            if (posicionJugadorY > 0 && posicionJugadorY < yMaxima) {
                velocidadJugador -= 250 * delta;
            } else if (posicionJugadorY == 0) {
                velocidadJugador = 0;
            } else if (posicionJugadorY >= yMaxima) {
                Gdx.app.log("Prueba if", "not pressing1, velocidad = " + velocidadJugador);
                velocidadJugador -= 5000 * delta;
                Gdx.app.log("Prueba if", "not pressing2, velocidad = " + velocidadJugador);
            }
        }
        posicionJugadorY += velocidadJugador * delta;
        posicionJugadorY = MathUtils.clamp(posicionJugadorY, 0, yMaxima);
        BARRA_JUGADOR.setY(posicionJugadorY);

        posicionPezY += velocidadPez * delta;
        if (MathUtils.randomBoolean(0.05f)) {
            velocidadPez = MathUtils.random(-150f, 250f);
        }
        posicionPezY = MathUtils.clamp(posicionPezY, 0, getHeight() - ALTURA_MAX_BARRA);
        PEZ.setY(posicionPezY);

        float centroDelPez = PEZ.getY() + PEZ.getHeight() / 2;
        boolean estaDentroBarra = centroDelPez > posicionJugadorY && centroDelPez < posicionJugadorY + ALTURA_MAX_BARRA;
        progreso += estaDentroBarra ? (1.25f * delta) : -delta;
        progreso = MathUtils.clamp(progreso, 0f, ProgressBarActor.VELOCIDAD_PROGRESO);
        Gdx.app.log("Prueba if", "vel final = " + velocidadJugador);
    }

    public float getProgreso() {
        return this.progreso;
    }

    public void setPressing(boolean pressing) {
        this.isPressing = pressing;
    }

    public boolean isFishing(){
        return this.isFishing;
    }

    public void resetearProgreso() {
        this.isPressing = false;
        this.posicionJugadorY = 0f;
        this.posicionPezY = (BARRA_JUGADOR.getHeight() - TAMANO_PEZ) / 2;
        this.velocidadPez = 0;
        this.velocidadJugador = 0f;
        this.progreso = 1.5f;
    }

    public void setFishing(boolean isFishing) {
        this.isFishing = isFishing;
    }
}
