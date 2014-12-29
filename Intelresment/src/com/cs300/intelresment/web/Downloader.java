package com.cs300.intelresment.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.net.ParseException;

public class Downloader {

	public static String GetData(String url) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);

			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String res = _getResponseBody(entity);
				return res;
			}
			return "Can't load content";
		} catch (Exception e) {
			return "ERROR!";
		}
	}

	public static String PostData(String url, String dataJSON) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new StringEntity(dataJSON));
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String res = _getResponseBody(entity);
				return res;
			}
			return "";
		} catch (Exception e) {
			return "ERROR!";
		}
	}
	
	public static String PutData(String url, String dataJSON) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPut httpput = new HttpPut(url);
			httpput.setEntity(new StringEntity(dataJSON));
			httpput.setHeader("Accept", "application/json");
			httpput.setHeader("Content-type", "application/json");
			HttpResponse response = httpclient.execute(httpput);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String res = _getResponseBody(entity);
				return res;
			}
			return "";
		} catch (Exception e) {
			return "ERROR!";
		}
	}


	public static String _getResponseBody(final HttpEntity entity) throws IOException, ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return "";
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
		}
		String charset = getContentCharSet(entity);
		if (charset == null) {
			charset = HTTP.DEFAULT_CONTENT_CHARSET;
		}
		Reader reader = new InputStreamReader(instream, charset);
		StringBuilder buffer = new StringBuilder();
		try {
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	public static String getContentCharSet(final HttpEntity entity) throws ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		String charset = null;
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					charset = param.getValue();
				}
			}
		}
		return charset;
	}
}
