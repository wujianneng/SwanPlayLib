package com.nesp.android.cling.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.text.TextUtils;

import com.nesp.android.cling.entity.LPPlayHeader;
import com.nesp.android.cling.entity.LPPlayItem;
import com.nesp.android.cling.entity.LPPlayMusicList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParserFactory;

public class LPXmlUtil {
    public LPXmlUtil() {
    }

    public static String Encode(String var0) {
        return TextUtils.isEmpty(var0) ? var0 : var0.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;").replace("\"", "&quot;");
    }

    public static String Decode(String var0) {
        return TextUtils.isEmpty(var0) ? var0 : var0.replace("&lt;", "<").replace("&gt;", ">").replace("&apos;", "'").replace("&quot;", "\"").replace("&amp;", "&");
    }

    public static String getCommonStr(String var0) {
        if (TextUtils.isEmpty(var0)) {
            return "";
        } else {
            char[] var1 = var0.toCharArray();
            boolean var2 = false;

            for(int var3 = 0; var3 < var1.length; ++var3) {
                char var4;
                if ((var4 = var1[var3]) > 0 && var4 < ' ') {
                    var1[var3] = ' ';
                    var2 = true;
                }
            }

            if (var2) {
                return (new String(var1)).trim();
            } else {
                return var0;
            }
        }
    }

    public static String convertFromQueueContext(String var0) {
        return TextUtils.isEmpty(var0) ? var0 : var0.replaceAll("<Key\\d{1,2}>", "<Key>").replaceAll("</Key\\d{1,2}>", "</Key>");
    }

    public static Object getValue(Object var0, String var1) {
        Object var10000 = var0;
        Object var2 = null;

        try {
            var10000 = var10000.getClass().getDeclaredMethod(var1).invoke(var0);
        } catch (NoSuchMethodException var3) {
            var3.printStackTrace();
            return var2;
        } catch (IllegalAccessException var4) {
            var4.printStackTrace();
            return var2;
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
            return var2;
        }

        var2 = var10000;
        return var2;
    }

    public static String getMetadataByHeader(String var0, LPPlayHeader var1) {
        if (var1 == null) {
            return "";
        } else {
            String var2 = "" + "<DIDL-Lite ";
            var2 = var2 + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ";
            var2 = var2 + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" ";
            var2 = var2 + "xmlns:song=\"www.wiimu.com/song/\" ";
            var2 = var2 + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"> ";
            var2 = var2 + "<upnp:class>object.item.audioItem.musicTrack</upnp:class> ";
            var2 = var2 + "<item> ";
            var2 = var2 + "<song:bitrate>0</song:bitrate> ";
            var2 = var2 + "<song:id>" + var1.getHeadId() + "</song:id>";
            var2 = var2 + "<song:singerid>0</song:singerid>";
            var2 = var2 + "<song:albumid>0</song:albumid>";
            var2 = var2 + "<res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;\" duration=\"" + "0" + "\">" + Encode(getCommonStr(var1.getSearchUrl())) + "</res>";
            var2 = var2 + "<dc:title>" + Encode(getCommonStr(var1.getHeadTitle())) + "</dc:title> ";
            var2 = var2 + "<upnp:artist>" + Encode(getCommonStr(var1.getMediaSource())) + "</upnp:artist> ";
            var0 = var2 + "<upnp:creator>" + Encode(getCommonStr(var0)) + "</upnp:creator> ";
            var0 = var0 + "<upnp:album></upnp:album> ";
            StringBuilder var3 = (new StringBuilder()).append(var0).append("<upnp:albumArtURI>");
            var0 = var3.append(Encode(getCommonStr(var1.getImageUrl() == null ? "" : var1.getImageUrl()))).append("</upnp:albumArtURI> ").toString();
            var0 = var0 + "</item> ";
            return Encode(var0 + "</DIDL-Lite> ");
        }
    }

    public static String getMetadata(String var0, LPPlayItem var1) {
        if (var1 == null) {
            return "";
        } else {
            String var2 = "" + "<DIDL-Lite ";
            var2 = var2 + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ";
            var2 = var2 + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" ";
            var2 = var2 + "xmlns:song=\"www.wiimu.com/song/\" ";
            var2 = var2 + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"> ";
            var2 = var2 + "<upnp:class>object.item.audioItem.musicTrack</upnp:class> ";
            var2 = var2 + "<item> ";
            var2 = var2 + "<song:bitrate>0</song:bitrate> ";
            var2 = var2 + "<song:id>" + var1.getTrackId() + "</song:id>";
            var2 = var2 + "<song:singerid>0</song:singerid>";
            var2 = var2 + "<song:albumid>" + var1.getAlbumId() + "</song:albumid>";
            String var3;
            if (var1.getTrackDuration() != 1L) {
                var3 = var1.getTrackDuration() + "";
            } else {
                var3 = "0";
            }

            var2 = var2 + "<res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;\" duration=\"" + var3 + "\">" + Encode(getCommonStr(var1.getTrackUrl())) + "</res>";
            var2 = var2 + "<dc:title>" + Encode(getCommonStr(var1.getTrackName())) + "</dc:title> ";
            var2 = var2 + "<upnp:artist>" + Encode(getCommonStr(var1.getTrackArtist())) + "</upnp:artist> ";
            var0 = var2 + "<upnp:creator>" + Encode(getCommonStr(var0)) + "</upnp:creator> ";
            var0 = var0 + "<upnp:album>" + Encode(getCommonStr(var1.getAlbumName())) + "</upnp:album> ";
            StringBuilder var4 = (new StringBuilder()).append(var0).append("<upnp:albumArtURI>");
            var0 = var4.append(Encode(getCommonStr(var1.getTrackImage() == null ? "" : var1.getTrackImage()))).append("</upnp:albumArtURI> ").toString();
            var0 = var0 + "</item> ";
            return Encode(var0 + "</DIDL-Lite> ");
        }
    }

    public static LPPlayMusicList convert2CurrentQueueItem(String var0) {
        LPPlayMusicList var1;
        LPPlayMusicList var10000 = var1 = new LPPlayMusicList();
        LPPlayHeader var2;
        LPPlayHeader var10002 = var2 = new LPPlayHeader();
        var10000.setHeader(var10002);
        ArrayList var3;
        ArrayList var10001 = var3 = new ArrayList();
        var10000.setList(var10001);
        Matcher var4 = Pattern.compile("<PlayList>\r?\n?<ListName>(.*)</ListName>\r?\n?<ListInfo>", 40).matcher(var0);

        while(var4.find()) {
            var2.setHeadTitle(var4.group(1));
        }

        var4 = Pattern.compile("<LastPlayIndex>(.*)</LastPlayIndex>", 40).matcher(var0);

        while(var4.find()) {
            var1.setIndex(Integer.parseInt(var4.group(1)));
        }

        String var25 = var0.replaceAll("<Track[0-9]+>", "<ListRoot>").replaceAll("</Track[0-9]+>", "</ListRoot>");
        Matcher var17 = Pattern.compile("^<Tracks>(.*)</Tracks>$", 40).matcher(var25);
        boolean var18 = var25.contains("<Source>qplay");

        while(var17.find()) {
            String[] var5;
            int var6 = (var5 = var17.group(1).split("</ListRoot>")).length;

            for(int var7 = 0; var7 < var6; ++var7) {
                if (var5[var7].trim().length() > 0) {
                    StringBuilder var8;
                    StringBuilder var26 = var8 = new StringBuilder();
                    String var19;
                    var25 = var19 = var26.append(var5[var7]).append("</ListRoot>").toString();
                    int var9 = var25.indexOf("<Source>") + 8;
                    String var22;
                    if (!TextUtils.isEmpty(var22 = var25.substring(var9, var25.indexOf("</Source>")))) {
                        var2.setMediaSource(var22);
                    }

                    LPPlayItem var21;
                    Exception var27;
                    boolean var28;
                    if (var18) {
                        label98: {
                            label97: {
                                boolean var32;
                                label96: {
                                    label133: {
                                        Matcher var31;
                                        try {
                                            var31 = Pattern.compile("<Metadata>\r?\n?(.*)\r?\n?</Metadata>\r?\n?", 41).matcher(var19);
                                        } catch (Exception var14) {
                                            var27 = var14;
                                            var28 = false;
                                            break label133;
                                        }

                                        Matcher var23 = var31;
                                        var22 = null;

                                        try {
                                            var32 = var31.find();
                                        } catch (Exception var13) {
                                            var27 = var13;
                                            var28 = false;
                                            break label133;
                                        }

                                        if (var32) {
                                            try {
                                                var25 = var23.group(1);
                                            } catch (Exception var12) {
                                                var27 = var12;
                                                var28 = false;
                                                break label133;
                                            }

                                            var22 = var25;
                                        }

                                        if (var22 == null) {
                                            break label97;
                                        }

                                        LPPlayItem var33;
                                        try {
                                            var33 = d(Decode(var22));
                                        } catch (Exception var11) {
                                            var27 = var11;
                                            var28 = false;
                                            break label133;
                                        }

                                        var21 = var33;
                                        if (var33 == null) {
                                            break label97;
                                        }

                                        try {
                                            var32 = TextUtils.isEmpty(var21.getTrackUrl());
                                            break label96;
                                        } catch (Exception var10) {
                                            var27 = var10;
                                            var28 = false;
                                        }
                                    }

                                    var27.printStackTrace();
                                    break label97;
                                }

                                if (var32) {
                                    break label98;
                                }
                            }

                            var21 = null;
                        }

                        if (var21 != null) {
                            var3.add(var21);
                        }
                    } else {
                        MyDefaultHandler var24;
                        var24 = new MyDefaultHandler(var19, var2);

                        label107: {
                            label134: {
                                ByteArrayInputStream var29;
                                try {
                                    var29 = new ByteArrayInputStream(var24.f.getBytes("utf-8"));
                                } catch (Exception var16) {
                                    var27 = var16;
                                    var28 = false;
                                    break label134;
                                }

                                ByteArrayInputStream var20;
                                ByteArrayInputStream var30 = var20 = var29;

                                try {
                                    SAXParserFactory.newInstance().newSAXParser().parse(var20, var24);
                                    var29.close();
                                    break label107;
                                } catch (Exception var15) {
                                    var27 = var15;
                                    var28 = false;
                                }
                            }

                            var27.printStackTrace();
                        }

                        if ((var21 = var24.b) != null && !TextUtils.isEmpty(var21.getTrackUrl())) {
                            var3.add(var21);
                        }
                    }
                }
            }
        }

        return var1;
    }

    public static synchronized LPPlayItem d(String var0) {
        JSONObject var10000;
        boolean var10001;
        try {
            var10000 = new JSONObject(var0);
        } catch (Exception var20) {
            var10001 = false;
            return null;
        }

        JSONObject var1;
        JSONObject var25 = var1 = var10000;

        LPPlayItem var26;
        try {
            var26 = new LPPlayItem();
        } catch (Exception var19) {
            var10001 = false;
            return null;
        }

        LPPlayItem var21 = var26;

        boolean var23;
        try {
            var23 = var10000.has("songID");
        } catch (Exception var18) {
            var10001 = false;
            return null;
        }

        if (var23) {
            try {
                var21.setTrackId(String.valueOf(var1.getLong("songID")));
            } catch (Exception var2) {
            }
        }

        try {
            var23 = var1.has("duration");
        } catch (Exception var17) {
            var10001 = false;
            return null;
        }

        if (var23) {
            LPPlayItem var24;
            long var28;
            try {
                var24 = var21;
                var28 = c(var1.getString("duration"));
            } catch (Exception var16) {
                var10001 = false;
                return null;
            }

            var28 = (long)((int)var28);

            try {
                var24.setTrackDuration(var28);
            } catch (Exception var15) {
                var10001 = false;
                return null;
            }
        }

        try {
            var23 = var1.has("title");
        } catch (Exception var14) {
            var10001 = false;
            return null;
        }

        if (var23) {
            try {
                var21.setTrackName(var1.getString("title"));
            } catch (Exception var13) {
                var10001 = false;
                return null;
            }
        }

        try {
            var23 = var1.has("albumArtURI");
        } catch (Exception var12) {
            var10001 = false;
            return null;
        }

        if (var23) {
            try {
                var21.setTrackImage(var1.getString("albumArtURI"));
            } catch (Exception var11) {
                var10001 = false;
                return null;
            }
        }

        try {
            var23 = var1.has("album");
        } catch (Exception var10) {
            var10001 = false;
            return null;
        }

        if (var23) {
            try {
                var21.setAlbumName(var1.getString("album"));
            } catch (Exception var9) {
                var10001 = false;
                return null;
            }
        }

        try {
            var23 = var1.has("creator");
        } catch (Exception var8) {
            var10001 = false;
            return null;
        }

        if (var23) {
            try {
                var21.setTrackArtist(var1.getString("creator"));
            } catch (Exception var7) {
                var10001 = false;
                return null;
            }
        }

        try {
            var23 = var1.has("trackURIs");
        } catch (Exception var6) {
            var10001 = false;
            return null;
        }

        if (var23) {
            JSONArray var27;
            try {
                var27 = var1.getJSONArray("trackURIs");
            } catch (Exception var5) {
                var10001 = false;
                return var21;
            }

            JSONArray var22 = var27;

            int var29;
            try {
                var29 = var27.length();
            } catch (Exception var4) {
                var10001 = false;
                return var21;
            }

            if (var29 > 0) {
                try {
                    var21.setTrackUrl(var22.getString(0));
                } catch (Exception var3) {
                    var10001 = false;
                }
            }
        }

        return var21;
    }

    public static long c(String var0) {
        long var1 = 0L;
        if (var0 != null && var0.trim().length() > 0) {
            long var18;
            if (var0.contains(":")) {
                label61: {
                    String[] var10;
                    if ((var10 = var0.split(":")).length > 0) {
                        for(int var11 = 0; var11 < var10.length; ++var11) {
                            String var2;
                            if ((var2 = var10[var11]).contains(".")) {
                                var2 = var2.substring(0, var2.indexOf("."));
                            }

                            var10[var11] = var2;
                        }

                        if (var10.length == 3) {
                            var18 = Long.parseLong(var10[0]) * 3600L;
                            var1 = Long.parseLong(var10[1]) * 60L;
                            long var13 = Long.parseLong(var10[2]) * 1L;
                            var1 = var18 + var1 + var13;
                            break label61;
                        }

                        if (var10.length == 2) {
                            var1 = Long.parseLong(var10[0]) * 60L + Long.parseLong(var10[1]) * 1L;
                            break label61;
                        }
                    }

                    var1 = 0L;
                }

                var1 *= 1000L;
            } else {
                NumberFormatException var10000;
                label113: {
                    boolean var14;
                    boolean var10001;
                    try {
                        var14 = TextUtils.isEmpty(var0);
                    } catch (NumberFormatException var8) {
                        var10000 = var8;
                        var10001 = false;
                        break label113;
                    }

                    boolean var12;
                    label109: {
                        if (!var14) {
                            int var3 = 0;

                            while(true) {
                                int var15;
                                int var16;
                                try {
                                    var15 = var3;
                                    var16 = var0.length();
                                } catch (NumberFormatException var7) {
                                    var10000 = var7;
                                    var10001 = false;
                                    break label113;
                                }

                                if (var15 >= var16) {
                                    var12 = true;
                                    break label109;
                                }

                                char var17;
                                try {
                                    var17 = var0.charAt(var3);
                                } catch (NumberFormatException var6) {
                                    var10000 = var6;
                                    var10001 = false;
                                    break label113;
                                }

                                char var4 = var17;
                                if (var17 < '0' || var4 > '9') {
                                    break;
                                }

                                ++var3;
                            }
                        }

                        var12 = false;
                    }

                    if (!var12) {
                        return 0L;
                    }

                    try {
                        var18 = Long.parseLong(var0);
                    } catch (NumberFormatException var5) {
                        var10000 = var5;
                        var10001 = false;
                        break label113;
                    }

                    var1 = var18;
                    return var1;
                }

                NumberFormatException var9 = var10000;
                var9.printStackTrace();
            }

            return var1;
        } else {
            return 0L;
        }
    }
}
