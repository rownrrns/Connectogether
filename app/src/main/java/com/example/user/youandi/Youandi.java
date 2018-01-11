package com.example.user.youandi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static android.content.Context.SENSOR_SERVICE;

public class Youandi extends Fragment {
    FloatingActionButton loginBtn;
    FloatingActionButton logoutBtn;
    TextView distanceview;
    ImageView goal;
    int room_num;
    public Youandi() {
        // Required empty public constructor
    }
    private boolean mLocationPermissionGranted;
    private double longitude;
    private double latitude;
    private JSONObject roomtest = new JSONObject();
    private JSONObject CounterData;
    private JSONObject location_data;
    private double distance;
    private double directions;
    private com.github.nkzawa.socketio.client.Socket mSocket;

    SensorManager sensorManager;
    rotationlistner mySensorListener;
    private UICompassView mCompassView;
    private boolean mCompassEnabled;
    private RelativeLayout youandi_relativelayout;
    PlaceAutocompleteFragment autocompleteFragment;


    {
        try {
            mSocket = IO.socket("http://13.124.188.29:3000");
        } catch(URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_youandi, container, false);

        loginBtn=(FloatingActionButton) view.findViewById(R.id.youandi_login);
        logoutBtn=(FloatingActionButton) view.findViewById(R.id.youandi_setting);
        View.OnClickListener btnlistener = new View.OnClickListener() {
            @Override
            public void onClick(View view){
                switch (view.getId()) {
                    case R.id.youandi_login:
                        login_show();
                        break;
                    case R.id.youandi_setting:
                        logout_show();
                        break;
                }
            }
        };
        loginBtn.setOnClickListener(btnlistener);
        logoutBtn.setOnClickListener(btnlistener);
        mSocket.on("newlocdata", getcounterlocations);

        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.


        youandi_relativelayout = (RelativeLayout) view.findViewById(R.id.youandi_relative);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        boolean sideBottom = true;
        mCompassView = new UICompassView(getActivity());
        mCompassView.setVisibility(View.VISIBLE);
        mySensorListener = new rotationlistner();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(sideBottom ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
        youandi_relativelayout.addView(mCompassView, params);
        mCompassEnabled = true;

        return view;
    }// end of onCreate

    private Emitter.Listener getcounterlocations = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 전달받은 데이터는 아래와 같이 추출할 수 있습니다.
            JSONObject receivedata = (JSONObject) args[0];
            CounterData = new JSONObject();
            try{
                CounterData.put("latitude", receivedata.getString("latitude"));
                CounterData.put("longitude", receivedata.getString("longitude"));
                distance = PersonDistance(location_data.getString("latitude"), location_data.getString("longitude"),
                        CounterData.getString("latitude"), CounterData.getString("longitude"));
                directions = PersonDirection(location_data.getString("latitude"), location_data.getString("longitude"),
                        CounterData.getString("latitude"), CounterData.getString("longitude"));
            }catch (JSONException e){
                e.printStackTrace();
            }
            Log.d("get counter locations", CounterData.toString());
            Log.d("Dis", Double.toString(distance));
            Log.d("Direc", Double.toString(directions));
        }
    };


    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            location_data = new JSONObject();
            Log.d("test", "onLocationChanged, location:" + location);
            longitude = location.getLongitude(); //경도
            latitude = location.getLatitude();   //위도
            if(CounterData == null){
                CounterData = new JSONObject();
                try {
                    CounterData.put("latitude", latitude);
                    CounterData.put("longitude", longitude);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            try {
                location_data.put("latitude", String.valueOf(location.getLatitude()));
                location_data.put("longitude", String.valueOf(location.getLongitude()));
                mSocket.emit("Locatedata", location_data);
                distance = PersonDistance( String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), CounterData.getString("latitude"), CounterData.getString("longitude"));
                directions = PersonDirection(location_data.getString("latitude"), location_data.getString("longitude"), CounterData.getString("latitude"), CounterData.getString("longitude"));
                distanceview = (TextView) getActivity().findViewById(R.id.youandi_distance);
                distanceview.setText(String.valueOf((int)distance) + "m");
                goal = (ImageView) getActivity().findViewById(R.id.youandi_arrow);
                if(distance < 5 && distance >0){
                    goal.setVisibility(View.VISIBLE);
                }else{
                    goal.setVisibility(View.GONE);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

            Log.d("Dis", Double.toString(distance));
            Log.d("Direc", Double.toString(directions));
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 8080: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        //call function
    }

    public static double PersonDistance(String Lat1, String Lon1, String Lat2, String Lon2) {
        double lat1 = Double.parseDouble(Lat1);
        double lon1 = Double.parseDouble(Lon1);
        double lat2 = Double.parseDouble(Lat2);
        double lon2 = Double.parseDouble(Lon2);

        return Math.acos(Math.cos(Math.toRadians(90-lat1))*Math.cos(Math.toRadians(90-lat2))
                +Math.sin(Math.toRadians(90-lat1))*Math.sin(Math.toRadians(90-lat2))*Math.cos(Math.toRadians(lon1-lon2)))*6358030.94791;
    }

    public static double PersonDirection(String Lat1, String Lon1, String Lat2, String Lon2) {
        double lat1 = Math.toRadians(Double.parseDouble(Lat1));
        double lon1 = Math.toRadians(Double.parseDouble(Lon1));
        double lat2 = Math.toRadians(Double.parseDouble(Lat2));
        double lon2 = Math.toRadians(Double.parseDouble(Lon2));
        double lonL = lon2 - lon1;
        double radbearing;
        if(lat1 == lat2 && lon1 == lon2){
            return 0.0;
        }
        if(Math.sin(lonL) > 0) {
            radbearing = Math.atan(Math.sin(lonL) / (Math.cos(lat1) * Math.tan(lat2) - Math.sin(lat1) * Math.cos(lonL)));
        }else{
            radbearing = Math.atan(Math.sin(360-lonL) / (Math.cos(lat2) * Math.tan(lat1) - Math.sin(lat2) * Math.cos(360-lonL)));
        }

        double true_bearing = 0.0;
        if (Math.sin(lon2 - lon1) < 0){
            true_bearing = 360 - Math.toDegrees(radbearing);

        } else {
            true_bearing = Math.toDegrees(radbearing);
        }

        return true_bearing;
    }

    void logout_show() {
        final EditText edittext = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final TextView room_num_text = (TextView) getActivity().findViewById(R.id.youandi_room_num);
        builder.setTitle("Logout");
        builder.setMessage("방에서 나가시겠습니까?.");
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSocket.emit("leaveRoom", roomtest);
                        roomtest.remove("room");
                        room_num_text.setText("");
                        Toast.makeText(getActivity(),"다음에 또 봐요!" ,Toast.LENGTH_LONG).show();
                    }
                });
        builder.setPositiveButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"취소되었습니다." ,Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

    void login_show() {
        final EditText edittext = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final TextView room_num_text = (TextView) getActivity().findViewById(R.id.youandi_room_num);
        builder.setTitle("Login");
        builder.setMessage("방번호를 입력하세요.");
        builder.setView(edittext);
        builder.setNegativeButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSocket.connect();
                        try {
                            roomtest = new JSONObject();
                            roomtest.put("room", edittext.getText().toString());
                            Log.d("Test", roomtest.toString());
                            mSocket.emit("joinRoom", roomtest);
                            Log.d("Test22", roomtest.toString());
                            room_num_text.setText(roomtest.getString("room"));
                            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mLocationPermissionGranted = true;
                            } else {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 8080);
                            }
                            // LocationManager 객체를 얻어온다
                            final LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                            try{
                                // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                        100, // 통지사이의 최소 시간간격 (miliSecond)
                                        1, // 통지사이의 최소 변경거리 (m)
                                        mLocationListener);
                                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                        100, // 통지사이의 최소 시간간격 (miliSecond)
                                        1, // 통지사이의 최소 변경거리 (m)
                                        mLocationListener);
                            } catch(SecurityException ex){
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        builder.setPositiveButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"취소되었습니다." ,Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCompassEnabled) {
            sensorManager.registerListener(mySensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCompassEnabled) {
            sensorManager.unregisterListener(mySensorListener);
        }
    }
    class rotationlistner implements SensorEventListener {
        private int iOrientation = -1;

        @SuppressLint("WrongConstant")
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Log.d("MySensorListener", "sensor #0 " + sensorEvent.values[0]);
            if (iOrientation < 0) {
                iOrientation = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            }
            Log.d("Orientation", String.valueOf(iOrientation));
            mCompassView.setAzimuth(-sensorEvent.values[0]+(float) directions);
            mCompassView.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}