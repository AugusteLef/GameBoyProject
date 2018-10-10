package Debug;

import ch.epfl.gameboj.component.Component;

public final class DebugPrintComponent implements Component {

    @Override
    public int read(int address) {
      return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
      if (address == 0xFF01)
        System.out.print((char)data);
    }
    
}
