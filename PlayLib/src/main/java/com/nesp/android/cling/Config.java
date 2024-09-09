package com.nesp.android.cling;

/**
 * 说明：

 * 日期：17/7/6 11:32
 */

public class Config {

    // mp4 格式
    //http://mp4.res.hunantv.com/video/1155/79c71f27a58042b23776691d206d23bf.mp4
    // ts 格式
//    public static String TEST_URL = "http://ottvideows.hifuntv.com/b36ea6f167c7b5785f3aa46c47b6d983/595f51c1/internettv/c1/2017/03/29/41E0B7C03C15AD472DB008A5FF4566EB.ts?uuid=0c18530ecda4454db49665b178396ff7";
    // m3u8 格式
//    public static String TEST_URL = "https://acfun.iqiyi-kuyun.com/20181001/q1E2Hf9S/index.m3u8";

    public static String TEST_URL = "http://isure6.stream.qqmusic.qq.com/C200004JZBzp1de2Lm.m4a?guid=2000000279&vkey=5843F5F893F661558A50D5D56895FA3642ACC0F6942695CF864C7FB37FD5E3F01C2D4CCC039E7F9C3F7D11482F2E9DC64E62934AE34D7B7F&uin=335&fromtag=99020279&src=C200000YS3f21R43GP.m4a&trace=765cb9d06e975205";
    public static String TEST_URL2 = "http://isure6.stream.qqmusic.qq.com/C400003XLUi843SvMZ.m4a?guid=2000000279&vkey=4859B11E2CC1D2379C89772C0A20BCF782755755DC0A440BD09732375190658E622886B38229359637A86A0FEA24457F9411E40B2A5EE52C&uin=335&fromtag=99030279&src=C400000BZ1bl0XFxUb.m4a&trace=12031d3954e0e673";
    /*** 因为后台给的地址是固定的，如果不测试投屏，请设置为 false*/
    public static final boolean DLAN_DEBUG = true;
    /*** 轮询获取播放位置时间间隔(单位毫秒)*/
    public static final long REQUEST_GET_INFO_INTERVAL = 2000;
    /** 投屏设备支持进度回传 */
    private boolean hasRelTimePosCallback;
    private static Config mInstance;

    public static Config getInstance() {
        if (null == mInstance) {
            mInstance = new Config();
        }
        return mInstance;
    }

    public boolean getHasRelTimePosCallback() {
        return hasRelTimePosCallback;
    }

    public void setHasRelTimePosCallback(boolean hasRelTimePosCallback) {
        this.hasRelTimePosCallback = hasRelTimePosCallback;
    }

}
