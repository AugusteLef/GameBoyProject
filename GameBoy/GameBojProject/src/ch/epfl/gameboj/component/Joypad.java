package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Joypad implements Component {

    private final Cpu cpu;

    private int P1 = 0;
    private int line0 = 0;
    private int line1 = 0;

    private static final int LENGTH = 4;

    /**
     * Enumération représentant les différents touches (public car utilisée dans dans Main)
     *
     */
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    /**
     * Enumération représentant les états des touches et de la ligne activée
     *
     */
    private enum KeyState implements Bit {
        COL0, COL1, COL2, COL3, LINE0, LINE1
    }

    /**
     * Constructeur public du Joypad
     * 
     * @param cpu
     *            le cpu qui est lié au joypad
     */
    public Joypad(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return address == AddressMap.REG_P1 ? Bits.complement8(P1) : NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_P1) {
            P1 = (Bits.complement8(data) & 0b0011_0000) | (P1 & 0b1100_1111);
            majP1();
        }
    }


    /**
     * Méthode simulant la pressions d'une touche
     * 
     * @param k
     *            la touche préssée
     */
    public void keyPressed(Key k) {

        if (k.ordinal() < LENGTH) {
            line0 = Bits.set(line0, k.ordinal(), true);
        } else {
            line1 = Bits.set(line1, k.ordinal() % LENGTH, true);
        }
        cpu.requestInterrupt(Interrupt.JOYPAD); // TODO
        majP1(); // TODO
    }

    /**
     * Méthode simulant le relachement d'une touche
     * 
     * @param k
     *            la touche rélachée
     */
    public void keyReleased(Key k) {
        if (k.ordinal() < LENGTH) {
            line0 = Bits.set(line0, k.ordinal(), false);
        } else {
            line1 = Bits.set(line1, k.ordinal() % LENGTH, false);
        }
        majP1();
    }

    /**
     * Méthode permettant de mettre a jour la registre P1 en fonction des lignes
     * activées et des touches préssées
     */
    private void majP1() {

        P1 = P1 & 0b1111_0000;

        if (Bits.test(P1, KeyState.LINE0)) {
            P1 |= line0;
        }

        if (Bits.test(P1, KeyState.LINE1)) {
            P1 |= line1;
        }

    }

}
