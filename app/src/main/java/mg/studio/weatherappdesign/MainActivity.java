package mg.studio.weatherappdesign;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import mg.studio.weather.util.NetUtil;
import mg.studio.weather.util.TodayWeather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextView cityTv, temperatureTv;
    private ImageView weatherImg;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        String cityCode="重庆";
        queryWeatherCode(cityCode);
        ((TextView)findViewById(R.id.tv_date)).setText(getTime());
        ((TextView)findViewById(R.id.weekView)).setText(getWeek());

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "Internet is OK");
            Toast.makeText(MainActivity.this, "Internet is OK！", Toast.LENGTH_LONG).show();

        } else {
            Log.d("myWeather", "Internet is off");
            Toast.makeText(MainActivity.this, "Internet is off！", Toast.LENGTH_LONG).show();
        }
    }

    void initView() {

        cityTv = (TextView) findViewById(R.id.tv_location);
        temperatureTv = (TextView) findViewById(R.id.temperature_of_the_day);
        weatherImg = (ImageView) findViewById(R.id.img_weather_condition);
    }

    public String getTime(){
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号
        String mHour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));//时
        String mMinute = String.valueOf(c.get(Calendar.MINUTE));//分
        String mSecond = String.valueOf(c.get(Calendar.SECOND));//秒

        return mMonth +"/"+ mDay+"/"+mYear;
    }

    public String getWeek(){
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if("1".equals(mWay)){
            return "SUNDAY";
        }else if("2".equals(mWay)){
            return "MONDAY";
        }else if("3".equals(mWay)){
            return "TUESDAY";
        }else if("4".equals(mWay)){
            return "WEDNESDAY";
        }else if("5".equals(mWay)){
            return "THURSDAY";
        }else if("6".equals(mWay)){
            return "FRIDAY";
        }else if("7".equals(mWay)){
            return "SATURDAY";
        }
        return mWay;
    }

    private int getImage(String type) {
        int imagetype=0;
        switch(type) {
            case "阴" : imagetype=R.drawable.windy_small;
                break;
            case "晴" : imagetype=R.drawable.sunny_small;
                break;
            case "暴雨" : imagetype=R.drawable.rainy_up;
                break;
            case "大雨" : imagetype=R.drawable.rainy_small;
                break;
            case "多云" : imagetype=R.drawable.partly_sunny_small;
                break;
            default:
                imagetype=R.drawable.windy_small;
        }
        return imagetype;
    }

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?city=重庆";

        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather=null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather=parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());

                        Message msg =new Message();
                        msg.what = 1;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (xmlPullParser.getName().equals("city")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "city: "+xmlPullParser.getText());
                            todayWeather.setCity(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("wendu")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "wendu: "+xmlPullParser.getText());
                            todayWeather.setTemperature(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "type: "+xmlPullParser.getText());
                            todayWeather.setType(xmlPullParser.getText());
                            typeCount++;
                        }

                        break;


                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    void updateTodayWeather(TodayWeather todayWeather){
        cityTv.setText(todayWeather.getCity());
        temperatureTv.setText(todayWeather.getTemperature());
        weatherImg.setImageResource(getImage(todayWeather.getType()));

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

    }

    public void btnClick(View view) {
        //((TextView)findViewById(R.id.temperature_of_the_day)).setText("28");
        new DownloadUpdate().execute();
        ((TextView)findViewById(R.id.tv_date)).setText(getTime());
        ((TextView)findViewById(R.id.weekView)).setText(getWeek());
    }

    public void btnRefresh(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code", "101010100");
        Log.d("myWeather", cityCode);


        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "Internet is OK");
            queryWeatherCode(cityCode);

        } else {
            Log.d("myWeather", "Internet is off");
            Toast.makeText(MainActivity.this, "Internet is off！", Toast.LENGTH_LONG).show();
        }
        //new DownloadUpdate().execute();
        ((TextView)findViewById(R.id.tv_date)).setText(getTime());
        ((TextView)findViewById(R.id.weekView)).setText(getWeek());
    }

    private class DownloadUpdate extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = "http://mpianatra.com/Courses/info.txt";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            try {
                URL url = new URL(stringUrl);

                // Create the request to get the information from the server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Mainly needed for debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                //The temperature
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(String temperature) {
            //Update the temperature displayed
            ((TextView) findViewById(R.id.temperature_of_the_day)).setText(temperature);
        }
    }
}
