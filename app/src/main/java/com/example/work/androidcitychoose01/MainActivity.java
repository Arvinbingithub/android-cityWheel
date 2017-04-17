package com.example.work.androidcitychoose01;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

public class MainActivity extends AppCompatActivity implements OnWheelChangedListener {
    private WheelView provinceWheel;
    private WheelView cityWheel;
    private WheelView areaWheel;
    private TextView showtext;
    // 所有省
    private String[] mProvinceDatas;
    //省-市
    private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
    //市-区
    private Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();

    private JSONObject mJsonObj;
    private static int provincePosition = 0;
    private static int cityPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //加载数据
        initJsonData();
        initDatas();
        //加载视图
        setView();
    }
    private void setView(){
        showtext = (TextView)findViewById(R.id.text01);
        provinceWheel = (WheelView)findViewById(R.id.provincewheel);
        cityWheel = (WheelView)findViewById(R.id.citywheel);
        areaWheel = (WheelView)findViewById(R.id.areawheel);
        provinceWheel.setViewAdapter(new ArrayWheelAdapter<String>(this, mProvinceDatas));
        provinceWheel.setCurrentItem(0);

        //监听
        provinceWheel.addChangingListener(this);
        cityWheel.addChangingListener(this);
        areaWheel.addChangingListener(this);
        //显示的单元格数目
        provinceWheel.setVisibleItems(9);
        cityWheel.setVisibleItems(9);
        areaWheel.setVisibleItems(9);
        //更新
        updateCityWheel();
        updateAreaWheel();

    }
    private void updateCityWheel(){
        provincePosition = provinceWheel.getCurrentItem();
        String[] cities = mCitisDatasMap.get(mProvinceDatas[provincePosition]);
        if (cities == null)
        {
            cities = new String[] { "" };
        }
        cityWheel.setViewAdapter(new ArrayWheelAdapter<String>(this, cities));
        cityWheel.setCurrentItem(0);
        updateAreaWheel();
    }
    private void updateAreaWheel(){
        cityPosition = cityWheel.getCurrentItem();
        String[] areas = mAreaDatasMap.get(mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]);

        if (areas == null)
        {
            areas = new String[] { "" };
        }
        areaWheel.setViewAdapter(new ArrayWheelAdapter<String>(this, areas));
        areaWheel.setCurrentItem(0);
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == provinceWheel)
        {
            updateCityWheel();
            if(mAreaDatasMap.get(mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]) == null) {
                showtext.setText(mProvinceDatas[provincePosition] + mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]);
            }else {
                showtext.setText(mProvinceDatas[provincePosition]+mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]+mAreaDatasMap.get(mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition])[0]);
            }
        } else if (wheel == cityWheel)
        {
            updateAreaWheel();
            if(mAreaDatasMap.get(mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]) == null) {
                showtext.setText(mProvinceDatas[provincePosition] + mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]);
            }else {
                showtext.setText(mProvinceDatas[provincePosition]+mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]+mAreaDatasMap.get(mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition])[0]);

            }
        } else if (wheel == areaWheel)
        {
            showtext.setText(mProvinceDatas[provincePosition]+mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition]+mAreaDatasMap.get(mCitisDatasMap.get(mProvinceDatas[provincePosition])[cityPosition])[newValue]);
        }
    }
    //从文件中读取数据
    private void initJsonData(){
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = MainActivity.this.getClass().getClassLoader().getResourceAsStream("assets/" + "city.json");
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "utf8"));
            }
            is.close();
            mJsonObj = new JSONObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 解析JSon
    private void initDatas(){
        try {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            mProvinceDatas = new String[jsonArray.length()];
            for(int i = 0;i<jsonArray.length();i++){
                JSONObject jsonP = jsonArray.getJSONObject(i);
                String province = jsonP.getString("p");
                mProvinceDatas[i] = province;
                JSONArray jsonCs = null;
                try{
                    jsonCs = jsonP.getJSONArray("c");
                }catch (Exception e1){
                    continue;
                }
                String[] mCitiesDatas = new String[jsonCs.length()];
                for(int j = 0;j<jsonCs.length();j++){
                    JSONObject jsonCity = jsonCs.getJSONObject(j);
                    String city = jsonCity.getString("n");
                    mCitiesDatas[j] = city;
                    JSONArray jsonAreas = null;
                    try{
                        jsonAreas = jsonCity.getJSONArray("a");
                    }catch (Exception e){
                        continue;
                    }

                    String[] mAreasDatas = new String[jsonAreas.length()];
                    for(int k = 0; k<jsonAreas.length();k++){
                        String area = jsonAreas.getJSONObject(k).getString("s");
                        mAreasDatas[k] = area;
                    }
                    mAreaDatasMap.put(city,mAreasDatas);
                }
                mCitisDatasMap.put(province,mCitiesDatas);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mJsonObj = null;
    }
}
