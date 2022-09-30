package at.gasronaut.android.classes.Async;

import at.gasronaut.android.classes.Configuration;
import at.gasronaut.android.classes.Devices.BluetoothManager;

/**
 * Created by peter on 01.07.2016.
 */
public class BluetoothThread extends Thread {
    public boolean run = false;

    private static BluetoothThread _instance;

    public final static synchronized BluetoothThread getInstance() {
        if (_instance == null) {
            _instance = new BluetoothThread();
        }

        return _instance;
    }

    private final static synchronized void removeInstance() {
        _instance = null;
    }

    @Override
    public void run() {
        while (run) {
            if (!BluetoothManager.getInstance().isConnected() && Configuration.getInstance().blueToothPrint && Configuration.getInstance().bluetoothMac.length() > 0) {
                /*BluetoothManager.getInstance().silentConnect = true;
                BluetoothManager.getInstance().tryToConnect(Configuration.getInstance().bluetoothMac);
                BluetoothManager.getInstance().silentConnect = false;*/

                BluetoothManager.getInstance().reconnectToDevice();

                /*BluetoothDevice con_dev = BluetoothManage r.getInstance(Abstract            receiver.setText(receiver.getText() + sender.getText());
Activity.getContext()).getBluetoothService().getDevByMac(Configuration.getInstance().bluetoothMac);
                BluetoothManager.getInstance().getBluetoothService().connect(con_dev);*/
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Looper.loop();
        }
    }

    public static void startThread() {
        if (!BluetoothThread.getInstance().run) {
            BluetoothThread.getInstance().run = true;
            BluetoothThread.getInstance().start();
        }
    }

    public static void stopThread() {
        BluetoothThread.getInstance().run = false;
        BluetoothThread.getInstance().interrupt();
        BluetoothThread.getInstance().removeInstance();
    }
}
