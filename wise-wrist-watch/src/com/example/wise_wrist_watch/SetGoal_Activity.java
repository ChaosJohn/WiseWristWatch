package com.example.wise_wrist_watch;

import net.simonvt.numberpicker.NumberPicker;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xzw.utils.DatabaseHelper;

public class SetGoal_Activity extends Activity {

	Bundle bundle;
	NumberPicker np1;
	NumberPicker np2;
	NumberPicker np3;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setgoal);
		databaseHelper = new DatabaseHelper(getApplicationContext(), "info.db",
				null, 1);
		db = databaseHelper.getWritableDatabase();

		np1 = (NumberPicker) findViewById(R.id.numberPicker1);
		np2 = (NumberPicker) findViewById(R.id.numberPicker2);
		np3 = (NumberPicker) findViewById(R.id.numberPicker3);

		showTextView();
		setNumberPicker();

		TextView saveRunTextView = (TextView) findViewById(R.id.save_run);
		saveRunTextView.setOnClickListener(new saverunListener());

	}

	private final class saverunListener implements View.OnClickListener {
		public void onClick(View v) {
			String sql = "";
			String value = null;
			switch (bundle.getInt("runtype")) {
			case 1: {
				sql = "update runparameter set runtime = ?";
				if (np1.getValue() != 0) {
					value = Integer.toString(np1.getValue())
							+ Integer.toString(np2.getValue())
							+ Integer.toString(np3.getValue());
				} else {
					if (np2.getValue() != 0) {
						value = Integer.toString(np2.getValue())
								+ Integer.toString(np3.getValue());
					} else {
						value = Integer.toString(np3.getValue());
					}
				}
				break;
			}
			case 2: {
				sql = "update runparameter set runspeed = ?";
				value = Integer.toString(np1.getValue()) + "."
						+ Integer.toString(np2.getValue());
				break;
			}
			case 3: {
				sql = "update runparameter set runinterval = ?";
				if (np1.getValue() != 0) {
					value = Integer.toString(np1.getValue())
							+ Integer.toString(np2.getValue())
							+ Integer.toString(np3.getValue());
				} else {
					if (np2.getValue() != 0) {
						value = Integer.toString(np2.getValue())
								+ Integer.toString(np3.getValue());
					} else {
						value = Integer.toString(np3.getValue());
					}
				}
				break;
			}
			default:
				break;
			}
			try {
				db.execSQL(sql, new String[] { value });
			} catch (Exception e) {
				// TODO: handle exception
				int i = 0;
			}
			db.close();
			SetGoal_Activity.this.finish();
		}
	}

	private void showTextView() {
		bundle = SetGoal_Activity.this.getIntent().getExtras();
		switch (bundle.getInt("runtype")) {
		case 1: {
			break;
		}
		case 2: {
			TextView head = (TextView) findViewById(R.id.headtext);
			head.setText("跑步速度");
			TextView tail = (TextView) findViewById(R.id.tailtext);
			tail.setText("m/s以上");
			TextView point = (TextView) findViewById(R.id.pointtext);
			point.setText(".");
			np3.setVisibility(View.GONE);
			break;
		}
		case 3: {
			TextView head = (TextView) findViewById(R.id.headtext);
			head.setText("跑步间隔");
			TextView tail = (TextView) findViewById(R.id.tailtext);
			tail.setText("小时");
			break;
		}
		default:
			break;
		}
	}

	private void setNumberPicker() {
		np1.setMaxValue(9);
		np1.setMinValue(0);
		np1.setFocusable(true);
		np1.setFocusableInTouchMode(true);

		np2.setMaxValue(9);
		np2.setMinValue(0);
		np2.setFocusable(true);
		np2.setFocusableInTouchMode(true);

		np3.setMaxValue(9);
		np3.setMinValue(0);
		np3.setFocusable(true);
		np3.setFocusableInTouchMode(true);
	}
}
