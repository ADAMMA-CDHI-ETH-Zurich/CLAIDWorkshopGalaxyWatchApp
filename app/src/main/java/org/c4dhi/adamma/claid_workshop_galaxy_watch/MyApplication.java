package org.c4dhi.adamma.claid_workshop_galaxy_watch;

import android.app.Application;

import adamma.c4dhi.claid_platform_impl.CLAID;
import adamma.c4dhi.claid_platform_impl.PersistentModuleFactory;
import adamma.c4dhi.claid_android.collectors.battery.BatteryCollector;


public class MyApplication extends Application
{
    static PersistentModuleFactory moduleFactory;
    @Override
    public void onCreate()
    {
        super.onCreate();
        moduleFactory = CLAID.getPersistentModuleFactory(this);

    }
}
