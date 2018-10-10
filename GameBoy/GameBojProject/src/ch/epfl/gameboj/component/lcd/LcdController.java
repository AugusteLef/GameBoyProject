package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * 
 * 
 * Classe qui représente l'écran de la GameBoy
 * 
 * @author Auguste Lefevre (269821)
 * @author Marc Watine (269508)
 */

public final class LcdController implements Clocked, Component {

    // Public car utilisé dans plusieus autres classes
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final int BG_SIZE = 256;
    private static final int TILE_LINE = 8;
    private static final int TILE_SIZE = 16;
    private static final int TILE_BY_BG = 32;
    private static final int IMAGE_DRAW = 17556;
    private static final int LINE_DRAW_CYCLE = 114;
    private static final int END_IMAGE_DRAW = 16416;
    private static final int TILE_SOURCE_NUMBER = 128;
    private static final int WX_OFF = 7;
    private static final int SPRITE_INFO = 4;
    private static final int SPRITEY_OFF = 16;
    private static final int SPRITEX_OFF = 8;
    private static final int MODE2_DURATION = 20;
    private static final int MODE3_DURATION = 43;
    private static final int MODE0_DURATION = 51;
    private static final int MAX_SPRITE = 10;
    private static final int MAX_COUNTER_OF_COPY = 160;
    private static final int NUMBER_SPRITE = 40;
    private static final BitVector EMPTY_VECTOR = new BitVector(LCD_WIDTH,
            false);
    private static final LcdImageLine EMPTY_LINE = new LcdImageLine(
            EMPTY_VECTOR, EMPTY_VECTOR, EMPTY_VECTOR);
    private static final LcdImage EMPTY_IMAGE = new LcdImage(LCD_WIDTH,
            LCD_HEIGHT, new ArrayList<LcdImageLine>(
                    Collections.nCopies(LCD_HEIGHT, EMPTY_LINE)));

    private final Cpu cpu;
    private final Ram ramVideo;
    private final Ram ramSprite;
    private final RegisterFile<Reg> register = new RegisterFile<>(Reg.values());

    private Bus bus;
    private LcdImage.Builder nextImageBuilder;
    private LcdImage nextImage;
    private int winY = 0;
    private int indexLine = 0;
    private boolean copyActive = false;
    private int counterOfCopy = 0;
    private long nextNonIdleCycle = Long.MAX_VALUE;
    private long lcdOnCycle = Long.MAX_VALUE;

    /**
     * Enumération représentant les différents registre de l'écran (LCD)
     *
     */
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    /**
     * Enumération représentant les différents bit du registre LCDC
     *
     */
    private enum LCDC implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    /**
     * Enumération représentant les différents bit du registre STAT
     *
     */
    private enum STAT implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED7
    }

    /**
     * Enumération représentant l'octet de l'entier (type int) contenant les
     * information dites (Y, X, INDEX et INFO) d'un sprite
     *
     */
    private enum SPRITE implements Bit {
        Y, X, INDEX, INFO
    }

    /**
     * Enumaration représentant les différents bit du de l'octet INFO d'un
     * sprite (cf. SPRITE enum)
     *
     */
    private enum SINFO implements Bit {
        NULL0, NULL1, NULL2, NULL3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    /**
     * Constructeur publique du LcdController permettant de créer la ramVideo et
     * la ram OAM ainsi que définir le cpu lié à cet écran
     * 
     * @param cpu
     *            le cpu lié a l'écran
     */
    public LcdController(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
        ramVideo = new Ram(AddressMap.VIDEO_RAM_SIZE);
        ramSprite = new Ram(AddressMap.OAM_RAM_SIZE);

    }

    /**
     * Redéfinition de la méthode attachTo afin de pouvoir ajouter le bus comme
     * attribut dans cette classe
     */
    @Override
    public void attachTo(Bus bus) {
        Objects.requireNonNull(bus);
        this.bus = bus;
        bus.attach(this);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            return this.ramVideo.read(address - AddressMap.VIDEO_RAM_START);
        } else {
            if (address >= AddressMap.REGS_LCDC_START
                    && address < AddressMap.REGS_LCDC_END) {
                int data = getRegValue(address);
                return data;
            } else {
                if (address >= AddressMap.OAM_START
                        && address < AddressMap.OAM_END) {
                    return this.ramSprite.read(address - AddressMap.OAM_START);
                } else {
                    return NO_DATA;
                }
            }
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);

        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            this.ramVideo.write(address - AddressMap.VIDEO_RAM_START, data);
        }

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {

            switch (address) {
            case regMap.LCDC: {
                register.set(Reg.LCDC, data);
                if (!(Bits.test(data, 7))) {
                    this.changeMode(0);
                    this.changeLyLyc(Reg.LY, 0);
                    this.nextNonIdleCycle = Long.MAX_VALUE;
                }
            }
                break;

            case regMap.STAT: {
                int newStat = (data & 0b11111000)
                        | ((register.get(Reg.STAT) & 0b00000111));
                register.set(Reg.STAT, newStat);
            }
                break;

            case regMap.LY: {
                // DO NOTHING
                // cf. 1.2.1 etape 9
                // ici car il ne faut pas rentrer dans le case défault
            }
                break;

            case regMap.LYC: {
                this.changeLyLyc(Reg.LYC, data);
            }
                break;
            case regMap.DMA: {
                // Active la copie directe dans la mémoire OAM (spriteRam)
                if (!copyActive) {
                    this.copyActive = true;
                    this.counterOfCopy = 0;
                }
            }

            default: {
                setRegValue(address, data);
            }
                break;
            }
        }

        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            this.ramSprite.write(address - AddressMap.OAM_START, data);
        }

    }

    @Override
    public void cycle(long cycle) {

        // Allumage de l'écran
        if (this.nextNonIdleCycle == Long.MAX_VALUE
                && Bits.test(register.get(Reg.LCDC), LCDC.LCD_STATUS)) {
            this.lcdOnCycle = cycle;
            this.nextNonIdleCycle = 0;
        }

        // Copie direct de la spriteRam
        if (this.copyActive && this.counterOfCopy < MAX_COUNTER_OF_COPY) {
            this.write(AddressMap.OAM_START + this.counterOfCopy, bus.read(
                    (register.get(Reg.DMA) << Byte.SIZE) + this.counterOfCopy));
            this.counterOfCopy += 1;
            if (this.counterOfCopy == MAX_COUNTER_OF_COPY) {
                this.copyActive = false;
                this.counterOfCopy = 0;
            }
        }

        // Calcul si il y a quelque chose à faire à ce cycle, si oui appelle la
        // méthode reallyCycle
        if ((cycle - this.lcdOnCycle) == this.nextNonIdleCycle) {
            indexLine = ((((int) ((cycle - this.lcdOnCycle))) % IMAGE_DRAW)
                    / LINE_DRAW_CYCLE);
            this.reallyCycle();
        }
    }

    /**
     * Méthode qui comporte les différents action à réaliser en fonction du
     * cycle (plus d'information dans la méthode)
     * 
     * @param cycle
     */
    private void reallyCycle() {

        // Cas ou les 144 lignes sont déssinés mais qu'il reste l'équivalent de
        // 10 lignes (en cycle soit 114*10) avant de passer au dessin de la
        // prochaine image
        if (this.nextNonIdleCycle % (IMAGE_DRAW) >= END_IMAGE_DRAW) {
            if (this.nextNonIdleCycle % (IMAGE_DRAW) == END_IMAGE_DRAW) {
                this.changeMode(1);
                this.nextImage = this.nextImageBuilder.build();
                this.winY = 0;
            }

            this.nextNonIdleCycle += LINE_DRAW_CYCLE;
            this.changeLyLyc(Reg.LY, this.indexLine);

            return;
        }

        // Cas ou l'image est complétement déssiné et prête a être afficher a
        // l'écran
        if (this.nextNonIdleCycle % IMAGE_DRAW == 0) {
            this.nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
        }

        switch (((int) this.nextNonIdleCycle % (IMAGE_DRAW))
                % LINE_DRAW_CYCLE) {

        case 0:
            this.nextNonIdleCycle += MODE2_DURATION;
            this.changeLyLyc(Reg.LY, this.indexLine);
            this.changeMode(2);
            break;

        case MODE2_DURATION:
            this.nextNonIdleCycle += MODE3_DURATION;
            this.nextImageBuilder.setLine(register.get(Reg.LY),
                    this.computeLine(register.get(Reg.LY)));
            this.changeMode(3);
            break;

        case MODE2_DURATION + MODE3_DURATION:
            this.nextNonIdleCycle += MODE0_DURATION;
            this.changeMode(0);
            break;

        }

    }

    /**
     * Methode qui retourne l'image actuelle qui vient d'être construite
     * 
     * @return nextImage, l'image construite à l'instant t ou on appel cette
     *         fonction
     */
    public LcdImage currentImage() {
        return this.nextImage == null ? EMPTY_IMAGE : nextImage;
    }

    /**
     * Méthode qui permet de changer le mode/état du lcdControler et qui lance
     * les intéreputions nécessaire en fonctions de certaines conditions
     * 
     * @param value
     *            la valeur du mode qu'on veut (entre 0 et 3 compris)
     */
    private void changeMode(int value) {
        Preconditions.checkArgument(value >= 0 && value <= 3);
        switch (value) {
        case 0: {
            setMode(false, false);
            if (Bits.test(register.get(Reg.STAT), STAT.INT_MODE0))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
            break;
        case 1: {
            setMode(true, false);
            if (Bits.test(register.get(Reg.STAT), STAT.INT_MODE1))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            cpu.requestInterrupt(Interrupt.VBLANK);
        }
            break;
        case 2: {
            setMode(false, true);
            if (Bits.test(register.get(Reg.STAT), STAT.INT_MODE2))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
            break;
        case 3: {
            setMode(true, true);
        }
            break;

        }
    }

    /**
     * Méthode modifiant les bits du registre STAT correspondant au mode du
     * lcdController
     * 
     * @param mode0
     *            la valeur du bit MODE0
     * @param mode1
     *            la valeur du bit MODE1
     */
    private void setMode(boolean mode0, boolean mode1) {
        register.setBit(Reg.STAT, STAT.MODE0, mode0);
        register.setBit(Reg.STAT, STAT.MODE1, mode1);
    }

    /**
     * Méthode permettant de changer la valeur du registre LY ou LYC, de
     * comparet si ils sont égaux et de lancer des excetpions sous certaines
     * conditions
     * 
     * @param reg
     *            le registre (LY ou LYC) qu'on veut modifier
     * @param data
     *            la valeur qu'on veut mettre dans ce registre
     */
    private void changeLyLyc(Reg reg, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkArgument(reg == Reg.LY || reg == Reg.LYC);

        register.set(reg, data);
        if (this.checkLyLyc()) {
            register.setBit(Reg.STAT, STAT.LYC_EQ_LY, true);
            if (Bits.test(register.get(Reg.STAT), STAT.INT_LYC)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else {
            register.setBit(Reg.STAT, STAT.LYC_EQ_LY, false);
        }

    }

    /**
     * Méthode qui verifie si les registre LY et LYC sont égaux
     * 
     * @return true si LY == LYC, false sinon
     */
    private boolean checkLyLyc() {
        return (register.get(Reg.LY) == register.get(Reg.LYC));
    }

    /**
     * Méthode permettant de recupérer la valeur d'un registre Reg en donnant
     * l'address de celui si
     * 
     * @param addresse
     *            l'address du registre
     * @return la valeur du registre correspondant
     */
    private int getRegValue(int address) {
        Preconditions.checkBits16(address);
        int value = 0;
        switch (address) {
        case regMap.LCDC:
            value = register.get(Reg.LCDC);
            break;
        case regMap.STAT:
            value = register.get(Reg.STAT);
            break;
        case regMap.SCY:
            value = register.get(Reg.SCY);
            break;
        case regMap.SCX:
            value = register.get(Reg.SCX);
            break;
        case regMap.LY:
            value = register.get(Reg.LY);
            break;
        case regMap.LYC:
            value = register.get(Reg.LYC);
            break;
        case regMap.DMA:
            value = register.get(Reg.DMA);
            break;
        case regMap.BGP:
            value = register.get(Reg.BGP);
            break;
        case regMap.OBP0:
            value = register.get(Reg.OBP0);
            break;
        case regMap.OBP1:
            value = register.get(Reg.OBP1);
            break;
        case regMap.WY:
            value = register.get(Reg.WY);
            break;
        case regMap.WX:
            value = register.get(Reg.WX);
            break;
        }

        return value;
    }

    /**
     * Méthode permettant de modifer la valeur d'un registre Reg en fournissant
     * l'adresse lui correspondant
     * 
     * @param address
     *            l'adresse lié au registre
     * @param data
     *            la valeur qu'on veut écrire dans le registre
     */
    private void setRegValue(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        switch (address) {
        case regMap.LCDC:
            register.set(Reg.LCDC, data);
            break;
        case regMap.STAT:
            register.set(Reg.STAT, data);
            break;
        case regMap.SCY:
            register.set(Reg.SCY, data);
            break;
        case regMap.SCX:
            register.set(Reg.SCX, data);
            break;
        case regMap.LY:
            register.set(Reg.LY, data);
            break;
        case regMap.LYC:
            register.set(Reg.LYC, data);
            break;
        case regMap.DMA:
            register.set(Reg.DMA, data);
            break;
        case regMap.BGP:
            register.set(Reg.BGP, data);
            break;
        case regMap.OBP0:
            register.set(Reg.OBP0, data);
            break;
        case regMap.OBP1:
            register.set(Reg.OBP1, data);
            break;
        case regMap.WY:
            register.set(Reg.WY, data);
            break;
        case regMap.WX:
            register.set(Reg.WX, data);
            break;
        }
    }

    /**
     * Méthode permettant de calculer la ligne à renvoyer pour créer l'image à
     * retourner
     * 
     * @param ligne
     *            l'index de la ligne a dessiner
     * @return la ligne composé du backgrounde, de la fenêtre et des sprites
     */
    private LcdImageLine computeLine(int ligne) {

        // BACKGROUND
        LcdImageLine.Builder nextLineBuilderBG = new LcdImageLine.Builder(
                BG_SIZE);
        int scx = register.get(Reg.SCX);
        int lineBG = Math.floorMod(ligne + register.get(Reg.SCY), BG_SIZE);
        int tileLineBG = lineBG / TILE_LINE;
        int lineInTileBG = Math.floorMod(lineBG, TILE_LINE);
        int displayDataBG = (Bits.test(register.get(Reg.LCDC), LCDC.BG_AREA))
                ? 1
                : 0;

        // FENETRE
        LcdImageLine.Builder nextLineBuilderWD = new LcdImageLine.Builder(
                BG_SIZE);
        int tileLineWD = winY / TILE_LINE;
        int lineInTileWD = winY % TILE_LINE;
        int displayDataWD = (Bits.test(register.get(Reg.LCDC), LCDC.WIN_AREA))
                ? 1
                : 0;
        int wxPrime = Math.max(0, register.get(Reg.WX) - WX_OFF); // POUR REGLER
                                                                  // ZELDA, VU
                                                                  // AVEC
                                                                  // L'INSTRUCTEUR
        int wy = register.get(Reg.WY);

        // SPRITES
        List<LcdImageLine> composeSpriteLine = null;
        BitVector opacityToBelow = EMPTY_VECTOR;

        if (Bits.test(register.get(Reg.LCDC), LCDC.OBJ)) {
            int[] spritesIndex = this.spritesIntersectingLine(ligne);
            List<LcdImageLine> SpritesLine = this.listSpritesLine(spritesIndex);
            composeSpriteLine = composeSprites(spritesIndex, SpritesLine);
        }

        // COMMUN
        int msb;
        int lsb;
        int palette = register.get(Reg.BGP);

        // CALCUL DU BACKGROUND
        if (register.testBit(Reg.LCDC, LCDC.BG)) {
            for (int i = 0; i < TILE_BY_BG; ++i) {
                int[] value = getMsbLsb(tileLineBG, lineInTileBG, displayDataBG,
                        i);
                msb = value[0];
                lsb = value[1];
                nextLineBuilderBG.setBytes(i, Bits.reverse8(msb),
                        Bits.reverse8(lsb));
            }
        }

        LcdImageLine nextImageLine = nextLineBuilderBG.build()
                .mapColors(palette).extractWrapped(scx, LCD_WIDTH);

        // CALCUL L'OPACITE ENTRE LE BACKGROUND ET LES SPRITES D'ARRIERE PLAN ET
        // COMPOSE LES DEUX LIGNES ENSMEMBLES
        if (register.testBit(Reg.LCDC, LCDC.OBJ)) {
            opacityToBelow = nextImageLine.opacity().not()
                    .and(composeSpriteLine.get(0).opacity());

            nextImageLine = nextImageLine.below(composeSpriteLine.get(0),
                    opacityToBelow);
        }

        // DESSIN DE LA FENETRE
        if (register.testBit(Reg.LCDC, LCDC.WIN) && wxPrime >= 0
                && wxPrime < LCD_WIDTH && wy <= ligne) {
            for (int i = 0; i < TILE_BY_BG; ++i) {
                int[] value = getMsbLsb(tileLineWD, lineInTileWD, displayDataWD,
                        i);
                msb = value[0];
                lsb = value[1];
                nextLineBuilderWD.setBytes(i, Bits.reverse8(msb),
                        Bits.reverse8(lsb));
            }

            winY += 1;
            LcdImageLine windowLine = nextLineBuilderWD.build()
                    .mapColors(palette).extractWrapped(-wxPrime, LCD_WIDTH);
            nextImageLine = nextImageLine.join(windowLine, wxPrime);

        }

        // DESSIN DES SPRITE DU PREMIER PLAN
        if (register.testBit(Reg.LCDC, LCDC.OBJ)) {
            nextImageLine = nextImageLine.below(composeSpriteLine.get(1));
        }

        // RETOURNE LA LIGNE COMPOSEE DES DIFFERENTES LIGNE (FENETRE +
        // BACKGROUND + SPRITES)
        return nextImageLine;

    }

    /**
     * Méthode permettant de calculer la valeur du msb et lsb d'un octet en
     * fonction de certains parametre
     * 
     * @param tileLine
     *            la ligne de la tuile
     * @param lineInTile
     *            la ligne dans la tuile
     * @param displayInfo
     *            l'information donnant le plage d'addresse ou récupérer les
     *            informations des tuiles
     * @param i
     *            l'index de l'octet qu'on veut sur la ligne
     * @return un tableau d'entier de taille 2 comportant les les 8bit du msb et
     *         les 8 bits du lsb
     */
    private int[] getMsbLsb(int tileLine, int lineInTile, int displayInfo,
            int i) {

        int lsb, msb;
        int tileNumber = this.read(AddressMap.BG_DISPLAY_DATA[displayInfo]
                + tileLine * TILE_BY_BG + i);
        if (register.testBit(Reg.LCDC, LCDC.TILE_SOURCE)) {
            lsb = this.read(AddressMap.TILE_SOURCE[1] + tileNumber * TILE_SIZE
                    + 2 * lineInTile);
            msb = this.read(AddressMap.TILE_SOURCE[1] + tileNumber * TILE_SIZE
                    + 2 * lineInTile + 1);
        } else {
            tileNumber = tileNumber < TILE_SOURCE_NUMBER
                    ? tileNumber + TILE_SOURCE_NUMBER
                    : tileNumber - TILE_SOURCE_NUMBER;
            lsb = this.read(AddressMap.TILE_SOURCE[0] + tileNumber * TILE_SIZE
                    + 2 * lineInTile);
            msb = this.read(AddressMap.TILE_SOURCE[0] + tileNumber * TILE_SIZE
                    + 2 * lineInTile + 1);
        }

        return new int[] { msb, lsb };
    }

    /**
     * Méthode permttant de calculer les sprites intersectant la ligne en cours
     * de dessin en fonction des règles de priorité (coord X puis index)
     * 
     * @param ligne
     *            la ligne en cours de dessin
     * @return un tableau d'entier contenant les indexs des sprites intersecant
     *         la ligne
     */
    private int[] spritesIntersectingLine(int ligne) {

        int index = 0;
        int spriteFound = 0;
        int[] spriteIntersect = new int[MAX_SPRITE];

        while (index < NUMBER_SPRITE && spriteFound < MAX_SPRITE) {
            int spriteSize = getSize();
            int spriteY = this.getSpriteInfo(index, SPRITE.Y) - SPRITEY_OFF;
            if (ligne >= spriteY && ligne < spriteY + spriteSize) {
                // on enlève pas SPRITEX_OFF pour ne pas avoir de coordonnées
                // négatives qui fausseraient le calcul de priorité des sprites
                int spriteX = this.getSpriteInfo(index, SPRITE.X);
                spriteIntersect[spriteFound] = Bits.make16(spriteX, index);
                spriteFound += 1;
            }
            index += 1;
        }

        Arrays.sort(spriteIntersect, 0, spriteFound);

        int[] finalTab = new int[spriteFound];
        for (int i = 0; i < spriteFound; ++i) {
            finalTab[i] = Bits.clip(Byte.SIZE, spriteIntersect[i]);
        }

        return finalTab;

    }

    /**
     * Méthode similaire a getMsbLsb mais adapté au sprite (leur taille, flipH
     * et flipV)
     * 
     * @param index
     *            l'index du sprite dont on veut calculer les valeurs msb et lsb
     * @return un tableau d'int contenant les valeurs msb et lsb de l'octet du
     *         sprite
     */
    private int[] getSpriteOctet(int index) {

        int lineIndex = register.get(Reg.LY);
        int valueOfIndex = this.getSpriteInfo(index, SPRITE.INDEX);
        int spriteY = this.getSpriteInfo(index, SPRITE.Y) - SPRITEY_OFF;
        int spriteSize = getSize();
        boolean isFlipV = Bits.test(this.getSpriteInfo(index, SPRITE.INFO),
                SINFO.FLIP_V);

        int lineInTile = isFlipV
                ? (spriteSize - 1
                        - Math.floorMod(lineIndex - spriteY, spriteSize))
                : Math.floorMod(lineIndex - spriteY, spriteSize);
        int lsb = this.read(AddressMap.TILE_SOURCE[1] + TILE_SIZE * valueOfIndex
                + 2 * lineInTile);
        int msb = this.read(AddressMap.TILE_SOURCE[1] + TILE_SIZE * valueOfIndex
                + 2 * lineInTile + 1);
        return new int[] { msb, lsb };
    }

    /**
     * Méthode qui calcule la ligne de chaque sprite afin de pouvoir les
     * composer ensemble par la suite
     * 
     * @param spritesIndex
     *            le tableau contenant les indexs des sprites intersectant la
     *            ligne en cours de dessin
     * @return une liste de ligne correspondant aux lignes de chaque sprite
     */
    private List<LcdImageLine> listSpritesLine(int[] spritesIndex) {
        List<LcdImageLine> listSpritesLine = new ArrayList<>();

        for (int i = 0; i < spritesIndex.length; ++i) {
            boolean isFlipH = Bits.test(
                    this.getSpriteInfo(spritesIndex[i], SPRITE.INFO),
                    SINFO.FLIP_H);
            int[] msbLsb = getSpriteOctet(spritesIndex[i]);
            int spriteX = this.getSpriteInfo(spritesIndex[i], SPRITE.X)
                    - SPRITEX_OFF;
            int palette = Bits.test(
                    this.getSpriteInfo(spritesIndex[i], SPRITE.INFO),
                    SINFO.PALETTE) ? register.get(Reg.OBP1)
                            : register.get(Reg.OBP0);

            int msb = isFlipH ? msbLsb[0] : Bits.reverse8(msbLsb[0]);
            int lsb = isFlipH ? msbLsb[1] : Bits.reverse8(msbLsb[1]);

            LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(
                    LCD_WIDTH).setBytes(Byte.SIZE, msb, lsb);
            LcdImageLine line = lineBuilder.build().mapColors(palette)
                    .shift(spriteX - Long.SIZE);

            listSpritesLine.add(line);
        }
        return listSpritesLine;
    }

    /**
     * Méthode qui compose les lignes de sprite ensembl. 2 lignes sont créees, 1
     * pour les sprites d'arrière plan, 1 pour ceux de premier plan
     * 
     * @param index
     *            le tableau contenant les index des spirtes intersectant la
     *            ligne en cours de dessin
     * @param spriteLine
     *            le tableau comportant les lignes de chaque sprites
     * @return
     */
    private List<LcdImageLine> composeSprites(int[] index,
            List<LcdImageLine> spriteLine) {

        LcdImageLine behind = EMPTY_LINE;
        LcdImageLine inFront = EMPTY_LINE;

        for (int i = index.length - 1; i >= 0; --i) {
            boolean isBehind = Bits.test(
                    this.getSpriteInfo(index[i], SPRITE.INFO), SINFO.BEHIND_BG);
            if (isBehind) {
                behind = behind.below(spriteLine.get(i));
            } else {
                inFront = inFront.below(spriteLine.get(i));
            }
        }

        List<LcdImageLine> list = new ArrayList<>();
        list.add(behind);
        list.add(inFront);
        return list;
    }

    /**
     * Méthode permettant de recuprer les différents informations d'un sprite
     * 
     * @param spriteIndex
     *            l'index du spirte dont on veut récuperer l'info
     * @param info
     *            le type d'info (Y, X, INDEX, INFO) SPRITE
     * @return l'info voulu
     */
    private int getSpriteInfo(int spriteIndex, SPRITE info) {
        return this.read(AddressMap.OAM_START + spriteIndex * SPRITE_INFO
                + info.index());
    }

    /**
     * Méthode qui retourne la taille des sprite de la ligne (8x8 ou 8x16)
     * 
     * @return la taille des sprites de la ligne
     */
    private int getSize() {
        return Bits.test(register.get(Reg.LCDC), LCDC.OBJ_SIZE) ? 2 * TILE_LINE
                : TILE_LINE;
    }
}