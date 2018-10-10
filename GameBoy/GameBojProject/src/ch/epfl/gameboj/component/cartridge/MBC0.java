package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * 
 * @author Auguste Lefevfe (269821) Marc Watine (269508) Classe qui représente
 *         un contrôleur de banque mémoire de type 0, c à d doté uniquement
 *         d'une mémoire morte de 32 768 octets
 *
 */
public final class MBC0 implements Component {

    private final Rom MBC0Rom;
    public static final int MBC0_SIZE_ROM = 0x8000;

    /**
     * Constrcuteur qui construit un contrôleur de type 0 pour la mémoire donnée ; lève
     * l'exception NullPointerException si la mémoire est nulle, et
     * IllegalArgumentException si elle ne contient pas exactement 32 768
     * octets.
     * 
     * @param rom
     *            la rom (mémoire morte) que va controler ce controleur de
     *            mémoire
     *            
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == MBC0.MBC0_SIZE_ROM);
        this.MBC0Rom = rom;

    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= MBC0.MBC0_SIZE_ROM) {
            return NO_DATA;
        } else {
            return this.MBC0Rom.read(address);
        }

    }

    @Override
    public void write(int address, int data) {
        // Does nothing because Rom is a dead memory
    }

}
