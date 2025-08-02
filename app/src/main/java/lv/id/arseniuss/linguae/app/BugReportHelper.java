package lv.id.arseniuss.linguae.app;

import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import lv.id.arseniuss.linguae.app.entities.SystemReport;

public class BugReportHelper {
    public static SystemReport GatherData(Context context) {
        SystemReport bug = new SystemReport();

        bug.Timestamp = System.currentTimeMillis();

        try {
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            bug.Version = packageInfo.versionName;
            bug.VersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            bug.Version = "Unknown";
            bug.VersionCode = 0;
        }

        bug.BuildType = BuildConfig.DEBUG ? "Debug" : "Release";
        bug.InstallationSource = getInstallationSource(context);

        bug.Device.Model = Build.MODEL;
        bug.Device.OSVersion = "Android " + Build.VERSION.RELEASE;
        bug.Device.APILevel = Build.VERSION.SDK_INT;

        bug.Device.ScreenResolution = getScreenResolution(context);

        bug.NetworkType = getNetworkType(context);

        return bug;
    }

    private static String getNetworkType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return "Wi-Fi";
                case ConnectivityManager.TYPE_MOBILE:
                    return "Mobile";
                default:
                    return "Other";
            }
        } else {
            return "No network";
        }
    }

    private static String getScreenResolution(Context context) {
        try {
            WindowManager windowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            int width = metrics.widthPixels;
            int height = metrics.heightPixels;

            return width + "x" + height;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static String getInstallationSource(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo sourceInfo =
                        context.getPackageManager().getInstallSourceInfo(context.getPackageName());
                String installerPackage = sourceInfo.getInstallingPackageName();

                return mapInstallerPackageToSource(installerPackage);
            } else {
                String installerPackage = context.getPackageManager()
                        .getInstallerPackageName(context.getPackageName());

                return mapInstallerPackageToSource(installerPackage);
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static String mapInstallerPackageToSource(String installerPackage) {
        if (installerPackage == null) {
            return "Sideloaded";
        }

        switch (installerPackage) {
            case "com.android.vending":
            case "com.google.android.feedback":
                return "Google Play Store";
            case "com.amazon.venezia":
                return "Amazon Appstore";
            case "com.android.packageinstaller":
                return "Manual Installation";
            default:
                return "Other (" + installerPackage + ")";
        }
    }

}
