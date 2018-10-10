package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Represente une mémoire vive
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class Ram {

    private final byte[] data;

    /**
     * Constructeur public
     * 
     * @param size
     *            la taille de la mémoire qu'on veut créer (en octet)
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Retourne la taille en octet de la mémoire vive
     * 
     * @return date.lenght , la taille du tableau représentant la mémoire vive
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
            throw new IndexOutOfBoundsException(
                    "The index is not in the bounds");
        } else {
            return Byte.toUnsignedInt(this.data[index]);
        }
    }

    /**
     * Modifie le contenu de la mémoire à l'index donné pour qu'il soit égal à
     * la valeur donnée
     * 
     * @param index
     *            L'index représentant l'endroit ou on veut modifier la donnée
     * @param value
     *            la valeur de la donnée qu'on va injecter dans la mémoire
     * 
     * @throws IndexOutOfBoundsException
     *             si l'index n'est pas valide
     */
    public void write(int index, int value) {
        Objects.requireNonNull(this.data);
        if ((index >= data.length) || (index < 0)) {
            throw new IndexOutOfBoundsException(
                    "The index is not in the bounds");
        } else {
            int valuecorrect = Preconditions.checkBits8(value);
            this.data[index] = (byte) valuecorrect;
        }
    }

}
