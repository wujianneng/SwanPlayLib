package com.nesp.android.cling.entity;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import com.nesp.android.cling.util.PhoneUUIDBuilder;

public final class LPPlaySourceType {
    public static final String LP_TUNEIN = "TuneIn";
    public static final String SPOTIFY = "SPOTIFY";
    public static final String LP_IHEARTRADIO = "iHeartRadio";
    public static final String LP_QPLAY = "QPLAY";
    public static final String LP_QINGTINGFM = "Qingtingfm";
    public static final String LP_ALI_RPC = "ALI-RPC";
    public static final String LP_TIDAL = "Tidal";
    public static final String LP_SIRIUSXM = "SIRIUSXM";
    public static final String LP_AMAZONMUSIC = "Amazon Music";
    public static final String LP_KINDLEBOOKS = "Kindle Books";
    public static final String LP_SAAVN = "SAAVN";
    public static final String LP_RADIONET = "RadioNet";
    public static final String LP_VTUNER = "vTuner";
    public static final String LP_RHAPSODY = "Rhapsody";
    public static final String LP_ALDILIFE = "AldiLife";
    public static final String LP_QOBUZ = "Qobuz";
    public static final String LP_DEEZER = "Deezer";
    public static final String LP_PRESETSONGS = "PresetSongs";
    public static final String LP_PRIME = "Prime";
    public static final String LP_ALEXA = "ALEXA";
    public static final String LP_DOUBAN = "Douban";
    public static final String LP_PANDORA = "Pandora";
    public static final String LP_XIMALAYA = "Ximalaya";
    public static final String LP_DUEROS = "DUEROS";
    public static final String LP_UPNPSERVER = "UPnPServer";
    public static final String LP_TUNEIN_NEW = "newTuneIn";
    public static final String LP_LINKPLAY_RADIO = "Linkplay Radio";
    public static final String LP_CALM_RADIO = "CalmRadio";
    public static final String LP_SOUND_MACHINE = "SoundMachine";
    public static final String LP_SPOTIFY = "LinkplaySpotify";
    public static final String LP_SOUND_CLOUD = "SoundCloud";
    public static final String ExtRemoteLocalSuffix = "_RemoteLocal";
    public static final String ExtLocalPreffix;
    public static final String LP_LOCALMUSIC;
    public static String BaseSuffix;

    public LPPlaySourceType() {
    }

    static {
        String var0;
        ExtLocalPreffix = var0 = "Android_" + PhoneUUIDBuilder.uuidBuild();
        LP_LOCALMUSIC = var0 + "_RemoteLocal";
        BaseSuffix = "_#~";
    }
}
