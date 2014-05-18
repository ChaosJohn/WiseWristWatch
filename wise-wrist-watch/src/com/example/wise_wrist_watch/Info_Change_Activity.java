package com.example.wise_wrist_watch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xzw.utils.DatabaseHelper;
import com.xzw.utils.Tools;

public class Info_Change_Activity extends Activity {
	/* 组件 */
	private LinearLayout switchAvatar;
	private ImageView faceImage;
	private TextView save;

	private String[] items = new String[] { "选择本地图片", "拍照" };
	/* 头像名称 */
	private static final String IMAGE_FILE_NAME = "faceImage.jpg";

	/* 请求码 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;

	private SQLiteDatabase db;
	private DatabaseHelper databaseHelper;

	private String nameString = "";
	private String sexString = "";
	private String ageString = "";
	private String heightString = "";
	private String weightString = "";
	private String nicknameString = "";
	private String sportsString = "";
	private String runtimeString = "30";
	private String runspeedString = "3";
	private String runintervalString = "24";
	ByteArrayInputStream stream = null;

	private Bitmap faceBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_register);
		switchAvatar = (LinearLayout) findViewById(R.id.switch_face_rl);
		faceImage = (ImageView) findViewById(R.id.face);
		BitmapDrawable bd = (BitmapDrawable) faceImage.getDrawable();
		faceBitmap = bd.getBitmap();
		// 设置事件监听
		switchAvatar.setOnClickListener(listener);
		save = (TextView) findViewById(R.id.save);
		save.setOnClickListener(saveListener);
		databaseHelper = new DatabaseHelper(getApplicationContext(), "info.db",
				null, 1);
		db = databaseHelper.getWritableDatabase();

		showInitialInfo();

	}

	private void showInitialInfo() {

		Cursor cursor = db.rawQuery("select * from infomation", null);
		if (cursor.moveToFirst()) {
			nameString = cursor.getString(cursor.getColumnIndex("name"));
			sexString = cursor.getString(cursor.getColumnIndex("sex"));
			ageString = cursor.getString(cursor.getColumnIndex("age"));
			heightString = cursor.getString(cursor.getColumnIndex("height"));
			weightString = cursor.getString(cursor.getColumnIndex("weight"));
			nicknameString = cursor
					.getString(cursor.getColumnIndex("nickname"));
			sportsString = cursor.getString(cursor.getColumnIndex("sports"));
			stream = new ByteArrayInputStream(cursor.getBlob(cursor
					.getColumnIndex("face")));

		}
		EditText name = (EditText) findViewById(R.id.nameEdit);
		name.setText(nameString);
		EditText sex = (EditText) findViewById(R.id.sexEdit);
		sex.setText(sexString);
		EditText age = (EditText) findViewById(R.id.ageEdit);
		age.setText(ageString);
		EditText height = (EditText) findViewById(R.id.heightEdit);
		height.setText(heightString);
		EditText weight = (EditText) findViewById(R.id.weightEdit);
		weight.setText(weightString);
		EditText nickname = (EditText) findViewById(R.id.nicknameEdit);
		nickname.setText(nicknameString);
		EditText sports = (EditText) findViewById(R.id.sportsEdit);
		sports.setText(sportsString);
		faceImage.setImageDrawable(Drawable.createFromStream(stream, "face"));
		BitmapDrawable bd = (BitmapDrawable) faceImage.getDrawable();
		faceBitmap = bd.getBitmap();
	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			showDialog();
		}
	};

	private View.OnClickListener saveListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			EditText name = (EditText) findViewById(R.id.nameEdit);
			nameString = name.getText().toString();
			EditText sex = (EditText) findViewById(R.id.sexEdit);
			sexString = sex.getText().toString();
			EditText age = (EditText) findViewById(R.id.ageEdit);
			ageString = age.getText().toString();
			EditText height = (EditText) findViewById(R.id.heightEdit);
			heightString = height.getText().toString();
			EditText weight = (EditText) findViewById(R.id.weightEdit);
			weightString = weight.getText().toString();
			EditText nickname = (EditText) findViewById(R.id.nicknameEdit);
			nicknameString = nickname.getText().toString();
			EditText sports = (EditText) findViewById(R.id.sportsEdit);
			sportsString = sports.getText().toString();

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

			String sqlDelete = "delete from infomation";
			try {
				db.execSQL(sqlDelete);
			} catch (SQLException ex) {

			}
			String sqlInsert = "insert into  " + "infomation "
					+ "values(null,?,?,?,?,?,?,?,?)";
			try {
				db.execSQL(sqlInsert, new Object[] { nameString, sexString,
						ageString, heightString, weightString, nicknameString,
						sportsString, os.toByteArray() });
			} catch (SQLException ex) {

			}
			Intent intent = new Intent();
			intent.setClass(Info_Change_Activity.this, Goal_Activity.class);
			startActivity(intent);
		}

	};

	/**
	 * 显示选择对话框
	 */
	private void showDialog() {

		new AlertDialog.Builder(this)
				.setTitle("设置头像")
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent intentFromGallery = new Intent();
							intentFromGallery.setType("image/*"); // 设置文件类型
							intentFromGallery
									.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(intentFromGallery,
									IMAGE_REQUEST_CODE);
							break;
						case 1:

							Intent intentFromCapture = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							// 判断存储卡是否可以用，可用进行存储
							if (Tools.hasSdcard()) {

								intentFromCapture.putExtra(
										MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(new File(Environment
												.getExternalStorageDirectory(),
												IMAGE_FILE_NAME)));
							}

							startActivityForResult(intentFromCapture,
									CAMERA_REQUEST_CODE);
							break;
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 结果码不等于取消时候
		if (resultCode != RESULT_CANCELED) {

			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
			case CAMERA_REQUEST_CODE:
				if (Tools.hasSdcard()) {
					File tempFile = new File(
							Environment.getExternalStorageDirectory()
									+ IMAGE_FILE_NAME);
					startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(Info_Change_Activity.this, "未找到存储卡，无法存储照片！",
							Toast.LENGTH_LONG).show();
				}

				break;
			case RESULT_REQUEST_CODE:
				if (data != null) {
					getImageToView(data);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 320);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 2);
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void getImageToView(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			faceBitmap = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(faceBitmap);
			faceImage.setImageDrawable(drawable);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.imfo, menu);
		return true;
	}

}
