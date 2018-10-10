package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

/**
 * 
 * 
 * Classe qui représente une image de Game Boy
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class LcdImage {

    private final static int MAX_SIZE = 256;

    private int width;
    private int height;
    private List<LcdImageLine> image;

    /**
     * Constructeur de LcdImage. Il utilise la hauteur/largeur et une liste de
     * LcdImageLine pour construire son image
     * 
     * @param w
     *            la largeur de l'image
     * @param h
     *            la hauteur de l'image
     * @param imageBis
     *            la liste de LcdImageLine composant l'image
     */
    public LcdImage(int w, int h, List<LcdImageLine> imageBis) {
        Objects.requireNonNull(imageBis);
        Preconditions.checkArgument(w > 0 && h > 0 && w <= MAX_SIZE && h <= MAX_SIZE && (imageBis.size() == h));
        this.width = w;
        this.height = h;
        this.image = imageBis;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LcdImage other = (LcdImage) obj;
        if (height != other.height)
            return false;
        if (image == null) {
            if (other.image != null)
                return false;
        } else if (!image.equals(other.image))
            return false;
        if (width != other.width)
            return false;
        return true;
    }

    /**
     * 
     * @return la largeur de l'image
     */
    public int width() {
        return this.width;
    }

    /**
     * 
     * @return la hauteur de l'image
     */
    public int height() {
        return this.height;
    }

    /**
     * Methode qui permet d'obtenir, sous la forme d'un entier compris entre 0
     * et 3, la couleur d'un pixel d'index (x, y) donné
     * 
     * @param x
     *            l'index ("axe des abscisse") du pixel dont on veut la couleur
     * @param y
     *            l'index ("axe des ordonnée") du pixel dont on veut la couleur
     * @return la couleur sous forme d'entier du pixel en position (x,y)
     */
    public int get(int x, int y) {
        Preconditions.checkArgument(x < this.width && y < this.height);
        LcdImageLine yLine = this.image.get(y);
        BitVector msb = yLine.msb();
        BitVector lsb = yLine.lsb();
        int first = msb.testBit(x) ? 0b10 : 0;
        int second = lsb.testBit(x) ? 0b01 : 0;
        return (first | second);
    }

    /**
     * 
     * 
     * Classe imbriqué statiquement dans LcdImage qui est un batisseur d'image
     * 
     *
     */
    public final static class Builder {
        private int width;
        private int height;
        private List<LcdImageLine> imageList;

        /**
         * Le constructeur prend en argument la largeur et la hauteur de l'image
         * à bâtir. Initialement, celle-ci est vide, c-à-d que tous ses pixels
         * ont la couleur 0
         * 
         * @param w la largeur
         * @param h la hauteur
         */
        public Builder(int w, int h) {
            Preconditions.checkArgument(w > 0 && h > 0 && w <= MAX_SIZE && h <= MAX_SIZE);
            this.width = w;
            this.height = h;
            BitVector v0 = new BitVector(w);
            LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
            imageList = new ArrayList<LcdImageLine>(Collections.nCopies(h, l0));
        }

        /**
         * Construit l'image en construction 
         * @return l'image en cours de construction
         */
        public LcdImage build() {
            return new LcdImage(this.width, this.height, imageList);
        }

        /**
         * Permet de changer la ligne d'index donnée (compris entre 0 et la taille de la list de ligne sinon lance un exception)
         * @param index l'index dont on veut changer la ligne
         * @param line la ligne qu'on va mette dans la liste a l'index donnée
         * @return this
         */
        public Builder setLine(int index, LcdImageLine line) {
            Objects.requireNonNull(line);
            Preconditions.checkArgument(index >= 0 && index < imageList.size());
            imageList.set(index, line);
            return this;
        }

    }

}
