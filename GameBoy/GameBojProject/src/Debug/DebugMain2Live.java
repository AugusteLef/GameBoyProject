//package Debug;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//import ch.epfl.gameboj.AddressMap;
//import ch.epfl.gameboj.GameBoy;
//import ch.epfl.gameboj.bits.Bit;
//import ch.epfl.gameboj.bits.Bits;
//import ch.epfl.gameboj.component.Component;
//import ch.epfl.gameboj.component.cartridge.Cartridge;
//import ch.epfl.gameboj.component.cpu.Cpu;
//import ch.epfl.gameboj.component.lcd.LcdController;
//import ch.epfl.gameboj.component.lcd.LcdImage;
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.embed.swing.SwingFXUtils;
//import javafx.scene.Group;
//import javafx.scene.Scene;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.HBox;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//
//public final class DebugMain2Live extends Application {
//
//    private static final String ROM_PATH = "tasmaniaStory.gb";
//
//    private static final int CYCLES_PER_ITERATION = 17_556;
//
//    private static final int[] COLOR_MAP = new int[] {
//            0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
//    };
//
//    @Override public void start(Stage stage) throws IOException, InterruptedException {
//        
//        System.out.println(stage);
//        // Create GameBoy
//        File romFile = new File(ROM_PATH);
//        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
//
//        // Create Scene
//        ImageView imageView = new ImageView();
//        imageView.setImage(getImage(gb));
//        Group root = new Group();
//        Scene scene = new Scene(root);
//        scene.setFill(Color.BLACK);
//        HBox box = new HBox();
//        box.getChildren().add(imageView);
//        root.getChildren().add(box);
//        stage.setWidth(LcdController.LCD_WIDTH);
//        stage.setHeight(LcdController.LCD_HEIGHT);
//        stage.setScene(scene);
//        stage.sizeToScene();
//        stage.show();
//
//        // Update GameBoy
//        new AnimationTimer() {
//            public void handle(long currentNanoTime) {
//                gb.runUntil(gb.cycles() + CYCLES_PER_ITERATION);
//                imageView.setImage(null);
//                imageView.setImage(getImage(gb));
//            }
//        }.start();
//    }
//
//    private static final Image getImage(GameBoy gb) {
//        LcdImage lcdImage = gb.lcdController().currentImage();
//        BufferedImage bufferedImage = new BufferedImage(lcdImage.width(),
//                lcdImage.height(), BufferedImage.TYPE_INT_RGB);
//        for (int y = 0; y < lcdImage.height(); ++y)
//            for (int x = 0; x < lcdImage.width(); ++x)
//                bufferedImage.setRGB(x, y, COLOR_MAP[lcdImage.get(x, y)]);
//        return SwingFXUtils.toFXImage(bufferedImage, null);
//    }
//
//    public static void main(String[] args) {
//        Application.launch(args);
//    }
//
//}

package Debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public final class DebugMain2Live extends Application {

    /** Configuration */

    private static final String ROM_PATH = "tasmaniaStory.gb";

    private static final float EMULATION_SPEED = 1f;

    private static final Map<KeyCode, DebugInputController.Key> KEYS = new HashMap<KeyCode, DebugInputController.Key>() {{
        put(KeyCode.RIGHT, DebugInputController.Key.RIGHT);
        put(KeyCode.LEFT, DebugInputController.Key.LEFT);
        put(KeyCode.UP, DebugInputController.Key.UP);
        put(KeyCode.DOWN, DebugInputController.Key.DOWN);
        put(KeyCode.Z, DebugInputController.Key.A);
        put(KeyCode.X, DebugInputController.Key.B);
        put(KeyCode.BACK_SPACE, DebugInputController.Key.SELECT);
        put(KeyCode.ENTER, DebugInputController.Key.START);
    }};

    private static final int CYCLES_PER_ITERATION = (int)(17_556 * EMULATION_SPEED);
    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    @Override public void start(Stage stage) throws IOException, InterruptedException {
        // Create GameBoy
        File romFile = new File(ROM_PATH);
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        // Create Scene
        ImageView imageView = new ImageView();
        imageView.setImage(getImage(gb));
        imageView.setSmooth(false);
        Group root = new Group();
        Scene scene = new Scene(root);
        imageView.fitWidthProperty().bind(scene.widthProperty());
        imageView.fitHeightProperty().bind(scene.heightProperty());
        scene.setFill(Color.BLACK);
        HBox box = new HBox();
        box.getChildren().add(imageView);
        root.getChildren().add(box);
        stage.setWidth(LcdController.LCD_WIDTH);
        stage.setHeight(LcdController.LCD_HEIGHT);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setTitle("gameboj");
        stage.show();

        // Update GameBoy
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                gb.runUntil(gb.cycles() + CYCLES_PER_ITERATION);
                imageView.setImage(null);
                imageView.setImage(getImage(gb));
            }
        }.start();

        // Set up input controller
        setupInputs(gb, scene);
    }

    private static final void setupInputs(GameBoy gb, Scene scene) {
        DebugInputController inputController = new DebugInputController(gb.cpu());
        inputController.attachTo(gb.bus());
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            DebugInputController.Key inputKey = KEYS.get(key.getCode());
            if (inputKey != null) {
                inputController.press(inputKey);
            }
        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, (key) -> {
            DebugInputController.Key inputKey = KEYS.get(key.getCode());
            if (inputKey != null) {
                inputController.release(inputKey);
            }
        });
    }

    private static final Image getImage(GameBoy gb) {
        LcdImage lcdImage = gb.lcdController().currentImage();
        BufferedImage bufferedImage = new BufferedImage(lcdImage.width(),
                lcdImage.height(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < lcdImage.height(); ++y)
            for (int x = 0; x < lcdImage.width(); ++x)
                bufferedImage.setRGB(x, y, COLOR_MAP[lcdImage.get(x, y)]);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}

class DebugInputController implements Component {

    private final Cpu cpu;

    private enum KeySelectionMode {
        BUTTON, DIRECTION
    }
    private KeySelectionMode keySelectionMode = null;

    private static final int
            SELECT_BUTTON_BIT = 5,
            SELECT_DIRECTION_BIT = 4;

    public enum Key implements Bit {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }
    private static final Key[] keys = Key.values();
    private boolean[] keyStates = new boolean[keys.length];

    public DebugInputController(Cpu cpu) {
        this.cpu = cpu;
    }

    @Override
    public int read(int address) {
        if (address != AddressMap.REG_P1 || keySelectionMode == null) {
            return Component.NO_DATA;
        }
        return getInputBits(keySelectionMode);
    }

    @Override
    public void write(int address, int data) {
        if (address != AddressMap.REG_P1) {
            return;
        }

        if (!Bits.test(data, SELECT_DIRECTION_BIT)) {
            keySelectionMode = KeySelectionMode.DIRECTION;
        }
        if (!Bits.test(data, SELECT_BUTTON_BIT)) {
            keySelectionMode = KeySelectionMode.BUTTON;
        }
    }

    public void press(Key key) {
        keyStates[key.index()] = true;
        cpu.requestInterrupt(Cpu.Interrupt.JOYPAD);
    }

    public void release(Key key) {
        keyStates[key.index()] = false;
    }

    private int getInputBits(KeySelectionMode keySelectionMode) {
        int offset = (keySelectionMode == KeySelectionMode.BUTTON) ? 4 : 0;
        int inputBits = 1;
        for (int i = 0; i < 4; ++i) {
            inputBits = Bits.set(inputBits, i, !keyStates[i + offset]);
        }
        return inputBits;
    }

}
