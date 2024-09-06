package com.nesp.android.cling.util;

import android.os.Environment;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Lenovo on 2018/11/5.
 */

public class OkHttp3Util {

    private static OkHttpClient okHttpClient = null;


    private OkHttp3Util() {
    }

    public static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            //加同步安全
            synchronized (OkHttp3Util.class) {
                if (okHttpClient == null) {
                    //okhttp可以缓存数据....指定缓存路径
                    File sdcache = new File(Environment.getExternalStorageDirectory(), "cache");
                    //指定缓存大小
                    int cacheSize = 10 * 1024 * 1024;

                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.MINUTES)
                            .connectionPool(new ConnectionPool(4, 60, TimeUnit.SECONDS))
                            .readTimeout(30, TimeUnit.MINUTES)
                            .writeTimeout(30, TimeUnit.MINUTES)
                            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
//                            .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize))//设置缓存
                            .build();
                }
            }

        }
        return okHttpClient;
    }

    public static Response doGet(String url) {
        Response response = null;
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建Request
        Request request = null;
        request = new Request.Builder()
                .url(url)
                .build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        try {
            //同步请求要创建子线程,是因为execute()方法，会阻塞后面代码的执行
            //只有执行了execute方法之后,得到了服务器的响应response之后，才会执行后面的代码
            //所以同步请求要在子线程中完成
            response = call.execute();
            //把服务器给我们响应的字符串数据打印出来
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }



    public static Call doGet(String url, Callback callback) {

        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建Request
        Request request = null;
        request = new Request.Builder()
                .url(url)
                .build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);

        return call;
    }

    public static Call doGetWithHeaders(String url, Headers headers, Callback callback) {

        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建Request
        Request request = null;
        request = new Request.Builder()
                .url(url)
                .headers(headers)
                .build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);
        return call;
    }



    public static Call doPost(String url, String paramString, Callback callback) {

//创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), paramString);
        //创建Request
        Request request = null;
        request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }


}
