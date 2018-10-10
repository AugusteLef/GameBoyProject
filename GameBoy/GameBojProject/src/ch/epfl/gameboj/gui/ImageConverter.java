package ch.epfl.gameboj.gui;



import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    
    private final static int[] COLOR_MAP = new int[] {0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 };
    
    public javafx.scene.image.Image convert(LcdImage lcdImage){
        WritableImage writeImage = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        for (int i= 0; i < LcdController.LCD_HEIGHT; ++i) {
            for (int j = 0; j < LcdController.LCD_WIDTH ; ++j) {
                writeImage.getPixelWriter().setArgb(j, i, COLOR_MAP[lcdImage.get(j, i)]);
            }
        }
        return writeImage;            
    }
    
    

}
