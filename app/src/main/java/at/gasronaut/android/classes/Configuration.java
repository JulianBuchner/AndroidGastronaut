package at.gasronaut.android.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import at.gasronaut.android.AbstractActivity;
import at.gasronaut.android.MainActivity;
import at.gasronaut.android.classes.Async.AsyncResponse;
import at.gasronaut.android.classes.Async.ServerRequest;
import at.gasronaut.android.classes.Async.SyncThread;
import at.gasronaut.android.classes.Devices.BluetoothManager;

/**
 * Created by peter on 02.06.2016.
 */
public class Configuration {
    private static Configuration _instance = null;

    public static final String SSL_PRODUCTIVE = "gastronaut.rathgeb.at";
    public static final String SSL_TEST = "test.rathgeb.at";
    public static final String SSL_VSERVER = "app.gastronaut.events";
    public static final String SSL_IP_VSERVER = "490brj.myvserver.online";
    //public static final String SSL_HOST_VSERVER = "185.164.4.144";

    public static final String SELECT_TABLE_TYPE_SELECT = "SELECT";
    public static final String SELECT_TABLE_TYPE_SCAN = "SCAN";

    public static final String[] SSL_HOSTS = new String[]{SSL_VSERVER, SSL_IP_VSERVER, SSL_PRODUCTIVE, SSL_TEST};

    public AbstractActivity activity = null;
    public MainActivity mainActivity = null;

    public AbstractActivity.CustomViewGroup overLay = null;

    public static HashMap<String, Long> offlinePrinters = new HashMap<>();

    public String hostUrl = "";
    public String userName = "";
    public String workZone = "";
    public String wifiName = "";
    public String wifiKey = "";
    public String bluetoothMac = "";
    public boolean blueToothPrint = true;
    public Document settingsXML = null;
    public static JSONObject settingsJson = null;
    public String customerHash = "";

    public static ArrayList<Voucher> tableVouchers = null;

    public RequestQueue requestQueue = null;

    public boolean showOrderHistory = true;
    public boolean showOrderReprint = true;
    public boolean showOrderStorno = false;
    public boolean sendBill = false;
    public boolean allowNegativeAmounts = true;
    public boolean showPaidItems = false;
    //public String selectTableType = SELECT_TABLE_TYPE_SELECT;

    public boolean showTableHistory = false;
    public boolean showTableStorno = true;

    public boolean hideStornoButton = false;
    public boolean hidePayLaterButton = true;

    public boolean offline = false;

    public static String fallbackServer = "";

    public int itemTimestamp = 0;

    public int stepTimestamp = 0;

    public static List<Integer> alwaysShownBillItems = new ArrayList<>();

    public boolean offlineSystem = false;

    // progress dialog for display work progress
    public ProgressDialog progress;

    // contains the menu items in a tree!
    public MenuItem menu;

    public static int timestamp = 0;

    // boolean flag for reloading configuration (because of changes of the timestamp)
    public static boolean reloadConfig = true;

    // contains all items - also used for generating the menu
    public static HashMap<Integer, MenuItem> itemsList = new HashMap<>();
    public static HashMap<Integer, MenuItem> categoryList = new HashMap<>();
    public static ArrayList<MenuItem> favoritesList = new ArrayList<>();
    public static HashMap<Integer, RenamedPosition> tempItems = new HashMap<>();

    public static ArrayList<Voucher> voucherList = new ArrayList<>();

    public static HashMap<Integer, Sponsor> sponsorHashMap = new HashMap<>();

    // Thread for syncing settings from the server
    public SyncThread sync = null;

    // list that contains all orders!
    public static HashMap<Integer, Integer> orderList = new HashMap<>();

    // sum up all pos automatically!
    public boolean sumUpAllPos;

    public static Configuration getInstance() {
        if (_instance == null) {
            _instance = new Configuration();
        }

        return _instance;
    }

    /**
     * Load Settings from memory
     */
    public void loadSettings() {
        try {
            FileInputStream fis = null;
            try {
                fis = activity.openFileInput("config");
            } catch (Exception e) {
                try {
                    String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<settings>\n" +
                            "\t<setting id=\"host_url\" value=\"test.rathgeb.at\"></setting>\n" +
                            "\t<setting id=\"hash\" value=\"015b2\"></setting>\n" +
                            "\t<setting id=\"username\" value=\"Kellner 1\"></setting>\n" +
                            "\t<setting id=\"bluetooth_print\" value=\"0\"></setting>\n" +
                            "\t<setting id=\"bluetooth_timestamp\" value=\"0\"></setting>\n" +
                            "\t<setting id=\"workzone\" value=\"Zelt\"></setting>\n" +
                            "\t<setting id=\"bluetooth_printer_mac\" value=\"\"></setting>\n" +
                            "\t<setting id=\"wifi_name\" value=\"\"></setting>\n" +
                            "\t<setting id=\"wifi_key\" value=\"\"></setting>\n" +
                            "\t<setting id=\"sum_up_all_pos\" value=\"0\"></setting>\n" +
                            "</settings>";

                    FileOutputStream fos = activity.openFileOutput("config", Context.MODE_PRIVATE);
                    fos.write(string.getBytes());
                    fos.close();
                } catch (Exception ignored) {
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }

            try {
                fis = activity.openFileInput("config");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(fis);

                fis.close();

                hostUrl = document.getElementById("host_url").getAttribute("value");
                customerHash = document.getElementById("hash").getAttribute("value");
                userName = document.getElementById("username").getAttribute("value");
                bluetoothMac = document.getElementById("bluetooth_printer_mac").getAttribute("value");
                blueToothPrint = document.getElementById("bluetooth_print").getAttribute("value").equalsIgnoreCase("1");
                sumUpAllPos = document.getElementById("sum_up_all_pos").getAttribute("value").equalsIgnoreCase("1");

                try {
                    BluetoothManager.lastConnect = Long.parseLong(document.getElementById("bluetooth_timestamp").getAttribute("value"));
                } catch(Exception e) {
                    BluetoothManager.lastConnect = 0;
                }

                try {
                    wifiName = document.getElementById("wifi_name").getAttribute("value");
                    wifiKey = document.getElementById("wifi_key").getAttribute("value");
                } catch (Exception e) {
                    wifiName = "";
                    wifiKey = "";
                }

                workZone = document.getElementById("workzone").getAttribute("value");

                blueToothPrint = blueToothPrint && !bluetoothMac.equals("");

                //configuration.offlineSystem = document.getElementById("offline_system").getAttribute("value").equalsIgnoreCase("1");
                //configuration.offlineSystem = true;

                // everything was ok -> start thread!
                if (Configuration.this.sync == null) {
                    Configuration.this.sync = new SyncThread();
                    Configuration.this.sync.start();
                }


                try {
                    fis = activity.openFileInput("settings_xml");

                    factory = DocumentBuilderFactory.newInstance();
                    builder = factory.newDocumentBuilder();
                    this.settingsXML = builder.parse(fis);

                } catch (Exception ignored) {
                    this.settingsXML = null;
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }

                try {
                    fis = activity.openFileInput("settings_json");

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
                    String json = "";
                    String receiveString = "";

                    while ((receiveString = bufferedReader.readLine()) != null ) {
                        json += receiveString;
                    }

                    this.settingsJson = new JSONObject(json);

                } catch(Exception ignored) {
                    this.settingsJson = null;
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }

                /*BluetoothManager.getInstance().setBluetoothPrint(blueToothPrint);
                if (blueToothPrint && ((BluetoothManager.lastConnect + 12 * 60 * 60 * 1000) < System.currentTimeMillis())) {
                    BluetoothManager.getInstance().tryToConnect(bluetoothMac);
                } else {
                    blueToothPrint = false;
                }*/

                saveSettings(false);

//                if (!wifiKey.equals("") && !wifiName.equals("")) {
//                    if (!WifiManager.getInstance(AbstractActivity.getContext()).isConnected(wifiName)) {
//                        WifiManager.getInstance(AbstractActivity.getContext()).tryToConnect(wifiName, wifiKey);
//                    }
//                } else {
//                    WifiManager.getInstance(AbstractActivity.getContext()).setData("", "");
//                }

            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
    }

    /**
     * Load Entries from the online Configuration File / from the memory (if not connected)
     */
    public void loadEntries() {
        if (Configuration.getInstance().offline) {
            //mainActivity.parseMenuItem(null);
            menu = null;

            closeProgress();

            return;
        }

        AsyncResponse respListener = new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                Configuration configuration = Configuration.getInstance();

                try {
                    Document doc = (Document) output;
                    if (doc != null) {
                        configuration.settingsXML = doc;

                        try {
                            TransformerFactory tf = TransformerFactory.newInstance();
                            Transformer transformer = tf.newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                            StringWriter writer = new StringWriter();
                            transformer.transform(new DOMSource(doc), new StreamResult(writer));
                            String settingsXML = writer.getBuffer().toString().replaceAll("\n|\r", "");

                            FileOutputStream fos = configuration.activity.openFileOutput("settings_xml", Context.MODE_PRIVATE);
                            fos.write(settingsXML.getBytes());
                            fos.close();

                            //sync = new SyncThread();
                            //activity.runOnUiThread(sync);
                            //sync.start();

                        } catch (Exception ignored) {
                        }
                    } else {
                        if (configuration.settingsXML != null) {
                            Toast.makeText(activity, "Fehler beim Laden des Menüs (Netzwerkverbindung überprüfen?!)\nDaten wurden aus Speicher geladen.", Toast.LENGTH_LONG).show();
                            doc = configuration.settingsXML;
                        } else {
                            Toast.makeText(activity, "Fehler beim Laden des Menüs (Netzwerkverbindung überprüfen?!)", Toast.LENGTH_LONG).show();
                        }
                    }

                    if (doc != null) {
                        reloadConfig = false;
                        alwaysShownBillItems.clear();

                        doc.getDocumentElement().normalize();

                        if (!configuration.workZone.equals("")) {
                            menu = parseXML(getNodeByName(getNodeByTagName(doc.getDocumentElement().getFirstChild(), "menu_card"), configuration.workZone), 0);
                        } else {
                            menu = parseXML(getNodeByTagName(doc.getDocumentElement().getFirstChild(), "menu_card"), 0);
                        }

                        TableConfig.getInstance().setTables(getNodeByTagName(doc.getDocumentElement().getFirstChild(), "tables"));

                        voucherList.clear();

                        voucherList = Voucher.getVoucherListFromXML(getNodeByTagName(doc.getDocumentElement().getFirstChild(), "vouchers"));

                        mainActivity.runOnUiThread(() -> {
                            mainActivity.rootMenu = true;
                            //mainActivity.parseMenuItem(menu);
                        });
                    } else {
                        //mainActivity.parseMenuItem(null);
                        menu = null;
                    }

                } catch (Exception e) {
                    Toast.makeText(activity, "Fehler beim Laden des Menüs (Netzwerkverbindung überprüfen?!)", Toast.LENGTH_LONG).show();
                } finally {
                    closeProgress();
                }
            }
        };

        String phoneId = android.provider.Settings.Secure.getString(Configuration.getInstance().activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        ServerRequest request = new ServerRequest("http://" + Configuration.getInstance().hostUrl, "/index/settings/hash/" + Configuration.getInstance().customerHash, "{\"device_id\":\"" + phoneId + "\"}", respListener);

        request.makeStringRequest();
    }

    private Node getNodeByName(Node childNode, String nodeName) {
        Node node = null;
        while (childNode != null) {
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                childNode = childNode.getNextSibling();
                continue;
            }

            Element childElement = (Element) childNode;
            if (childElement.getAttribute("name").equalsIgnoreCase(nodeName)) {
                return childNode.getFirstChild();
            } else if (childNode.getFirstChild() != null) {
                node = getNodeByName(childNode.getFirstChild(), nodeName);
            }

            if (node == null) {
                childNode = childNode.getNextSibling();
            } else {
                return node;
            }
        }

        return null;
    }

    private Node getNodeByTagName(Node childNode, String nodeName) {
        Node node = null;
        while (childNode != null) {
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                childNode = childNode.getNextSibling();
                continue;
            }

            Element childElement = (Element) childNode;
            if (childElement.getTagName().equalsIgnoreCase(nodeName)) {
                return childNode.getFirstChild();
            } else if (childNode.getFirstChild() != null) {
                node = getNodeByName(childNode.getFirstChild(), nodeName);
            }

            if (node == null) {
                childNode = childNode.getNextSibling();
            } else {
                return node;
            }
        }

        return null;
    }

    private MenuItem parseXML(Node childNode, int parentCat) {
        if (parentCat <= 0) {
            favoritesList.clear();
        }

        MenuItem prev = null, item = null, firstItem = null;

        while (childNode != null) {
            try {
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    childNode = childNode.getNextSibling();
                    continue;
                }

                Element childElement = (Element) childNode;

                MenuItem subItem = null;
                if (childNode.getFirstChild() != null) {
                    subItem = parseXML(childNode.getFirstChild(), Integer.parseInt(childElement.getAttribute("cat_id")));
                }

                if (item != null) {
                    prev = item;
                }

                item = new MenuItem();
                item.name = childElement.getAttribute("name");
                item.useColor = Integer.parseInt(childElement.getAttribute("use_color"));

                if (item.useColor > 0) {
                    item.color = childElement.getAttribute("color");
                }

                if (childElement.getAttribute("cat_id").compareTo("") > 0) {
                    boolean placeholder = false;
                    try {
                        placeholder = Integer.parseInt(childElement.getAttribute("placeholder")) > 0;
                    } catch (Exception e) {
                    }

                    if (placeholder) {
                        item.name = "Placeholder";
                        item.id = -1;
                    } else {
                        item.id = 0;
                        item.categoryId = Integer.parseInt(childElement.getAttribute("cat_id"));
                        item.parentCategoryId = parentCat;
                        item.shortlink = Integer.parseInt(childElement.getAttribute("shortlink")) > 0;
                    }
                } else {
                    try {
                        Integer.parseInt(childElement.getAttribute("id"));
                        if (childElement.getAttribute("price").compareTo("") > 0) {
                            item.price = Float.parseFloat(childElement.getAttribute("price"));
                        }

                        item.lockedReason = childElement.getAttribute("reason_for_lock");
                        item.type = childElement.getAttribute("type");
                        item.id = Integer.parseInt(childElement.getAttribute("id"));
                        item.parentCategoryId = parentCat;
                        item.locked = Integer.parseInt(childElement.getAttribute("locked")) > 0;
                        item.hideOnBill = Integer.parseInt(childElement.getAttribute("hide_on_bill")) == MenuItem.VISIBILITY_HIDE_ON_BILL;

                        if (!item.hideOnBill) {
                            item.showAlwaysOnBill = Integer.parseInt(childElement.getAttribute("hide_on_bill")) == MenuItem.VISIBILITY_SHOW_ALWAYS_ON_BILL;

                            if (item.showAlwaysOnBill) {
                                alwaysShownBillItems.add(item.id);
                            } else {
                                item.hideOnPrinting = Integer.parseInt(childElement.getAttribute("hide_on_bill")) == MenuItem.VISIBILITY_HIDE_ON_PRINTING;
                            }
                        }

                        item.overrideNegativeAmounts = Integer.parseInt(childElement.getAttribute("override_negative_amounts"));

                        item.sort = Integer.parseInt(childElement.getAttribute("sort")) > 0 ? Integer.parseInt(childElement.getAttribute("sort")) : item.id;
                        item.hidden = Integer.parseInt(childElement.getAttribute("hidden")) > 0;
                        item.showBillDialog = Integer.parseInt(childElement.getAttribute("show_bill_dialog")) > 0;
                        item.showTableDialog = Integer.parseInt(childElement.getAttribute("show_table_dialog")) > 0;
                    } catch (Exception e) {
                        item.name = "Placeholder";
                        item.id = -1;
                    }
                }

                if (item.id > 0) {
                    //item.subItem = subItem;
                    this.itemsList.put(item.id, item);
                } else if (item.categoryId > 0) {
                    item.subItem = subItem;
                    categoryList.put(item.categoryId, item);

                    // add root categories
                    if (item.shortlink && item.id >= 0) {
                        MenuItem copy = item.clone();
                        favoritesList.add(copy);
                    }
                }

                if (firstItem == null) {
                    firstItem = item;
                }

                if (prev != null) {
                    prev.nextItem = item;
                }

            } finally {
                childNode = childNode.getNextSibling();
            }
        }

        return firstItem;
    }

    public synchronized void showProgress(Context c, String heading, String msg) {
        this.progress = ProgressDialog.show(c, heading, msg, true);
    }

    public void showProgress(String heading, String msg) {
        this.showProgress(AbstractActivity.getActivity(), heading, msg);
    }

    public void showProgress(String heading, String msg, final int timeout) {
        this.showProgress(AbstractActivity.getActivity(), heading, msg);

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                }

                closeProgress();
            }
        }.start();
    }

    public synchronized void closeProgress() {
        if (this.progress != null) {
            try {
                this.progress.dismiss();
                this.progress = null;
            } catch (Exception e) {
            }
        }
    }

    public void saveSettings(boolean reload) {
        try {
            blueToothPrint = blueToothPrint && !bluetoothMac.equals("");

            String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<settings>\n" +
                    "\t<setting id=\"host_url\" value=\"" + hostUrl + "\"></setting>\n" +
                    "\t<setting id=\"hash\" value=\"" + customerHash + "\"></setting>\n" +
                    "\t<setting id=\"username\" value=\"" + userName + "\"></setting>\n" +
                    "\t<setting id=\"bluetooth_print\" value=\"" + (blueToothPrint ? "1" : "0") + "\"></setting>\n" +
                    "\t<setting id=\"bluetooth_timestamp\" value=\"" + BluetoothManager.lastConnect + "\"></setting>\n" +
                    "\t<setting id=\"workzone\" value=\"" + workZone + "\"></setting>\n" +
                    "\t<setting id=\"bluetooth_printer_mac\" value=\"" + bluetoothMac + "\"></setting>\n" +
                    "\t<setting id=\"wifi_name\" value=\"" + wifiName + "\"></setting>\n" +
                    "\t<setting id=\"wifi_key\" value=\"" + wifiKey + "\"></setting>\n" +
                    "\t<setting id=\"sum_up_all_pos\" value=\"" + (sumUpAllPos ? "1" : "0") + "\"></setting>\n" +
                    "</settings>";

            FileOutputStream fos = AbstractActivity.getContext().openFileOutput("config", Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();

            if (reload) {
                loadSettings();
            }
        } catch (Exception e) {
        }
    }

    public static int getTempMenuItemId() {
        int time = (int) (System.currentTimeMillis() / 1000L);
        while (itemsList.containsKey(time)) {
            ++time;
        }

        return time;
    }

    public static MenuItem getItem(int key) {
        if (itemsList.containsKey(key)) {
            return itemsList.get(key);
        } else if (tempItems.containsKey(key)) {
            return tempItems.get(key);
        }

        return null;
    }

    public static void saveSettingsJson(JSONObject obj) {
        obj.remove("message_id");
        obj.remove("offline_printers");
        settingsJson = obj;

        try {
            String settingsXML = obj.toString().replaceAll("\n|\r", "");

            FileOutputStream fos = Configuration.getInstance().activity.openFileOutput("settings_json", Context.MODE_PRIVATE);
            fos.write(settingsXML.getBytes());
            fos.close();
        } catch (Exception e) {
        }
    }

    public static int generateVersionForItem(int id) {
        int maxVersion = 1;
        for (int key : tempItems.keySet()) {
            if (tempItems.get(key).id == id) {
                if (tempItems.get(key).version >= maxVersion) {
                    maxVersion = tempItems.get(key).version + 1;
                }
            }
        }

        return maxVersion;
    }
}