package at.gasronaut.android.classes.Devices;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import at.gasronaut.android.AbstractActivity;
import at.gasronaut.android.classes.ForegroundServiceLauncher;
import at.gasronaut.android.classes.print.BluetoothService;
import at.gasronaut.android.classes.print.Other;

/**
 * Created by p.rathgeb on 10.05.2017.
 */
public class BluetoothManager extends Device {
    private String bluetoothmac = null;
    private static BluetoothService service;
    private static boolean print = false;
    private boolean _pairing = false;
    private boolean _connecting = false;
    public boolean silentConnect = false;
    private static ArrayList<String> devices = new ArrayList<>();

    private static BluetoothManager _instance;

    private static boolean saveTimestamp = false;
    public static long lastConnect = 0;
    /**
     * the MAC - Adress we want / wanted to connect
     */
    private String _requestedPrinterMAC;

    /**
     * the MAC - Adress we are connected recently
     */
    private String _connectedPrinterMAC = "";

    private BluetoothManager(Context c) {
        super(c);

        //enableBluetoothListener();
        service = getBluetoothService();

        devices.add("00:19:56:78:D8:C3");
        devices.add("00:19:56:78:D8:EC");
        devices.add("00:19:56:78:A3:12");
        devices.add("00:19:56:78:D8:DF");
        devices.add("00:19:56:78:D9:32");
        devices.add("00:19:5D:26:0D:B7");
        devices.add("00:19:56:78:D9:1B");
        devices.add("00:19:56:78:93:27");
        devices.add("00:19:56:78:D9:15");
        devices.add("00:19:56:78:D8:E4");
        devices.add("00:19:56:78:AF:3F");
        devices.add("00:19:56:78:A4:D4");
        devices.add("00:19:56:78:D9:46");
        devices.add("00:19:5D:25:D5:09");
        devices.add("00:19:5D:25:D5:25");
        devices.add("00:19:56:78:D8:E4");
        devices.add("0F:02:17:B1:34:78");
        devices.add("0F:02:17:B0:15:CC");
        devices.add("0F:02:17:C2:8F:BE");
        devices.add("0F:02:17:B1:34:BD");
        devices.add("0F:02:17:B1:34:D4");
        devices.add("0F:02:17:C2:5A:FE");
        devices.add("0F:02:17:C2:5A:36");
        devices.add("0F:02:17:C2:59:D1");
        devices.add("0F:02:17:C2:5A:4A");
        devices.add("0F:02:17:C2:5B:6A");
        devices.add("0F:02:17:C2:5F:1F");
        devices.add("0F:02:17:C2:58:DD");
        devices.add("0F:02:17:C2:5F:77");
        devices.add("0F:02:17:C2:90:17");
        devices.add("0F:02:17:C2:5B:31");
        devices.add("0F:02:17:C2:59:5D");
        devices.add("0F:02:17:C2:5A:49");
        devices.add("0F:02:17:C2:5B:B3");
        devices.add("0F:02:17:C2:87:4B");
        devices.add("0F:02:17:C0:1D:87");

        // newer
        devices.add("66:12:0E:9C:8C:75");
        devices.add("66:12:4A:BF:D3:11");
        devices.add("66:12:AD:86:0C:8A");
        devices.add("66:12:C9:10:F8:68");
        devices.add("66:12:AC:30:4C:27");
        devices.add("66:12:C8:2C:9C:A6");
        devices.add("66:12:BE:7C:FB:56");
        devices.add("66:12:5E:99:1C:BC");
        devices.add("66:12:1D:EB:75:58");
        devices.add("66:12:7C:02:38:E6");
        devices.add("66:12:4B:8B:E2:A7");
        devices.add("66:12:AD:5E:26:44");
        devices.add("66:12:1F:48:FE:81");
        devices.add("66:12:95:C0:1A:AA");
        devices.add("66:12:66:6C:19:01");
        devices.add("66:12:91:08:48:B8");
        devices.add("66:12:B7:6B:F1:80");
        devices.add("66:12:A7:58:03:CA");
        devices.add("66:12:D3:04:64:E1");
        devices.add("66:12:3A:93:96:72");

        // FF Dietach
        devices.add("66:12:5F:16:B8:75");
        devices.add("66:12:D0:AF:63:03");
        devices.add("66:12:12:14:A8:20");
        devices.add("66:12:3F:42:66:F5");
        devices.add("66:12:1E:2E:F7:09");
        devices.add("66:12:19:0F:28:22");
        devices.add("66:12:58:37:E7:B6");
        devices.add("66:12:8F:38:47:43");
        devices.add("66:12:66:BC:B3:4E");
        devices.add("66:12:06:76:D9:3A");
        devices.add("66:12:58:93:E4:9D");
        devices.add("66:12:5F:50:2E:18");
        devices.add("66:12:FB:6A:4B:A5");
        devices.add("66:12:EE:A1:22:C5");
        devices.add("66:12:0E:BA:63:85");
    }

    public static BluetoothManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new BluetoothManager(context);
        }

        return _instance;
    }

    public static BluetoothManager getInstance() {
        if (_instance == null) {
            _instance = new BluetoothManager(AbstractActivity.getActivity());
        }

        return (BluetoothManager) _instance;
    }

    public void connectToBlueToothPrinter(String btMac) {
        _requestedPrinterMAC = btMac;

        this.connectToBlueToothPrinter(btMac, false);
    }

    public void connectToBlueToothPrinter(String btMac, boolean forceReconnect) {
        if (!_connecting) {
            _requestedPrinterMAC = btMac;

            if (forceReconnect && isConnected() && !isConnected(btMac)) {
                disconnectFromBluetoothDevie();
            }

            if (!isConnected() && !btMac.equals("")) {
                if (getBluetoothService().isAvailable() == false) {
                    if (!silentConnect) {
                        Toast.makeText(getContext(), "Bluetooth ist nicht verfügbar!", Toast.LENGTH_LONG).show();
                    }
                    print = false;
                    return;
                }

                if (getBluetoothService().isBTopen() == false) {
                    enable();
                    // Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // AbstractActivity.getActivity().startActivityForResult(enableIntent, AbstractActivity.REQUEST_CODE_BLUETOOTH);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            _connecting = true;

                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(15000);
                                    } catch (InterruptedException e) {
                                    }
                                    _connecting = false;
                                }
                            }.start();

                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {

                            }

                            BluetoothDevice con_dev = getBluetoothService().getDevByMac(_requestedPrinterMAC);
                            getBluetoothService().connect(con_dev);
                        }
                    }.start();
                }
            }
        }
    }

    public void reconnectToDevice() {
        new Thread() {
            @Override
            public void run() {
                _connecting = true;

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                        }
                        _connecting = false;
                    }
                }.start();

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {

                }

                BluetoothDevice con_dev = getBluetoothService().getDevByMac(_requestedPrinterMAC);
                getBluetoothService().connect(con_dev);
            }
        }.start();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothService getBluetoothService() {
        if (service == null) {
            service = new BluetoothService(btHandler);
        }

        return service;
    }

    public boolean isConnected(String btMac) {
        return _connectedPrinterMAC.equalsIgnoreCase(btMac);
    }

    public boolean isConnected() {
        return getBluetoothService().getState() == BluetoothService.STATE_CONNECTED;
    }

    public void disconnectFromBluetoothDevie() {
        if (isConnected()) {
            getBluetoothService().stop();
        }
        this._connectedPrinterMAC = "";
    }

    public String getConnectedPrinterMac() {
        return _connectedPrinterMAC;
    }

    public void setBluetoothService(BluetoothService btService) {
        if (getBluetoothService() != btService) {
            this.disconnectFromBluetoothDevie();
        }

        service = btService;
    }

    public void setBluetoothPrint(boolean p) {
        this.print = p;
    }

    @SuppressLint("HandlerLeak")
    public final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                System.out.println("HandleMessage: " + msg.what + " - " + msg.arg1);
                switch (msg.what) {
                    case BluetoothService.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:

                                _connecting = false;
                                _connectedPrinterMAC = _requestedPrinterMAC;
                                if (!silentConnect) {
                                    Toast.makeText(getContext(), "Bluetoothverbindung erfolgreich hergestellt!",
                                            Toast.LENGTH_SHORT).show();
                                }

                                if (saveTimestamp) {
                                    lastConnect = System.currentTimeMillis();
                                }

                                setBluetoothPrint(true);

                                ForegroundServiceLauncher.getInstance().startService(AbstractActivity.getContext());

                                //BluetoothThread.startThread();

                                notifySettingsChanged();

                                break;
                        }
                        break;
                    case BluetoothService.MESSAGE_CONNECTION_LOST:
                        if (isConnected()) {
                            _connecting = false;

                            disconnectFromBluetoothDevie();
                            if (!silentConnect) {
                                Toast.makeText(getContext(), "Bluetoothverbindung verloren!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case BluetoothService.MESSAGE_UNABLE_CONNECT:
                        _connecting = false;

                        //disconnectFromBluetoothDevie();

                        //Toast.makeText(AbstractActivity.getContext(), "Keine Verbindung zum Bluetoothgerät möglich. Fehler?",
                        //        Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (Exception e) {
                disconnectFromBluetoothDevie();
            }
        }
    };

    public void blueToothPrint(String heading, String msg, int orderId) {
        if (print) {
            this.blueToothPrint(heading, msg, orderId, service);
        }
    }

    public void blueToothPrint(String heading, String msg, int orderId, BluetoothService mService) {
        if (mService != null) {
            /*byte[] cmd = new byte[3];
            cmd[0] = 0x1b;
            cmd[1] = 0x21;
            cmd[2] |= 0x10;
            mService.write(cmd);
            cmd[2] &= 0xEF;

            mService.write(cmd);*/
            mService.sendMessage(heading, msg, "GBK");

            /*
            for (Sponsor s : Configuration.sponsorHashMap.values()) {
                if (s.getBitmap() != null) {
                    byte[] data = POS_PrintBMP(s.getBitmap(), 350, 0);
                    mService.write(data);
                }

                mService.sendMessage("", "\n" + s.getText() + "\n\n", "GBK");
            }
             */

            mService.sendMessage("", "\n\n", "GBK");

            /*if (orderId > 0) {
                mService.sendMessage("Verfolge deine Bestellung:\n", "GBK");

                QRCodeWriter writer = new QRCodeWriter();
                try {
                    BitMatrix bitMatrix = writer.encode(Configuration.getInstance().hostUrl + "/index/tracking/id/" + orderId, BarcodeFormat.QR_CODE, 384, 384);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    byte[] sendData = null;
                    PrintQRCode pg = new PrintQRCode();
                    pg.initCanvas(384);
                    pg.initPaint();
                    pg.drawImage(0, 0, bmp);
                    sendData = pg.printDraw();
                    mService.write(sendData);

                    mService.sendMessage(Configuration.getInstance().hostUrl + "/index/tracking/id/" + orderId + "\n\n\n", "GBK");
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
            */
        }
    }

    public static byte[] POS_PrintBMP(Bitmap mBitmap, int nWidth, int nMode) {
        int width = ((nWidth + 7) / 8) * 8;
        int height = mBitmap.getHeight() * width / mBitmap.getWidth();
        height = ((height + 7) / 8) * 8;

        Bitmap rszBitmap = mBitmap;
        if (mBitmap.getWidth() != width) {
            rszBitmap = Other.resizeImage(mBitmap, width, height);
        }

        Bitmap grayBitmap = Other.toGrayscale(rszBitmap);

        byte[] dithered = Other.thresholdToBWPic(grayBitmap);

        //byte[] bw = invertBW(dithered);

        byte[] data = Other.eachLinePixToCmd(dithered, width, nMode);

        return data;
    }

    public static byte[] invertBW(byte[] data) {
        byte[] array = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == 0) {
                array[i] = 1;
            } else {
                array[i] = 0;
            }
        }
        return array;
    }

    public boolean findInPairedDevices(String btMac) {
        boolean found = false;
        Set<BluetoothDevice> pairedDevices = getBluetoothService().getPairedDev();

        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices and search for our device!
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equalsIgnoreCase(btMac)) {
                    /**
                     * byte[] pinBytes = convertPinToBytes("0000");
                     Method m = device.getClass().getMethod("setPin", byte[].class);
                     m.invoke(device, pinBytes);
                     */
                    _requestedPrinterMAC = device.getAddress();
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    @Override
    public void enable() {
        if (service.checkPermission() && !getBluetoothAdapter().isEnabled()) {
            getBluetoothAdapter().enable();
        }

        // Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // AbstractActivity.getActivity().startActivityForResult(enableIntent, AbstractActivity.REQUEST_CODE_BLUETOOTH);
    }

    @Override
    public void disable() {
        if (service.checkPermission()) {
            getBluetoothAdapter().disable();
        }

        // Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // AbstractActivity.getActivity().startActivityForResult(enableIntent, AbstractActivity.REQUEST_CODE_BLUETOOTH);
    }

    @Override
    public boolean isEnabled() {
        return getBluetoothAdapter().isEnabled();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final String action = intent.getAction();

            System.out.println("ACTION: " + action);

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        disconnectFromBluetoothDevie();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        disconnectFromBluetoothDevie();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        if (_requestedPrinterMAC.length() > 0) {
                            // check if device is allready paired
                            if (findInPairedDevices(_requestedPrinterMAC)) {
                                // if yes -> connect
//                                Configuration.getInstance().blueToothPrint = true;

                                connectToBlueToothPrinter(_requestedPrinterMAC, true);
//                                Configuration.getInstance().closeProgress();
                            } else {
                                // else start a new discovery
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        BluetoothManager.this.startDiscovery();
                                    }
                                }, 3000);
                            }

                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                this._pairing = state != BluetoothDevice.BOND_BONDED && state != BluetoothDevice.BOND_NONE;

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING && (_requestedPrinterMAC.length() > 0)) {
                    /**
                     * device was successfully paired
                     */
                    BluetoothManager.getInstance(getContext()).connectToBlueToothPrinter(_requestedPrinterMAC, true);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    BluetoothManager.getInstance(getContext()).disconnectFromBluetoothDevie();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                // derzeit nichts machen!
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (!this._pairing && !isConnected(_requestedPrinterMAC) && !_connecting) {
                    if (!silentConnect) {
                        Toast.makeText(getContext(), "Bluetoothverbindung konnte nicht hergestellt werden!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (_requestedPrinterMAC.length() > 0) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (device.getAddress().equalsIgnoreCase(_requestedPrinterMAC)) {
                        _requestedPrinterMAC = device.getAddress();
                        /**
                         * device was found, but it is not bonded (paired).
                         */
                        if (!this._pairing && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            try {
                                // nicht aktivieren - es wird automatisch der pin geschickt!
                                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                                method.invoke(device, (Object[]) null);
                                this._pairing = true;
                            } catch (Exception e) {
                            }
                        } else {
                            connectToBlueToothPrinter(_requestedPrinterMAC, true);
                        }
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                this._pairing = true;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Class<?> btDeviceInstance = Class.forName(BluetoothDevice.class.getCanonicalName());

                Method convert = btDeviceInstance.getMethod("convertPinToBytes", String.class);

                byte[] pin = (byte[]) convert.invoke(device, "1234");

                Method setPin = btDeviceInstance.getMethod("setPin", byte[].class);
                Thread.sleep(2000);
                boolean success = (Boolean) setPin.invoke(device, pin);

                this._pairing = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean startDiscovery() {
        return service.startDiscovery();
    }

    public void enableBluetoothListener() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

        context.registerReceiver(this, filter);
    }

    public boolean isAvailable() {
        return getBluetoothAdapter() != null;
    }

    public void tryToConnect(String btMacAddress, boolean newScan) {
        enable();
        if (isAvailable() && !_connecting) {
            _requestedPrinterMAC = btMacAddress;

            if (isEnabled()) {
                if (isConnected(btMacAddress)) {
                    /*Toast.makeText(AbstractActivity.getContext(), "Bluetoothverbindung erfolgreich hergestellt!",
                            Toast.LENGTH_SHORT).show();*/
                    setBluetoothPrint(true);

                    ForegroundServiceLauncher.getInstance().startService(AbstractActivity.getContext());

                    //BluetoothThread.startThread();
                    return;
                } else {
                    if (isConnected()) {
                        disconnectFromBluetoothDevie();
                    }
                }

                if (newScan) {
                    //saveTimestamp = true;

                    //unpairAllDevices();
                    //System.out.println("unpair");
                }
                // Configuration.getInstance().showProgress("Verbindung wird hergestellt", "Bitte warten Sie während die Verbindung mit dem Gerät hergestellt wird.");

                if (findInPairedDevices(_requestedPrinterMAC)) {
                    setBluetoothPrint(true);

                    connectToBlueToothPrinter(_requestedPrinterMAC, true);
                    //Configuration.getInstance().closeProgress();

                    return;
                } else {
                    this._pairing = true;

                    startDiscovery();
                }

            } else {
                enable();
                // Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // startActivityForResult(intent, 1000);
            }
        } else {
            if (!silentConnect) {
                Toast.makeText(getContext(), "Bluetooth ist bei diesem Gerät leider nicht verfügbar!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void tryToConnect(String btMacAddress) {
        tryToConnect(btMacAddress, false);
    }

    public void unpairAllDevices() {
        Set<BluetoothDevice> pairedDevices = getBluetoothService().getPairedDev();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (searchForDevice(device.getAddress()) || device.getName().equalsIgnoreCase("BlueTooth Printer")) {
                    try {
                        Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                        m.invoke(device, (Object[]) null);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private boolean searchForDevice(String addr) {
        for (String str : devices) {
            if (str.equalsIgnoreCase(addr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check that a pin is valid and convert to byte array.
     * <p>
     * Bluetooth pin's are 1 to 16 bytes of UTF-8 characters.
     *
     * @param pin pin as java String
     * @return the pin code as a UTF-8 byte array, or null if it is an invalid
     * Bluetooth pin.
     * @hide
     */
    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        byte[] pinBytes;
        try {
            pinBytes = pin.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
        if (pinBytes.length <= 0 || pinBytes.length > 16) {
            return null;
        }
        return pinBytes;
    }

    public String getBluetoothmac() {
        return bluetoothmac;
    }

    public void setBluetoothmac(String bluetoothmac) {
        this.bluetoothmac = bluetoothmac;
    }
}