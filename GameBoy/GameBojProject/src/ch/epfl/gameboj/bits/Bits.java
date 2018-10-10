package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Methode qui a pour seul but de contenir des méthodes utilitaires statiques.
 * Elle est donc non instanciable
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class Bits {
    
    private final static int[] table = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60,
            0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0, 0x08,
            0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58,
            0xD8, 0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24,
            0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74,
            0xF4, 0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C,
            0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42,
            0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32,
            0xB2, 0x72, 0xF2, 0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A,
            0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06,
            0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56,
            0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE, 0x2E,
            0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E,
            0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11,
            0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49,
            0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39,
            0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65,
            0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D,
            0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D,
            0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23,
            0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73,
            0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B,
            0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47,
            0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37,
            0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F,
            0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };

    /**
     * Conctructeur privé interdisant d'instancier un object Bits
     */
    private Bits() {
    };

    /**
     * retourne un entier int dont seul le bit d'index donné vaut 1, ou lève
     * IndexOutOfBoundsException si l'index est invalide, c-à-d s'il n'est pas
     * compris entre 0 (inclus) et 32 (exclus)
     * 
     * @param index
     *            l'index sont on veut que le bit 1 se situe
     * @return un entier int dont le seul bit valant 1 est celui de l'index
     */
    public static int mask(int index) {

        Objects.checkIndex(index, Integer.SIZE); // We install JDK-9 after
                                                 // coding the test upside

        if (index == 31) {
            return Integer.MIN_VALUE;
        } else {
//            int out = (int) Math.pow(2, index);
//            return out;
              int out = 1 << index;
              return out;
        }

//        int out = (int) Math.pow(2, index);
//        return out;
    }

    /**
     * Retourne vrai ssi le bit d'index donné de bits vaut 1, ou lève
     * IndexOutOfBoundsException si l'index est invalide,
     * 
     * @param bits
     *            le bits dont on va verifier si le bit d'index donnée vaut 1
     * @param index
     *            l'index auquel on va tester le bit
     * @return true si le bit d'index vaut 1, false sinon
     * 
     * @throws IndexOutOfBoundsException
     *             si l'index n'est pas valide ( < 0 ou >= 32)
     */
    public static boolean test(int bits, int index) {
        if (index < 0 || index >= Integer.SIZE) {
            throw new IndexOutOfBoundsException(
                    "The index sent to the method test from the class Bits"
                            + " is out of bounds (index must be 0 <= index < 32");
        }

        int mask1 = mask(index);

//        if ((mask1 & bits) == mask1) {
//            return true;
//        } else {
//            return false;
//        }
        
        return ((mask1 & bits) == mask1);
    }

    /**
     * se comporte comme la méthode précédente (test) mais obtient l'index à
     * tester du bit donné
     * 
     * @param bits
     *            le Bit dont on va recupérer l'index
     * @param bit
     *            le bit qu'on va tester (voir methode test précédente)
     * @return true si le bit d'index vaut 1, false sinon
     */
    public static boolean test(int bits, Bit bit) {

        int index = bit.index();
        return test(bits, index);

    }

    /**
     * retourne une valeur dont tous les bits sont égaux à ceux de bits, sauf
     * celui d'index donné, qui est égal à newValue (false correspondant à 0,
     * true à vrai)
     * 
     * @param bits
     *            le bits qu'on va retourner après avoir changer(ou pas) la
     *            valeur dite
     * @param index
     *            l'index du bit qu'on va modifier
     * @param newValue
     *            l'information permettant de savoir si on veut mettre un
     *            1(true) ou 0(false)
     * @return le bits reçu en argument après avoir modifier le bit d'index
     *         donné
     * @throws IndexOutOfBoundsException
     *             si l'index n'est pas compris entre 0(inclus) et 32 (exclus)
     */
    public static int set(int bits, int index, boolean newValue) {

        if (index < 0 || index >= Integer.SIZE) {
            throw new IndexOutOfBoundsException(
                    "The index sent to the method set from the class Bits"
                            + " is out of bounds (index must be 0 <= index < 32");
        }

        if (newValue && !test(bits, index)) {
            return bits + mask(index);
        } else {
            if (!newValue && test(bits, index)) {
                return bits - mask(index);
            }
        }

        return bits;

    }

    /**
     * retourne une valeur dont les size bits de poids faible sont égaux à ceux
     * de bits, les autres valant 0 ; lève IllegalArgumentException (!) si size
     * n'est pas compris entre 0 (inclus) et 32 (inclus !)
     * 
     * @param size
     *            la taille (le nombre) de bits qu'on veut garder du bits de
     *            départ
     * @param bits
     *            le bits qu'on va couper (cliper)
     * @return le bits de depart auquel on a gardé que les size premiers bits,
     *         le reste valant 0
     * 
     */
    public static int clip(int size, int bits) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.SIZE);
        if (size == 32) {
            return bits;
        } else {
            int out = bits & (~(-1 << size));
            return out;
        }

    }

    /**
     * retourne une valeur dont les size bits de poids faible sont égaux à ceux
     * de bits allant de l'index start (inclus) à l'index start + size
     * (exclus) ; lève IndexOutOfBoundsException si start et size ne désignent
     * pas une plage de bits valide
     * 
     * @param bits
     *            le bits dont on va extraire une partie
     * @param start
     *            l'index de départ partir duquel on veut extraire les bits du
     *            bits
     * @param size
     *            la taille de la partie qu'on veut extraire
     * @return une valeur dont les bits de poids faibles sont ceux extraits par
     *         la méthodes, les autres étant 0
     * @throws IndexOutOfBoundsException
     *             si start et size ne désignent pas une plage de bits valide
     */
    
    public static int extract(int bits, int start, int size) {
        Preconditions.checkArgument(start < Integer.SIZE);
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        int out = clip(size, (bits >> start));
        return out;

    }

    /**
     * retourne une valeur dont les size bits de poids faible sont ceux de bits
     * mais auxquels une rotation de la distance donnée a été appliquée ; si la
     * distance est positive, la rotation se fait vers la gauche, sinon elle se
     * fait vers la droite ; lève IllegalArgumentException si size n'est pas
     * compris entre 0 (exclus) et 32 (inclus), ou si la valeur donnée n'est pas
     * une valeur de size bits
     * 
     * @param size
     *            la taille (le nombre) de bit de poids faibles après rotation
     *            qu'on veut garder
     * @param bits
     *            le bits sur lequel on va appliquer la méthode
     * @param distance
     *            la "distance" de rotation qu'on veut effectuer
     * @return une valeur dont les bits de poids faibles sont ceux de bits après
     *         avoir effectuer ls rotations
     */
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(size > 0 && size <= Integer.SIZE);
        distance = (int) Math.floorMod(distance, size);
        if (size == Integer.SIZE) {
            return Integer.rotateLeft(bits, distance);
        }
        int out = clip(size, ((bits << distance) | bits >> (size - distance)));
        return out;
    }

    /**
     * « étend le signe » de la valeur 8 bits donnée, c-à-d copie le bit d'index
     * 7 dans les bits d'index 8 à 31 de la valeur retournée ; lève
     * IllegalArgumentException si la valeur donnée n'est pas une valeur de 8
     * bits.
     * 
     * @param b
     *            le bits qu'on va "étendre"
     * @return le bit auquel on a mit dans les bits d'index 8 a 31 la valeur du
     *         bit d'index 7
     */
    public static int signExtend8(int b) {
        Preconditions.checkBits8(b);
        byte reduc = (byte) b;
        int extend = (int) reduc;
        return extend;
    }

    /**
     * retourne une valeur égale à celle donnée, si ce n'est que les 8 bits de
     * poids faible ont été renversés, c-à-d que les bits d'index 0 et 7 ont été
     * échangés, de même que ceux d'index 1 et 6, 2 et 5, et 3 et 4 ; lève
     * IllegalArgumentException si la valeur donnée n'est pas une valeur de 8
     * bits,
     * 
     * @param b
     *            le bits qu'on veut renverser
     * @return le bits recu auquel on inverse les bits d'index 7-0, 6-1 etc..
     * 
     */
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        int out = table[b];
        return out;

    }

    /**
     * retourne une valeur égale à celle donnée, si ce n'est que les 8 bits de
     * poids faible ont été inversés bit à bit, c-à-d que les 0 et les 1 ont été
     * échangés ; lève IllegalArgumentException si la valeur donnée n'est pas
     * une valeur de 8 bits,
     * 
     * @param b
     *            le bits dont je veux le complément a 1
     * @return le bits du départ auquel on a inversé les bits(0 en 1, 1 en 0)
     */
    public static int complement8(int b) {
        Preconditions.checkBits8(b);
        int out = b ^ 0b11111111;
        return out;
    }

    /**
     * retourne une valeur 16 bits dont les 8 bits de poids forts sont les 8
     * bits de poids faible de highB, et dont les 8 bits de poids faible sont
     * ceux de lowB ; lève IllegalArgumentException si l'une des deux valeurs
     * données n'est pas une valeur de 8 bits.
     * 
     * @param highB
     *            les 8bits de poids fort du résultat final
     * @param lowB
     *            les 8 bits de poids faible du résultat final
     * @return une valeur de 16bits dont les 8 de poids faible sont lowB, les 8
     *         de poids fort sont ceux de hihgB
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(lowB);
        Preconditions.checkBits8(highB);
        //int out = (highB * 256) + lowB;
        int out = ((highB << 8) | lowB);
        return out;
    }

}
