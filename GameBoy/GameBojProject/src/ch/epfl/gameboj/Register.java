package ch.epfl.gameboj;

/**
 * Interface qui a pour but d'être implémentée par les types énumérés
 * représentant les registres d'un même banc
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 *
 */
public interface Register {

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

}
