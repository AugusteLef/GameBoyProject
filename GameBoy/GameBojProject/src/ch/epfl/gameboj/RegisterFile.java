package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Classe générique représentant un banc de registre 8bits.
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 * @param <E>
 *            représente le type du banc
 */
public final class RegisterFile<E extends Register> {

    private final int[] banc;

    /**
     * Constructeur publique construit un banc de registres 8 bits dont la
     * taille (c-à-d le nombre de registres) est égale à la taille du tableau
     * donné.
     * 
     * @param allRegs
     *            le tableau dont on va recuperer la taille
     * 
     */
    public RegisterFile(E[] allRegs) {
        // Copie ou juste créeation d'un banc vide de taille allRegs.length ?
        banc = new int[allRegs.length];
    }

    /**
     * retourne la valeur 8 bits contenue dans le registre donné, sous la forme
     * d'un entier compris entre 0 (inclus) et FF16 (inclus)
     * 
     * @param reg
     *            l'object(registre dans notre cas) dont on veut extraire la
     *            valeur
     * @return la valeur 8Bits contenue dans le registre reg
     */
    public int get(E reg) {
        Objects.requireNonNull(banc);
        int index = reg.index();
        int out = banc[index];
        Preconditions.checkBits8(out);
        return out;
    }
    
    // METHODE PERSO A VOIR
    public int get(int value) {
        
        return banc[value];
    }
    
    

    /**
     * modifie le contenu du registre donné pour qu'il soit égal à la valeur 8
     * bits donnée ; lève IllegalArgumentException si la valeur n'est pas une
     * valeur 8 bits valide
     * 
     * @param reg
     *            l'object (ici un registre) dont on veut modifier la valeur
     * @param newValue
     *            la valeur 8Bits qu'on va mettre dans le regsitre reg
     */
    public void set(E reg, int newValue) {
        Objects.requireNonNull(banc);
        Preconditions.checkBits8(newValue);
        int index = reg.index();
        banc[index] = newValue;

    }

    /**
     * retourne vrai si et seulement si le bit donné du registre donné vaut 1
     * 
     * @param reg
     *            le registre dont on va tester la valeur du bit donné
     * @param b
     *            le bit donné
     * @return true si le bit donné du registre vaut 1, false sinon
     */
    public boolean testBit(E reg, Bit b) {
        Objects.requireNonNull(banc);
        int index = reg.index();
        int value = banc[index];
        boolean test = Bits.test(value, b);
        return test;
    }

    /**
     * modifie la valeur stockée dans le registre donné pour que le bit donné
     * ait la nouvelle valeur donnée
     * 
     * @param reg
     *            le registre dont on veut modifier un bit
     * @param bit
     *            le bit qu'on veut modifier
     * @param newValue
     *            la valeur du bit qu'on va modifier (true = 1, false = 0)
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        Objects.requireNonNull(banc);
        int valueRegister = get(reg);
        valueRegister = Bits.set(valueRegister, bit.index(), newValue);
        set(reg, valueRegister);
    }

}
