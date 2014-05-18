package com.xzw.utils;

import android.os.Environment;

/**
 * 
 * @author XuZhiwei (xuzw13@gmail.com) Create at 2012-8-17 上午10:14:40
 */
public class Tools {
	/**
	 * 检查是否存在SDCard
	 * 
	 * @return
	 */
	public static boolean hasSdcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据身高和每两秒的步数计算平均每步的长度 cm
	 * 
	 * @param height
	 * @param rate
	 * @return
	 */
	public static double getStepSizeByHeightAndRate(int height, double rate) {
		// double stepSize = 0;
		//
		// if (rate > 0 && rate <= 2) {
		// stepSize = height / 5;
		// } else if (rate > 2 && rate <= 3) {
		// stepSize = height / 4;
		// } else if (rate > 3 && rate <= 4) {
		// stepSize = height / 3;
		// } else if (rate > 4 && rate <= 5) {
		// stepSize = height / 2;
		// } else if (rate > 5 && rate <= 6) {
		// stepSize = (int) (height / 1.2);
		// } else if (rate > 6 && rate <= 8) {
		// stepSize = height;
		// } else if (rate > 8) {
		// stepSize = (int) (height * 1.2);
		// }
		//
		// return stepSize;
		return 150;
	}

	/**
	 * 根据步长和每2s的步数计算速度（m/min）
	 * 
	 * @param stepSize
	 *            -cm
	 * @param rate
	 * @return
	 */
	public static double getSpeedByStepSizeAndRate(double stepSize, double rate) {
		double speed = stepSize * rate / 2; // cm/s
		speed = speed * 60 / 100; // m/min
		return speed;
	}

	/**
	 * 计算卡路里消耗
	 * 
	 * @param speed
	 *            速度-m/min
	 * @param weight
	 *            体重-kg
	 * @param time
	 *            时间-h
	 * @return
	 */
	public static double getCalorieBySpeedAndWeightWithTime(double speed,
			int weight, double time) {
		speed = speed * 60 / 1000; // 将m/min转换成km/h
		double caloriePer = 1.25 * speed;
		return caloriePer * weight * time;
	}
}
