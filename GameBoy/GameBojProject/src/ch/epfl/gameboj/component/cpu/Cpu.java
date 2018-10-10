package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.*;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;
import ch.epfl.gameboj.component.memory.Ram;

import java.util.Objects;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;

/**
 * Classe qui représente le processeur du Game Boy
 * @author Auguste Lefevre (269821) Marc Watine (269508)
 *
 */
public final class Cpu implements Component, Clocked {

    private final static int MAX_VALUE = 0xFFFF;
    
    /**
     * Type enuméré qui représente les registres (8Bits)
     *
     */
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    /**
     * Type enuméré qui représente les paires de registre (16Bits)
     *
     */
    private enum Reg16 implements Register {
        AF(Reg.A, Reg.F), BC(Reg.B, Reg.C), DE(Reg.D, Reg.E), HL(Reg.H, Reg.L);

        private Reg h;
        private Reg l;

        private Reg16(Reg h, Reg l) {
            this.h = h;
            this.l = l;
        }
    }

    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    /**
     * Quelque variable necessaire au bon fonctionnement de la classe CPU (les
     * noms sont explicites)
     */
    private final RegisterFile<Reg> bits8Register = new RegisterFile<>(
            Reg.values());
    private int PC = 0;
    private int SP = 0;
    private int IE = 0;
    private int IF = 0;
    private long nextNonIdleCycle = 0;
    private Bus bus;
    private final Ram HighRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private boolean IME = false;

    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.PREFIXED);

    private static final int PREFIXED_ENCODING = 0xCB;
    private static final int INTERRUPTS_CYCLE = 5;
    private static final int MAX_PC16 = 0xFFFE;

    @Override
    public void cycle(long cycle) {

        if (cycle == nextNonIdleCycle) {
            reallyCycle();

        } else if (((IE & IF) != 0) && (Long.MAX_VALUE == nextNonIdleCycle)) {
            nextNonIdleCycle = cycle;
            reallyCycle();
        }
    }

    /**
     * Methode qui regarde si les interruptions sont activées (c à d si IME est
     * vrai) et si une interruption est en attente, auquel cas elle la gère
     * comme décrit plus haut ; sinon, elle exécute normalement la prochaine
     * instruction
     */
    private void reallyCycle() {
        if (IME && ((IE & IF) != 0)) {
            IME = false;
            int index = getIEIFIndex();
            IF = Bits.set(IF, index, false);
            push16(PC);
            PC = AddressMap.INTERRUPTS[index];
            this.nextNonIdleCycle += INTERRUPTS_CYCLE;
        } else {
            int code = read8(PC);
            Opcode opcode;
            if (code == PREFIXED_ENCODING) {
                int prefixedInstruction = read8AfterOpcode();
                opcode = PREFIXED_OPCODE_TABLE[prefixedInstruction];
            } else {
                opcode = DIRECT_OPCODE_TABLE[code];
            }
            dispatch(opcode);
        }

    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if ((address >= AddressMap.HIGH_RAM_START)
                && (address < AddressMap.HIGH_RAM_END)) {
            return this.HighRam.read(address - AddressMap.HIGH_RAM_START);
        } else if (address == AddressMap.REG_IE) {
            return IE;

        } else if (address == AddressMap.REG_IF) {
            return IF;

        } else
            return NO_DATA;
    }

    @Override
    public void write(int address, int data) {

        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if ((address >= AddressMap.HIGH_RAM_START)
                && (address < AddressMap.HIGH_RAM_END)) {
            HighRam.write(address - AddressMap.HIGH_RAM_START, data);

        } else if (address == AddressMap.REG_IE) {
            IE = data;

        } else if (address == AddressMap.REG_IF) {
            IF = data;
        }
    }

    /**
     * Methode dont le seul but est de faciliter les tests
     * 
     * @return un tableau avec les valeur de PCSPAFBCDEHL
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] registerValue = new int[10];
        registerValue[0] = PC;
        registerValue[1] = SP;
        registerValue[2] = bits8Register.get(Reg.A);
        registerValue[3] = bits8Register.get(Reg.F);
        registerValue[4] = bits8Register.get(Reg.B);
        registerValue[5] = bits8Register.get(Reg.C);
        registerValue[6] = bits8Register.get(Reg.D);
        registerValue[7] = bits8Register.get(Reg.E);
        registerValue[8] = bits8Register.get(Reg.H);
        registerValue[9] = bits8Register.get(Reg.L);

        return registerValue;
    }

    /*
     * 
     * 
     * 
     * 
     * EXECUTION D'UNE INSTRUCTION
     * 
     * 
     * 
     * 
     */

    /**
     * Methode qui définie l'action a réaliser en fonction de l'Opcode recu. On
     * retrouve donc tout les opcodes possible (par famille) ainsi que l'action
     * qu'ils définissent
     * 
     * @param opcode
     *            L'Opcode qui définit l'action a réaliser (ce que le programme
     *            doit faire)
     */
    private void dispatch(Opcode opcode) {
        boolean isConditional = false;
        int nextPC = PC + opcode.totalBytes;

        switch (opcode.family) {

        case NOP: {
        }
            break;
        case LD_R8_HLR: {
            Reg R8 = extractReg(opcode, 3);
            int HLData = read8AtHl();
            bits8Register.set(R8, HLData);
        }
            break;
        case LD_A_HLRU: {
            int HLData = read8AtHl();
            int HL = reg16(Reg16.HL);
            int increment = extractHlIncrement(opcode);
            bits8Register.set(Reg.A, HLData);
            setReg16(Reg16.HL, Bits.clip(Short.SIZE, HL + increment));
        }
            break;
        case LD_A_N8R: {
            bits8Register.set(Reg.A,
                    read8(AddressMap.REGS_START + read8AfterOpcode()));
        }
            break;
        case LD_A_CR: {
            bits8Register.set(Reg.A,
                    read8(AddressMap.REGS_START + bits8Register.get(Reg.C)));
        }
            break;
        case LD_A_N16R: {
            bits8Register.set(Reg.A, read8(read16AfterOpcode()));
        }
            break;
        case LD_A_BCR: {
            bits8Register.set(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;
        case LD_A_DER: {
            bits8Register.set(Reg.A, read8(reg16(Reg16.DE)));
        }
            break;
        case LD_R8_N8: {
            Reg R8 = extractReg(opcode, 3);
            int N8 = read8AfterOpcode();
            bits8Register.set(R8, N8);
        }
            break;
        case LD_R16SP_N16: {
            Reg16 R16 = extractReg16(opcode);
            int N16 = read16AfterOpcode();
            setReg16SP(R16, N16);
        }
            break;
        case POP_R16: {
            Reg16 R16 = extractReg16(opcode);
            setReg16(R16, pop16());
        }
            break;
        case LD_HLR_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            write8AtHl(R8Value);
        }
            break;
        case LD_HLRU_A: {
            int HL = reg16(Reg16.HL);
            int v = bits8Register.get(Reg.A);
            write8AtHl(v);
            setReg16(Reg16.HL,
                    Bits.clip(Short.SIZE, HL + extractHlIncrement(opcode)));
        }
            break;
        case LD_N8R_A: {
            int address = AddressMap.REGS_START + read8AfterOpcode();
            int v = bits8Register.get(Reg.A);
            write8(address, v);
        }
            break;
        case LD_CR_A: {
            int address = AddressMap.REGS_START + bits8Register.get(Reg.C);
            int v = bits8Register.get(Reg.A);
            write8(address, v);
        }
            break;
        case LD_N16R_A: {
            int address = read16AfterOpcode();
            int v = bits8Register.get(Reg.A);
            write8(address, v);
        }
            break;
        case LD_BCR_A: {
            int address = reg16(Reg16.BC);
            int v = bits8Register.get(Reg.A);
            write8(address, v);
        }
            break;
        case LD_DER_A: {
            int address = reg16(Reg16.DE);
            int v = bits8Register.get(Reg.A);
            write8(address, v);
        }
            break;
        case LD_HLR_N8: {
            int v = read8AfterOpcode();
            write8AtHl(v);
        }
            break;
        case LD_N16R_SP: {
            int address = read16AfterOpcode();
            write16(address, SP);
        }
            break;
        case LD_R8_R8: {
            Reg R8Direction = extractReg(opcode, 3);
            int R8ToPutIn = bits8Register.get(extractReg(opcode, 0));
            bits8Register.set(R8Direction, R8ToPutIn);
        }
            break;
        case LD_SP_HL: {
            int HL = reg16(Reg16.HL);
            SP = HL;
        }
            break;
        case PUSH_R16: {
            int R16 = reg16(extractReg16(opcode));
            push16(R16);
        }
            break;

        case ADD_A_R8: {
            boolean carryTest = carryTest(opcode);
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int AValue = bits8Register.get(Reg.A);
            int addition = Alu.add(AValue, R8Value, carryTest);
            setRegFlags(Reg.A, addition);

        }
            break;
        case ADD_A_N8: {
            boolean carryTest = carryTest(opcode);
            int N8Value = read8AfterOpcode();
            int AValue = bits8Register.get(Reg.A);
            int addition = Alu.add(AValue, N8Value, carryTest);
            setRegFlags(Reg.A, addition);

        }
            break;
        case ADD_A_HLR: {
            boolean carryTest = carryTest(opcode);
            int HLValue = read8AtHl();
            int AValue = bits8Register.get(Reg.A);
            int addition = Alu.add(AValue, HLValue, carryTest);
            setRegFlags(Reg.A, addition);

        }
            break;
        case INC_R8: {
            Reg R8 = extractReg(opcode, 3);
            int R8Value = bits8Register.get(R8);
            int addition = Alu.add(R8Value, 1);
            setRegFromAlu(R8, addition);
            combineAluFlags(addition, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);

        }
            break;
        case INC_HLR: {
            int HLValue = read8AtHl();
            int addition = Alu.add(HLValue, 1);
            int valueAdd = Alu.unpackValue(addition);
            write8AtHl(valueAdd);
            combineAluFlags(addition, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case INC_R16SP: {
            Reg16 R16 = extractReg16(opcode);
            int value;
            if (R16 == Reg16.AF) {
                value = SP;
            } else {
                value = reg16(R16);
            }
            setReg16SP(R16, Alu.unpackValue(Alu.add16L(value, 1)));
        }
            break;
        case ADD_HL_R16SP: {
            int HL = reg16(Reg16.HL);
            Reg16 R16 = extractReg16(opcode);
            int R16Value;
            if (R16 == Reg16.AF) {
                R16Value = SP;
            } else {
                R16Value = reg16(R16);
            }
            int addition = Alu.add16H(HL, R16Value);
            int result = Alu.unpackValue(addition);
            setReg16SP(Reg16.HL, result);
            combineAluFlags(addition, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);

        }
            break;
        case LD_HLSP_S8: {
            int SPValue = SP;
            int S8 = read8AfterOpcode();
            S8 = Bits.signExtend8(S8);
            S8 = Bits.clip(16, S8);
            int addition = Alu.add16L(SPValue, S8);
            int addValue = Alu.unpackValue(addition);
            if (Bits.test(opcode.encoding, 4)) {
                setReg16(Reg16.HL, addValue);
            } else {
                SP = addValue;
            }
            combineAluFlags(addition, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);

        }
            break;

        // Subtract
        case SUB_A_R8: {
            boolean carryTest = carryTest(opcode);
            int AValue = bits8Register.get(Reg.A);
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int soustraction = Alu.sub(AValue, R8Value, carryTest);
            setRegFlags(Reg.A, soustraction);
        }
            break;
        case SUB_A_N8: {
            boolean carryTest = carryTest(opcode);
            int AValue = bits8Register.get(Reg.A);
            int N8 = read8AfterOpcode();
            int soustraction = Alu.sub(AValue, N8, carryTest);
            setRegFlags(Reg.A, soustraction);
        }
            break;
        case SUB_A_HLR: {
            boolean carryTest = carryTest(opcode);
            int HLValue = read8AtHl();
            int AValue = bits8Register.get(Reg.A);
            int soustraction = Alu.sub(AValue, HLValue, carryTest);
            setRegFlags(Reg.A, soustraction);
        }
            break;
        case DEC_R8: {
            Reg R8 = extractReg(opcode, 3);
            int R8Value = bits8Register.get(R8);
            int soustraction = Alu.sub(R8Value, 1);
            setRegFromAlu(R8, soustraction);
            combineAluFlags(soustraction, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case DEC_HLR: {
            int HLValue = read8AtHl();
            int soustraction = Alu.sub(HLValue, 1);
            int subValue = Alu.unpackValue(soustraction);
            write8AtHl(subValue);
            combineAluFlags(soustraction, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;
        case CP_A_R8: {
            // boolean carryTest = carryTest(opcode);
            int AValue = bits8Register.get(Reg.A);
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int soustraction = Alu.sub(AValue, R8Value);
            setFlags(soustraction);
        }
            break;
        case CP_A_N8: {
            // boolean carryTest = carryTest(opcode);
            int AValue = bits8Register.get(Reg.A);
            int N8 = read8AfterOpcode();
            int soustraction = Alu.sub(AValue, N8);
            setFlags(soustraction);
        }
            break;
        case CP_A_HLR: {
            // boolean carryTest = carryTest(opcode);
            int HLValue = read8AtHl();
            int AValue = bits8Register.get(Reg.A);
            int soustraction = Alu.sub(AValue, HLValue);
            setFlags(soustraction);
        }
            break;
        case DEC_R16SP: {
            Reg16 R16 = extractReg16(opcode);
            int R16Value;
            if (R16 == Reg16.AF) {
                R16Value = SP;
            } else {
                R16Value = reg16(R16);
            }
            int soustractionLow = Alu.sub(Bits.clip(Byte.SIZE, R16Value), 1);
            int soustractionHigh = Bits.extract(R16Value, Byte.SIZE, Byte.SIZE);
            if (Bits.test(Alu.unpackFlags(soustractionLow), Alu.Flag.C)) {
                soustractionHigh = Alu
                        .unpackValue(Alu.sub(soustractionHigh, 1));
            }
            int result = Bits.make16(soustractionHigh,
                    Alu.unpackValue(soustractionLow));
            setReg16SP(R16, result);
        }
            break;

        // And, or, xor, complement
        case AND_A_N8: {
            int AValue = bits8Register.get(Reg.A);
            int N8 = read8AfterOpcode();
            int result = Alu.and(AValue, N8);
            setRegFlags(Reg.A, result);
        }
            break;
        case AND_A_R8: {
            int AValue = bits8Register.get(Reg.A);
            int R8Value = bits8Register.get(extractReg(opcode, 0));
            int result = Alu.and(AValue, R8Value);
            setRegFlags(Reg.A, result);
        }
            break;
        case AND_A_HLR: {
            int HLValue = read8AtHl();
            int AValue = bits8Register.get(Reg.A);
            int result = Alu.and(AValue, HLValue);
            setRegFlags(Reg.A, result);
        }
            break;
        case OR_A_R8: {
            int AValue = bits8Register.get(Reg.A);
            int R8Value = bits8Register.get(extractReg(opcode, 0));
            int result = Alu.or(AValue, R8Value);
            setRegFlags(Reg.A, result);
        }
            break;
        case OR_A_N8: {
            int AValue = bits8Register.get(Reg.A);
            int N8 = read8AfterOpcode();
            int result = Alu.or(AValue, N8);
            setRegFlags(Reg.A, result);
        }
            break;
        case OR_A_HLR: {
            int HLValue = read8AtHl();
            int AValue = bits8Register.get(Reg.A);
            int result = Alu.or(AValue, HLValue);
            setRegFlags(Reg.A, result);
        }
            break;
        case XOR_A_R8: {
            int AValue = bits8Register.get(Reg.A);
            int R8Value = bits8Register.get(extractReg(opcode, 0));
            int result = Alu.xor(AValue, R8Value);
            setRegFlags(Reg.A, result);
        }
            break;
        case XOR_A_N8: {
            int AValue = bits8Register.get(Reg.A);
            int N8 = read8AfterOpcode();
            int result = Alu.xor(AValue, N8);
            setRegFlags(Reg.A, result);
        }
            break;
        case XOR_A_HLR: {
            int HLValue = read8AtHl();
            int AValue = bits8Register.get(Reg.A);
            int result = Alu.xor(AValue, HLValue);
            setRegFlags(Reg.A, result);
        }
            break;
        case CPL: {
            int AValue = bits8Register.get(Reg.A);
            int AComplement = Bits.complement8(AValue);
            bits8Register.set(Reg.A, AComplement);
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;

        // Rotate, shift
        case ROTCA: {
            int AValue = bits8Register.get(Reg.A);
            int result = Alu.rotate(getDir(opcode), AValue);
            setRegFromAlu(Reg.A, result);
            combineAluFlags(result, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;
        case ROTA: {
            boolean C = getFanion(Flag.C);
            int AValue = bits8Register.get(Reg.A);
            int result = Alu.rotate(getDir(opcode), AValue, C);

            setRegFromAlu(Reg.A, result);
            combineAluFlags(result, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;
        case ROTC_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int result = Alu.rotate(getDir(opcode), R8Value);
            setRegFlags(R8, result);
        }
            break;
        case ROT_R8: {
            boolean C = getFanion(Flag.C);
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int result = Alu.rotate(getDir(opcode), R8Value, C);
            setRegFlags(R8, result);
        }
            break;
        case ROTC_HLR: {
            int HLValue = read8AtHl();
            int result = Alu.rotate(getDir(opcode), HLValue);
            write8AtHlAndSetFlags(result);
        }
            break;
        case ROT_HLR: {
            boolean C = getFanion(Flag.C);
            int HLValue = read8AtHl();
            int result = Alu.rotate(getDir(opcode), HLValue, C);
            write8AtHlAndSetFlags(result);
        }
            break;
        case SWAP_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int result = Alu.swap(R8Value);
            setRegFlags(R8, result);
        }
            break;
        case SWAP_HLR: {
            int HLValue = read8AtHl();
            int result = Alu.swap(HLValue);
            write8AtHlAndSetFlags(result);

        }
            break;
        case SLA_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            R8Value = Alu.shiftLeft(R8Value);
            setRegFlags(R8, R8Value);

        }
            break;
        case SRA_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            R8Value = Alu.shiftRightA(R8Value);
            setRegFlags(R8, R8Value);

        }
            break;
        case SRL_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int result = Alu.shiftRightL(R8Value);
            setRegFlags(R8, result);
        }
            break;
        case SLA_HLR: {
            int HLValue = read8AtHl();
            int result = Alu.shiftLeft(HLValue);
            write8AtHlAndSetFlags(result);
        }
            break;
        case SRA_HLR: {
            int HLValue = read8AtHl();
            int result = Alu.shiftRightA(HLValue);
            write8AtHlAndSetFlags(result);
        }
            break;
        case SRL_HLR: {
            int HLValue = read8AtHl();
            int result = Alu.shiftRightL(HLValue);
            write8AtHlAndSetFlags(result);
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int index = getIndex(opcode);
            int maskTest = Alu.testBit(R8Value, index);
            combineAluFlags(maskTest, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;
        case BIT_U3_HLR: {
            int HLValue = read8AtHl();
            int index = getIndex(opcode);
            int maskTest = Alu.testBit(HLValue, index);
            combineAluFlags(maskTest, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;
        case CHG_U3_R8: {
            Reg R8 = extractReg(opcode, 0);
            int R8Value = bits8Register.get(R8);
            int index = getIndex(opcode);
            int result;
            if (getInfo(opcode)) {
                int mask = Bits.mask(index);
                result = R8Value | mask;
            } else {
                int mask = Bits.complement8(Bits.mask(index));
                result = R8Value & mask;
            }
            bits8Register.set(R8, result);

        }
            break;
        case CHG_U3_HLR: {
            int HLValue = read8AtHl();
            int index = getIndex(opcode);
            int result;
            if (getInfo(opcode)) {
                int mask = Bits.mask(index);
                result = HLValue | mask;
            } else {
                int mask = Bits.complement8(Bits.mask(index));
                result = HLValue & mask;
            }
            write8AtHl(result);
        }
            break;

        // Misc. ALU
        case DAA: {
            int AValue = bits8Register.get(Reg.A);
            int FValue = bits8Register.get(Reg.F);
            boolean N = Bits.test(FValue, Flag.N.index());
            boolean H = Bits.test(FValue, Flag.H.index());
            boolean C = Bits.test(FValue, Flag.C.index());
            int result = Alu.bcdAdjust(AValue, N, H, C);
            setRegFlags(Reg.A, result);

        }
            break;
        case SCCF: {
            boolean C = !(carryTest(opcode));
            int mask = Alu.maskZNHC(false, false, false, C);
            combineAluFlags(mask, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;

        // Jumps
        case JP_HL: {
            nextPC = reg16(Reg16.HL);
        }
            break;
        case JP_N16: {
            int n16 = read16AfterOpcode();
            nextPC = n16;

        }
            break;
        case JP_CC_N16: {
            boolean condi = testCondi(opcode);
            if (condi) {
                isConditional = true;
                int n16 = read16AfterOpcode();
                nextPC = n16;
            } 
        }
            break;
        case JR_E8: {
            int E8 = Bits.signExtend8(read8AfterOpcode());
            nextPC = nextPC + E8;
        }
            break;
        case JR_CC_E8: {
            if (testCondi(opcode)) {
                isConditional = true;
                int E8 = Bits.signExtend8(read8AfterOpcode());
                nextPC = nextPC + E8;
            }
        }
            break;

        // Calls and returns
        case CALL_N16: {

            int n16 = read16AfterOpcode();
            push16(nextPC);
            nextPC = n16;
        }
            break;
        case CALL_CC_N16: {

            boolean condi = testCondi(opcode);
            if (condi) {
                isConditional = true;
                int n16 = read16AfterOpcode();
                push16(nextPC);
                nextPC = n16;
            }
        }
            break;
        case RST_U3: {
            int U3 = getRST(opcode);
            push16(nextPC);
            nextPC = AddressMap.RESETS[U3];

        }
            break;
        case RET: {
            nextPC = pop16();
        }
            break;
        case RET_CC: {
            boolean condi = testCondi(opcode);
            if (condi) {
                isConditional = true;
                nextPC = pop16();
            }
        }
            break;

        // Interrupts
        case EDI: {
//            if (Bits.test(opcode.encoding, 3)) {
//                IME = true;
//            } else {
//                IME = false;
//            }
            
            IME = Bits.test(opcode.encoding, 3);
        }
            break;
        case RETI: {
            IME = true;
            nextPC = pop16();
        }
            break;

        // Misc control
        case HALT: {
            this.nextNonIdleCycle = Long.MAX_VALUE;
        }
            break;
        case STOP:
            throw new Error("STOP is not implemented");

        default: {
            throw new IllegalArgumentException(
                    "La famille d'opcode voulu n'a pas encore était codé (étape 4");
        }

        }

        this.nextNonIdleCycle += opcode.cycles;
        if (isConditional) {
            this.nextNonIdleCycle += opcode.additionalCycles;
        }

        this.PC = Bits.clip(16, nextPC);

    }

    /**
     * Methode qui permet la construction d'un tableau d'Opcode afin de pouvoir
     * itérer dessus en cas de besoin
     * 
     * @param kind
     *            le "type" des opcodes qu'on veut placer dans le tableau de
     *            retour
     * @return un tableau avec tout les opcodes (trié) qui sont du type "kind"
     *         recu en argument
     */
    private static Opcode[] buildOpcodeTable(Kind kind) {
        Opcode[] tab = new Opcode[256];
        for (Opcode o : Opcode.values()) {
            if (o.kind == kind) {
                tab[o.encoding] = o;
            }

        }
        return tab;
    }

    /*
     * 
     * 
     * 
     * 
     * ACCES AU BUS
     * 
     * 
     * 
     * 
     * 
     */

    @Override
    public void attachTo(Bus bus) {
        Objects.requireNonNull(bus);
        this.bus = bus;
        bus.attach(this);
    }

    /**
     * lit depuis le bus la valeur 8 bits à l'adresse donnée
     * 
     * @param address
     *            l'adresse a laquelle on va lire la valeur de retour
     * @return la valeur lu a l'adresse passer en argument
     */
    private int read8(int address) {
        Preconditions.checkBits16(address);
        int out = bus.read(address);
        return out;
    }

    /**
     * lit depuis le bus la valeur 8 bits à l'adresse contenue dans la paire de
     * registres HL
     * 
     * @return la valeur contenue a l'adresse correspondant a la valeur de la
     *         pair de registre HL
     */
    private int read8AtHl() {
        int out = read8(reg16(Reg16.HL));
        return out;
    }

    /**
     * lit depuis le bus la valeur 8 bits à l'adresse suivant celle contenue
     * dans le compteur de programme
     * 
     * @return la valeur lu a l'adresse PC+1
     */
    private int read8AfterOpcode() {
        assert PC < MAX_VALUE : "PC doit etre inférieur a 0xFFFF pour la fonction read8AfterOpcode";
        int out = read8(PC + 1);
        return out;
    }

    /**
     * lit depuis le bus la valeur 16 bits à l'adresse donnée
     * 
     * @param address
     *            l'adresse a laquelle on veut lire la valeur contenue
     * @return la valeur (16bits) contenur a l'adresse passer en argument
     * 
     *         PS : on utilise le fait qu'on stocke une valeur 16Bits sur 2
     *         adresses consecutive pour pouvoir les lire
     */
    private int read16(int address) {
        assert address < MAX_VALUE : "L'adresse pour la fonction read16 n'est pas valide ( doit etre <= 0xFFFE)";
        Preconditions.checkBits16(address);
        int out = Bits.make16(read8(Bits.clip(Short.SIZE, address + 1)),
                read8(address));
        return out;
    }

    /**
     * lit depuis le bus la valeur 16 bits à l'adresse suivant celle contenue
     * dans le compteur de programme
     * 
     * @return la valeur 16Bits lu a l'adress PC+1 (et donc PC+2 car valeur
     *         16bits)
     */
    private int read16AfterOpcode() {
        assert PC < MAX_PC16 : "PC doit etre inférieur a 0xFFFE pour la fonction read16AfterOpcode";
        int out = read16(PC + 1);
        return out;
    }

    /**
     * écrit sur le bus, à l'adresse donnée, la valeur 8 bits donnée
     * 
     * @param address
     *            l'adresse a laquelle on veut ecrire
     * @param v
     *            la valeur qu'on va ecrire a l'adresse donnée
     */
    private void write8(int address, int v) {
        Preconditions.checkBits8(v);
        Preconditions.checkBits16(address);
        bus.write(address, v);
    }

    /**
     * qui écrit sur le bus, à l'adresse donnée, la valeur 16 bits donnée
     * 
     * @param address
     *            l'adresse a laquelle on veut ecrire
     * @param v
     *            la valeur qu'on va ecrire a l'adresse donnée
     */
    private void write16(int address, int v) {
        assert address < MAX_VALUE : "L'adresse pour la fonction write16 n'est pas valide ( doit etre <= 0xFFFE)";
        Preconditions.checkBits16(address);
        Preconditions.checkBits16(v);
        write8(address, Bits.clip(Byte.SIZE, v));
        write8(Bits.clip(Short.SIZE, address + 1),
                Bits.extract(v, Byte.SIZE, Byte.SIZE));
    }

    /**
     * écrit sur le bus, à l'adresse contenue dans la paire de registres HL, la
     * valeur 8 bits donnée
     * 
     * @param v
     *            la valeur qu'on veut ecrire a l'adresse HL
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    }

    /**
     * décrémente l'adresse contenue dans le pointeur de pile (registre SP) de 2
     * unités, puis écrit à cette nouvelle adresse la valeur 16 bits donnée
     * 
     * @param v
     *            la valeur qu'on va ecrire à l'adresse SP après l'avoir
     *            decrémenté
     */
    private void push16(int v) {
        Preconditions.checkBits16(v);
        SP = Bits.clip(Short.SIZE, SP - 2);
        write16(SP, v);
    }

    /**
     * lit depuis le bus (et retourne) la valeur 16 bits à l'adresse contenue
     * dans le pointeur de pile (registre SP), puis l'incrémente de 2 unités
     * 
     * @return la valeur contenu à l'adresse SP
     */
    private int pop16() {
        int out = read16(SP);
        SP = Bits.clip(Short.SIZE, SP + 2);
        return out;
    }

    /*
     * 
     * 
     * 
     * 
     * 
     * GESTION DES PAIRES DE REGISTRES
     * 
     * 
     * 
     * 
     * 
     * 
     */

    /**
     * retourne la valeur contenue dans la paire de registres donnée
     * 
     * @param r
     *            la pair de registre dont on veut obtenir la valeur
     * @return la valeur 16Bits de la pair de registre
     */
    private int reg16(Reg16 r) {
        int out = Bits.make16(bits8Register.get(r.h), bits8Register.get(r.l));
        return out;
    }

    /**
     * modifie la valeur contenue dans la paire de registres donnée, en faisant
     * attention de mettre à 0 les bits de poids faible si la paire en question
     * est AF
     * 
     * @param r
     *            la pair de registre dont on veut modifier la valeur
     * @param newV
     *            la valeur qu'on va mettre dans la pair de regsitre
     */
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if (r == Reg16.AF) {
            newV = 0xFFF0 & newV;
            bits8Register.set(r.h, Bits.extract(newV, Byte.SIZE, Byte.SIZE));
            bits8Register.set(r.l, Bits.clip(Byte.SIZE, newV));
        } else {
            bits8Register.set(r.h, Bits.extract(newV, Byte.SIZE, Byte.SIZE));
            bits8Register.set(r.l, Bits.clip(Byte.SIZE, newV));
        }
    }

    /**
     * fait la même chose que setReg16 sauf dans le cas où la paire passée est
     * AF, auquel cas le registre SP est modifié en lieu et place de la paire AF
     * 
     * @param r
     *            la pair de regsitre dont on veut modifier la valeur
     * @param newV
     *            la valeur qu'on va mettre dans la pair de registre
     */
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }
    }

    /*
     * 
     * 
     * 
     * 
     * 
     * EXTRACTION DE PARAMETRES (public ? private ?)
     * 
     * 
     * 
     * 
     * 
     * 
     */

    /**
     * extrait et retourne l'identité d'un registre 8 bits de l'encodage de
     * l'opcode donné, à partir du bit d'index donné
     * 
     * @param opcode
     *            l'opcode dont on va extraire l'identité du regsitre voulu
     * @param startBit
     *            le bit de départ auquel on doit commencer a fair l'etraction
     *            sur l'opcode
     * @return le regsitre extrait sur l'opcode
     */
    public Reg extractReg(Opcode opcode, int startBit) {
        int index = Bits.extract(opcode.encoding, startBit, 3);
        Reg out = null;
        switch (index) {
        case 0: {
            out = Reg.B;
        }
            break;
        case 1: {
            out = Reg.C;
        }
            break;
        case 2: {
            out = Reg.D;
        }
            break;
        case 3: {
            out = Reg.E;
        }
            break;
        case 4: {
            out = Reg.H;
        }
            break;
        case 5: {
            out = Reg.L;
        }
            break;
        case 6: {
            out = null;
            throw new NullPointerException("Registre inexistant");
        }
        case 7: {
            out = Reg.A;
        }
            break;

        }

        return out;
    }

    /**
     * fait la même chose que extractReg mais pour les paires de registres (ne
     * prend pas le paramètre startBit car il vaut 4 pour toutes les
     * instructions du processeur)
     * 
     * @param opcode
     *            l'opcode dont on va extraire l'identité du regsitre recherché
     * @return le regsitre recherché
     */
    public Reg16 extractReg16(Opcode opcode) {
        int index = Bits.extract(opcode.encoding, 4, 2);
        Reg16 out = null;
        switch (index) {
        case 0: {
            out = Reg16.BC;
        }
            break;
        case 1: {
            out = Reg16.DE;
        }
            break;
        case 2: {
            out = Reg16.HL;
        }
            break;
        case 3: {
            out = Reg16.AF;
        }
            break;

        }
        return out;
    }

    /**
     * retourne -1 ou +1 en fonction du bit d'index 4, qui est utilisé pour
     * encoder l'incrémentation ou la décrémentation de la paire HL dans
     * différentes instructions
     * 
     * @param opcode
     *            l'opcode dont on va tester le bit d'index 4
     * @return -1 si le bit d'index 4 vaut 1, 1 sinon
     */
    public int extractHlIncrement(Opcode opcode) {
        int out = Bits.test(opcode.encoding, 4) ? -1 : 1;
        return out;
    }

    /*
     * 
     * 
     * 
     * 
     * 
     * METHODE UTILITAIRE + GESTION DES FANIONS
     * 
     * 
     * 
     * 
     * 
     */

    /**
     * Methode permettant de combiner le fanion C et le bit 3 de l'opcode selon
     * l'une des deux tables données dans l'étape 4 du projet (attention le
     * tableau 2 étant la négation de tableau 1, lorsqu'on veut obtenir une
     * valeur se calculant en fonction du tableau on doit prendre le resultat
     * opposé a celui retourné)
     * 
     * @param opcode
     *            l'opcode dont on va tester le bit 3 avec le fanion C du
     *            registre F
     * @return true si le bit 3 et le fanion C valent 1, 0 sinon
     */
    private boolean carryTest(Opcode opcode) {
        boolean bit3Opcode = Bits.test(opcode.encoding, 3);
        boolean fanionC = Bits.test(bits8Register.get(Reg.F), Flag.C.index());
        return bit3Opcode & fanionC;

    }

    /**
     * qui extrait la valeur stockée dans la paire donnée et la place dans le
     * registre donné
     * 
     * @param r
     *            le regsitre dans lequel on veut stocker la valeur extraite
     * @param vf
     *            la pair (valeur + fanion) dont on va extraitre la valeur
     */
    private void setRegFromAlu(Reg r, int vf) {
        Preconditions.checkBits16(vf);
        int value = Alu.unpackValue(vf);
        bits8Register.set(r, value);
    }

    /**
     * Methode qui extrait les fanions stockés dans la paire donnée et les place
     * dans le registre F
     * 
     * @param valueFlags
     *            la pair (valeur + fanion) dont on va extraire les fanions pour
     *            les mettre dans le registre F
     */
    private void setFlags(int valueFlags) {
        Preconditions.checkBits16(valueFlags);
        int fanion = Alu.unpackFlags(valueFlags);
        bits8Register.set(Reg.F, fanion);

    }

    /**
     * Methode qui combine les effets de setRegFromAlu et setFlags
     * 
     * @param r
     *            le registre ou on veut stocker la valeur
     * @param vf
     *            la pair (valeur + fanions) qu'on va traiter
     */
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    /**
     * qui extrait la valeur stockée dans la paire donnée et l'écrit sur le bus
     * à l'adresse contenue dans la paire de registres HL, puis extrait les
     * fanions stockés dans la paire et les place dans le registre F
     * 
     * @param vf
     *            la pair (valeur et fanion)
     */
    private void write8AtHlAndSetFlags(int vf) {
        int value = Alu.unpackValue(vf);
        write8AtHl(value);
        setFlags(vf);
    }

    /**
     * Methode qui combine les fanions stockés dans le registre F avec ceux
     * contenus dans la paire vf, en fonction des quatre derniers paramètres,
     * qui correspondent chacun à un fanion, et stocke le résultat dans le
     * registre F
     * 
     * @param vf
     *            la valeur qu'on va teste contenant a la fois le resultat d'un
     *            opération et les fanions (cette valeur est en générale issu
     *            d'un resulat d'une methode de ALU
     * @param z
     *            parametre correspondant au fanion Z
     * @param n
     *            parametre correspondant au fanion N
     * @param h
     *            parametre correspondant au fanion H
     * @param c
     *            parametre correspondant au fanion C
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h,
            FlagSrc c) {
        boolean Z, N, H, C;
        Z = combineAluFlagsHelp(vf, z, Flag.Z.index());
        N = combineAluFlagsHelp(vf, n, Flag.N.index());
        H = combineAluFlagsHelp(vf, h, Flag.H.index());
        C = combineAluFlagsHelp(vf, c, Flag.C.index());
        int ZNHC = Alu.maskZNHC(Z, N, H, C);
        bits8Register.set(Reg.F, ZNHC);

    }

    // Methode personnelle pour faciliter le fonctionnement de la fonction
    // précedente combineAluFlags
    private boolean combineAluFlagsHelp(int vf, FlagSrc flag, int index) {
        boolean value = false;
        switch (flag) {
        case V0: {
            value = false;
        }
            break;
        case V1: {
            value = true;
        }
            break;
        case ALU: {
            int flags = Alu.unpackFlags(vf);
            value = Bits.test(flags, index);
        }
            break;
        case CPU: {
            value = Bits.test(bits8Register.get(Reg.F), index);
        }
            break;
        }
        return value;
    }

    /**
     * Methode extrait test la direction de rotation (bit 3), pour toutes les
     * familles regroupant des instructions de rotation à gauche et à droite
     * 
     * @param opcode
     *            l'opcode dont on va extraire le bit 3 pour analyser le sens de
     *            rotation
     * @return le sens de rotation (si le bit 3 vaut 1 RIGHT, sinon LEFT)
     */
    private RotDir getDir(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }

    /**
     * Methode qui extrait l'index du bit à tester ou modifier (bits 3 à 5),
     * pour les instructions BIT, RES et SET
     * 
     * @param opcode
     *            l'opcode dont on va extraire l'index
     * @return la valeur en base 10 du chiffre ecrit en binaire composé des bit
     *         3 à 5
     */
    private int getIndex(Opcode opcode) {
        int index = Bits.extract(opcode.encoding, 3, 3);
        return index;
    }

    /**
     * Methode qui retourne la valeur à attribuer au bit (bit 6) à modifier,
     * pour les instructions RES et SET
     * 
     * @param opcode
     *            L'opcode dont on va tester le bit 6
     * @return true si le bit 6 vaut 1, false sinon
     */
    private boolean getInfo(Opcode opcode) {
        boolean bit = Bits.test(opcode.encoding, 6);
        return bit;
    }

    /**
     * Methode qui permet de tester la valeur d'un fanion (ZNHC) dans le
     * registre F(methode personelle)
     * 
     * @param f
     *            le fanion (ZNHC) dont on veut connaitre la valeur (1 = true, 0
     *            = false)
     * @return true si le fanon tester vaut 1, 0 sinon
     */
    private boolean getFanion(Flag f) {
        boolean fanion = bits8Register.testBit(Reg.F, f);
        return fanion;
    }

    /**
     * 
     * 
     * 
     * 
     * 
     * 
     * Interruptions
     * 
     * 
     * 
     * 
     * 
     * 
     */

    /**
     * Methode qui lève l'interruption donnée, c-à-d met à 1 le bit
     * correspondant dans le registre IF
     * 
     * @param i
     *            l'interruption qu'on veut lever
     */
    public void requestInterrupt(Interrupt i) {
        int index = i.index();
        IF = Bits.set(IF, index, true);
    }

    /**
     * Methode qui test si la condition contenu dans l'opcode est vrai ou fausse
     * et retourne (voir les conditions dans le tableau partie 1.4 de l'etape 5
     * du porjet)
     * 
     * @param opcode
     *            l'opcode dont on extrait la condition a tester
     * @return true si la condition est vérifiée, false sinon
     */
    
    private boolean testCondi(Opcode opcode) {
        int cc = Bits.extract(opcode.encoding, 3, 2);
        boolean toReturn = false;
        switch (cc) {
        case 0: {
            toReturn = !getFanion(Flag.Z);
        }
            break;
        case 1: {
            toReturn = getFanion(Flag.Z);
        }
            break;
        case 2: {
            toReturn = !getFanion(Flag.C);
        }
            break;
        case 3: {
            toReturn = getFanion(Flag.C);
        }
            break;

        }

        return toReturn;
    }

    /**
     * Methode qui extrait la valeur de 3 bit situé dans l'opcode afin d'avoir
     * l'index du tableau contenant les valeurs RESETS (voir AddressMap)
     * 
     * @param opcode
     *            l'opcode dont on va extraire la valeur 3bit
     * @return la valeur du tableau RESEST(AddressMap) situé a l'index de la
     *         valeur 3bit extraite
     */
    private int getRST(Opcode opcode) {
        int value = Bits.extract(opcode.encoding, 3, 3);
        return value;
    }

    /**
     * Methode qui cherche l'index du bit valant 1 a la fois dans IE et IF (le
     * plus petit) n'est utlisé que dans le cas ou il existe un index commune
     * valant 1 entre IE et IF (donc le cas return = 0xFFFFFFFF n'arrivera
     * jamais)
     * 
     * @return l'index du plus petit bit valant 1 a la fois dans IE et IF
     */
    private int getIEIFIndex() {
        for (int i = 0; i < 6; ++i) {
            if (Bits.test(IF, i) && (Bits.test(IE, i))) {
                return i;
            }
        }
        return MAX_VALUE;
    }

    /**
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * METHODES NE FAISANT PAS PARTIE DU PROJET, SEULEMENT UTILISE POUR LES
     * TESTS PERSONNELS
     * 
     * 
     * 
     * 
     * 
     */
    public void setF(int a) {
        Preconditions.checkBits8(a);
        bits8Register.set(Reg.F, a);
    }

    public void setIE(int a) {
        IE = a;
    }

    public void setIF(int a) {
        IF = a;
    }

    public void setF(boolean Z, boolean C) {
        bits8Register.setBit(Reg.F, Flag.UNUSED_0, false);
        bits8Register.setBit(Reg.F, Flag.UNUSED_1, false);
        bits8Register.setBit(Reg.F, Flag.UNUSED_2, false);
        bits8Register.setBit(Reg.F, Flag.UNUSED_3, false);
        bits8Register.setBit(Reg.F, Flag.Z, Z);
        bits8Register.setBit(Reg.F, Flag.N, false);
        bits8Register.setBit(Reg.F, Flag.H, false);
        bits8Register.setBit(Reg.F, Flag.C, C);
    }

    public boolean getIME() {
        return IME;
    }

}
