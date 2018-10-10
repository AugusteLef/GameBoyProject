package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Représente les bus d'adresses et de données connectant les composants de Game
 * Boy entre eux
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 *
 */
public final class Bus {
    
    private static final int DEFAULT_READ = 255;

    private final ArrayList<Component> listOfComponent = new ArrayList<Component>();

    /**
     * attache le composant donné au bus, ou lève l'exception
     * NullPointerException si le composant vaut null
     * 
     * @param component
     *            Le composant qu'on veut lier au bus
     */
    public void attach(Component component) {

        Objects.requireNonNull(component);

        listOfComponent.add(component);
    }

    /**
     * retourne la valeur stockée à l'adresse donnée si au moins un des
     * composants attaché au bus possède une valeur à cette adresse, ou 0xFF
     * sinon
     * 
     * @param address
     *            l'adresse a laquelle on va chercher si une valeur est stockée
     * @return la donnée stocké si elle existe ou OxFF sinon
     */   
        public int read(int address) {
        int dataValue;
        Preconditions.checkBits16(address);
        for (int i = 0; i < listOfComponent.size(); ++i) {
            dataValue = listOfComponent.get(i).read(address);
            if (dataValue != Component.NO_DATA) {
                return dataValue;
            }
        }

        return DEFAULT_READ;
    }

    /**
     * écrit la valeur à l'adresse donnée dans tous les composants connectés au
     * bus
     * 
     * @param address
     *            l'adresse a laquelle on va ecrire notre donnée
     * @param data
     *            la donnée qu'on va ecrire
     */
    public void write(int address, int data) {
        
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (int i = 0; i < listOfComponent.size(); ++i) {
            listOfComponent.get(i).write(address, data);
        }

    }

}
