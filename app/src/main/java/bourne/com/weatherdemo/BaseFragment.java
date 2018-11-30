package bourne.com.weatherdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bourne.com.weatherdemo.db.City;
import bourne.com.weatherdemo.db.Country;
import bourne.com.weatherdemo.db.Province;
import bourne.com.weatherdemo.util.HttpUtil;
import bourne.com.weatherdemo.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BaseFragment extends Fragment {
    public  static  final int LEVEL_PROVINCE=0;
    public  static  final int LEVEL_CITY=1;
    public  static  final int LEVEL_COUNTRY=2;
    private Button btn;
    private TextView text_title;
    private ListView list;
    private List<String> datalist=new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private ProgressDialog progressDialog;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.chooes_area,container,false);
        btn=view.findViewById(R.id.btn_back);
        text_title=view.findViewById(R.id.txt_title);
        list=view.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
        list.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCites();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCountries();
                }else if(currentLevel==LEVEL_COUNTRY){
                    String weatherId=countryList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }else if(currentLevel==LEVEL_COUNTRY){
                    queryCites();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        text_title.setText("中国");
        btn.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            list.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    private void queryCites(){
        text_title.setText("城市");
        btn.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            list.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private void queryCountries(){
        text_title.setText("县");
        countryList=DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size()>0){
            datalist.clear();
            for(Country country:countryList){
                datalist.add(country.getCountryName());
            }
            arrayAdapter.notifyDataSetChanged();
            list.setSelection(0);
            currentLevel=LEVEL_COUNTRY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int countryCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+countryCode;
            queryFromServer(address,"country");
        }
    }
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type)){
                    result=Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCites();
                            }else if("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
