package at.gasronaut.android.classes;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;

public class Voucher implements Cloneable {
    public static final int TYPE_FIXED_VALUE = 0;
    public static final int TYPE_PERCENT = 1;
    public static final int TYPE_EURO = 2;
    public static final int TYPE_TABLE = 3;

    public static final int TABLE_TYPE_POSITION = 0;
    public static final int TABLE_TYPE_CASH = 1;

    private int id;
    private String name;
    private double value;
    private String type;
    private int voucherType;
    private int voucherTableType;
    private ArrayList<Integer> tables;
    private int version = 1;

    private int versionCounter = 0;

    public Voucher(int id, String name, double value, String type, int voucherType) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
        this.voucherType = voucherType;
        this.version = 1;
    }

    public Voucher(JSONObject obj) {
        try {
            this.id = obj.getInt("voucher_id");
            this.name = obj.getString("name");
            this.value = obj.getDouble("value") - obj.getDouble("usedAmount");
            this.type = obj.getString("type");
            this.voucherType = obj.getInt("voucher_type");
            this.voucherTableType = obj.getInt("voucher_table_type");

            this.tables = new ArrayList<>();
            JSONArray tables = obj.getJSONArray("tables");

            for (int i = 0; i < tables.length(); ++i) {
                this.tables.add(tables.getJSONObject(i).getInt("table_id"));
            }

            this.version = 1;
        } catch (Exception e) {
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public int getVoucherType() {
        return voucherType;
    }

    public int getVoucherTableType() {
        return voucherTableType;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean voucherForTable(int table) {
        return this.tables != null && this.tables.contains(table);
    }

    public static ArrayList<Voucher> getVouchersListForType(ArrayList<Voucher> list, String type) {
        ArrayList<Voucher> result = new ArrayList<>();
        for (Voucher v : list) {
            if (v.getType().equals(type)) {
                result.add(v);
            }
        }

        return result;
    }

    public static ArrayList<Voucher> getVoucherListFromXML(Node config) {
        ArrayList<Voucher> result = new ArrayList<>();
        while (config != null) {
            if (config.getNodeType() == Node.TEXT_NODE) {
                config = config.getNextSibling();
                continue;
            }

            Element childElement = (Element) config;

            Voucher v = new Voucher(Integer.parseInt(childElement.getAttribute("id")), childElement.getAttribute("name"), Double.parseDouble(childElement.getAttribute("voucher_value")), childElement.getAttribute("type"), Integer.parseInt(childElement.getAttribute("voucher_type")));

            result.add(v);

            config = config.getNextSibling();
        }

        return result;
    }

    public static Voucher getTableVoucher(int mcKey) {
        Voucher tableVoucher = null;

        if (Configuration.tableVouchers != null) {
            for (Voucher voucher : Configuration.tableVouchers) {
                if (voucher.getType().equalsIgnoreCase("") || voucher.getType().equalsIgnoreCase(Configuration.getItem(mcKey).type)) {
                    tableVoucher = voucher;

                    break;
                }
            }
        }

        return tableVoucher;
    }

    public static Voucher getTableVoucherByVoucherId(int voucherId) {
        Voucher tableVoucher = null;

        if (Configuration.tableVouchers != null) {
            for (Voucher v : Configuration.tableVouchers) {
                if (v.getId() == voucherId) {
                    tableVoucher = v;

                    break;
                }
            }
        }

        return tableVoucher;
    }

    @Override
    public Voucher clone() {
        Voucher v = new Voucher(id, name, value, type, voucherType);
        v.voucherTableType = voucherTableType;

        ++versionCounter;
        v.version = this.version + versionCounter;

        return v;
    }


    public boolean equals(Voucher v) {
        if (v != null && v.getId() == this.getId() && v.version == this.version) {
            return true;
        }

        return false;
    }

    public static Voucher getVoucherAndSplit(int itemId) {
        Voucher tableVoucher = Voucher.getTableVoucher(itemId);

        if (tableVoucher != null && tableVoucher.getValue() > 0.01) {
            if (tableVoucher.getVoucherTableType() == Voucher.TABLE_TYPE_POSITION) {
                tableVoucher.setValue(tableVoucher.getValue() - 1);
            } else if (tableVoucher.getVoucherTableType() == Voucher.TABLE_TYPE_CASH) {
                tableVoucher.setValue(tableVoucher.getValue() - Configuration.getItem(itemId).price);

                if (tableVoucher.getValue() < 0) {
                    Voucher tmp = tableVoucher.clone();
                    tableVoucher.setValue(0);
                    tableVoucher = tmp;
                }
            }
        } else {
            tableVoucher = null;
        }

        return tableVoucher;
    }

    public static void removeTableVoucherFromPos(BillPosition bp) {
        if (bp != null && Configuration.tableVouchers != null && bp.voucher != null) {
            Voucher v = Voucher.getTableVoucherByVoucherId(bp.voucher.getId());
            if (v != null) {
                if (v.getVoucherTableType() == Voucher.TABLE_TYPE_POSITION) {
                    v.setValue(v.getValue() + 1);
                } else if (v.getVoucherTableType() == Voucher.TABLE_TYPE_CASH) {
                    if (bp.voucher.getValue() < 0) {
                        v.setValue(v.getValue() + bp.item.price - Math.abs(bp.voucher.getValue()));
                    } else {
                        v.setValue(v.getValue() + bp.item.price);
                    }
                }
            }
        }
    }
}