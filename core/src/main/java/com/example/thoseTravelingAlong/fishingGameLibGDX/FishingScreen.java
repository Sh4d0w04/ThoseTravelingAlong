package com.example.thoseTravelingAlong.fishingGameLibGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.thoseTravelingAlong.fishingGameLibGDX.libGDXObjects.FishingBarActor;
import com.example.thoseTravelingAlong.fishingGameLibGDX.libGDXObjects.ProgressBarActor;

public class FishingScreen extends ScreenAdapter {

    private Stage escenario;
    private FishingBarActor barraDePesca;
    private ProgressBarActor progressBarActor;
    private Texture fondoPesca;

    private Skin skinDialogo;

    private boolean isFishing = false;

    private int numeroMonedas;
    private Preferences preferences;
    @Override
    public void show() {
        preferences = Gdx.app.getPreferences("Info_User");

        escenario = new Stage(new ScreenViewport());

        fondoPesca = new Texture(Gdx.files.internal("backgroud_fishing_game.png"));
        Image background = new Image(fondoPesca);
        background.setFillParent(true);
        escenario.addActor(background);

        barraDePesca = new FishingBarActor();
        barraDePesca.setVisible(false);

        progressBarActor = new ProgressBarActor(barraDePesca);
        progressBarActor.setVisible(false);

        escenario.addActor(barraDePesca);
        escenario.addActor(progressBarActor);

        GestureDetector detector = new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                numeroMonedas = preferences.getInteger("numero_de_monedas",0);
                if(numeroMonedas >= 1) {
                    if (!isFishing) {
                        isFishing = true;
                        barraDePesca.resetearProgreso();
                        barraDePesca.setFishing(true);
                        barraDePesca.setVisible(true);
                        progressBarActor.setVisible(true);
                    }else{
                        barraDePesca.setPressing(false);
                    }
                }else{
                    mostrarDialogoNecesitaMonedas();
                }
                return true;
            }
        });

        InputAdapter inputAdapter = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isFishing) barraDePesca.setPressing(true);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (isFishing) barraDePesca.setPressing(false);
                return true;
            }
        };

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(escenario);
        multiplexer.addProcessor(detector);
        multiplexer.addProcessor(inputAdapter);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        if (isFishing) {
            if (barraDePesca.getProgreso() >= ProgressBarActor.VELOCIDAD_PROGRESO) {
                mostrarDialogoResultado(true);
                isFishing = false;
                barraDePesca.setFishing(false);
                barraDePesca.setVisible(false);
                progressBarActor.setVisible(false);
                progressBarActor.act(delta);
                barraDePesca.resetearProgreso();
            } else if (barraDePesca.getProgreso() <= 0f) {
                mostrarDialogoResultado(false);
                isFishing = false;
                barraDePesca.setFishing(false);
                barraDePesca.setVisible(false);
                progressBarActor.setVisible(false);
                barraDePesca.resetearProgreso();
            }
        }
        escenario.draw();
        escenario.act(delta);
    }

    @Override
    public void dispose() {
        escenario.dispose();
        fondoPesca.dispose();
    }

    public void mostrarDialogoResultado(boolean esGanador) {
        skinDialogo = createMinimalSkin();
        String titulo = esGanador ? "Has capturado el pez!": "El pez se escapó!";

        Dialog dialogo = new Dialog(titulo, skinDialogo) {
            @Override
            protected void result(Object object) {
                Gdx.app.log("entra al boton", "si");
                barraDePesca.resetearProgreso();
                this.hide();
            }
        };
        int numPecesCapturados = preferences.getInteger("num_peces_capturados",0);
        if(esGanador){
            numPecesCapturados ++;
            preferences.putInteger("num_peces_capturados", numPecesCapturados);
        }
        numeroMonedas = preferences.getInteger("numero_de_monedas",0);
        numeroMonedas --;
        preferences.putInteger("numero_de_monedas", numeroMonedas).flush();
        dialogo.button("Continuar", true);
        dialogo.getContentTable().align(Align.center);
        dialogo.padTop(150);
        dialogo.padLeft(50);
        dialogo.padRight(50);
        dialogo.getButtonTable().pad(50);
        dialogo.show(escenario);
    }

    public void mostrarDialogoNecesitaMonedas(){
        skinDialogo = createMinimalSkin();
        Dialog dialogo = new Dialog("Monedas insuficientes", skinDialogo){
            @Override
            protected void result(Object object) {
                this.hide();
            }
        };
        dialogo.button("Continuar", true);
        dialogo.getContentTable().align(Align.center);
        dialogo.padTop(150);
        dialogo.padLeft(50);
        dialogo.padRight(50);
        dialogo.getButtonTable().pad(50);
        dialogo.show(escenario);
    }

    public Skin createMinimalSkin() {
        Skin skin = new Skin();

        // Fuente básica
        BitmapFont font = new BitmapFont();
        font.getData().setScale(5f);
        skin.add("default-font", font);

        // Pixmap para textura del botón (color gris claro)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(Color.LIGHT_GRAY);
        pixmap.fill();
        skin.add("button-up", new Texture(pixmap));

        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        skin.add("button-down", new Texture(pixmap));
        pixmap.dispose();


        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("button-up");
        textButtonStyle.down = skin.newDrawable("button-down");
        textButtonStyle.font = skin.getFont("default-font");
        skin.add("default", textButtonStyle);


        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);


        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = skin.getFont("default-font");
        windowStyle.titleFontColor = Color.WHITE;

        Pixmap windowBackground = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        windowBackground.setColor(0, 0, 0, 0.8f);
        windowBackground.fill();
        skin.add("window-background", new Texture(windowBackground));
        windowBackground.dispose();

        windowStyle.background = skin.newDrawable("window-background");
        skin.add("default", windowStyle);

        return skin;
    }
}
