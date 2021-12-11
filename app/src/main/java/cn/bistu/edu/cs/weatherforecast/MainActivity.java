package cn.bistu.edu.cs.weatherforecast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.content.ContentValues;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.bistu.edu.cs.weatherforecast.model.Casts;
import cn.bistu.edu.cs.weatherforecast.model.Forecasts;
import cn.bistu.edu.cs.weatherforecast.model.Lives;
import cn.bistu.edu.cs.weatherforecast.model.TimeWeather;
import cn.bistu.edu.cs.weatherforecast.model.Weather;
import cn.bistu.edu.cs.weatherforecast.utils.CreateSQL;
import cn.bistu.edu.cs.weatherforecast.utils.HttpClient;
import cn.bistu.edu.cs.weatherforecast.utils.WeatherUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private CreateSQL dbHelper;
    private String cityName;
    private String adcode;
    private LinearLayout relativeLayout;
    private LinearLayout linearLayout;
    private TextView headerLabel;
    private TextView temperatureLabel;
    private TextView posttimeLabel;
    private TextView windLabel;
    private TextView windDirectionLabel;
    private TextView weatherLabel;
    private ImageView weatherImage;
    private Button switchBtn;
    private Button attentionBtn;
    private ImageButton refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        dbHelper =new CreateSQL(this,"City.db",null,1);
        init();
        //SQLiteDatabase db=dbHelper.getWritableDatabase();
        //dbHelper.onUpgrade(db,1,2);

        //更新按钮点击操作
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.putExtra(SplashActivity.ADCODE,adcode);
                intent.putExtra(SplashActivity.CITYNAME,cityName);
                Log.d("sss",adcode+cityName);
                startActivity(intent);
                //finish();
            }
        });

        //关注按钮更新显示操作
        attentionBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View view) {
                String value = (String) attentionBtn.getText();

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                boolean exit =false;
                Cursor cursor = db.query("weather", null,null , null, null, null, null);
                int id = 0;
                if (cursor!=null&&cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndex("selectCode"));
                    } while (cursor.moveToNext()&&id==Integer.parseInt(adcode));
                }
                if (value.equals("关注")&&cursor.getCount()==0){
                    ContentValues values=new ContentValues();
                    values.put("selectCode",Integer.parseInt(adcode));
                    values.put("selectName",cityName);
                    values.put("selectHistory","2");
                    db.insert("weather",null,values);
                    attentionBtn.setText("已关注");
                    Toast.makeText(MainActivity.this,"关注成功",Toast.LENGTH_SHORT).show();
                }else if (value.equals("已关注")){
                    ContentValues values=new ContentValues();
                    values.put("selectHistory","1");
                    int flag = db.update("weather",values, "selectCode=?",new String[]{adcode});    //把对应数据删除
                    attentionBtn.setText("关注");
                    Toast.makeText(MainActivity.this,"取消关注成功",Toast.LENGTH_SHORT).show();

                }else{
                    ContentValues values=new ContentValues();
                    values.put("selectHistory","2");
                    int flag = db.update("weather",values, "selectCode=?",new String[]{adcode});    //把对应数据删除
                    attentionBtn.setText("已关注");
                    Toast.makeText(MainActivity.this,"关注成功",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    //初始化界面元素以及更新数据
    private void init(){
        initView();
        initData();
    }

    private void initView(){
        Intent intent = getIntent();
        relativeLayout = findViewById(R.id.activity_main);
        linearLayout = findViewById(R.id.main_weather_info_layout);
        headerLabel = findViewById(R.id.main_header_label);
        weatherImage = findViewById(R.id.main_weather_image);
        weatherLabel = findViewById(R.id.main_weather_info);
        windDirectionLabel = findViewById(R.id.main_weather_direction);
        windLabel = findViewById(R.id.main_weather_wind);
        posttimeLabel = findViewById(R.id.main_weather_posttime);
        temperatureLabel = findViewById(R.id.main_wearher_temperature);
        switchBtn = findViewById(R.id.change_btn);
        refreshBtn = findViewById(R.id.refresh_btn);
        attentionBtn = findViewById(R.id.attention_btn);
        //看是否是有输入框传送的数据还是列表的显示数据
        if (intent.getIntExtra("code",0)!=0){//列表数据
            dbHelper =new CreateSQL(this,"City.db",null,1);
            SQLiteDatabase db=dbHelper.getWritableDatabase();
            int id2=intent.getIntExtra("code",0);
            Cursor cursor=db.query("weather",null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do{
                    @SuppressLint("Range")
                    int id = cursor.getInt(cursor.getColumnIndex("selectCode"));
                    @SuppressLint("Range")
                    String history = cursor.getString(cursor.getColumnIndex("selectHistory"));
                    if (id == id2 && history.equals("2")){//accode与数据库查到的id相同，且历史记录为“2”(城市存入数据库history=1，存入并关注history=2)
                        Log.d("attention-----","已关注");
                        attentionBtn.setText("已关注");
                        break;
                    }else{
                        Log.d("attention-----","关注");
                        attentionBtn.setText("关注");
                    }
                }while (cursor.moveToNext());
            }
        }else{//输入框数据
            dbHelper =new CreateSQL(this,"City.db",null,1);
            SQLiteDatabase db=dbHelper.getWritableDatabase();
            adcode = intent.getStringExtra(SplashActivity.ADCODE);
            Cursor cursor=db.query("weather",null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do{
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("selectCode"));
                    @SuppressLint("Range") String history = cursor.getString(cursor.getColumnIndex("selectHistory"));
                    if (id == Integer.parseInt(adcode) && history.equals("2")){
                        attentionBtn.setText("已关注");
                        break;
                    }else{
                        attentionBtn.setText("关注");
                    }

                }while (cursor.moveToNext());
            }
        }

        //返回切换界面
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Cursor cursor = db.query("weather", null,null , null, null, null, null);
                Cursor cursor1 = db.query("weather", null,"selectCode=?" , new String[]{adcode}, null, null, null);

                int id = 0;
                if (cursor!=null&&cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndex("selectCode"));
                    }while (cursor.moveToNext() && id == Integer.parseInt(adcode));
                    if (id != Integer.parseInt(adcode)&&cursor1.getCount()==0) {
                        ContentValues values = new ContentValues();
                        values.put("selectCode", Integer.parseInt(adcode));
                        values.put("selectName", cityName);
                        values.put("selectHistory", "1");
                        db.insert("weather", null, values);
                    }
                }

                Intent intent = new Intent(MainActivity.this, SelectCityActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.HOUR_OF_DAY) > 18 || now.get(Calendar.HOUR_OF_DAY) < 7){//设置天气背景
            relativeLayout.setBackgroundResource(R.drawable.dark);
        }else {
            relativeLayout.setBackgroundResource(R.drawable.light);
        }
    }

    private void initData(){
        Intent intent = getIntent();
        cityName = intent.getStringExtra(SplashActivity.CITYNAME);
        if (intent.getIntExtra("code",0)!=0){
            adcode = String.valueOf(intent.getIntExtra("code",0));
        }else{
            adcode = intent.getStringExtra(SplashActivity.ADCODE);
        }
        headerLabel.setText(cityName);


        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //得到base类型的结果
        HttpClient.query(adcode, HttpClient.WEATHER_TYPE_BASE, Weather.class, new HttpClient.IHttpCallback() {
            @Override
            public <T> void onSuccess(T result, boolean isSuccess) {
                if(isSuccess){
                    Weather weather = (Weather)result;
                    if (weather.getInfo().equals("OK") && weather.getCount().equals("1")){
                        final Lives info = weather.getLives().get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureLabel.setText(info.getTemperature());
                                posttimeLabel.setText(format(info.getReporttime()) + "更新");
                                windDirectionLabel.setText(info.getWinddirection());
                                windLabel.setText("湿度" + info.getHumidity() + "%");

                                if (WeatherUtils.WeatherKV.containsKey(info.getWeather())){
                                    weatherLabel.setText(info.getWeather());
                                    weatherImage.setImageResource(WeatherUtils.WeatherKV.get(info.getWeather()));
                                }else {
                                    temperatureLabel.setText("N/A");
                                }
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureLabel.setText("服务不可用");
                                Toast.makeText(MainActivity.this, "服务不可用", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            temperatureLabel.setText("无法提供天气信息");
                            Toast.makeText(MainActivity.this, "无法提供天气信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        //得到返回类型为all类型
        HttpClient.query(adcode, HttpClient.WEATHER_TYPE_ALL, TimeWeather.class, new HttpClient.IHttpCallback() {
            @Override
            public <T> void onSuccess(T result, boolean isSuccess) {
                if (isSuccess){
                    final TimeWeather timeWeather = (TimeWeather)result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (timeWeather.getInfo().equals("OK") && timeWeather.getCount().equals("1")){
                                for (Forecasts forecasts : timeWeather.getForecasts()){
                                    for (Casts casts : forecasts.getCasts()){
                                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.weather_itme,linearLayout,false);
                                        TextView date = view.findViewById(R.id.item_date);
                                        TextView max = view.findViewById(R.id.item_max);
                                        TextView min = view.findViewById(R.id.itme_min);
                                        TextView currentWeather = view.findViewById(R.id.item_weather);
                                        TextView week = view.findViewById(R.id.item_week);

                                        date.setText(getDay(casts.getDate()));
                                        max.setText(casts.getDaytemp() + "°");
                                        min.setText(casts.getNighttemp() + "°");
                                        currentWeather.setText(casts.getDayweather());
                                        week.setText(getWeek(casts.getWeek()));

                                        linearLayout.addView(view);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private String format(String posttime){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(posttime);

            return new SimpleDateFormat("HH:MM").format(date);
        } catch (ParseException e) {
            return "刚刚更新";
        }
    }

    private String getDay(String date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date toDate = dateFormat.parse(date);

            return new SimpleDateFormat("yyyy-MM-dd").format(toDate);
        } catch (ParseException e) {
            return "N/A";
        }
    }

    private String getWeek(String week){
        switch (week)
        {
            case "1":
                return "星期一";
            case "2":
                return "星期二";
            case "3":
                return "星期三";
            case "4":
                return "星期四";
            case "5":
                return "星期五";
            case "6":
                return "星期六";
            default:
                return "星期日";
        }
    }
}
