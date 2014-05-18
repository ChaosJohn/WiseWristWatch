package com.example.wise_wrist_watch;

import java.io.ByteArrayInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.xzw.utils.BluetoothChatService;
import com.xzw.utils.DatabaseHelper;
import com.xzw.utils.Tools;

public class Goal_Activity extends Activity {

	// database relevantly
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase db;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static boolean CONNECTED = false;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	private int isFirst = 1;
	private int firstStep;
	private int lastStep;
	private TextView minsTextView = null;
	private TextView stepsTextView = null;

	// layout views
	private LinearLayout runtimeLayout;
	private LinearLayout runspeedLayout;
	private LinearLayout runintervalLayout;
	private TextView startFitTextView;
	private LinearLayout headLayout;
	private long startTime = 0;
	private long curTime = 0;
	private long durTime = 0;
	private long seconds = 0;
	private long minutes = 0;
	private long hours = 0;
	private double totalHours = 0;
	private double totalSteps = 0;
	private double totalSeconds = 0;
	private Handler handler = null;
	private Runnable runnable = null;

	// bluetooth relevantly
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	private final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share", RequestType.SOCIAL);

	private int height = 0;
	private int weight = 0;
	private double stepSize = 0;
	private double speed = 0;
	private double calorie = 0;
	private double distance = 0;
	private double rate = 0;
	private String shareString = null;
	private String popUpString = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goal);

		// Initialize the layout views
		runtimeLayout = (LinearLayout) findViewById(R.id.run_time_layout);
		runspeedLayout = (LinearLayout) findViewById(R.id.run_speed_layout);
		runintervalLayout = (LinearLayout) findViewById(R.id.run_interval_layout);
		runtimeLayout.setOnClickListener(runLayoutListener);
		runspeedLayout.setOnClickListener(runLayoutListener);
		runintervalLayout.setOnClickListener(runLayoutListener);
		startFitTextView = (TextView) findViewById(R.id.start_fit);
		headLayout = (LinearLayout) findViewById(R.id.head_layout);
		stepsTextView = (TextView) findViewById(R.id.steps);
		minsTextView = (TextView) findViewById(R.id.mins);

		// add a listener to several widgets
		startFitTextView.setOnClickListener(startListener);

		headLayout.setOnClickListener(headlayoutListener);
	}

	protected void onResume() {
		super.onResume();
		// InitializeLayout the textviews of the layout with data from database
		initializeLayout();
		// Get local Bluetooth adapter
		Log.e("state", "--- ON ONRESUME ---");
	}

	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (mBluetoothAdapter != null)
			mBluetoothAdapter.disable();
		Log.e("state", "--- ON DESTROY ---");
	}

	/**
	 * 
	 * @Title: initializeLayout
	 * @Description: Do some initial work, including filling in the textviews
	 *               with the data from database
	 * @param
	 * @return void
	 */
	private void initializeLayout() {
		ImageView faceImageView = (ImageView) findViewById(R.id.face2);
		databaseHelper = new DatabaseHelper(getApplicationContext(), "info.db",
				null, 1);
		db = databaseHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from infomation", null);
		TextView nameTextView = (TextView) findViewById(R.id.myname);
		TextView runtimeTextView = (TextView) findViewById(R.id.runtime_tv);
		TextView runspeedTextView = (TextView) findViewById(R.id.runspeed_tv);
		TextView runintervalTextView = (TextView) findViewById(R.id.runinterval_tv);
		if (cursor.moveToFirst()) {
			ByteArrayInputStream stream = new ByteArrayInputStream(
					cursor.getBlob(cursor.getColumnIndex("face")));
			faceImageView.setImageDrawable(Drawable.createFromStream(stream,
					"face2"));
			String nicknameString = cursor.getString(cursor
					.getColumnIndex("nickname"));
			String heightString = cursor.getString(cursor
					.getColumnIndex("height"));
			String weightString = cursor.getString(cursor
					.getColumnIndex("weight"));

			height = Integer.parseInt(heightString);
			weight = Integer.parseInt(weightString);
			// Toast.makeText(getApplicationContext(), " " + height,
			// Toast.LENGTH_LONG).show();
			nameTextView.setText(nicknameString);
		}
		cursor = db.rawQuery("select * from runparameter", null);
		if (cursor.moveToFirst()) {
			runtimeTextView.setText(cursor.getString(cursor
					.getColumnIndex("runtime")));
			runspeedTextView.setText(cursor.getString(cursor
					.getColumnIndex("runspeed")));
			runintervalTextView.setText(cursor.getString(cursor
					.getColumnIndex("runinterval")));
		}
		if (cursor != null) {
			cursor.close();
		}
		if (db != null) {
			db.close();
		}

	}

	private View.OnClickListener stopListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mChatService.stop();
			// mBluetoothAdapter.
			handler.removeCallbacks(runnable);
			// isFirst = 0;
			// startFitTextView.setOnClickListener(startListener);
			// startFitTextView.setText("开始健身");
			// startActivity(new Intent(getApplicationContext(),
			// Goal_Activity.class));
			// Goal_Activity.this.onDestroy();
			// finish();
			// ///////////
			// startFitTextView.setText("退出");
			// startFitTextView.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// // TODO Auto-generated method stub
			// finish();
			// }
			// });
			rate = totalSteps * 2 / totalSeconds;
			stepSize = Tools.getStepSizeByHeightAndRate(height, rate);
			speed = Tools.getSpeedByStepSizeAndRate(stepSize, rate);
			distance = totalSeconds * speed / 60; // 米
			calorie = Tools.getCalorieBySpeedAndWeightWithTime(rate, weight,
					totalHours);
			popUpString = "您运动了: \t" + hours + ":" + minutes + ":" + seconds
					+ "\n您的速度为: \t" + (int) speed + "米/分钟\t\n" + "您跑了: \t"
					+ (int) totalSteps + "\t步\n合计\t" + (int) distance
					+ "\t米\t\n"/* + "您消耗的卡路里为: \t" + calorie + "\t" */;
			shareString = "今天跑了" + (int) totalSteps + "步， 合计" + (int) distance
					+ "米, 速度是" + (int) speed + "米/分钟"/*
													 * + ", 消耗了" + calorie +
													 * "卡路里"
													 */;

			DialogInterface.OnClickListener shareClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					startFitTextView.setText("退出");
					startFitTextView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					mController.setShareContent(shareString);
					// mController.getConfig().removePlatform(SHARE_MEDIA.DOUBAN);
					mController.openShare(Goal_Activity.this, false);
				}
			};
			DialogInterface.OnClickListener exitClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			};
			new AlertDialog.Builder(Goal_Activity.this,
					AlertDialog.THEME_HOLO_LIGHT).setTitle("分享到社区")
					.setMessage(popUpString)
					.setPositiveButton("分享", shareClickListener)
					.setNegativeButton("退出", exitClickListener).show();
		}
	};

	private View.OnClickListener startListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// if not connected to the remote,start the connection ,else , open
			// the fit activity directly
			if (CONNECTED == false) {// get the adapter when it's null
				if (mBluetoothAdapter == null) {
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				}

				// If the adapter is null, then Bluetooth is not supported
				if (mBluetoothAdapter == null) {
					Toast.makeText(Goal_Activity.this,
							"Bluetooth is not available", Toast.LENGTH_LONG)
							.show();
					finish();
					return;
				}
				// If BT is not on, request that it be enabled.
				// service will be created then during onActivityResult
				if (!mBluetoothAdapter.isEnabled()) {
					Intent enableIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
					// Otherwise, new the service
				} else {
					mChatService = new BluetoothChatService(Goal_Activity.this,
							mHandler);
					openList();
				}
				if (mChatService != null) {
					// Only if the state is STATE_NONE, do we know that we
					// haven't started already
					if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
						// Start the Bluetooth chat services
						mChatService.start();
					}
				}
			} else {
				// Intent intent = new Intent();
				// intent.setClass(Gole_Activity.this, Fit_Activity.class);
				// startActivity(intent);
			}

		}
	};

	private void openList() {

		Intent serverIntent = null;
		serverIntent = new Intent(Goal_Activity.this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
	}

	private View.OnClickListener headlayoutListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(Goal_Activity.this, Info_Change_Activity.class);
			startActivity(intent);
		}
	};

	private View.OnClickListener runLayoutListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.run_time_layout: {
				Intent intent = new Intent();
				intent.putExtra("runtype", 1);// 1表示设置跑步时间
				intent.setClass(Goal_Activity.this, SetGoal_Activity.class);
				startActivity(intent);
				break;
			}
			case R.id.run_speed_layout: {
				Intent intent = new Intent();
				intent.putExtra("runtype", 2);// 2表示设置跑步速度
				intent.setClass(Goal_Activity.this, SetGoal_Activity.class);
				startActivity(intent);
				break;
			}
			case R.id.run_interval_layout: {
				Intent intent = new Intent();
				intent.putExtra("runtype", 3);// 3表示设置跑步间隔
				intent.setClass(Goal_Activity.this, SetGoal_Activity.class);
				startActivity(intent);
				break;
			}
			default:
				break;
			}
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					Toast.makeText(Goal_Activity.this, "连接成功",
							Toast.LENGTH_LONG);
					// Intent intent = new Intent();
					// intent.setClass(Gole_Activity.this, Fit_Activity.class);
					// startActivity(intent);
					CONNECTED = true;
					break;
				case BluetoothChatService.STATE_CONNECTING:
					// new AlertDialog.Builder(Gole_Activity.this)
					// .setTitle("连接中...").show();
					Toast.makeText(Goal_Activity.this, "连接中", Toast.LENGTH_LONG);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					Toast.makeText(Goal_Activity.this, "连接已断开",
							Toast.LENGTH_LONG);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// String
				// String readMessage = new String(readBuf, 0, 4);
				int num = getNumFromBuffer(readMessage);
				if (-1 != num && num < 10000) {
					Log.e("print", Integer.toString(num));
					if (1 == isFirst) {
						firstStep = num;
						startTime = System.currentTimeMillis();
						isFirst = 0;
						startFitTextView.setOnClickListener(stopListener);
						startFitTextView.setText("暂停");
						handler = new Handler();
						runnable = new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								// 要做的事情
								curTime = System.currentTimeMillis();
								durTime = (curTime - startTime) / 1000;
								totalSeconds = durTime;
								hours = durTime / 3600;
								totalHours = durTime / 3600;
								minutes = (durTime - hours * 3600) / 60;
								seconds = (durTime - hours * 3600 - minutes * 60);
								minsTextView.setText("已持续\t" + hours + ":"
										+ minutes + ":" + seconds);
								handler.postDelayed(this, 1000);
							}
						};
						handler.postDelayed(runnable, 1000);

					} else {
						lastStep = num;
						if (lastStep - firstStep < 0) {
							firstStep = lastStep;
							startTime = System.currentTimeMillis();
						}
						totalSteps = lastStep - firstStep;
						stepsTextView.setText("已跑\t" + (lastStep - firstStep)
								+ "\t步");
					}
					// curTime = System.currentTimeMillis();
					// durTime = curTime - startTime;
					// minsTextView.setText(String.valueOf(durTime / 1000));
				}
				// Log.e("print", readMessage);
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(Goal_Activity.this,
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(Goal_Activity.this,
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so new the service
				mChatService = new BluetoothChatService(Goal_Activity.this,
						mHandler);
				// startTime = System.currentTimeMillis();
				openList();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(Goal_Activity.this,
						R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
	}

	public int getNumFromBuffer(String string) {
		char[] chs = string.toCharArray();
		int len = string.length();
		int lastright = string.indexOf('>', len - 6);
		if (lastright >= 4) {
			int d4 = chs[lastright - 1] - '0';
			int d3 = chs[lastright - 2] - '0';
			int d2 = chs[lastright - 3] - '0';
			int d1 = chs[lastright - 4] - '0';
			return d1 * 1000 + d2 * 100 + d3 * 10 + d4;
		} else {
			return -1;
		}
	}
}
