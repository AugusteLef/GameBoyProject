package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Représente un composant contrôlant l'accès à une mémoire vive
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class RamController implements Component {
    private final Ram ram;
    private final int startAddress;
    private final int endAddress;

    /**
     * construit un contrôleur pour la mémoire vive donnée, accessible entre
     * l'adresse startAddress (inclue) et endAddress (exclue) ; lève l'exception
     * NullPointerException si la mémoire donnée est nulle et l'exception
     * IllegalArgumentException si l'une des deux adresses n'est pas une valeur
     * 16 bits, ou si l'intervalle qu'elles décrivent a une taille négative ou
     * supérieure à celle de la mémoire,
     * 
     * @param ram
     *            la ram qu'on veut controller
     * @param startAddress
     *            l'adresse de depart donnant le début de l'endroit ou on
     *            controle la ram
     * @param endAddress
     *            l'adresse de fin donnant l'endroit a partir duquel on ne
     *            controle plus la ram
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Objects.requireNonNull(ram);
        Preconditions.checkArgument((startAddress <= endAddress)
                && ((endAddress - startAddress) <= ram.size()));
        this.ram = ram;
        this.startAddress = startAddress;
        this.endAddress = endAddress;

    }

    /**
     * appelle le premier constructeur en lui passant une adresse de fin telle
     * que la totalité de la mémoire vive soit accessible au travers du
     * contrôleur
     * 
     * @param ram
     *            la ram qu'on veut controler
     * @param startAddress
     *            l'adresse de depart donnant le début de l'endroit ou on
     *            controle la ram
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, (startAddress + ram.size()));
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < startAddress || address >= endAddress) {
            return Component.NO_DATA;
        } else {
            return this.ram.read(address - startAddress);
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address < startAddress || address >= endAddress) {
            return;
        } else {
            this.ram.write(address - startAddress, data);
        }

    }

}
