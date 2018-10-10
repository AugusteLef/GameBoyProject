package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * 
 * @author Auguste Lefevre (269821) et Marc Watine (269508)
 * 
 *         Classe qui représente le contrôleur de la mémoire morte de démarrage
 *
 */
public final class BootRomController implements Component {

    private final Cartridge c;
    private final Rom bootRom;
    private boolean boot;

    /**
     * Constructeur qui construit un contrôleur de mémoire de démarrage auquel
     * la cartouche donnée est attachée ; lève l'exception NullPointerException
     * si cette cartouche est nulle
     * 
     * @param cartridge
     *            la cartouche que ce controleur va gérer
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.c = cartridge;
        bootRom = new Rom(BootRom.DATA);
        boot = true;
    }

   
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (boot && address < AddressMap.BOOT_ROM_END) {
            Preconditions.checkBits16(address - AddressMap.BOOT_ROM_START);
            return bootRom.read(address - AddressMap.BOOT_ROM_START);
        } else {
            return c.read(address);
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_BOOT_ROM_DISABLE && boot) {
            boot = false;
        }
        c.write(address, data);
    }

}