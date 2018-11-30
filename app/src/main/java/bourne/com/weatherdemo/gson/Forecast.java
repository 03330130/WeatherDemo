package bourne.com.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;
    @SerializedName("cond")

    public More more;
    @SerializedName("tmp")

    public Temprature temprature;

    public class More{

    @SerializedName("txt_d")
    public String info;

    }

    public class Temprature{

        public String max;
        public String min;

    }
}
