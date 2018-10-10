package ch.epfl.gameboj.component;

/**
 * Interface qui permet de représenter un composant piloté par l'horloge du
 * système
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 * 
 */
public interface Clocked {

    /**
     * Methode abstraite qui demande au composant d'évoluer en exécutant toutes
     * les opérations qu'il doit exécuter durant le cycle d'index donné en
     * argument
     * 
     * @param cycle
     *            le nombre de cycle a effectuer
     */
    public abstract void cycle(long cycle);

}
