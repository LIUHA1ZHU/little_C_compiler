package midEnd.ir;

import midEnd.ir.values.IrUser;

import java.util.ArrayList;

public abstract class IrValue {
    protected String name;
    protected IrValueType valueType;
    protected ArrayList<IrUser> userList;

    public IrValue(String name, IrValueType valueType) {
        this.name = name;
        this.valueType = valueType;
        this.userList = new ArrayList<>();
    }

    public IrValueType getValueType() {
        return valueType;
    }

    public String getName() {
        return name;
    }

    public ArrayList<IrUser> getUserList() {
        return userList;
    }

    public void addUser(IrUser user) {
        userList.add(user);
    }

    public abstract String toString();
}
