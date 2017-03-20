package io.hypertrack.sendeta.util;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.hypertrack.lib.HyperTrack;


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

    public static void setHTLogLevel(int logLevel) {

        HyperTrack.enableDebugLogging(logLevel);
    }

    public static void sdkVersionMessage(Context context) {
        Toast.makeText(context, HyperTrack.getSDKVersion(), Toast.LENGTH_LONG).show();
    }
}
