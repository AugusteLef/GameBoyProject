package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Représente une mémoire morte
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class Rom {

    private final byte[] data;

    /**
     * Constructeur de la classe
     * 
     * @param data
     *            un tableau contenant des valeurs de type byte qui représente
     *            la mémoir morte
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Methode qui retourne la taille en octet de la mémoire morte
     * 
     * @return data.lenght la taille du tableau représentant la mémoire morte
     */
    public int size() {
        Objects.requireNonNull(this.data);
        return data.length;
    }

    /**
     * Methode qui retourne l'octet de la mémoire morte se trouvant a l'index
     * passe en argument
     * 
     * @param index
     *            l'index de l'octet que vont retourner (sa position dans le
     *            tableau)
     * @return l'octet a l'index passé en argument en le passant en type int
     *         grace a la methode toUnsignedInt
     * 
     * @throws IndexOutOfBoundsException
     *             si l'index n'est pas valide
     * 
     */
    public int read(int index) {
        Objects.requireNonNull(this.data);
        if ((index >= data.length) || (index < 0)) {
            throw new IndexOutOfBoundsException();
        } else {
            return Byte.toUnsignedInt(this.data[index]);
        }        
    }

}
