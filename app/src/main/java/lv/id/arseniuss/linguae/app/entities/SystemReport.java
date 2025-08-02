package lv.id.arseniuss.linguae.app.entities;

import com.google.gson.annotations.SerializedName;

public class SystemReport {
    @SerializedName("timestamp")
    public long Timestamp;

    @SerializedName("version")
    public String Version;

    @SerializedName("version_code")
    public int VersionCode;

    @SerializedName("build_type")
    public String BuildType;

    @SerializedName("install_source")
    public String InstallationSource;

    @SerializedName("device")
    public Device Device = new Device();

    @SerializedName("network_type")
    public String NetworkType;

    public static class Device {
        @SerializedName("model")
        public String Model;

        @SerializedName("os_version")
        public String OSVersion;

        @SerializedName("api_level")
        public int APILevel;

        @SerializedName("screen_resolution")
        public String ScreenResolution;
    }
}
