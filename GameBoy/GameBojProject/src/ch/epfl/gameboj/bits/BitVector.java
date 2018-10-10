package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * 
 * Représente un vecteur de bits dont la taille est un multiple de Integer.SIZE
 * (32) et strictement positif
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class BitVector {

    private final int[] vector;

    /*
     * Enumeration qui donne les 2 types possibles d'extraction infinie,
     * ZeroExtended et Wrapped
     */
    private enum Type {
        Zero, Wrapped
    }

    // Tout les constructeurs publics passent par la méthode vectorCreator qui
    // vérifie les conditions sur la taille

    /**
     * Constructeur privé de BitVector
     * 
     * @param v
     *            le tableau utilisé pour créer le vecteur de bit
     */
    private BitVector(int[] v) {
        this.vector = v;
    }

    /**
     * Constructeur de BitVector, prend une taille (en bits) et une valeur
     * initiale (sous forme d'un booléen) et construit un vecteur de bits de la
     * taille donnée, dont tous les bits ont la valeur donnée
     * 
     * @param size
     *            la taille (qui doit etre multiple de 32)
     * @param value
     *            la valeur qu'on veut donner au bits
     */
    public BitVector(int size, boolean value) {
        this(vectorCreator(size, value));
    }

    /**
     * Constructeur de BitVector qui ne prend qu'une taille en argument et
     * initialise tous les bits à 0.
     * 
     * @param size
     *            la taille du vecteur de bit qu'on veut créer (multiple de 32)
     */
    public BitVector(int size) {
        this(size, false);
    }

    /**
     * Redéfinition de la fonction hashCode afin de l'adpater à la classe
     * BitVector
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(vector);
        return result;
    }

    /**
     * Redéfinition de la fonction equals permettant de comparer
     * structurellement des instance de type BitVector
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BitVector other = (BitVector) obj;
        if (!Arrays.equals(vector, other.vector))
            return false;
        return true;
    }

    /**
     * Redéfinition de la méthodee to String afin de pouvoir afficher bit a bit
     * les élement d'une instance de BitVector
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = this.size() - 1; i >= 0; i -= 1) {
            s.append(testBit(i) ? 1 : 0);
        }
        return s.toString();
    }

    /**
     * Methode qui retourne la taille du vecteur de bits (un multiple de 32, en
     * bits)
     * 
     * @return la taille du vecteur de bit
     */
    public int size() {
        return vector.length * Integer.SIZE;
    }

    /**
     * Methode qui determine si le bit d'index donné vaut 1 ou 0. L'index doit
     * etre compris entre 0 (inclus) et la taille du vecteur (exclus) sinon on
     * lance une IllegalArgumentException
     * 
     * @param index
     *            l'index du bit qu'on va tester
     * @return true si le bit vaut 1, false sinon
     */
    public boolean testBit(int index) {
        Preconditions.checkArgument(index >= 0 && index < this.size());
        return Bits.test(vector[vector.length - 1 - index / Integer.SIZE],
                index % Integer.SIZE);
    }

    /**
     * Methode qui renvoie le complément du vecteur de base
     * 
     * @return le complément du vecteur
     */
    public BitVector not() {
        int[] notVector = new int[this.vector.length];
        for (int i = 0; i < this.vector.length; ++i) {
            notVector[i] = ~this.vector[i];
        }

        BitVector bitvector = new BitVector(notVector);
        return bitvector;
    }

    /**
     * Methode qui calcule la conjonction bit a bit avec le vecteur de base et
     * celui recu en argument. Lance une exception si le vecetur recu n'est pas
     * de meme taille.
     * 
     * @param vectorBis
     *            le vecteur qui va nous servir pour faire la conjonction bit a
     *            bit
     * @return la resultante de la conjonction entre le vecteur de base et celui
     *         recu en argument
     */
    public BitVector and(BitVector vectorBis) {
        Objects.requireNonNull(vectorBis);
        Preconditions.checkArgument(this.size() == vectorBis.size());
        int[] v = vectorBis.vector;
        int[] andVector = new int[this.vector.length];
        for (int i = 0; i < this.vector.length; ++i) {
            andVector[i] = this.vector[i] & v[i];
        }
        BitVector bitvector = new BitVector(andVector);
        return bitvector;
    }

    /**
     * Methode qui calcule la disjonction bit a bit avec le vecteur de base et
     * celui recu en argument. Lance une exception si le vecetur recu n'est pas
     * de meme taille.
     * 
     * @param vectorBis
     *            le vecteur qui va nous servir pour faire la disjonction bit a
     *            bit
     * @return la resultante de la disjonction entre le vecteur de base et celui
     *         recu en argument
     */
    public BitVector or(BitVector vectorBis) {
        Objects.requireNonNull(vectorBis);
        Preconditions.checkArgument(this.size() == vectorBis.size());
        int[] v = vectorBis.vector;
        int[] orVector = new int[this.vector.length];
        for (int i = 0; i < this.vector.length; ++i) {
            orVector[i] = this.vector[i] | v[i];
        }
        BitVector bitvector = new BitVector(orVector);
        return bitvector;
    }

    /**
     * Methode qui effectue une extraction d'un vecteur de taille donnée
     * (multiple de 32 ou lance une excception) du vecetur de base par
     * l'extention par 0
     * 
     * @param index
     *            l'index a partir duquel on veut commencer l'extraction (index
     *            inclus)
     * @param size
     *            la taille du vecteur qu'on veut extraire
     * @return le vecteur extrait de l'index donnée et de taille donnée par
     *         rapport a l'extention par 0 du vecteur de base
     */
    public BitVector extractZeroExtended(int index, int size) {
        return extract(index, size, Type.Zero);
    }

    /**
     * Methode qui effectue une extraction d'un vecteur de taille
     * donnée(multiple de 32 ou lance une exception) du vecteur de base par
     * enroulement
     * 
     * @param index
     *            l'index a partir duquel on veut commencer l'extraction (index
     *            inclus)
     * @param size
     *            la taille du vecteur qu'on veut extraire
     * @return le vecteur extrait de l'index donnée et de taille donnée par
     *         rapport a l'extention par enroulement du vecteur de base
     */
    public BitVector extractWrapped(int index, int size) {
        return extract(index, size, Type.Wrapped);
    }

    /**
     * Methode qui décalle le vecteur d'une distance quelconque, en utilisant la
     * convention habituelle qu'une distance positive représente un décalage à
     * gauche, une distance négative un décalage à droite
     * 
     * @param distance
     *            la distance de décallage qu'on veut effectué
     * @return
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, this.size());
    }

    /*
     * 
     * 
     * 
     * METHODES PRIVEES
     * 
     * 
     * 
     * 
     */

    /**
     * Méthode secondaire permettant d'effectuer les extractions sur un
     * BitVector (avec l'aide d'une méthode auxiliaire intExtract(int start,
     * Type type) )
     * 
     * @param index
     *            l'index a partir duquel on veut extraire le BitVector
     * @param size
     *            la taille de l'extraction (multiple de 32)
     * @param type
     *            le type d'extraction (Zero extend ou Wrapped)
     * @return Un nouveau BitVector correspondant à l'extraction voulu
     */
    private BitVector extract(int index, int size, Type type) {
        Preconditions.checkArgument(size % Integer.SIZE == 0 && size >= 0);
        int[] extractVector = new int[size / Integer.SIZE];
        for (int i = 0; i < extractVector.length; ++i) {
            extractVector[extractVector.length - 1 - i] = intExtract(
                    index + i * Integer.SIZE, type);
        }

        return new BitVector(extractVector);

    }

    private int intExtract(int start, Type type) {

        /*
         * Cas ou le start index est un multiple de 0 (permet de diminuer la
         * complexité de l'algorithme)
         */
        if (Math.floorMod(start, Integer.SIZE) == 0) {
            if (type == Type.Zero) {
                if (start <= -Integer.SIZE || start >= this.size()) {
                    return 0;
                } else {
                    return vector[vector.length - 1 - start / Integer.SIZE];
                }
            } else {
                return vector[vector.length - 1
                        - (Math.floorMod(start, this.size()) / Integer.SIZE)];
            }
        }

        if (type == Type.Wrapped) {
            int startIntIndex = vector.length - 1
                    - (Math.floorMod(start, this.size()) / Integer.SIZE);
            int startPosition = Math.floorMod(start, Integer.SIZE);
            return (vector[startIntIndex] >>> startPosition)
                    | (vector[Math.floorMod((startIntIndex - 1),
                            vector.length)] << Integer.SIZE - startPosition);
        } else {
            if (start <= -Integer.SIZE || start >= size()) {
                return 0;
            } else if (start <= 0) {
                return vector[0] << Math.abs(start);
            } else {

                int startIntIndex = start / Integer.SIZE;
                int startPosition = Math.floorMod(start, Integer.SIZE);
                int nextVector = start + Integer.SIZE < size()
                        ? vector[(startIntIndex + 1)]
                        : 0;

                return ((vector[startIntIndex] >>> startPosition)
                        | (nextVector << Integer.SIZE - startPosition));
            }
        }
    }

    /**
     * Méthode servant de constructeur pour les BitVector. Tout les constructeur
     * passe par cette méthide c'est donc elle qui vérifie l'argument "size".
     * cette méthode retourne un tableau d'entier contenant size bit de valeur
     * value
     * 
     * @param size
     *            la taille du BotVector qu'on veut créer (multiple de 32)
     * @param value
     *            des bits du Bitvector qu'on veut créer
     * @return un
     */
    private static int[] vectorCreator(int size, boolean value) {
        Preconditions.checkArgument(size > 0 && size % Integer.SIZE == 0);
        int[] v = new int[size / Integer.SIZE];
        Arrays.fill(v, value ? -1 : 0);
        return v;
    }

    /*
     * 
     * 
     * Class BitVector.Builder imbriqué statiquement dasn BitVector. Le but
     * principal de cette classe est de permettre la construction d'une vecteur
     * de bits de manière incrémentale, octet par octet.
     * 
     * 
     */

    public final static class Builder {

        private boolean state;
        private int[] vector;
        
        private final static int mask0 = 0b11111111_11111111_11111111_00000000;
        private final static int mask1 = 0b11111111_11111111_00000000_11111111;
        private final static int mask2 = 0b11111111_00000000_11111111_11111111;
        private final static int mask3 = 0b00000000_11111111_11111111_11111111;

        /**
         * constructeur de la classe Builder prend en argument la taille du
         * vecteur de bits à construire, qui doit être un multiple de 32
         * strictement positif. La totalité des bits du vecteur à construire
         * valent initialement 0
         * 
         * @param size
         *            la taille du vecteur de bit qu'on veut créer
         */
        public Builder(int size) {
            Preconditions.checkArgument(size % Integer.SIZE == 0 && size > 0);
            vector = new int[size / Integer.SIZE];
            state = true;
        }

        /**
         * Method qui construit le vecteur de bit
         * 
         * @return le vecteur de bit construit
         * 
         * @throws IllegalStateException
         *             si la methode build à déja était appelé
         */
        public BitVector build() {
            if (state) {
                state = false;
                return new BitVector(vector);
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Méthode qui permet de définir la valeur d'un octet désigné par son
         * index. Lance une exception si la valu n'est pas un octet ou si la
         * position n'est pas valide
         * 
         * @param position
         *            la postion de l'octet
         * @param value
         *            la valeur qu'on va assigner a l'octet
         * @return
         * 
         * @throws IllegalStateException
         *             si la methode build à déja était appelé
         * @throws IndexOutOfBoundsException
         *             si la postion n'est pas entre 0 (inclus) et le nombre
         *             d'octet compris d'en la vecteur (vector.length *4
         */
        public Builder setByte(int position, int value) {
            Preconditions.checkBits8(value);
            if (!state) {
                throw new IllegalStateException();
            }
            if (position >= this.vector.length * 4 || position < 0) {
                throw new IndexOutOfBoundsException();
            }

            int index = vector.length - 1 - (position / 4);
            int intPosition = position % 4;
            value = value << intPosition * Byte.SIZE;
            switch (intPosition) {
            case 0:
                vector[index] = vector[index] & mask0;
                break;
            case 1:
                vector[index] = vector[index] & mask1;
                break;
            case 2:
                vector[index] = vector[index] & mask2;
                break;
            case 3:
                vector[index] = vector[index] & mask3;
                break;
            }

            vector[index] = vector[index] | value;

            return this;
        }
    }
}
