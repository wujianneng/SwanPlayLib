package com.nesp.android.cling.util;

import android.text.TextUtils;

import com.nesp.android.cling.entity.LPPlayHeader;
import com.nesp.android.cling.entity.LPPlayItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyDefaultHandler extends DefaultHandler {
    public LPPlayHeader a;
    public LPPlayItem b;
    public StringBuffer c;
    public StringBuffer d;
    public StringBuffer e;
    public String f;
    public String g;

    public MyDefaultHandler(String var1, LPPlayHeader var2) {
        super();
        this.c = null;
        this.d = null;
        this.e = null;
        this.g = null;
        this.f = var1;
        LPPlayItem var3;
        var3 = new LPPlayItem();
        this.b = var3;
        this.a = var2;
    }

    public void characters(char[] var1, int var2, int var3) throws SAXException {
        super.characters(var1,var2,var3);
        if (this.g != null) {
            String var4;
            var4 = new String(var1, var2, var3);
            StringBuffer var5;
            if (this.g.equals("URL")) {
                if (this.c == null) {
                    var5 = new StringBuffer();
                    this.c = var5;
                }

                this.c.append(var4);
            } else if (this.g.equals("Metadata")) {
                if (this.e == null) {
                    var5 = new StringBuffer();
                    this.e = var5;
                }

                this.e.append(var4);
            } else if (this.g.equals("Source")) {
                if (this.d == null) {
                    var5 = new StringBuffer();
                    this.d = var5;
                }

                this.d.append(var4);
            }
        }

    }

    public void endElement(String var1, String var2, String var3) throws SAXException {
        super.endElement(var1, var2, var3);
        if (var3 != null && var3.equals("ListRoot")) {
            try {
                this.b = a(LPXmlUtil.Decode(this.e.toString()), this.a);
            } catch (Exception var4) {
                var4.printStackTrace();
            }

            this.b.setTrackUrl(this.c.toString());
            StringBuffer var5;
            if ((var5 = this.d) != null) {
                this.a.setMediaSource(var5.toString());
            }

            this.e = null;
            this.d = null;
            this.c = null;
        }

    }

    public static synchronized LPPlayItem a(String var0, LPPlayHeader var1) {
        LPPlayItem var2;
        var2 = new LPPlayItem();
        if (TextUtils.isEmpty(var0)) {
            return var2;
        } else {
            String var10000 = var0;
            String var3 = "";

            boolean var10001;
            int var10002;
            boolean var48;
            String var49;
            label291: {
                label290: {
                    Exception var47;
                    label297: {
                        try {
                            var48 = var10000.contains("<song:description>");
                        } catch (Exception var38) {
                            var47 = var38;
                            var10001 = false;
                            break label297;
                        }

                        if (!var48) {
                            break label291;
                        }

                        try {
                            var10000 = var0;
                            var49 = var0;
                            var10002 = var0.indexOf("<song:description>");
                        } catch (Exception var37) {
                            var47 = var37;
                            var10001 = false;
                            break label297;
                        }

                        int var40 = var10002;

                        try {
                            var10000 = var10000.substring(var40, var49.indexOf("</song:description>")).replace("<song:description>", "");
                            break label290;
                        } catch (Exception var36) {
                            var47 = var36;
                            var10001 = false;
                        }
                    }

                    var47.printStackTrace();
                    var3 = "";
                    break label291;
                }

                var3 = var10000;
            }

            label275: {
                try {
                    var10000 = var0.replaceAll("<upnp:artist\\s*[\\w\\W]*>(.*)</upnp:artist>", "<upnp:artist>$1</upnp:artist>");
                } catch (Exception var35) {
                    break label275;
                }

                var0 = var10000;
            }

            label271: {
                try {
                    var10000 = var0.replaceAll("<upnp:albumArtURI\\s*[\\w\\W]*>(.*)</upnp:albumArtURI>", "<upnp:albumArtURI>$1</upnp:albumArtURI>");
                } catch (Exception var34) {
                    break label271;
                }

                var0 = var10000;
            }

            String var4 = "";
            if (var0.contains("<dc:title>")) {
                int var41 = var0.indexOf("<dc:title>");
                var4 = var0.substring(var41, var0.indexOf("</dc:title>")).replace("<dc:title>", "");
            }

            var10000 = var0;
            String var5 = "";

            label264: {
                label307: {
                    int var6;
                    label299: {
                        try {
                            var48 = var10000.contains("<upnp:artist>");
                        } catch (Exception var33) {
                            var10001 = false;
                            break label299;
                        }

                        if (!var48) {
                            break label264;
                        }

                        try {
                            var10000 = var0;
                            var49 = var0;
                            var10002 = var0.indexOf("<upnp:artist>");
                        } catch (Exception var32) {
                            var10001 = false;
                            break label299;
                        }

                        var6 = var10002;

                        try {
                            var10000 = var10000.substring(var6, var49.indexOf("</upnp:artist>")).replace("<upnp:artist>", "");
                            break label307;
                        } catch (Exception var31) {
                            var10001 = false;
                        }
                    }

                    try {
                        var48 = var0.contains("<dc:creator>");
                    } catch (Exception var30) {
                        var10001 = false;
                        break label264;
                    }

                    if (!var48) {
                        break label264;
                    }

                    try {
                        var10000 = var0;
                        var49 = var0;
                        var10002 = var0.indexOf("<dc:creator>");
                    } catch (Exception var29) {
                        var10001 = false;
                        break label264;
                    }

                    var6 = var10002;

                    try {
                        var10000 = var10000.substring(var6, var49.indexOf("</dc:creator>")).replace("<dc:creator>", "");
                    } catch (Exception var28) {
                        var10001 = false;
                        break label264;
                    }
                }

                var5 = var10000;
            }

            var10000 = var0;
            String var42 = "";

            label317: {
                try {
                    var48 = var10000.contains("<upnp:album>");
                } catch (Exception var27) {
                    var10001 = false;
                    break label317;
                }

                if (var48) {
                    try {
                        var10000 = var0;
                        var49 = var0;
                        var10002 = var0.indexOf("<upnp:album>");
                    } catch (Exception var26) {
                        var10001 = false;
                        break label317;
                    }

                    int var7 = var10002;

                    try {
                        var10000 = var10000.substring(var7, var49.indexOf("</upnp:album>")).replace("<upnp:album>", "");
                    } catch (Exception var25) {
                        var10001 = false;
                        break label317;
                    }

                    var42 = var10000;
                }
            }

            var10000 = var0;
            String var43 = "";

            label318: {
                try {
                    var48 = var10000.contains("<upnp:albumArtURI>");
                } catch (Exception var24) {
                    var10001 = false;
                    break label318;
                }

                if (var48) {
                    try {
                        var10000 = var0;
                        var49 = var0;
                        var10002 = var0.indexOf("<upnp:albumArtURI>");
                    } catch (Exception var23) {
                        var10001 = false;
                        break label318;
                    }

                    int var8 = var10002;

                    try {
                        var10000 = var10000.substring(var8, var49.indexOf("</upnp:albumArtURI>")).replace("<upnp:albumArtURI>", "");
                    } catch (Exception var22) {
                        var10001 = false;
                        break label318;
                    }

                    var43 = var10000;
                }
            }

            var10000 = var0;
            String var44 = "";

            int var9;
            label319: {
                try {
                    var48 = var10000.contains("<song:id>");
                } catch (Exception var21) {
                    var10001 = false;
                    break label319;
                }

                if (var48) {
                    try {
                        var10000 = var0;
                        var49 = var0;
                        var10002 = var0.indexOf("<song:id>");
                    } catch (Exception var20) {
                        var10001 = false;
                        break label319;
                    }

                    var9 = var10002;

                    try {
                        var10000 = var10000.substring(var9, var49.indexOf("</song:id>")).replace("<song:id>", "");
                    } catch (Exception var19) {
                        var10001 = false;
                        break label319;
                    }

                    var44 = var10000;
                }
            }

            if (var0.contains("<song:subid>")) {
                var9 = var0.indexOf("<song:subid>");
                var0.substring(var9, var0.indexOf("</song:subid>")).replace("<song:subid>", "");
            }

            var10000 = var0;
            String var45 = "";

            label180: {
                label179: {
                    label304: {
                        try {
                            var48 = var10000.contains("<song:quality>");
                        } catch (Exception var18) {
                            var10001 = false;
                            break label304;
                        }

                        if (!var48) {
                            break label180;
                        }

                        try {
                            var10000 = var0;
                            var49 = var0;
                            var10002 = var0.indexOf("<song:quality>");
                        } catch (Exception var17) {
                            var10001 = false;
                            break label304;
                        }

                        var9 = var10002;

                        try {
                            var10000 = var10000.substring(var9, var49.indexOf("</song:quality>")).replace("<song:quality>", "");
                            break label179;
                        } catch (Exception var16) {
                            var10001 = false;
                        }
                    }

                    var45 = "0";
                    break label180;
                }

                var45 = var10000;
            }

            label305: {
                try {
                    var48 = var0.contains("<song:skiplimit>");
                } catch (Exception var15) {
                    var10001 = false;
                    break label305;
                }

                if (var48) {
                    label312: {
                        try {
                            var10000 = var0;
                            var49 = var0;
                            var10002 = var0.indexOf("<song:skiplimit>");
                        } catch (Exception var14) {
                            var10001 = false;
                            break label312;
                        }

                        int var10 = var10002;

                        try {
                            var10000.substring(var10, var49.indexOf("</song:skiplimit>")).replace("<song:skiplimit>", "");
                        } catch (Exception var13) {
                            var10001 = false;
                        }
                    }
                }
            }

            String var46 = "";
            String var11 = "";
            Matcher var39;
            if ((var39 = Pattern.compile("<res protocolInfo=\"(.*)\" duration=\"(.*)\">(.*)</res>", 43).matcher(var0)).find()) {
                var46 = var39.group(2);
                var11 = var39.group(3);
            }

            var2.setTrackName(LPXmlUtil.Decode(var4).replace("<![CDATA[", "").replace("]]>", ""));
            var2.setTrackArtist(LPXmlUtil.Decode(var5).replace("<![CDATA[", "").replace("]]>", ""));
            var2.setAlbumName(LPXmlUtil.Decode(var42).replace("<![CDATA[", "").replace("]]>", ""));
            var2.setTrackImage(LPXmlUtil.Decode(var43).replace("<![CDATA[", "").replace("]]>", ""));
            if (!TextUtils.isEmpty(var44)) {
                try {
                    var2.setTrackId(String.valueOf(Long.parseLong(var44)));
                } catch (Exception var12) {
                    var2.setTrackId("0");
                }
            }

            var1.setDescribe(var3);
            var2.setTrackDuration((long)((int)c(var46)));
            var2.setTrackUrl(var11);
            var0 = String.valueOf((TextUtils.isEmpty(var45) ? 0 : Integer.parseInt(var45)) == 0 ? 1 : Integer.parseInt(var45));
            var1.setQuality(var0);
            return var2;
        }
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

    public void startElement(String var1, String var2, String var3, Attributes var4) throws SAXException {
        super.startElement(var1, var2, var3, var4);
        this.g = var3;
    }
}
