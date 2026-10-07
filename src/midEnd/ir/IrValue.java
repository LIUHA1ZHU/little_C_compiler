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

    public void setName(String name) {
        this.name = name;
    }

    public String getMipsName() {
        return name.replace("%", "").replace("@", "");
    }

    public ArrayList<IrUser> getUserList() {
        return userList;
    }

    public void addUser(IrUser user) {
        userList.add(user);
    }

    public void removeUser(IrUser user) {
        userList.remove(user);
    }

    public void modifyAllUsersToNewValue(IrValue newValue) {
        ArrayList<IrUser> users = new ArrayList<>(this.userList);
        for (IrUser user : users) {
            user.replaceUse(this, newValue);
        }
    }

    public abstract String toString();

    public abstract void toMips();
}
