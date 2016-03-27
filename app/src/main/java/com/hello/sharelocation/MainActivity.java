package com.hello.sharelocation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * 使用baidumapapi_v3_5_0.jar
 */
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;

/**
 * 使用locSDK_6.23.jar
 */
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * 6E:0B:D4:56:F4:B8:AB:64:8A:26:E9:A2:96:62:B8:8A:05:99:E4:E0
 * <p/>
 * AK: OLb92NLhOyzMTtpmZ05UxqBNMV68WTtC
 */


public class MainActivity extends Activity {
    public MapView mapView = null;
    public BaiduMap baiduMap = null;
    public BaiduMapOptions baiduMapOptions;

    // 定位相关声明
    public LocationClient locationClient = null;

    //自定义图标
    BitmapDescriptor mCurrentMarker = null;
    boolean isFirstLoc = true;// 是否首次定位



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        // 获取地图控件引用
        mapView = (MapView) this.findViewById(R.id.bmapView);

        //获取地图控制器
        baiduMap = mapView.getMap();

        //??? 不生效
        //设置是否显示缩放控件
//        baiduMapOptions = new BaiduMapOptions();
//        baiduMapOptions.zoomControlsEnabled(false);

        //设置地图类型 MAP_TYPE_NORMAL 普通图； MAP_TYPE_SATELLITE 卫星图；MAP_TYPE_NONE 卫星图
        //MAP_TYPE_NORMAL = 1, MAP_TYPE_SATELLITE = 2
        baiduMap.setMapType(baiduMap.MAP_TYPE_NORMAL);
        System.out.println("地图类型::" + baiduMap.getMapType());

        //开启定位图层
        baiduMap.setMyLocationEnabled(true);


        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        locationClient = new LocationClient(getApplicationContext());
        System.out.println("定位版本：：" + locationClient.getVersion());

        //注册定位监听函数,arg: BDLocationListener listener
        locationClient.registerLocationListener(myListener);

        //封装定位参数
        setLocationOption();

        //启动定位sdk
        locationClient.start();


    }


    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {

            System.out.println("城市:" + location.getCity());

            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null)
                return;

            /**
             * MyLocationData.Builder() 定位数据建造器
             * MyLocationData  定位数据
             */
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()) //设置定位数据的精度信息，单位：米
                    .direction(100) //设置定位数据的方向信息
                    .latitude(location.getLatitude()) //设置定位数据的纬度
                    .longitude(location.getLongitude())//设置定位数据的经度
                    .build(); //构建生成定位数据对象 return new MyLocationData

            baiduMap.setMyLocationData(locData);	//设置定位数据, 只有先允许定位图层后设置数据才会生效


            if (isFirstLoc) {
                isFirstLoc = false;

                //地理坐标基本数据结构(维度，经度)
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                /**
                 * 设置地图中心点
                 * newLatLngZoom 设置地图中心点以及缩放级别
                 * latLng :地图中心点，不能为 null
                 * 18 : 缩放级别 [3, 21]
                 */
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latLng, 18);

                //以动画方式更新地图状态
                baiduMap.animateMapStatus(u);
            }
        }
    };

    /**
     * 设置定位参数
     */
    private void setLocationOption() {
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption option = new LocationClientOption();

        //是否打开gps进行定位
        option.setOpenGps(true);

        //设置定位模式( 高精度模式)
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //设置坐标类型,
        //取值有3个： 返回国测局经纬度坐标系：gcj02 返回百度墨卡托坐标系 ：bd09 返回百度经纬度坐标系 ：bd09ll
        option.setCoorType("bd09ll");

        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        option.setScanSpan(2000);

        //设置是否需要地址信息，默认为无地址
        option.setIsNeedAddress(true);

        //在网络定位时，是否需要设备方向 true:需要 ; false:不需要。默认为false
        option.setNeedDeviceDirect(true);

        //设置 LocationClientOption
        locationClient.setLocOption(option);
    }

    // 三个状态实现地图生命周期管理
    @Override
    protected void onDestroy() {
        //退出时销毁定位
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        super.onDestroy();
        mapView.onDestroy();
        mapView = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }



}

