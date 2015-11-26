package com.motus.motusupper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private boolean doWrite = false;
	String Str1 = "";
	private TextView t1 = null;
	private TextView t2 = null;
	private TextView t3 = null;
	private MotusUdp mmotusUdp = null;
	private KalmanFilter KalmanX = null;
	private KalmanFilter KalmanY = null;
	private final int TIMER = 10;// //////////////////////////////////////////////////////////////////////////////////
	private Button upButton = null;
	private Button middleButton = null;
	private Button downButton = null;
	private Button startButton = null;
	private Button workButton = null;
	private Button start1Button = null;
	private Button stopButton = null;
	private Button playButton = null;
	private EditText editstatusEditText = null;
	private EditText TT = null;
	private OnClickListener listen = null;
	private byte ncmd = 0;
	private int mcount = 0;
	private int statusCmd = 0;
	private float atitude[] = null;
	private float atitude1[] = null;
	private float atitude2[] = null;
	private float v0[] = null;
	private float vt[] = null;
	private float SData[] = null;
	private float roll = 0;
	private float pitch = 0;
	private long LastTime = 0;
	private float KalAngleX = 0;
	private float KalAngleY = 0;
	private float GyroRateX = 0;
	private float GyroRateY = 0;
	private float RadToDeg = 57.2958f;
	private boolean IsStart = true;
	DatagramSocket udpSocket = null;
	DatagramPacket dataPacket = null;
	private static final int UPTATE_INTERVAL_TIME = 2;// ///////////////////////////////////////////////////////
	private SensorManager sensorManager = null;
	private SensorDemo SensorDemouser = null;
	private SensorDemo SensorDemouser1 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		atitude = new float[3];
		atitude1 = new float[3];
		atitude2 = new float[3];
		SData = new float[3];
		v0 = new float[3];
		vt = new float[3];
		atitude2[0] = 0;
		atitude2[1] = 0;
		atitude2[2] = 0;
		v0[0] = 0;
		v0[1] = 0;
		v0[2] = 0;
		vt[0] = 0;
		vt[1] = 0;
		vt[2] = 0;
		editstatusEditText = (EditText) findViewById(R.id.editStatus);// 绑定id
		upButton = (Button) findViewById(R.id.up);// 绑定id
		middleButton = (Button) findViewById(R.id.middle);// 绑定id
		downButton = (Button) findViewById(R.id.down);// 绑定id
		startButton = (Button) findViewById(R.id.start);// 绑定id
		workButton = (Button) findViewById(R.id.work);// 绑定id
		start1Button = (Button) findViewById(R.id.bStart);
		stopButton = (Button) findViewById(R.id.bStop);
		playButton = (Button) findViewById(R.id.bPlay);
		TT = (EditText) findViewById(R.id.TT);
		t1 = (TextView) findViewById(R.id.attu1);
		t1.setText("x:" + 0.0 + "\ny:" + 0.0);
		t2 = (TextView) findViewById(R.id.attu2);
		t2.setText("x:" + 0.0 + "\ny:" + 0.0);
		t3 = (TextView) findViewById(R.id.attu3);
		t3.setText("x:" + 0.0 + "\ny:" + 0.0);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		SensorDemouser = new SensorDemo(
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				atitude, t1);
		SensorDemouser1 = new SensorDemo(
				sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				atitude1, t2);
	
		listen = new OnClickListener()
		{
			public void onClick(View arg0)
			{
				Button btn = (Button) arg0;// 转换成Button
				switch (btn.getId())
				{// 得到id
				case R.id.up:
				{
					statusCmd = 1;
					break;
				}
				case R.id.middle:
				{
					statusCmd = 2;
					break;
				}
				case R.id.down:
				{
					statusCmd = 3;
					break;
				}
				case R.id.start:
				{
					statusCmd = 4;
					/*
					roll = (float) (Math.atan2(atitude[1], atitude[2]) * RadToDeg);
					pitch = (float) (Math.atan(-atitude[0]/ Math.sqrt(atitude[1] * atitude[1] + atitude[2]* atitude[2]))* RadToDeg);
					
					
					KalmanX.angle = roll;
					KalmanY.angle = pitch;
					KalAngleX = roll;
					KalAngleY = pitch;
					LastTime = System.currentTimeMillis();
					*/
					break;
				}
				case R.id.work:
				{
					statusCmd = 5;
					break;
				}
				case R.id.bStart:
				{
					doWrite = true;
					Str1 = "";
					break;
				}
				case R.id.bStop:
				{
					doWrite = false;
					break;
				}
				case R.id.bPlay:
				{
					TT.setText(Str1);
					break;
				}
				default:
					break;
				}
			}
		};

		upButton.setOnClickListener(listen);// 绑定点击事件
		middleButton.setOnClickListener(listen);// 绑定点击事件
		downButton.setOnClickListener(listen);// 绑定点击事件
		startButton.setOnClickListener(listen);// 绑定点击事件
		workButton.setOnClickListener(listen);// 绑定点击事件
		start1Button.setOnClickListener(listen);// 绑定点击事件
		stopButton.setOnClickListener(listen);// 绑定点击事件
		playButton.setOnClickListener(listen);// 绑定点击事件
		timer.schedule(task, TIMER, TIMER); // 1s后执行task,经过1s再次执行
		mmotusUdp = new MotusUdp();
		mmotusUdp.init();
		KalmanY = new KalmanFilter();
		KalmanY.init();
		KalmanX = new KalmanFilter();
		KalmanX.init();
		
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		SensorDemouser.register();
		SensorDemouser1.register();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		SensorDemouser.unregister();
		SensorDemouser1.unregister();
	}

	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			DecimalFormat df = new DecimalFormat("##.####");

			long CurrentTime = System.currentTimeMillis();
			float dt = 0;
			if(IsStart)
			{
				if(atitude[1] == 0 && atitude[1] == 0 && atitude[1] == 0)//有时候读到加速度传感器的值均为0，则 pitch计算得到NaN
				{
					roll = 0;
					pitch = 0;
				}
				else
				{
					roll = (float) (Math.atan2(atitude[1], atitude[2]) * RadToDeg);
					pitch = (float) (Math.atan(-atitude[0] / Math.sqrt(atitude[1] * atitude[1] + atitude[2] * atitude[2]))* RadToDeg);
				}
				KalmanX.angle = roll;
				KalmanY.angle = pitch;
				KalAngleX = roll;
				KalAngleY = pitch;
				LastTime = System.currentTimeMillis();
				IsStart = false;
			}
			if (CurrentTime == LastTime)
			{
				return;
			}
			if (CurrentTime > LastTime)
			{
				dt = (float)(CurrentTime - LastTime) / 1000000;
			} 
			if(atitude[1] == 0 && atitude[1] == 0 && atitude[1] == 0)
			{
				roll = 0;
				pitch = 0;
			}
			else
			{
				roll = (float) (Math.atan2(atitude[1], atitude[2]) * RadToDeg);
				pitch = (float) (Math.atan(-atitude[0] / Math.sqrt(atitude[1] * atitude[1] + atitude[2] * atitude[2]))* RadToDeg);
			}
			GyroRateX = atitude1[0] * RadToDeg;// deg/s
			GyroRateY = atitude1[1] * RadToDeg;// deg/s
			
			if ((roll < -90 && KalAngleX > 90)|| (roll > 90 && KalAngleX < -90))
			{
				KalmanX.angle = roll;
				KalAngleX = roll;

			} 
			else
			{
				KalAngleX = KalmanX.GetAngle(roll, GyroRateX, dt); 
				//KalAngleY = KalmanY.GetAngle(pitch, GyroRateY, dt);
			}
			
			
			if (Math.abs(KalAngleX) > 90)
			{
				GyroRateY = -GyroRateY; // Invert rate, so it fits the restriced
				// accelerometer reading
			}
			
			KalAngleY = KalmanY.GetAngle(pitch, GyroRateY, dt);
			t1.setText("roll:     " + roll + "\npitch:    " + pitch + "\nKalAngleX:"+ KalAngleX + "\nKalAngleY:" + KalAngleY);

			if (msg.what == 1)
			{


				if (doWrite)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("SSS");
					Str1 += sdf.format(new Date()) + " ";
					//Str1 += df.format(roll) + " " + df.format(pitch) + " " + df.format(atitude1[0]) + " " + df.format(atitude1[1]) + " " + df.format(KalAngleX) + " " + df.format(KalAngleY) +" " + df.format(dt) +"\n";
					Str1 += df.format(roll) + " " + df.format(pitch) + " " + df.format(KalAngleX) + " " + df.format(KalAngleY) +"\n";
				}
				if (mcount % 200 == 0)
				{
					editstatusEditText.setText(Integer.toString(mmotusUdp.getStatus()));
					mcount = 0;
				}
				mcount++;
				switch (statusCmd)
				{// 得到id
				case 1:
				{
					platfromUp();
					break;
				}
				case 2:
				{
					platfromMiddle();
					break;
				}
				case 3:
				{
					platfromDown();
					break;
				}
				case 4:
				{
					torun();

					break;
				}
				case 5:
				{
					towork();
					break;
				}
				default:
					break;
				}
			}
			super.handleMessage(msg);
		};
	};

	void platfromUp()
	{
		ncmd = 6;
		mmotusUdp.sendData(ncmd, atitude, atitude1);
	}

	void torun()
	{
		ncmd = 0;

		SData[0] = (float) KalAngleX;
		SData[1] = (float) KalAngleY;
		SData[2] = 0;

		mmotusUdp.sendData(ncmd, SData, atitude1);
	}

	void towork()
	{
		ncmd = 4;
		mmotusUdp.sendData(ncmd, atitude, atitude1);
	}

	void platfromMiddle()
	{
		ncmd = 2;
		mmotusUdp.sendData(ncmd, atitude, atitude1);
	}

	void platfromDown()
	{
		ncmd = 7;
		mmotusUdp.sendData(ncmd, atitude, atitude1);
	}

	Timer timer = new Timer();
	TimerTask task = new TimerTask()
	{
		@Override
		public void run()
		{
			// 需要做的事:发送消息
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	};

	@Override
	protected void onStop()
	{
		super.onStop();
		mmotusUdp.closeSocket();
	}

	class SensorDemo implements SensorEventListener
	{

		Sensor sensoruser;
		TextView t;
		float atitu[];
		private long lastUpdateTime;

		public SensorDemo(Sensor sensor, float tempatitu[], TextView t)
		{
			this.sensoruser = sensor;
			this.t = t;
			this.atitu = tempatitu;

		}

		public void register()
		{
			sensorManager.registerListener(this, sensoruser,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		public void unregister()
		{
			sensorManager.unregisterListener(this);
		}

		public void onAccuracyChanged(Sensor arg0, int arg1)
		{
			// TODO Auto-generated method stub

		}

		public void onSensorChanged(SensorEvent arg0)
		{
			// TODO Auto-generated method stub
			// 鐜板湪妫�娴嬫椂闂�
			long currentUpdateTime = System.currentTimeMillis();
			// 涓ゆ妫�娴嬬殑鏃堕棿闂撮殧
			long timeInterval = currentUpdateTime - lastUpdateTime;
			// 鍒ゆ柇鏄惁杈惧埌浜嗘娴嬫椂闂撮棿闅�
			if (timeInterval < UPTATE_INTERVAL_TIME)
				return;
			// 鐜板湪鐨勬椂闂村彉鎴恖ast鏃堕棿
			lastUpdateTime = currentUpdateTime;
			atitu[0] = arg0.values[0];
			atitu[1] = arg0.values[1];
			atitu[2] = arg0.values[2];
			// t.setText("x:" + atitu[0] + "\ny:" + atitu[1] + "\nz:" + atitu[2]
			// + "\n");

		}

	}

}
