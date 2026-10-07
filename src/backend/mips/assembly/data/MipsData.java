package backend.mips.assembly.data;

import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public abstract class MipsData extends MipsAssembly {
    public MipsData() {
        super(MipsType.DATA);
    }
}
