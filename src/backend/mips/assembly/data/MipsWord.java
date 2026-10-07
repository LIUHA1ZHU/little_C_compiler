package backend.mips.assembly.data;

import java.util.ArrayList;

public class MipsWord extends MipsData {
    private final String name;
    private final ArrayList<Integer> values;

    public MipsWord(String name, ArrayList<Integer> values) {
        this.name = name;
        this.values = values;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": .word ");
        for (int i = 0; i < values.size(); i++) {
            Integer value = values.get(i);
            sb.append(value);
            if (i != values.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
