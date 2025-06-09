package com.example.thoseTravelingAlong.fishingGameLibGDX.libGDXObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ProgressBarActor extends Actor {
    private final FishingBarActor BARRA_DE_PESCA;
    private final ShapeRenderer RENDERIZADO_DE_FORMAS;
    private final float ANCHO_BARRA = 25;
    private final float ALTO_BARRA = 580f;

    public static final float VELOCIDAD_PROGRESO = 7.5f;

    public ProgressBarActor(FishingBarActor barraDePesca) {
        this.BARRA_DE_PESCA = barraDePesca;
        this.RENDERIZADO_DE_FORMAS = new ShapeRenderer();
        setPosition(378,610);
        setSize(ANCHO_BARRA,ALTO_BARRA);
    }

    @Override
    public void act(float delta) {
        if(BARRA_DE_PESCA.isFishing()) {
            RENDERIZADO_DE_FORMAS.begin(ShapeRenderer.ShapeType.Filled);
            RENDERIZADO_DE_FORMAS.setColor(Color.DARK_GRAY);
            RENDERIZADO_DE_FORMAS.rect(getX(), getY(), getWidth(), getHeight());
            RENDERIZADO_DE_FORMAS.setColor(Color.GREEN);
            float progresoBarra = (BARRA_DE_PESCA.getProgreso() / VELOCIDAD_PROGRESO) * getHeight();
            RENDERIZADO_DE_FORMAS.rect(getX(), getY(), getWidth(), progresoBarra);
            RENDERIZADO_DE_FORMAS.end();
            super.act(delta);
        }
    }

    @Override
    public boolean remove() {
        this.RENDERIZADO_DE_FORMAS.dispose();
        return super.remove();
    }
}
