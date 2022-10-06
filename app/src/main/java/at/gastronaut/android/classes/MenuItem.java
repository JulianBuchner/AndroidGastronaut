package at.gastronaut.android.classes;

public class MenuItem {
    public static final int VISIBILITY_DEFAULT = 0;
    public static final int VISIBILITY_HIDE_ON_BILL = 1;
    public static final int VISIBILITY_SHOW_ALWAYS_ON_BILL = 2;
    public static final int VISIBILITY_HIDE_ON_PRINTING = 3;

    public static final int OVERRIDE_NEGATIVE_AMOUNT_GLOBAL = 0;
    public static final int OVERRIDE_NEGATIVE_AMOUNT_YES = 1;
    public static final int OVERRIDE_NEGATIVE_AMOUNT_NO = 2;

    public static final int USE_NO_COLOR =  0;
    public static final int USE_PARENT_COLOR = 1;
    public static final int USE_COLOR = 2;

    public MenuItem subItem;

    public MenuItem nextItem;

    public String name;
    public float price;
    public String type;
    public int id;
    public int categoryId = 0;
    public int parentCategoryId = 0;
    public boolean locked = false;
    public boolean hideOnBill= false;
    public boolean showAlwaysOnBill = false;
    public boolean hideOnPrinting = false;
    public int overrideNegativeAmounts = 0;
    public int sort;

    public String lockedReason;
    public boolean hidden = false;
    public boolean showBillDialog = true;
    public boolean showTableDialog = true;
    public boolean shortlink = false;

    public int useColor = 0;
    public String color = "";


    @Override
    public MenuItem clone() {
        MenuItem clone = new MenuItem();

        clone.subItem = subItem;
        clone.nextItem = nextItem;
        clone.name = name;
        clone.price = price;
        clone.id = id;
        clone.type = type;
        clone.categoryId = categoryId;
        clone.parentCategoryId = parentCategoryId;
        clone.locked = locked;
        clone.sort = sort;
        clone.lockedReason = lockedReason;
        clone.hidden = hidden;
        clone.hideOnBill = hideOnBill;
        clone.showBillDialog = showBillDialog;
        clone.showTableDialog = showTableDialog;
        clone.showAlwaysOnBill = showAlwaysOnBill;
        clone.hideOnPrinting = hideOnPrinting;
        clone.overrideNegativeAmounts = overrideNegativeAmounts;
        clone.shortlink = shortlink;
        clone.useColor = useColor;
        clone.color = color;

        return clone;
    }

    protected void takeProperties(MenuItem i) {
        this.subItem = i.subItem;
        this.nextItem = i.nextItem;
        this.name = i.name;
        this.price = i.price;
        this.id = i.id;
        this.type = i.type;
        this.categoryId = i.categoryId;
        this.parentCategoryId = i.parentCategoryId;
        this.locked = i.locked;
        this.sort = i.sort;
        this.lockedReason = i.lockedReason;
        this.hidden = i.hidden;
        this.hideOnBill = i.hideOnBill;
        this.showBillDialog = i.showBillDialog;
        this.showTableDialog = i.showTableDialog;
        this.showAlwaysOnBill = i.showAlwaysOnBill;
        this.hideOnPrinting = i.hideOnPrinting;
        this.overrideNegativeAmounts = i.overrideNegativeAmounts;
        this.shortlink = i.shortlink;
        this.useColor = i.useColor;
        this.color = i.color;
    }

    @Override
    public String toString() {
        return name;
    }
}
