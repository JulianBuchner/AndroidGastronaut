package at.gastronaut.android.classes.Async;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

import at.gastronaut.android.R;
import at.gastronaut.android.classes.Configuration;
import at.gastronaut.android.classes.Devices.BluetoothManager;

public class BluetoothService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start Foreground-service
        startForeground(62318, builtNotification());

        // Start Handler
        timerHandler.postDelayed(timerRunnable, timerTime);
    }

    /**
     * Build notification for Foreground-service
     *
     * @return notification
     */
    public Notification builtNotification() {

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;

        NotificationCompat.Builder builder = null;
        /*Uri soundUri = Uri.parse("android.resource://" +
                getApplicationContext().getPackageName()
                + "/" + R.raw.notification_sound);*/

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel =
                    new NotificationChannel("ID", "Name", importance);
            // Creating an Audio Attribute
            /*AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();*/

            //notificationChannel.setSound(soundUri,audioAttributes);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        //builder.setSound(soundUri);

        String message = "Gastronaut Bluetooth Watchdog";
        builder//.setSmallIcon(R.drawable.logo)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                //.setSound(soundUri)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.parseColor("#0f9595"))
                .setContentTitle("Gastronaut")
                .setContentText(message);

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent,
                PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        return notification;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Restart service when destroyed
        //ForegroundServiceLauncher.getInstance().startService(this);
        running = false;
    }

    /**
     * Delay between requests
     */
    private long timerTime = TimeUnit.SECONDS.toMillis(10);

    private boolean running = true;

    /**
     * Handler for requests
     */
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running && !BluetoothManager.getInstance().isConnected() && Configuration.getInstance().blueToothPrint && Configuration.getInstance().bluetoothMac.length() > 0) {
                /*BluetoothManager.getInstance().silentConnect = true;
                BluetoothManager.getInstance().tryToConnect(Configuration.getInstance().bluetoothMac);
                BluetoothManager.getInstance().silentConnect = false;*/

                BluetoothManager.getInstance().reconnectToDevice();

                /*BluetoothDevice con_dev = BluetoothManage r.getInstance(Abstract            receiver.setText(receiver.getText() + sender.getText());
Activity.getContext()).getBluetoothService().getDevByMac(Configuration.getInstance().bluetoothMac);
                BluetoothManager.getInstance().getBluetoothService().connect(con_dev);*/
            }

            if (running) {
                timerHandler.postDelayed(this, timerTime);
            }
        }
    };
}

