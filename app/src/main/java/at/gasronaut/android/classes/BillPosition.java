package at.gasronaut.android.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

public class BillPosition {
    public Voucher voucher = null;
    public MenuItem item = null;

    private int amount = 0;

    public BillPosition(MenuItem item, Voucher v) {
        this.item = item;
        this.voucher = v;
    }

    public static ArrayList<BillPosition> sumUpEntries(Stack<BillPosition> bill) {
        BillPositionComparator bvc = new BillPositionComparator();
        ArrayList<BillPosition> sortedMap = new ArrayList<>();
        ArrayList<BillPosition> result = new ArrayList<>();
        sortedMap.addAll(bill);

        Collections.sort(sortedMap, bvc);

        Voucher v = null;
        Boolean firstRun = true;
        BillPosition pos = null;
        for (BillPosition bp : sortedMap) {
            if (firstRun || (bp.voucher != null) && !bp.voucher.equals(v)) {
                pos = new BillPosition(bp.item, bp.voucher);
                result.add(pos);
                v = bp.voucher;
                firstRun = false;
            }

            pos.setAmount(pos.getAmount() + 1);
        }

        return result;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        double price = item.price;

        if (voucher != null) {
            switch (voucher.getVoucherType()) {
                case Voucher.TYPE_FIXED_VALUE:
                    price = voucher.getValue();
                    break;

                case Voucher.TYPE_PERCENT:
                    price = (item.price * voucher.getValue()) / 100;
                    break;

                case Voucher.TYPE_EURO:
                    price = (item.price + voucher.getValue());
                    break;

                case Voucher.TYPE_TABLE:
                    if (voucher.getVoucherTableType() == Voucher.TABLE_TYPE_POSITION) {
                        price = 0;
                    } else if (voucher.getVoucherTableType() == Voucher.TABLE_TYPE_CASH) {
                        price = 0;
                        if (voucher.getValue() < 0) {
                            price = Math.abs(voucher.getValue());
                        }
                    }

                    break;
            }
        }

        return price;
    }

    @Override
    public String toString() {
        String addon = "";
        if (voucher != null) {
            addon = ", Angebot: " + voucher.getName();
        }

        return item.name + addon;
    }
}


class BillPositionComparator implements Comparator<BillPosition> {
    @Override
    public int compare(BillPosition key1, BillPosition key2) {
        if (key1.voucher == null && key2.voucher == null) {
            return -1;
        } else if (key1.voucher == null && key2.voucher != null) {
            return -1;
        } else if (key1.voucher != null && key2.voucher == null) {
            return 1;
        } else if (key1.voucher.getId() < key2.voucher.getId()) {
            return -1;
        } else {
            return 1;
        }
    }
}