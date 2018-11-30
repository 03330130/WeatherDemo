package bourne.com.weatherdemo.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient httpClient=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        httpClient.newCall(request).enqueue(callback);
    }
}
