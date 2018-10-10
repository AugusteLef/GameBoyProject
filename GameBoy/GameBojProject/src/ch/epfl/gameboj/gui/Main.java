package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public final class Main extends Application {


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        final Map<String, Joypad.Key> button = new HashMap<String, Joypad.Key>() {
            {
                put("a", Joypad.Key.A);
                put("b", Joypad.Key.B);
                put(" ", Joypad.Key.SELECT);
                put("s", Joypad.Key.START);
            }
        };

        final Map<KeyCode, Joypad.Key> direction = new HashMap<KeyCode, Joypad.Key>() {
            {
                put(KeyCode.RIGHT, Joypad.Key.RIGHT);
                put(KeyCode.LEFT, Joypad.Key.LEFT);
                put(KeyCode.UP, Joypad.Key.UP);
                put(KeyCode.DOWN, Joypad.Key.DOWN);
            }
        };

        if (getParameters().getRaw().size() != 1) {
            System.exit(1);
        } else {

            // Creation de la Gameboy et des attributs nécessaires
            String ROM_PATH = getParameters().getRaw().get(0);
            File romFile = new File(ROM_PATH);
            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
            Joypad joypad = gb.joypad();
            LcdController lcd = gb.lcdController();

            // Creation de la scène/image etc..
            ImageConverter converter = new ImageConverter();
            ImageView imageView = new ImageView();
            BorderPane borderPane = new BorderPane(imageView);
            Scene scene = new Scene(borderPane);

            imageView.setImage(converter.convert(lcd.currentImage()));
            imageView.fitWidthProperty().bind(scene.widthProperty());
            imageView.fitHeightProperty().bind(scene.heightProperty());

            // Gestion des touches améliorée
            primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
                Joypad.Key inputKey = direction.get(key.getCode());
                if (inputKey != null) {
                    joypad.keyPressed(inputKey);
                } else {
                    inputKey = button.get(key.getText());
                    if (inputKey != null) {
                        joypad.keyPressed(inputKey);
                    }
                }
            });

            primaryStage.addEventFilter(KeyEvent.KEY_RELEASED, (key) -> {
                Joypad.Key inputKey = direction.get(key.getCode());
                if (inputKey != null) {
                    joypad.keyReleased(inputKey);
                } else {
                    inputKey = button.get(key.getText());
                    if (inputKey != null) {
                        joypad.keyReleased(inputKey);
                    }
                }
            });

            // Gestion de la scène (stage)
            primaryStage.setWidth(LcdController.LCD_WIDTH * 2);
            primaryStage.setHeight(LcdController.LCD_HEIGHT * 2);
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.minWidthProperty().bind(scene.heightProperty());
            primaryStage.minHeightProperty().bind(scene.widthProperty());
            primaryStage.setTitle("GameBoy by A&M " + ROM_PATH);
            primaryStage.show();
            primaryStage.requestFocus();

            long start = System.nanoTime();
            AnimationTimer timer = new AnimationTimer() {

                @Override
                public void handle(long now) {
                    long elapsed = (long) ((now - start));
                    long cycleElapsed = (long) ((elapsed
                            * GameBoy.CYCLE_BY_NANO));
                    gb.runUntil(cycleElapsed);
                    imageView.setImage(converter.convert(lcd.currentImage()));
                }
            };

            timer.start();
        }
    }

}
