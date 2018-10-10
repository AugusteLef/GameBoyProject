
package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * Classe qui représente une ligne d'image GameBoy
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */
public final class LcdImageLine {

    private final static int NO_CHANGE_PALETTE = 0b11_10_01_00;
    
    private BitVector msb;
    private BitVector lsb;
    private BitVector opacity;

    /**
     * Constructeur de LcdImageLine qui prend en argument le vecetur
     * correspondant aux MSB, celui des LSB et le vecteur correspondant a
     * l'opacité afin de créer une ligne(vérifie que les tailles sont égales et
     * que aucun des BitVector est égale a null)
     * 
     * @param v1
     *            le vecteur msb
     * @param v2
     *            le vecteur lsb
     * @param v3
     *            le vecteur représentant l'opacité
     */
    public LcdImageLine(BitVector v1, BitVector v2, BitVector v3) {
        Preconditions.checkArgument(
                v1.size() == v2.size() && v2.size() == v3.size());
        Objects.requireNonNull(v1);
        Objects.requireNonNull(v2);
        Objects.requireNonNull(v3);

        msb = v1;
        lsb = v2;
        opacity = v3;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lsb == null) ? 0 : lsb.hashCode());
        result = prime * result + ((msb == null) ? 0 : msb.hashCode());
        result = prime * result + ((opacity == null) ? 0 : opacity.hashCode());
        return result;
    }

    /**
     * Redéfinition de equals permettant de comparer de maniere structurelle les LcdImageLine
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LcdImageLine other = (LcdImageLine) obj;
        if (lsb == null) {
            if (other.lsb != null)
                return false;
        } else if (!lsb.equals(other.lsb))
            return false;
        if (msb == null) {
            if (other.msb != null)
                return false;
        } else if (!msb.equals(other.msb))
            return false;
        if (opacity == null) {
            if (other.opacity != null)
                return false;
        } else if (!opacity.equals(other.opacity))
            return false;
        return true;
    }

    /**
     * Retourne la taille de la ligne (pixel)
     * 
     * @return la taille de la ligne en pixel
     */
    public int size() {
        // On a choisit msb, mais lsb ou opacity aurait également pur etre choisit
        return msb.size();
    }

    /**
     * 
     * @return le vecteur msb
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * 
     * @return le vecteur lsb
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * 
     * @return le vecteur représentant l'opacité
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * Methode qui décalle la ligne d'une certaine distance passé en argument.
     * (On décalle donc chaque vecteur qui compose la ligne)
     * 
     * @param distance
     *            la distance dont on veut decaller la ligne
     * @return une nouvelle ligne représentant la ligne recu en argument décallé de distance
     */
    public LcdImageLine shift(int distance) {
        LcdImageLine shiftLine = new LcdImageLine(msb.shift(distance),
                lsb.shift(distance), opacity.shift(distance));
        return shiftLine;
    }

    /**
     * Methode qui permet d'extraire de l'extension infinie par enroulement, à
     * partir d'un pixel donné, une ligne de longueur donnée
     * 
     * @param index
     *            l'index a parti duquel on veut extraire la ligne
     * @param size
     *            la taille de la ligne qu'on veut extraire (un multiplde de 32
     *            sinon lance une exception)
     * @return Une nouvelle ligne resultant de l'extraction par enroulement de la ligne
     *         de base
     */
    public LcdImageLine extractWrapped(int index, int size) {
        Preconditions.checkArgument(size % Integer.SIZE == 0);
        LcdImageLine extractWrapped = new LcdImageLine(
                msb.extractWrapped(index, size),
                lsb.extractWrapped(index, size),
                opacity.extractWrapped(index, size));
        return extractWrapped;
    }

    /**
     * Methode qui permet de transformer les couleurs de la ligne en fonction
     * d'une palette, donnée sous la forme d'un octet
     * 
     * @param palette
     *            l'octet qui donne les inforamtion sur les transforamtion de
     *            couleurs voulu
     * @return la ligne dont les couleurs ont été changees en fonction de la
     *         palette donnée en arguments
     */
    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);
        if (palette == NO_CHANGE_PALETTE)
            return new LcdImageLine(this.msb(), this.lsb(), this.opacity());

        BitVector msbNew = new BitVector(this.size(), false);
        BitVector lsbNew = new BitVector(this.size(), false);

        for (int i = 0; i < 4; ++i) {
            boolean first = Bits.test(palette, 2 * i);
            boolean second = Bits.test(palette, 2 * i + 1);
            if (first | second) {
                BitVector lsbMask = Bits.test(i, 0) ? this.lsb()
                        : this.lsb().not();
                BitVector msbMask = Bits.test(i, 1) ? this.msb()
                        : this.msb().not();
                BitVector andMask = lsbMask.and(msbMask);
                if (first)
                    lsbNew = lsbNew.or(andMask);
                if (second)
                    msbNew = msbNew.or(andMask);
            }
        }

        return new LcdImageLine(msbNew, lsbNew, this.opacity());
    }

    /**
     * Methode qui permet de composer la ligne avec une seconde de même
     * longueur, placée au-dessus d'elle, en utilisant l'opacité de la ligne
     * supérieure pour effectuer la composition 
     * 
     * @param topLine
     *            la ligne qu'on va placer au dessus pour effectuer la
     *            composition (même taille que la ligne a composer sinon lance une exception)
     * @return la composotion de la ligne et de topLine en fonction du vecteur
     *         opacité
     */
    public LcdImageLine below(LcdImageLine topLine) {
        Objects.requireNonNull(topLine);
        Preconditions.checkArgument(this.size() == topLine.size());
        BitVector rm = ((topLine.msb().and(topLine.opacity()))
                .or((this.msb().and(topLine.opacity().not()))));
        BitVector rl = (topLine.lsb.and(topLine.opacity()))
                .or((this.lsb().and(topLine.opacity().not())));
        BitVector ro = topLine.opacity().or(this.opacity());
        return new LcdImageLine(rm, rl, ro);
    }

    /**
     * Methode qui permet de composer la ligne avec une seconde de même
     * longueur, placée au-dessus d'elle, en utilisant un vecteur d'opacité
     * passé en argument pour effectuer la composition, celui de la ligne
     * supérieure étant ignoré
     * 
     * @param topLine
     *            la ligne qu'on va placer au dessus pour effectuer la
     *            composition
     * @param opacityBis
     *            le vecteur d'opacité qui sera utilise pour faire la compostion
     *            (celui de topLine étant ignoré)
     * @return la composition de la ligne de base avec topLine en fonction du
     *         vecteur d'opacité passé en argument
     */
    public LcdImageLine below(LcdImageLine topLine, BitVector opacityBis) {
        Objects.requireNonNull(topLine);
        Preconditions.checkArgument(this.size() == topLine.size()
                && this.size() == opacityBis.size());
        BitVector rm = (this.msb().and(opacityBis.not()))
                .or((topLine.msb().and(opacityBis)));
        BitVector rl = (this.lsb().and(opacityBis.not()))
                .or((topLine.lsb().and(opacityBis)));
        BitVector ro = opacityBis.or(this.opacity());

        return new LcdImageLine(rm, rl, ro);
    }

    /**
     * Methode qui permet de joindre la ligne avec une autre de même longueur, à
     * partir d'un pixel d'index donné
     * 
     * @param toJoin
     *            la ligne qu'on va joinde a la ligne de base
     * @param index
     *            l'index jusqu'au quelle on prend les valeur de la premiere
     *            ligne
     * @return la "jonction" entre la ligne de base et celle passé en argument a
     *         partir de l'index donnée
     */
    public LcdImageLine join(LcdImageLine toJoin, int index) {

        Objects.requireNonNull(toJoin);
        Preconditions
                .checkArgument((this.size() == toJoin.size()) && index >= 0);
        BitVector template = new BitVector(this.size(), true);
        BitVector mask1 = template.shift(-this.size() + index);
        BitVector mask2 = template.shift(index);

        BitVector rm = (msb.and(mask1)).or((toJoin.msb().and(mask2)));
        BitVector rl = (lsb.and(mask1)).or((toJoin.lsb().and(mask2)));
        BitVector ro = (opacity.and(mask1)).or((toJoin.opacity().and(mask2)));

        return new LcdImageLine(rm, rl, ro);

    }

    /**
     * 
     * 
     * 
     * Classe Builder imbriqué statiquement dans LcdImageLine et qui fait office
     * de batisseur de ligne d'une image
     * 
     * 
     *
     */
    public final static class Builder {
        private BitVector.Builder msb;
        private BitVector.Builder lsb;

        /**
         * Le constructeur publique de Builder qui définit les vecteur d'une
         * ligne à partir de la taille donnée (multiple de 32 et supérieur a 0
         * sinon lance une exception)
         * 
         * @param size
         *            la taille de la ligne qu'on veut batir
         */
        public Builder(int size) {
            msb = new BitVector.Builder(size);
            lsb = new BitVector.Builder(size);
        }

        /**
         * Methode qui va construire la ligne avec les octets définis jusqu'à
         * présent, dans laquelle tous les pixels de couleur 0 sont
         * transparents, les autres opaques
         * 
         * @return une ligne d'une image avec les vecteur correspondant
         */
        public LcdImageLine build() {
            BitVector v1 = msb.build();
            BitVector v2 = lsb.build();
            
            return new LcdImageLine(v1, v2, v1.or(v2));
        }

        /**
         * Methode qui permet de définir la valeur des octets de poids fort et
         * de poids faible de la ligne, à un index donné
         * 
         * @param position
         *            la position des octet auxquels on veut assigner une
         *            nouvelle valeur
         * @param valueMsb
         *            la valeur des bits de poid fort de cet octet
         * @param valueLsb
         *            la valeur des bits de poid faible de cet eoctet
         * @return this
         */
        public Builder setBytes(int position, int valueMsb, int valueLsb) {
            msb.setByte(position, valueMsb);
            lsb.setByte(position, valueLsb);

            return this;
        }

    }

}