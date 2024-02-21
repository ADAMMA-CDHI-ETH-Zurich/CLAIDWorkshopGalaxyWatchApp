package org.c4dhi.adamma.claid_workshop_galaxy_watch;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import adamma.c4dhi.claid.Logger.Logger;
import adamma.c4dhi.claid.Module.ModuleFactory;
import adamma.c4dhi.claid_android.Configuration.CLAIDMightinessConfig;
import adamma.c4dhi.claid_android.Configuration.CLAIDPersistanceConfig;
import adamma.c4dhi.claid_platform_impl.CLAID;

public class MainActivity extends Activity {

    int onResumeCtr = 0;
    boolean permissionRequestDone = false;
    boolean claidStarted = false;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);


       /* // Initialize the BootReceiver
        OnBootReceiver bootReceiver = new OnBootReceiver();

        Log.d("CLAID", "Before register");
        // Register the BroadcastReceiver to listen for BOOT_COMPLETED
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bootReceiver, intentFilter);
        Log.d("CLAID", "After register");*/

        mTextView.setText("Data collection is running");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("Checking adapter");




        ActivityCompat.requestPermissions((Activity) this,
                new String[]{Manifest.permission.BODY_SENSORS,
                        Manifest.permission.ACTIVITY_RECOGNITION

                },1337);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        onResumeCtr++;
        if(onResumeCtr >= 3 && permissionRequestDone)
        {
            checkAllPermissions();
        }

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults)
    {

        System.out.println("Grant results " + grantResults.length);
        Logger.logInfo("Looping");
        boolean allPermissionsGranted = true;
        for(int i = 0; i < grantResults.length; i++)
        {
            Logger.logInfo("grant results " + i +  " " + grantResults[i]);

            if(grantResults[i] != 0)
            {
                allPermissionsGranted = false;
            }
        }

        if(!allPermissionsGranted)
        {
            requestGrantAllPermissions();
        }
        else
        {
            checkAllPermissions();
        }


        permissionRequestDone = true;
    }

    private void requestGrantAllPermissions()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("On the following page, please click on \"permissions\" and "
                + " allow all permissions all the time. Additionally, please disable \"remove permissions if app isn't used\"");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = IntentCompat.createManageUnusedAppRestrictionsIntent
                                (getApplicationContext(), getPackageName());

                        // Must use startActivityForResult(), not startActivity(), even if
                        // you don't use the result code returned in onActivityResult().
                        startActivityForResult(intent, 1337);

                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void requestGrantStoragePermissionAllTheTime()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("On the following page, please click on \"permissions\", and choose "
                + "\"allow all the time\" for files and media");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = IntentCompat.createManageUnusedAppRestrictionsIntent
                                (getApplicationContext(), getPackageName());

                        // Must use startActivityForResult(), not startActivity(), even if
                        // you don't use the result code returned in onActivityResult().
                        startActivityForResult(intent, 1337);

                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }




    private void requestRemovePermissionAutoRevoke()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("On the following page, please click on \"permissions\" and \n"
                + " disable \"remove permissions if app isn't used\" at the bottom of the page.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = IntentCompat.createManageUnusedAppRestrictionsIntent
                                (getApplicationContext(), getPackageName());

                        // Must use startActivityForResult(), not startActivity(), even if
                        // you don't use the result code returned in onActivityResult().
                        startActivityForResult(intent, 1337);

                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    void requestPermissionBodySensorBackground()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("On the following page, please click on \"permissions\" and \n"
                + " enable permissions for \"body sensor\" at all times (always).");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = IntentCompat.createManageUnusedAppRestrictionsIntent
                                (getApplicationContext(), getPackageName());

                        // Must use startActivityForResult(), not startActivity(), even if
                        // you don't use the result code returned in onActivityResult().
                        startActivityForResult(intent, 1337);

                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private boolean checkPermission(String permission)
    {
        return ContextCompat.checkSelfPermission(
                getApplicationContext(), permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasStoragePermissions()
    {
        File[] files = getExternalMediaDirs();
        String filepath ="/sdcard/Android/media/org.c4dhi.adamma.claid_workshop_galaxy_watch/adamma_claid_test.txt";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filepath);
            byte[] buffer = "This will be writtent in test.txt".getBytes();
            fos.write(buffer, 0, buffer.length);
            fos.close();
            return true;
        } catch (Exception e)
        {
            return false;
        }
    }


    private  boolean isPermissionsAutoRevokeEnabled()
    {
        if (SDK_INT >= android.os.Build.VERSION_CODES.R) {
            PackageManager packageManager = getPackageManager();
            boolean result = packageManager.isAutoRevokeWhitelisted(); // result should be true

            return !result;
        }
        return false;
    }

    private void checkAllPermissions()
    {

        boolean allPermissionsSufficient = true;

        if(!checkPermission(Manifest.permission.BODY_SENSORS) ||
                !checkPermission(Manifest.permission.ACTIVITY_RECOGNITION))
        {
            requestGrantAllPermissions();
        }
        else if(SDK_INT >= Build.VERSION_CODES.TIRAMISU && !checkPermission(Manifest.permission.BODY_SENSORS_BACKGROUND))
        {
            requestPermissionBodySensorBackground();
        }
        else if(!hasStoragePermissions())
        {
            requestGrantStoragePermissionAllTheTime();
        }
        else if(isPermissionsAutoRevokeEnabled())
        {
            requestRemovePermissionAutoRevoke();
        }
        else
        {
            if(claidStarted)
            {
                return;
            }
            Logger.logInfo("CLAID LAUNCH");
            claidStarted = true;
            CLAID.startInPersistentService(this,
                    "assets://CLAIDConfig.json",
                    "galaxy_watch_host",
                    "galaxy_watch_user",
                    "galaxywatch5",
                    MyApplication.moduleFactory,
                    CLAIDMightinessConfig.regularConfig(),
                    CLAIDPersistanceConfig.maximumPersistance());


        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

    }



}
