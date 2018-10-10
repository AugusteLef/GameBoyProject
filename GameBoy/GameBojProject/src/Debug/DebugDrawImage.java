package Debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gameboj.component.lcd.LcdImageLine;

public final class DebugDrawImage {
    private static final int[] COLOR_MAP = new int[] {
      0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    public static void main(String[] args) throws IOException {
      String f = "sml.bin.gz";
      int w = 256, h = 256;
      LcdImage.Builder ib = new LcdImage.Builder(w, h);

      try (InputStream s =
         new GZIPInputStream(new FileInputStream(f))) {
        for (int y = 0; y < h; ++y) {
      LcdImageLine.Builder lb = new LcdImageLine.Builder(w);
      for (int x = 0; x < w / Byte.SIZE; ++x)
        lb.setBytes(x, s.read(), s.read());
      ib.setLine(y, lb.build());
        }
      }
      LcdImage li = ib.build();

      BufferedImage i =
        new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      for (int y = 0; y < h; ++y)
        for (int x = 0; x < w; ++x)
      i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
      ImageIO.write(i, "png", new File("sml.png"));
      System.out.println("done");
    }
  }

