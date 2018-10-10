package ch.epfl.gameboj;

public interface AddressMap {
    int[] RESETS = new int[] { 0x00, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x38 };
    int[] INTERRUPTS = new int[] { 0x40, 0x48, 0x50, 0x58, 0x60 };

    int BOOT_ROM_START = 0x0000, BOOT_ROM_END = 0x0100,
            BOOT_ROM_SIZE = BOOT_ROM_END - BOOT_ROM_START;
    int VIDEO_RAM_START = 0x8000, VIDEO_RAM_END = 0xA000,
            VIDEO_RAM_SIZE = VIDEO_RAM_END - VIDEO_RAM_START;
    int WORK_RAM_START = 0xC000, WORK_RAM_END = 0xE000,
            WORK_RAM_SIZE = WORK_RAM_END - WORK_RAM_START;
    int ECHO_RAM_START = 0xE000, ECHO_RAM_END = 0xFE00,
            ECHO_RAM_SIZE = ECHO_RAM_END - ECHO_RAM_START;
    int OAM_START = 0xFE00, OAM_END = 0xFEA0,
            OAM_RAM_SIZE = OAM_END - OAM_START;
    int HIGH_RAM_START = 0xFF80, HIGH_RAM_END = 0xFFFF,
            HIGH_RAM_SIZE = HIGH_RAM_END - HIGH_RAM_START;

    // Video RAM
    int[] TILE_SOURCE = new int[] { 0x8800, 0x8000 };
    int[] BG_DISPLAY_DATA = new int[] { 0x9800, 0x9C00 };

    // Memory-mapped "IO" registers
    int REGS_START = 0xFF00;
    int REG_P1 = 0xFF00;
    int REG_DIV = 0xFF04;
    int REG_TIMA = 0xFF05;
    int REG_TMA = 0xFF06;
    int REG_TAC = 0xFF07;
    int REG_IF = 0xFF0F;
    int REGS_LCDC_START = 0xFF40, REGS_LCDC_END = 0xFF4C;
    int REG_BOOT_ROM_DISABLE = 0xFF50;
    int REG_IE = 0xFFFF;
}
