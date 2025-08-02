package lv.id.arseniuss.linguae.app.entities;

public class ApplicationBug {
    public long Timestamp;
    public String Version;
    public int VersionCode;
    public String BuildType;
    public String InstallationSource;
    public Device Device = new Device();
    public String NetworkType;

    public static class Device {
        public String Model;
        public String OSVersion;
        public int APILevel;
        public String ScreenResolution;
    }

    public static class Network {
        public String Type;
    }
}
