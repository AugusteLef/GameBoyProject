package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * 
 * @author Auguste Lefevre (269821) Marw Watine (269508) Classe qui représente
 *         une cartouche.
 */
public final class Cartridge implements Component {

    private final Component cartridgeController;

    private final static int TYPE_CARTRIDGE = 0x147;
    private final static int RAM_SIZE = 0x149;
    private final static int[] RAM_SIZE_TABLE = new int[] { 0, 2048, 8192,
            32768 };

    /**
     * Constructeur privé construisant une cartouche contenant un contrôleur et
     * la mémoire morte qui lui est attachée
     * 
     * @param bankController
     *            controleur de banque mémoire gérant la mémoire morte de la
     *            cartouche
     * 
     */
    private Cartridge(Component bankController) {
        this.cartridgeController = bankController;
    }

    /**
     * Retourne une cartouche dont la mémoire morte contient les octets du
     * fichier donné (et une ram si la cartouche est une MBC1) ; lève
     * l'exception IOException en cas d'erreur d'entrée sortie, y compris si le
     * fichier donné n'existe pas, et l'exception IllegalArgumentException si le
     * fichier en question ne contient pas 0,1,2 ou 3 à la position 0x147
     * 
     * @param romFile
     *            Le fichier sur lequel on va lire les octets afin de les placer
     *            dans la mémoir de la cartouche
     * @return un cartouche contenant une mémoire morte (initialisé) et son
     *         controleur de banque mémoire
     * @throws IOException
     *             si le fichier passé en argument n'existe pas ou si ça taille
     *             est égale a 0
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        Objects.requireNonNull(romFile);
        byte[] data = new byte[(int) romFile.length()];

        if (!romFile.exists()) {
            throw new IOException();
        } else {
            try (InputStream stream = new FileInputStream(romFile)) {
                data = stream.readAllBytes().clone();
                // le stream.close est automatique par l'utilisation du try (cf.
                // cours)
            } catch (FileNotFoundException e) {
                throw new IOException();
            }

            int type = data[TYPE_CARTRIDGE];
            int ramInfo = data[RAM_SIZE];

            Preconditions.checkArgument(type >= 0 && type < 4);
            Rom rom = new Rom(data);
            Cartridge cartridge;
            if (type == 0) {
                MBC0 mbc0 = new MBC0(rom);
                cartridge = new Cartridge(mbc0);
            } else {
                int sizeRam = RAM_SIZE_TABLE[ramInfo];
                MBC1 mbc1 = new MBC1(rom, sizeRam);
                cartridge = new Cartridge(mbc1);
            }

            return cartridge;
        }

    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return cartridgeController.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        this.cartridgeController.write(address, data);
    }

}
