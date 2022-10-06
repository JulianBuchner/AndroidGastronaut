package at.gastronaut.android.classes;

public class RenamedPosition extends MenuItem {
    public int tempId;
    public String addon;
    public int version;

    public RenamedPosition(int id, String name, MenuItem parent) {
        this.tempId = id;
        this.addon = name;
        version = 1;

        takeProperties(parent);
    }

    @Override
    public String toString() {
        return super.toString() + " " + addon;
    }
}
