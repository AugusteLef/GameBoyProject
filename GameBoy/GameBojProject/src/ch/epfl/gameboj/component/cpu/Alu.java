package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Classe qui contient principalement des méthodes statiques permettant
 * d'effectuer des opérations sur des valeurs 8 ou 16 bits et d'obtenir à la
 * fois le résultat et la valeur des fanions.
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class Alu {

    private Alu() {
    };

    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z;
    }

    public enum RotDir {
        LEFT, RIGHT;
    }

    /**
     * Methode qui créer le paquet rejoingant valeur et fanions(true = 1, false
     * = 0)
     * 
     * @param v
     *            La valeur
     * @param z
     *            le fanion Z
     * @param n
     *            le fanion N
     * @param h
     *            le fanion H
     * @param c
     *            le fanion C
     * @return retourne un entier qui continet la valeur et les fanions
     */
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h,
            boolean c) {
        int out = v << Byte.SIZE;
        out = out | maskZNHC(z, n, h, c);
        return out;

    }

    /**
     * Retourne une valeur dont les bits correspondant aux différents fanions
     * valent 1 ssi l'argument correspondant est vrai
     * 
     * @param z
     *            Fanion Z
     * @param n
     *            Fanion N
     * @param h
     *            Fanion H
     * @param c
     *            Fanion H
     * @return la valeur dont les bits corresspondent aux fanions
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int out = 0;
        if (c)
            out += Alu.Flag.C.mask();
        if (h)
            out += Alu.Flag.H.mask();
        if (n)
            out += Alu.Flag.N.mask();
        if (z)
            out += Alu.Flag.Z.mask();
        return out;
    }

    /**
     * Retourne la valeur contenue dans le paquet valeur/fanion donné
     * 
     * @param valueFlags
     *            Le paquet contenant valeur et fanions
     * @return la valeur du paquet recu
     * @throws IllegalArgumentException
     *             si l'un des bits 0 a 3 est différent que 0
     */
    public static int unpackValue(int valueFlags) {
        if (Bits.test(valueFlags, Flag.UNUSED_0.index())
                || Bits.test(valueFlags, Flag.UNUSED_1.index())
                || Bits.test(valueFlags, Flag.UNUSED_2.index())
                || Bits.test(valueFlags, Flag.UNUSED_3.index())) {
            throw new IllegalArgumentException(
                    "There is a bit = 1 between bit 1 to 4. Or this bit must be 0");
        }
        valueFlags = valueFlags >>> Byte.SIZE;
        return valueFlags;
    }

    /**
     * Retourne les fanions contenus dans le paquet valeur/fanion donné
     * 
     * @param valueFlags
     *            le paquet contenant valeur et fanions
     * @return le bits des fanions
     * @throws IllegalArgumentException
     *             si l'un des bits 0 a 3 est différent que 0
     */
    public static int unpackFlags(int valueFlags) {
        if (Bits.test(valueFlags, Flag.UNUSED_0.index())
                || Bits.test(valueFlags, Flag.UNUSED_1.index())
                || Bits.test(valueFlags, Flag.UNUSED_2.index())
                || Bits.test(valueFlags, Flag.UNUSED_3.index())) {
            throw new IllegalArgumentException(
                    "There is a bit = 1 between bit 1 to 4. thus bits must be equal to 0");
        }
        valueFlags = Bits.clip(Byte.SIZE, valueFlags);
        return valueFlags;
    }

    /**
     * Retourne la somme des deux valeurs 8 bits données et du bit de retenue
     * initial c0 et la fanions Z0HC
     * 
     * @param l
     *            le premier bits
     * @param r
     *            le 2eme bits
     * @param c0
     *            la retenue (carry)
     * @return la somme de l et r et de la retenue si c0 = true, et la fanions
     *         correspondant
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int c0value = 0;
        if (c0)
            c0value = 1;
        int add = c0value + l + r;
        boolean c = (add > 0xFF);
        add = Bits.clip(Byte.SIZE, add);
        boolean z = (add == 0);
        boolean n = false;
        int addHalf = c0value + Bits.clip(4, l) + Bits.clip(4, r);
        boolean h = (addHalf > 0xF);
        return packValueZNHC(add, z, n, h, c);

    }

    /**
     * Identique que add, sauf qu'il n'y a pas d'ajout de retenue possible
     * 
     * @param l
     *            premier bits
     * @param r
     *            2eme bits
     * @return la somme des deux bits et la fanions correspondant
     */
    public static int add(int l, int r) {
        int out = add(l, r, false);
        return out;
    }

    /**
     * Retourne la somme des deux valeurs 16 bits données et les fanions 00HC,
     * où H et C sont les fanions correspondant à l'addition des 8 bits de poids
     * faible
     * 
     * @param l
     *            la premiere valeur sur 16bits
     * @param r
     *            la deuxieme valeur sur 16bits
     * @return la somme des deux bits et la fanions correspondant a l'addition
     *         des 8bits de poids faibles
     */

    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int lL = Bits.clip(Byte.SIZE, l);
        int rL = Bits.clip(Byte.SIZE, r);
        int addLpackValue = add(lL, rL);
        boolean carry8 = (Bits.test(addLpackValue, Flag.C.index()));
        boolean carryHalf8 = (Bits.test(addLpackValue, Flag.H.index()));
        int lH = Bits.clip(8, Bits.extract(l, Byte.SIZE, Byte.SIZE));
        int rH = Bits.clip(8, Bits.extract(r, Byte.SIZE, Byte.SIZE));
        int addHpackValue = add(lH, rH, carry8);
        int addRealValue = (Bits.extract(addHpackValue, Byte.SIZE, Byte.SIZE) << Byte.SIZE)
                | Bits.extract(addLpackValue, Byte.SIZE, Byte.SIZE);
        return packValueZNHC(addRealValue, false, false, carryHalf8, carry8);

    }

    /**
     * Identique à add16L, si ce n'est que les fanions H et C correspondent à
     * l'addition des 8 bits de poids fort
     * 
     * @param l
     *            1er valeur sur 16bits
     * @param r
     *            2eme valeur sur 16bits
     * @return la somme de l et r et les fanions ocrrespondant a l'addition des
     *         8bits de poids forts
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int lL = Bits.clip(Byte.SIZE, l);
        int rL = Bits.clip(Byte.SIZE, r);
        int addLpackValue = add(lL, rL);
        boolean carry8 = (Bits.test(addLpackValue, Flag.C.index()));
        int lH = Bits.clip(Byte.SIZE, Bits.extract(l, Byte.SIZE, Byte.SIZE));
        int rH = Bits.clip(Byte.SIZE, Bits.extract(r, Byte.SIZE, Byte.SIZE));
        int addHpackValue = add(lH, rH, carry8);
        boolean carry16 = (Bits.test(addHpackValue, Flag.C.index()));
        boolean carryHalf16 = (Bits.test(addHpackValue, Flag.H.index()));
        int addRealValue = (Bits.extract(addHpackValue, Byte.SIZE, Byte.SIZE) << Byte.SIZE)
                | Bits.extract(addLpackValue, Byte.SIZE, Byte.SIZE);
        return packValueZNHC(addRealValue, false, false, carryHalf16, carry16);

    }

    /**
     * Retourne la différence des valeurs de 8 bits données et du bit d'emprunt
     * initial b0 et les fanions Z1HC
     * 
     * @param l
     *            le premier bits
     * @param r
     *            le deuxieme bits
     * @param b0
     *            la retenue (carry) ou bit d'emprunt
     * @return la différence entre l, r et le bit d'emprunt si b0 == true et les
     *         fanions correspondant
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int b0value = 0;
        if (b0)
            b0value = 1;
        int sub = l - b0value - r;
        boolean c = (l - b0value < r);
        sub = Bits.clip(Byte.SIZE, sub);
        boolean z = (sub == 0);
        boolean n = true;
        boolean h = (Bits.clip(4, l) - b0value < Bits.clip(4, r));
        return packValueZNHC(sub, z, n, h, c);
    }

    /**
     * Retourne la différence des valeurs de 8 bits données et les fanions Z1HC
     * 
     * @param l
     *            premier bits
     * @param r
     *            2eme bits
     * @return la différence entre l et r et les fanions correspondant et les
     *         fanions
     */
    public static int sub(int l, int r) {
        int out = sub(l, r, false);
        return out;
    }

    /**
     * Ajuste la valeur 8 bits donnée en argument afin qu'elle soit au format
     * DCB
     * 
     * @param v
     *            la valeur 8bits à ajuster
     * @param n
     *            Fanion N
     * @param h
     *            Fanion H
     * @param c
     *            Fanion C
     * @return la valeur ajuster, suivie des fanions (selon l'algorithme fournit
     *         en cours)
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        boolean fixL = (h || (!n && (Bits.clip(4, v) > 9)));
        boolean fixH = (c || (!n && (v > 0x99)));
        int fixHValue = 0;
        int fixLValue = 0;
        if (fixH)
            fixHValue = 1;
        if (fixL)
            fixLValue = 1;
        int fix = 0x60 * fixHValue + 0x06 * fixLValue;
        int va = n ? v - fix : v + fix;
        va = Bits.clip(Byte.SIZE, va);
        return packValueZNHC(va, va == 0, n, false, fixH);
    }

    /**
     * Retourne le « et » bit à bit des deux valeurs 8 bits données et les
     * fanions Z010
     * 
     * @param l
     *            le premier bit
     * @param r
     *            le 2eme bit
     * @return la résultante de l'opération logique "et" bit a bit sur l et r et
     *         les fanions
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int out = l & r;
        boolean z = (out == 0);
        return packValueZNHC(out, z, false, true, false);
    }

    /**
     * Retourne le « ou » bit à bit des deux valeurs 8 bits données et les
     * fanions Z010
     * 
     * @param l
     *            le premier bit
     * @param r
     *            le 2eme bit
     * @return la résultante de l'opération logique "ou" bit a bit sur l et r et
     *         les fanions
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int out = l | r;
        boolean z = (out == 0);
        return packValueZNHC(out, z, false, false, false);

    }

    /**
     * Retourne le « ou exclusif» bit à bit des deux valeurs 8 bits données et
     * les fanions Z010
     * 
     * @param l
     *            le premier bit
     * @param r
     *            le 2eme bit
     * @return la résultante de l'opération logique "xor" bit a bit sur l et r
     *         et les fanions
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int out = l ^ r;
        boolean z = (out == 0);
        return packValueZNHC(out, z, false, false, false);

    }

    /**
     * Retourne la valeur 8 bits donnée décalée à gauche d'un bit, et les
     * fanions Z00C où le fanion C contient le bit éjecté par le décalage
     * 
     * @param v
     *            le bits qu'on va "shifter"
     * @return le bits de départ qu'on a décaler de 1 vers la gauche et les
     *         fanions
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        boolean c = (Bits.test(v, 7));
        v = Bits.clip(7, v) << 1;
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, c);
    }

    /**
     * Retourne la valeur 8 bits donnée décalée à droite d'un bit, de manière
     * arithmétique, et les fanions Z00C où C contient le bit éjecté par le
     * décalage
     * 
     * @param v
     *            le bits qu'on va shifter
     * @return le bit de départ auquel on a effectué un décallage arithmétique
     *         de 1 sur la droite, et les fanions
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 0);
        boolean bit8 = Bits.test(v, 7);
        v = Bits.extract(v, 1, 7);
        if (bit8)
            v += Bits.mask(7);
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, c);

    }

    /**
     * Retourne la valeur 8 bits donnée décalée à droite d'un bit, de manière
     * logique, et les fanions Z00C où C contient le bit éjecté par le décalage
     * 
     * @param v
     *            le bits qu'on va shifter
     * @return le bit de départ auquel on a effectué un décallage logique de 1
     *         sur la droite, et les fanions
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 0);
        v = Bits.extract(v, 1, 7);
        boolean z = (v == 0);
        return packValueZNHC(v, z, false, false, c);
    }

    /**
     * Retourne la rotation de la valeur 8 bits donnée, d'une distance de un bit
     * dans la direction donnée, et les fanions Z00C où C contient le bit qui
     * est passé d'une extrémité à l'autre lors de la rotation
     * 
     * @param d
     *            la direction de la rotation
     * @param v
     *            la valeur (bits) a laquelle on va effectuer une rotation
     * @return la valeur après la rotation ainsi que ses fanions
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        boolean c;
        if (d.ordinal() == 0) {
            c = Bits.test(v, 7);
            v = Bits.rotate(Byte.SIZE, v, 1);
        } else {
            c = Bits.test(v, 0);
            v = Bits.rotate(Byte.SIZE, v, -1);
        }

        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Retourne la rotation à travers la retenue, dans la direction donnée, de
     * la combinaison de la valeur 8 bits et du fanion de retenue donnés, ainsi
     * que les fanions Z00C
     * 
     * @param d
     *            le sens de la rotation
     * @param v
     *            la valeur sur laquelle on effectue la rotation
     * @param c
     *            la retenue
     * @return la valeur v, après la rotation et ses fanions
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        if (c)
            v = v + Bits.mask(Byte.SIZE);
        if (d.ordinal() == 0) {
            v = Bits.rotate(9, v, 1);
            c = Bits.test(v, Byte.SIZE);
        } else {
            v = Bits.rotate(9, v, -1);
            c = Bits.test(v, Byte.SIZE);
        }
        v = Bits.clip(Byte.SIZE, v);

        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Retourne la valeur obtenue en échangeant les 4 bits de poids faible et de
     * poids fort de la valeur 8 bits donnée, et les fanions Z000
     * 
     * @param v
     *            la valeur a "swapper"
     * @return la valeur swapper et ses fanions
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        v = Bits.rotate(Byte.SIZE, v, 4);
        return packValueZNHC(v, v == 0, false, false, false);
    }

    /**
     * Retourne la valeur 0 et les fanions Z010 où Z est vrai ssi le bit d'index
     * donné de la valeur 8 bits donnée vaut 1 ; en plus de la validation de la
     * valeur 8 bits reçue, cette méthode valide l'index reçu et lève
     * IndexOutOfBoundsException s'il n'est pas compris entre 0 et 7.
     * 
     * @param v
     *            la valeur à tester
     * @param bitIndex
     *            l'index sur lequel on test la valeur du bit
     * @return la valeur 0 suivie des fanions correspondant
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        if (bitIndex > 7 || bitIndex < 0)
            throw new IndexOutOfBoundsException();
        boolean z = Bits.test(v, bitIndex);
        return packValueZNHC(0x00, !z, false, true, false);
    }

}
