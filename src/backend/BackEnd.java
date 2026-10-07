package backend;

import backend.mips.MipsBuilder;
import backend.mips.MipsModule;
import midEnd.ir.IrModule;
import utils.Config;
import optimizers.modifiers.BasicBlockMerger;


public class BackEnd {
    private IrModule midEndModule;
    private MipsModule backEndModule;

    public BackEnd(IrModule midEndModule) {
        this.midEndModule = midEndModule;
    }

    public void buildMips() {
        backEndModule = new MipsModule();
        MipsBuilder.SetBackEndModule(backEndModule);

        midEndModule.toMips();

    }

    public MipsModule getMipsModule() {
        return backEndModule;
    }
}
