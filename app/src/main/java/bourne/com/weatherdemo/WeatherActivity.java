package bourne.com.weatherdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import bourne.com.weatherdemo.gson.Forecast;
import bourne.com.weatherdemo.gson.Weather;
import bourne.com.weatherdemo.util.HttpUtil;
import bourne.com.weatherdemo.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity{
    private ScrollView scrollViewWeatherLayout;
    private TextView txt_title;
    private TextView txt_city;
    private TextView txt_update_time;
    private TextView txt_degree;
    private TextView txt_weather_info;
    private LinearLayout forecast_layout;
    private TextView txt_aqi;
    private TextView txt_pm25;
    private TextView txt_comfort;
    private TextView txt_car_wash;
    private TextView txt_sport;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        scrollViewWeatherLayout=findViewById(R.id.scro_weather_layout);
        txt_title = findViewById(R.id.txt_title);
        txt_city = findViewById(R.id.txt_city);
        txt_update_time=findViewById(R.id.txt_time);
        txt_degree = findViewById(R.id.txt_degree);
        txt_weather_info = findViewById(R.id.txt_weather_info);
        forecast_layout = findViewById(R.id.forecast_layout);
        txt_aqi = findViewById(R.id.txt_aqi);
        txt_pm25 = findViewById(R.id.txt_pm25);
        txt_comfort = findViewById(R.id.txt_comfort);
        txt_car_wash = findViewById(R.id.txt_car_wash);
        txt_sport = findViewById(R.id.txt_sport);
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String str_weather=sp.getString("weather",null);
        if(str_weather!=null){
            //有缓存时直接解析天气信息
            Weather weather= Utility.handleWeatherResponse(str_weather);
        }else {
            //无缓存时发起天气请求
        String weatherId=getIntent().getStringExtra("weather_id");
        scrollViewWeatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(weatherId);
        }

    }
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.com/api/weather?cityid="+weatherId+"&key=5af0e1c17f5f4aa9b508dd19d8b3ef7f";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WeatherActivity.this,"加载天气数据失败",Toast.LENGTH_LONG).show();
                }
            });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
               final String strWeather=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(strWeather);
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       if(weather!=null&"ok".equals(weather.status)){
                           SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                           editor.putString("weather",strWeather);
                           editor.apply();
                       }else{
                           Toast.makeText(WeatherActivity.this,"加载天气数据失败",Toast.LENGTH_LONG).show();
                       }
                   }
               });
            }
        });
    }
    public void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split("")[0];
        String  degree=weather.now.temprature+"℃";
        String weatherInfo=weather.now.more.info;
        txt_city.setText(cityName);
        txt_update_time.setText(updateTime);
        txt_degree.setText(degree);
        txt_weather_info.setText(weatherInfo);
        forecast_layout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item,forecast_layout,false);
            TextView dateTxt=view.findViewById(R.id.txt_date);
            TextView infoTxt=view.findViewById(R.id.txt_info);
            TextView maxTxt=view.findViewById(R.id.txt_max);
            TextView minTxt=view.findViewById(R.id.txt_min);

            dateTxt.setText(forecast.date);
            infoTxt.setText(forecast.more.info);
            maxTxt.setText(forecast.temprature.max);
            minTxt.setText(forecast.temprature.min);
            forecast_layout.addView(view);
        }
        if(weather.aqi!=null){
            txt_aqi.setText(weather.aqi.city.aqi);
            txt_pm25.setText(weather.aqi.city.pm25);
        }
        String strComfort="舒适度"+weather.suggestion.comfort.info;
        String strCarWash="洗车"+weather.suggestion.carWash.info;
        String strSport="适合运动"+weather.suggestion.sport.info;
        txt_comfort.setText(strComfort);
        txt_car_wash.setText(strCarWash);
        txt_sport.setText(strSport);
        scrollViewWeatherLayout.setVisibility(View.VISIBLE);
    }
}
