package cn.bistu.edu.cs.weatherforecast;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import cn.bistu.edu.cs.weatherforecast.model.City;
import cn.bistu.edu.cs.weatherforecast.model.DisCity;
import cn.bistu.edu.cs.weatherforecast.model.Districts;
import cn.bistu.edu.cs.weatherforecast.model.DistrictsRoot;
import cn.bistu.edu.cs.weatherforecast.model.Weather;
import cn.bistu.edu.cs.weatherforecast.utils.CreateSQL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SelectCityActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    private List<Integer> cities;
    private List<String> cityName;//存放解析json数据后的城市名
    private List<String> cityId;////存放解析json数据后的城市ID
    private final Gson jsonConverter = new Gson();
    private ListView listView;//关注列表
    private ListView selectlistView;//查询历史列表

    private String selectName;
    private String selectCode;
    private SharedPreferences userSettings;
    private CreateSQL dbHelper;
    private final List<City> diaryList=new ArrayList<>();//关注列表
    private final List<City> selectList=new ArrayList<>();//查询历史列表
    private City city;
    private City select_city;

    @Override
    protected void onStart() {
        super.onStart();
        dbHelper =new CreateSQL(this,"City.db",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        diaryList.clear();
        Cursor cursor0=db.query("weather",null,"selectHistory=?",new String[]{"1"},null,null,null);
        Cursor cursor=db.query("weather",null,"selectHistory=?",new String[]{"2"},null,null,null);
        if(cursor0.moveToFirst()){
            do{
                @SuppressLint("Range") int code = cursor0.getInt(cursor0.getColumnIndex("selectCode"));
                @SuppressLint("Range") String name=cursor0.getString(cursor0.getColumnIndex("selectName"));
                select_city = new City(name,code);
                selectList.add(select_city);
            }while (cursor0.moveToNext());
        }
        if(cursor.moveToFirst()){
            do{
                @SuppressLint("Range") int code = cursor.getInt(cursor.getColumnIndex("selectCode"));
                @SuppressLint("Range") String name=cursor.getString(cursor.getColumnIndex("selectName"));
                city=new City(name,code);
                diaryList.add(city);
            }while (cursor.moveToNext());
        }
        cityAdapter adapter=new cityAdapter(SelectCityActivity.this,R.layout.attention_city,diaryList);
        listView =  findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        adapter = new cityAdapter(SelectCityActivity.this,R.layout.attention_city,selectList);
        selectlistView=  findViewById(R.id.select_list_view);
        selectlistView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_select_city);

        selectlistView= findViewById(R.id.select_list_view);
        listView= findViewById(R.id.list_view);
        userSettings = getSharedPreferences("setting", MODE_PRIVATE);

        cities = new ArrayList<>();
        //将封装的省市信息添加到动态数组之中
        cities.add(R.raw.anhui);
        cities.add(R.raw.aomeng);
        cities.add(R.raw.beijin);
        cities.add(R.raw.chongqing);
        cities.add(R.raw.fujiang);
        cities.add(R.raw.gangsu);
        cities.add(R.raw.guangdong);
        cities.add(R.raw.guangxi);
        cities.add(R.raw.guizhou);
        cities.add(R.raw.hainang);
        cities.add(R.raw.hebei);
        cities.add(R.raw.heilongjiang);
        cities.add(R.raw.henang);
        cities.add(R.raw.hongkong);
        cities.add(R.raw.hubei);
        cities.add(R.raw.hunang);
        cities.add(R.raw.jiangsu);
        cities.add(R.raw.jiangxi);
        cities.add(R.raw.jiling);
        cities.add(R.raw.liaoning);
        cities.add(R.raw.neimenggu);
        cities.add(R.raw.ningxia);
        cities.add(R.raw.qinghai);
        cities.add(R.raw.shangdong);
        cities.add(R.raw.shanghai);
        cities.add(R.raw.shangxi);
        cities.add(R.raw.shanxi);
        cities.add(R.raw.sichuang);
        cities.add(R.raw.tianjin);
        cities.add(R.raw.xinjiang);
        cities.add(R.raw.xizan);
        cities.add(R.raw.yunnang);
        cities.add(R.raw.zhejiang);

        Button doneBtn = findViewById(R.id.done_btn);
        autoCompleteTextView = findViewById(R.id.city_textview);

        cityName = new ArrayList<>();
        cityId = new ArrayList<>();

        findCity();//对json数据读取解析
        ArrayAdapter<String> autoTextString = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,cityName);
        autoCompleteTextView.setAdapter(autoTextString);

        //自动匹配框
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object obj = adapterView.getItemAtPosition(i);
                int index = cityName.indexOf(obj);
                selectCode = cityId.get(index);
                selectName = obj.toString();
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (!TextUtils.isEmpty(selectCode) && !TextUtils.isEmpty(selectName)){
//                    SharedPreferences.Editor editor = userSettings.edit();
//                    editor.putString(SplashActivity.ADCODE, selectCode);
//                    editor.putString(SplashActivity.CITYNAME, selectName);
//                    editor.apply();

                    Intent intent = new Intent(SelectCityActivity.this,MainActivity.class);
                    intent.putExtra(SplashActivity.ADCODE,selectCode);
                    intent.putExtra(SplashActivity.CITYNAME,selectName);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(SelectCityActivity.this, "请输入正确的城市！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        selectlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city=selectList.get(position);
                Intent intent=new Intent(SelectCityActivity.this,MainActivity.class);//跳转到天气内容页面
                intent.putExtra("code",city.getId());
                intent.putExtra(SplashActivity.CITYNAME,city.getName());
                Log.d("usss",city.getId()+city.getName());
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city=diaryList.get(position);
                Intent intent=new Intent(SelectCityActivity.this,MainActivity.class);//跳转到天气内容页面
                intent.putExtra("code",city.getId());
                intent.putExtra(SplashActivity.CITYNAME,city.getName());
                Log.d("usss",city.getId()+city.getName());
                startActivity(intent);
            }
        });

        //查询历史--长按监听
        selectlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                //长按则显示出一个对话框
                new AlertDialog.Builder(SelectCityActivity.this)
                        .setTitle("删除提示") //无标题
                        .setMessage("是否移除该城市记录") //内容
                        .setNegativeButton("取消",null) //连个按钮
                        .setPositiveButton("移除", new DialogInterface.OnClickListener() {
                            //删除键
                            @SuppressLint("Range")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                City city =selectList.get(position);
                                dbHelper =new CreateSQL(SelectCityActivity.this,"City.db",null,1);
                                SQLiteDatabase db=dbHelper.getWritableDatabase();
                                String[] strings = {String.valueOf(city.getId())}; //获取删除的数据库主键
                                db.delete("weather", "selectCode=?",strings);    //把对应数据删除

                                Cursor cursor = db.query("weather", null, "selectHistory=?",   new String[]{"2"}, null, null, null);
                                selectList.clear();  //清空当前ListView重新写入
                                if(cursor.moveToFirst()){
                                    do{
                                        int code = cursor.getInt(cursor.getColumnIndex("selectCode"));
                                        String name=cursor.getString(cursor.getColumnIndex("selectName"));
                                        select_city = new City(name,code);
                                        selectList.add(select_city);
                                    }while (cursor.moveToNext());
                                }

                                cityAdapter adapter= new cityAdapter(SelectCityActivity.this,R.layout.attention_city,selectList);
                                ListView selectlistView=(ListView) findViewById(R.id.select_list_view);
                                selectlistView.setAdapter(adapter);
                                Intent intent = new Intent(SelectCityActivity.this,SelectCityActivity.class);
                                startActivity(intent);
                            }
                        }).show();
                return true;
            }
        });


        //关注列表--长按监听
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                //长按则显示出一个对话框
                new AlertDialog.Builder(SelectCityActivity.this)
                        .setTitle("删除提示") //无标题
                        .setMessage("是否移除该城市") //内容
                        .setNegativeButton("取消",null) //连个按钮
                        .setPositiveButton("移除", new DialogInterface.OnClickListener() {
                            //删除键
                            @SuppressLint("Range")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                City city =diaryList.get(position);
                                dbHelper =new CreateSQL(SelectCityActivity.this,"City.db",null,1);
                                SQLiteDatabase db=dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("selectHistory","1");
                                String[] strings = {String.valueOf(city.getId())}; //获取删除的数据库主键
                                 db.update("weather", values,"selectCode=?",strings);    //把对应数据更新

                                Cursor cursor = db.query("weather", null, "selectHistory=?",  new String[]{"2"}, null, null, null);
                                diaryList.clear();  //清空当前ListView重新写入
                                if(cursor.moveToFirst()){
                                    do{
                                        int code = cursor.getInt(cursor.getColumnIndex("selectCode"));
                                        String name=cursor.getString(cursor.getColumnIndex("selectName"));
                                        city=new City(name,code);
                                        diaryList.add(city);
                                    }while (cursor.moveToNext());
                                }
                                cityAdapter adapter=new cityAdapter(SelectCityActivity.this,R.layout.attention_city,diaryList);
                                ListView listView= findViewById(R.id.list_view);
                                listView.setAdapter(adapter);
                                Intent intent = new Intent(SelectCityActivity.this,SelectCityActivity.class);
                                startActivity(intent);
                            }
                        }).show();
                return true;
            }
        });

    }

    //对json数据读取解析
    private void findCity(){
        for (int i : cities){
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = getResources().openRawResource(i);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line = "";
                while ((line = reader.readLine()) != null){//读取数据，添加到可变字符序列中
                    stringBuilder.append(line);
                }
                DistrictsRoot dis = jsonConverter.fromJson(stringBuilder.toString(),DistrictsRoot.class);
                if (dis.getDistricts().size() > 0){
                    List<Districts> _dis = dis.getDistricts();
                    if (_dis.size() > 0){
                        Districts currentDis = _dis.get(0);
                        DisCity disCity = new DisCity();
                        disCity.setAdcode(currentDis.getAdcode());
                        disCity.setName(currentDis.getName());
                        disCity.setDistricts(currentDis.getDistricts());
                        whileCity(currentDis.getDistricts(),disCity);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //传入城市名以及城市adcode数组中
    private void whileCity(List<DisCity> districtses, DisCity parenCity){
        for (DisCity c : districtses){
            if (c.getDistricts().size() > 0){
                whileCity(c.getDistricts(),c);
            }else {
                cityName.add(parenCity.getName() + " " + c.getName());
                cityId.add(c.getAdcode());
            }
        }
    }
}

//城市适配器，对应显示
class cityAdapter extends ArrayAdapter<City> {
    private final int resourceId;
    public cityAdapter(@NonNull Context context, int resource, List<City> objects) {
        super(context, resource,objects);
        resourceId=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        City City=getItem(position);
        @SuppressLint("ViewHolder") View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView item_name=(TextView) view.findViewById(R.id.item_name);
        //TextView item_id=(TextView) view.findViewById(R.id.item_id);
        //item_id.setText(String.valueOf(City.getId()));
        item_name.setText(City.getName());
        return view;
    }
}