package ch.epfl.gameboj.bits;

/**
 * Interface qui a pour but d'être implémentée par les type énumérés
 * représentant un ensemble de bits
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public interface Bit {

    /**
     * Méthode implémenter par le type ENUM
     * 
     * @return l'index dans l'enumération de l'object
     */
    public abstract int ordinal();

    /**
     * retourne la même valeur que la méthode ordinal mais dont le nom est plus
     * parlant
     * 
     * @return l'index dans l'enumération de l'object
     * 
     */
    public default int index() {
        return ordinal();
    }

    /**
     * retourne le masque correspondant au bit, c à d une valeur dont seul le
     * bit de même index que celui du récepteur vaut 1
     * 
     * @return une valeur dont le seul bit (en binaire) valant 1 est celui du
     *         meme index que celui du recepteur
     */
    public default int mask() {
        int i = this.index();
        //int out = (int) Math.pow(2, i);
        int out = Bits.mask(i);
        return out;
    }

}
