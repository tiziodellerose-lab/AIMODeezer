package AIMODeezer;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

public class ChangeIconPlugin {
    private final Context context;

    public ChangeIconPlugin(Context context) {
        this.context = context;
    }

    public void initWith(BinaryMessenger binaryMessenger) {
        MethodChannel channel = new MethodChannel(binaryMessenger, "change_icon");
        channel.setMethodCallHandler((call, result) -> {
            if (call.method.equals("changeIcon")) {
                String iconName = call.argument("iconName");
                if (iconName != null) {
                    LauncherIcon icon = LauncherIcon.fromKey(iconName);
                    if (icon != null) {
                        setIcon(icon);
                        result.success(true);
                    } else {
                        result.error("INVALID_ICON", "Invalid icon name", null);
                    }
                } else {
                    result.error("INVALID_ARGUMENT", "Icon name is required", null);
                }
            } else {
                result.notImplemented();
            }
        });
    }

    public void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }
        setIcon(LauncherIcon.DEFAULT);
    }

    public boolean isEnabled(LauncherIcon icon) {
        int state = context.getPackageManager().getComponentEnabledSetting(icon.getComponentName(context));
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
               (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT);
    }

    public void setIcon(LauncherIcon icon) {
        PackageManager pm = context.getPackageManager();
        // Enable the new icon first
        pm.setComponentEnabledSetting(
                icon.getComponentName(context),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        // Disable all other icons (except the newly enabled one)
        for (LauncherIcon i : LauncherIcon.values()) {
            if (i != icon) {
                pm.setComponentEnabledSetting(
                        i.getComponentName(context),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon"),
        CAT("CatIcon"),
        DEEZER("DeezerBlueIcon");
        // Add more icons as needed

        private final String key;
        private static final Map<String, String> activityMap = new HashMap<>();
        private ComponentName componentName;

        static {
            activityMap.put("DefaultIcon", "AIMODeezer.DefaultIconActivity");
            activityMap.put("CatIcon", "AIMODeezer.CatIconActivity");
            activityMap.put("DeezerBlueIcon", "AIMODeezer.DeezerBlueIconActivity");
        }

        LauncherIcon(String key) {
            this.key = key;
        }

        public ComponentName getComponentName(Context context) {
            if (componentName == null) {
                componentName = new ComponentName(context.getPackageName(), Objects.requireNonNull(activityMap.get(key)));
            }
            return componentName;
        }

        public static LauncherIcon fromKey(String key) {
            for (LauncherIcon icon : values()) {
                if (icon.key.equals(key)) {
                    return icon;
                }
            }
            return null;
        }
    }
}
