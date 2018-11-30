package bourne.com.weatherdemo.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bourne.com.weatherdemo.db.City;
import bourne.com.weatherdemo.db.Country;
import bourne.com.weatherdemo.db.Province;
import bourne.com.weatherdemo.gson.Weather;

public class Utility {
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject object=new JSONObject(response);
            JSONArray jsonArray=object.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            Log.d("weathercontent",weatherContent+"");
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
    * 解析处理省级数据
    * */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray provincesArray=new JSONArray(response);
                for(int i=0;i<provincesArray.length();i++){
                    JSONObject provinceObject=provincesArray.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }else
            return false;
    }
    /*
    * 处理市级数据
    * */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray cityArray=new JSONArray(response);
                for(int i=0;i<cityArray.length();i++){
                    JSONObject objectCity=cityArray.getJSONObject(i);
                    City city=new City();
                    city.setCityCode(objectCity.getInt("id"));
                    city.setCityName(objectCity.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /*
    * 处理解析县级数据
    * */
    public static boolean handleCountryResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray countryArray = new JSONArray(response);
                for(int i=0;i<countryArray.length();i++){
                    JSONObject objectCountry=countryArray.getJSONObject(i);
                    Country country=new Country();
                    country.setCountryName(objectCountry.getString("name"));
                    country.setCityId(cityId);
                    country.setWeatherId(objectCountry.getString("weather_Id"));
                    country.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
