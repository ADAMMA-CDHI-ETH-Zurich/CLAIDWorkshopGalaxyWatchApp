package org.c4dhi.adamma.claid_workshop_galaxy_watch;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import adamma.c4dhi.claid.DataPackage;
import adamma.c4dhi.claid.Logger.Logger;
import adamma.c4dhi.claid.Module.ModuleFactory;
import adamma.c4dhi.claid_android.Configuration.CLAIDSpecialPermissionsConfig;
import adamma.c4dhi.claid_android.Configuration.CLAIDPersistanceConfig;
import adamma.c4dhi.claid_platform_impl.CLAID;

public class MainActivity extends Activity {

    int onResumeCtr = 0;
    boolean permissionRequestDone = false;
    boolean claidStarted = false;
    private TextView mTextView;
    private TextView pythonRuntimeText;
    private TextView additionalText;

    Button startButton;
    Button resetButton;

    Thread pythonThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        pythonRuntimeText = findViewById(R.id.pythonRuntimeText);
        additionalText = findViewById(R.id.additionalText);

        pythonRuntimeText.setText("");
        additionalText.setText("");

        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        copyDefaultConfigIfNoConfigExists();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartStopButtonClicked();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetButtonClicked();
            }
        });


       /* // Initialize the BootReceiver
        OnBootReceiver bootReceiver = new OnBootReceiver();

        Log.d("CLAID", "Before register");
        // Register the BroadcastReceiver to listen for BOOT_COMPLETED
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bootReceiver, intentFilter);
        Log.d("CLAID", "After register");*/

        mTextView.setText("Please select: ");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("Checking adapter");



        ActivityCompat.requestPermissions((Activity) this,
                new String[]{Manifest.permission.BODY_SENSORS,
                        Manifest.permission.ACTIVITY_RECOGNITION,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,

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
                !checkPermission(Manifest.permission.ACTIVITY_RECOGNITION) || !checkPermission(Manifest.permission.POST_NOTIFICATIONS)
                || !checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || !checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
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


        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

    }


    private void launchCLAID()
    {
        if(claidStarted)
        {
            return;
        }
        Logger.logInfo("CLAID LAUNCH");
        claidStarted = true;

        CLAIDSpecialPermissionsConfig config = CLAIDSpecialPermissionsConfig.regularConfig();
        config.MANAGE_ALL_STORAGE = true;
        config.DISABLE_BATTERY_OPTIMIZATIONS = false;

        CLAID.onStarted(() -> onCLAIDStarted());

        CLAID.startInPersistentService(this,
                getLaunchConfigPath(),
                "Smartwatch",
                "galaxy_watch_user",
                "galaxywatch5",
                MyApplication.moduleFactory,
                config,
                CLAIDPersistanceConfig.minimumPersistance());
        /*CLAID.start(this,
                getLaunchConfigPath(),
                "Smartwatch",
                "galaxy_watch_user",
                "galaxy_watch_5",
                MyApplication.moduleFactory,
                config);*/
        CLAID.enableKeepAppAwake(getApplicationContext());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        CLAID.enableDesignerMode();

        String payloadDataPath = CLAID.getMediaDirPath(getApplicationContext()) + "/injections";

        // For module injections
        createModuleInjectionsFolder(payloadDataPath);
        this.pythonThread = new Thread(() -> startPyCLAID());
        pythonThread.start();
    }

    void onCLAIDStarted()
    {
        String payloadDataPath = CLAID.getMediaDirPath(getApplicationContext()) + "/injections";

        CLAID.setPayloadDataPath(payloadDataPath);
        CLAID.enableDesignerMode();
    }

    void createModuleInjectionsFolder(String path)
    {

        File directory = new File(path);
        if (! directory.exists()){
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
    }

    void startPyCLAID()
    {
        System.out.println("Starting py CLAID from Java");
        if (! Python.isStarted()) {
            System.out.println("Starting py CLAID from Java 1");

            Python.start(new AndroidPlatform(getApplicationContext()));
            System.out.println("Starting py CLAID from Java 2");

            Python py = Python.getInstance();
            System.out.println("Starting py CLAID from Java 3");
            runOnUiThread(() ->
                    mTextView.setText("CLAID is running!\nAddress of this device: " + getCurrentIp() + ":1337\non-device python ready"));

            System.out.println("SettingText");
         //   runOnUiThread(() -> pythonRuntimeText.setText("python ready"));

            try
            {
                Logger.logWarning("Attaching PythonRuntime, socket path: " + CLAID.getSocketPath() + " payload path: " +
                        CLAID.getPayloadDataPath());
                py.getModule("main").callAttr("attach", CLAID.getSocketPath(), CLAID.getPayloadDataPath());
            }
            catch(Exception e)
            {
                Logger.logError(e.getMessage() + " " + e.getCause());
            }
            System.out.println("Starting py CLAID from Java 4");



        }
    }

    void copyDefaultConfigIfNoConfigExists()
    {
        File file = new File(getConfigPath());
        if(!file.exists())
        {
            copyDefaultConfig();
        }
    }

    private void onStartStopButtonClicked()
    {
        if(claidStarted)
        {
            showStopDialog();
            return;
        }
        String configContent = readFileToString(getApplicationContext(), getConfigPath());
        configContent = configContent.replace("$(workshop_smartwatch_ip_to_replace)", getCurrentIp() + ":1337");

        writeStringToFile(getLaunchConfigPath(), configContent);
        launchCLAID();

        resetButton.setVisibility(View.INVISIBLE);
        resetButton.setEnabled(false);
//        startButton.setEnabled(false);
        startButton.setText("Stop");
        mTextView.setText("CLAID is running!\nAddress of this device: " + getCurrentIp() + ":1337");
    }

    private void onResetButtonClicked()
    {
        deleteCurrentConfigIfExists();
        copyDefaultConfig();
        String payloadDataPath = CLAID.getMediaDirPath(getApplicationContext()) + "/injections";
        deleteDirectoryRecursively(new File(payloadDataPath));
        createModuleInjectionsFolder(payloadDataPath);
    }

    private void copyDefaultConfig()
    {
        String configData = readFileFromAssets(getApplicationContext(),"CLAIDConfig.json");
        System.out.println("Config content "+ configData);

        writeStringToFile(getConfigPath(), configData);
    }

    boolean deleteDirectoryRecursively(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents)
            {
                deleteDirectoryRecursively(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private String getCurrentIp()
    {
        Context context = getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    private String getConfigPath()
    {
        return CLAID.getMediaDirPath(getApplicationContext()) + "/current_config.json";
    }

    private String getLaunchConfigPath()
    {
        return CLAID.getMediaDirPath(getApplicationContext()) + "/current_config_launch.json";
    }

    void showStopDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Stop CLAID and restart application?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CLAID.shutdown();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        PackageManager packageManager = getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
                        ComponentName componentName = intent.getComponent();
                        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                        startActivity(mainIntent);
                        Runtime.getRuntime().exit(0);
                        mTextView.setText("Please select: ");
                        startButton.setText("Start");
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    void deleteCurrentConfigIfExists()
    {
        File file = new File(getConfigPath());
        if(file.exists())
        {
            file.delete();
        }
    }
    public static String readFileFromAssets(Context context, String assetsPath)  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            InputStream is = context.getResources().getAssets().open(assetsPath);
            byte[] buffer = new byte[1024];
            for (int length = is.read(buffer); length != -1; length = is.read(buffer)) {
                baos.write(buffer, 0, length);
                System.out.println("Config content " + new String(buffer));
            }
            is.close();
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new String(baos.toByteArray()) ;
    }

    public static void writeStringToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFileToString(Context context, String path) {

        StringBuilder stringBuilder = new StringBuilder();

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(path));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            // Close the streams
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}
