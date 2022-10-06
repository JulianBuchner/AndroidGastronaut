package at.gastronaut.android.classes;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;

/**
 * Created by p.rathgeb on 21.02.2017.
 */

public class TableConfig {

    public static final int SELECT_TABLE_END = 0;
    public static final int SELECT_TABLE_FIRST = 1;

    public static int showTableDialog = SELECT_TABLE_END;
    public static boolean tableSelected = false;

    public static boolean dialogOpen = false;

    //public static final int SELECT_TABLE_ANYTIME = 2;
    public static int lastTableId = 1;
    public static int selectedTableId = 1;

    private static TableConfig _instance = null;
    private int numTables = 100;
    private boolean isNumeric = true;

    private HashMap<Integer, String> tables = new HashMap<>();

    private TableConfig() { }

    public void setTables(Node config) {
        //int i = 0;
        tables.clear();

        while (config != null) {
            if (config.getNodeType() == Node.TEXT_NODE) {
                config = config.getNextSibling();
                continue;
            }
            //++i;
            Element childElement = (Element) config;

            tables.put(Integer.parseInt(childElement.getAttribute("id")), childElement.getAttribute("name"));

            if (isNumeric) {
                try {
                    Integer.parseInt(childElement.getAttribute("name"));
                } catch (Exception e) {
                    isNumeric = false;
                }
            }

            config = config.getNextSibling();
        }

        //setNumTables(i);
    }

    public static TableConfig getInstance() {
        if (_instance == null) {
            _instance = new TableConfig();
        }

        return _instance;
    }

    public String getTableName(int tableId) {
        return tables.containsKey(tableId) ? tables.get(tableId) : Integer.toString(tableId);
    }

    public void setNumTables(int num) {
        numTables = num;
    }

    public int getNumTables() {
        return numTables;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public static boolean showTableForEntries() {
        boolean showTable = false;
        for (Integer key : Configuration.getInstance().orderList.keySet()) {
            showTable = Configuration.getItem(key).showTableDialog;

            if (showTable) {
                break;
            }
        }

        return showTable;
    }
}
