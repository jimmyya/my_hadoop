package com.chen.service.model;

import java.text.SimpleDateFormat;

/**
 * 统一处理时间格式
 * @author CHEN
 *
 */
public class TimeDealer {

	public static String timeFormat(long time) {
		SimpleDateFormat format=new SimpleDateFormat("yy-MM-dd hh:mm");
		return format.format(time);
	}
}
