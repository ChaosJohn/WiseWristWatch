package com.example.wise_wrist_watch;

import java.io.ByteArrayInputStream;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xzw.utils.DatabaseHelper;

public class Fit_Activity extends Activity {
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fit);
		TextView mygoalTextView = (TextView) findViewById(R.id.mygoal);
		mygoalTextView.setOnClickListener(mygoleListener);
		initializeLayout();
	}

	private void initializeLayout() {
		ImageView faceImageView = (ImageView) findViewById(R.id.face3);
		databaseHelper = new DatabaseHelper(getApplicationContext(), "info.db",
				null, 1);
		db = databaseHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from infomation", null);
		TextView nameTextView = (TextView) findViewById(R.id.name3);
		if (cursor.moveToFirst()) {
			ByteArrayInputStream stream = new ByteArrayInputStream(
					cursor.getBlob(cursor.getColumnIndex("face")));
			faceImageView.setImageDrawable(Drawable.createFromStream(stream,
					"face3"));
			String nicknameString = cursor.getString(cursor
					.getColumnIndex("nickname"));
			nameTextView.setText(nicknameString);

		}
		cursor.close();
		db.close();
	}

	private View.OnClickListener mygoleListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Fit_Activity.this.finish();
		}
	};

	protected void onResume() {
		initializeLayout();
		super.onResume();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.imfo, menu);
		return true;
	}

}
