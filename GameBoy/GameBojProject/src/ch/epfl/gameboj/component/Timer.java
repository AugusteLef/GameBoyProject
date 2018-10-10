package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Classe qui représente le minuteur du Game Boy.
 * 
 * @author Auguste Lefevre (269821) Marc Watine (269508)
 *
 */
public final class Timer implements Component, Clocked {

    private final Cpu cpu;
    private int counterPrincipal;
    private int TIMA;
    private int TMA;
    private int TAC;
    private static final int INCREMENT_TIMA_CONDITION = 0xFF;

    /**
     * Constructeur qui construit un minuteur associé au processeur donné, ou
     * lève l'exception NullPointerException si celui-ci est nul
     * 
     * @param cpu
     *            le cpu lié au minuteur (afin de pouvoir lancé des exceptions
     *            via le cpu)
     */
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
        counterPrincipal = 0;
        this.TAC = 0;
        this.TMA = 0;
        this.TIMA = 0;
    }

    @Override
    public void cycle(long cycle) {
        boolean previousState = state();
        this.counterPrincipal = Bits.clip(Short.SIZE, counterPrincipal + 4);
        incIfChange(previousState);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV: {
            return Bits.extract(counterPrincipal, Byte.SIZE, Byte.SIZE);
        }
        case AddressMap.REG_TIMA: {
            return this.TIMA;
        }
        case AddressMap.REG_TMA: {
            return this.TMA;
        }
        case AddressMap.REG_TAC: {
            return this.TAC;
        }
        }

        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        switch (address) {
        case AddressMap.REG_DIV: {
            boolean previousState = state();
            counterPrincipal = 0;
            incIfChange(previousState);
        }
            break;
        case AddressMap.REG_TIMA: {
            this.TIMA = data;
        }
            break;
        case AddressMap.REG_TMA: {
            this.TMA = data;
        }
            break;
        case AddressMap.REG_TAC: {
            boolean previousState = state();
            this.TAC = data;
            incIfChange(previousState);
        }
            break;
        }
        return;

    }

    /**
     * Methode retournant ce que nous avons appelé l'état du minuteur, c à d la
     * conjonction logique du bit 2 du registre TAC et du bit du compteur
     * principal désigné par les 2 bits de poids faible de ce même registre
     * 
     * @return l'etat actuel du minuteur (true/false)
     */
    private boolean state() {
        boolean activation = Bits.test(TAC, 2);
        int indexBitCounterPrincipal = Integer.MAX_VALUE; // To put somethings

        switch (Bits.clip(2, TAC)) {
        case 0: {
            indexBitCounterPrincipal = 9;
        }
            break;
        case 1: {
            indexBitCounterPrincipal = 3;
        }
            break;
        case 2: {
            indexBitCounterPrincipal = 5;
        }
            break;
        case 3: {
            indexBitCounterPrincipal = 7;
        }
            break;
        }

        boolean principal = Bits.test(this.counterPrincipal,
                indexBitCounterPrincipal);

        return activation && principal;
    }

    /**
     * Methode représentant l'état précédent et incrémentant le compteur
     * secondaire si et seulement si l'état passé en argument est vrai et l'état
     * actuel (retourné par state) est faux
     * 
     * @param s0
     *            l'état du minuteur avant la modification de TAC ou du compteur
     *            principal
     */
    private void incIfChange(boolean s0) {
        boolean s1 = state();
        if (s0 && !s1) {
            if (TIMA != INCREMENT_TIMA_CONDITION) {
                TIMA += 1;
            } else {
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            }

        }
    }

}
