package at.gastronaut.android.classes.Devices;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;


import at.gastronaut.android.AbstractActivity;

public class Flashlight {
    private static boolean toggle = false;

    public static void toggleFlashlight() {
        toggle = !toggle;

        switchFlashLight(toggle);
    }

    private static void switchFlashLight(boolean on) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CameraManager cameraManager = (CameraManager) AbstractActivity.getContext().getSystemService(Context.CAMERA_SERVICE);
                for (String id : cameraManager.getCameraIdList()) {

                    // Turn on the flash if camera has one
                    if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cameraManager.setTorchMode(id, on);
                        }
                    }
                }
            } else {
                String val = on ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;

                Camera mCam = Camera.open();
                Camera.Parameters p = mCam.getParameters();
                p.setFlashMode(val);
                mCam.setParameters(p);
            }
        } catch (Exception e2) {
            System.out.println("Torch Failed: " + e2.getMessage());
        }

    }

    public static void turnOnFlashlight() {
        toggle = true;
        switchFlashLight(toggle);
    }

    public static void turnOffFlashlight() {
        toggle = false;
        switchFlashLight(toggle);
    }
}
