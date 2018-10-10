package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.Bus;

/**
 * Représente un composant du Game Boy connecté aux bus d'adresses et de données
 * (processeur, clavier, etc.)
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public interface Component {

    public static final int NO_DATA = 256;

    /**
     * 
     * Retourne l'octet stocké à l'adresse donnée par le composant, ou NO_DATA
     * si le composant ne possède aucune valeur à cette adresse ; lève
     * l'exception IllegalArgumentException si l'adresse n'est pas une valeur 16
     * bits
     * 
     * @param address
     *            l'adresse a laquelle on veut récupérer/lire la donnée
     * @return la valeur stocké a l'adresse passé en argument
     */
    public abstract int read(int address);

    /**
     * stocke la valeur donnée à l'adresse donnée dans le composant, ou ne fait
     * rien si le composant ne permet pas de stocker de valeur à cette adresse ;
     * lève l'exception IllegalArgumentException si l'adresse n'est pas une
     * valeur 16 bits ou si la donnée n'est pas une valeur 8 bits.
     * 
     * @param address
     *            l'adresse à la quelle on va ecrire/injecter la donnée
     * @param data
     *            la donnée à injecter
     */
    public abstract void write(int address, int data);

    /**
     * Appelle la methode attach de Bus pour lier le composant au bus voulu
     * 
     * @param bus
     *            le bus auquel on veut lier l'élément
     * 
     */
    public default void attachTo(Bus bus) {
        Objects.requireNonNull(bus);
        bus.attach(this);
    }

}
