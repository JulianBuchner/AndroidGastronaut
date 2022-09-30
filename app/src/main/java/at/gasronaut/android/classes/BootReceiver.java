package at.gasronaut.android.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import at.gasronaut.android.MainActivity;

/**
 * Created by p.rathgeb on 16.08.2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        //}
    }
}