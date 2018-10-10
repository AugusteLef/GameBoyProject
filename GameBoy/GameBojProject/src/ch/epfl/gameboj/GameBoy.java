package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * 
 * 
 * Classe réprésentant la GameBoy dans son intégralité
 * @author Auguste Lefevre (269821) et Marc Watine (269508)
 *
 */
public final class GameBoy {

    private final static long CYCLE_BY_SECOND = (long) 1 << 20;
    
    // Public because used in Main
    public final static double CYCLE_BY_NANO = (float)(CYCLE_BY_SECOND) / (double)1e9  ;

    private final Bus bus;
    private final Cpu cpu;
    private final BootRomController bootRom;
    private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;
    
    private long totalCycle;

    /**
     * Constructeur de la gameboy, a ce stade (étape6) il construit un Bus, un
     * Cpu, un Timer, un lcdController, un Joypad, une BootRomController et une workRam ainsi que sa copie
     * (echoRam). Il attache les différents composants au bus.
     * 
     * @param cartridge
     *            la cartouche qui va etre lu par la gameboy
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.bus = new Bus();
        this.cpu = new Cpu();
        this.timer = new Timer(cpu);
        this.bootRom = new BootRomController(cartridge);
        this.lcdController = new LcdController(cpu);
        this.joypad = new Joypad(cpu);

        Ram workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        RamController workRamController = new RamController(workRam,
                AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        RamController echoRamController = new RamController(workRam,
                AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);

        cpu.attachTo(bus);
        bootRom.attachTo(bus);
        timer.attachTo(bus);
        lcdController.attachTo(bus);
        joypad.attachTo(bus);
        workRamController.attachTo(bus);
        echoRamController.attachTo(bus);

        totalCycle = 0;
    }

    /**
     * 
     * @return le Bus de la Gameboy
     */
    public Bus bus() {
        return this.bus;
    }

    /**
     * 
     * @return le Cpu de la Gameboy
     */
    public Cpu cpu() {
        return this.cpu;
    }

    /**
     * 
     * @return le Timer de la Gameboy
     */
    public Timer timer() {
        return this.timer;
    }
    
    /**
     * 
     * @return le LcdController de la GameBoy
     */
    public LcdController lcdController() {
        return this.lcdController;
    }
    
    /**
     * 
     * @return le Joypad de la Gameboy
     */
    public Joypad joypad() {
        return this.joypad;
    }

    /**
     * Methode qui simule le fonctionnement du GameBoy jusqu'au cycle donné
     * moins 1, ou lève l'exception IllegalArgumentException si un nombre
     * (strictement) supérieur de cycles a déjà été simulé
     * 
     * @param cycle
     *            le nomre de cycle a effectué until le nomre de cycle simulé
     *            dépasse cette valeur
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycles() <= cycle);
        
        while (totalCycle < cycle) {
            timer.cycle(totalCycle);
            lcdController.cycle(totalCycle);
            cpu.cycle(totalCycle);
            this.totalCycle += 1;
        }

    }

    /**
     * Methode qui retourne le nombre de cycle déja simulé
     * 
     * @return le nombre de cycle déja simulé
     */
    public long cycles() {
        return totalCycle;
    }

}