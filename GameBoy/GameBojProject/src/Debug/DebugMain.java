package Debug;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public class DebugMain {

    public static void main(String[] args) throws IOException {
        File romFile = new File(args[0]);
        long cycles = Long.parseLong(args[1]);

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        Component printer = new DebugPrintComponent();
        printer.attachTo(gb.bus());
        while (gb.cycles() < cycles) {
          long nextCycles = Math.min(gb.cycles() + 17556, cycles);
          gb.runUntil(nextCycles);
          gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
        }
      }
}
