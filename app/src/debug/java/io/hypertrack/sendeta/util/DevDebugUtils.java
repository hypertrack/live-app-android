package io.hypertrack.sendeta.util;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by piyush on 03/07/16.
 */
public class DevDebugUtils {

    public static void installStetho(Application application) {

        Stetho.initialize(
                Stetho.newInitializerBuilder(application)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(application))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(application))
                        .build());
    }
}
