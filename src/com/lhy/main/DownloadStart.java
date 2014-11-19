package com.lhy.main;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lhy.support.Controller;
import com.lhy.support.Support;

public class DownloadStart implements Runnable {
	File file;
	Support s;
	Thread[] t;
	String url;
	String path;
	String range;
	Controller c;
	String real_url;
	String filename;
	long filelength;
	int thread_num;
	String suffix;
	JSONArray ja;
	HttpURLConnection con;

	public DownloadStart(String url) {
		this.url = url;
		thread_num = 6;
		range = "NO";
		path = "/Users/apple/Desktop/My_Download/";
	}

	public DownloadStart(String url, String range) {
		this.url = url;
		thread_num = 10;
		this.range = range;
		path = "/Users/apple/Desktop/My_Download/";
	}

	@Override
	public void run() {
		get_con();
		get_info();
		con.disconnect();
		assign_tasks();
	}

	private void get_info() {
		real_url = get_realurl();
		filename = get_filename();
		filelength = get_filelength();
	}

	private long get_filelength() {
		return con.getContentLengthLong();
	}

	private String get_realurl() {
		return con.toString().replace(
				"sun.net.www.protocol.http.HttpURLConnection:", "");
	}

	private String get_filename() {
		return con.toString()
				.replace("sun.net.www.protocol.http.HttpURLConnection:", "")
				.split("/")[con.toString().split("/").length - 1];
	}

	private void get_con() {
		try {
			con = (HttpURLConnection) new URL(url).openConnection();
			con.setDoInput(true);
			con.connect();
			con.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void assign_tasks() {
		suffix = ".Lhy";
		file = new File(path + filename + suffix);
		s = new Support(thread_num);
		s.set_filelength(filelength);
		t = new Thread[thread_num];
		if (!file.exists()) {
			new Thread(c = new Controller(real_url, filename, s, file)).start();
			if (range.equals("NO")) {
				long index = filelength / thread_num;
				String[] tasks = new String[thread_num];
				for (int i = 0; i < thread_num; i++) {
					if (i == 0)
						tasks[i] = "0-" + index;
					else if (0 < i && i < thread_num - 1)
						tasks[i] = index * i + "-" + index * (i + 1);
					else
						tasks[i] = index * i + "-" + filelength;
				}
				for (int i = 0; i < thread_num; i++) {
					t[i] = new Thread(new Downloading(real_url, path, filename,
							tasks[i], i, s));
					t[i].setPriority(10);
					t[i].start();
				}
			}
		} else {
			new Thread(c = new Controller(real_url, filename, s, file)).start();
			String json = c.get_json_data();
			try {
				ja = new JSONArray(json);
				for (int i = 0; i < thread_num; i++) {
					t[i] = new Thread(
							new Downloading(real_url, path, filename,
									(String) ((JSONObject) ja.get(2)).get(i
											+ ""), i, s));
					t[i].setPriority(10);
					t[i].start();
					System.out.println("断点下载"+(String) ((JSONObject) ja.get(2)).get(i
							+ ""));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}