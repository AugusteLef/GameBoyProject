package ch.epfl.gameboj;

/**
 * Interface "Preonditions" qui contient des méthodes vérifiant les conditions
 * nécessaire sur certains argument des methodes du programme
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */

public interface Preconditions {

    /**
     * Lance IllegalArgumentException si l'argument est faux
     * 
     * @param b
     *            l'argument vérifie
     * 
     * @throws IllegalArgumentException
     *             si l'argument est faux
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Vérifie que l'entier peut s'ecrire sur 8bits
     * 
     * @param v
     *            l'entier testé
     * @return l'entier ci celui si peut s'écrire sur 8bits
     * 
     * @throws IllegalArgumentException
     *             si l'entier ne peut pas s'ecrire sur 8bits
     */
    public static int checkBits8(int v) {
//        if ((v >= 0) && (v <= 255)) {
//            return v;
//        } else {
//            throw new IllegalArgumentException("Not an 8 bits value");
//        }
        checkArgument((v>=0) && (v<=255));
        return v;
    }

    /**
     * Vérifie que l'entier peut s'ecrire sur 16bits
     * 
     * @param v
     *            l'entier testé
     * @return l'entier ci celui si peut s'écrire sur 16bits
     * 
     * @throws IllegalArgumentException
     *             si l'entier ne peut pas s'ecrire sur 16bits
     */
    public static int checkBits16(int v) {
//        if ((v >= 0) && (v <= 65535)) {
//            return v;
//        } else {
//            throw new IllegalArgumentException("Not a 16bits value");
//        }
        checkArgument((v >= 0) && (v <= 65535));
        return v;
    }

}
