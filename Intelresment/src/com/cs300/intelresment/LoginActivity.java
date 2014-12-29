package com.cs300.intelresment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs300.intelresment.data.Singleton;

public class LoginActivity extends Activity {
	private static final int MAIN_ACTIVITY_LOGOUT = 1;
	private Button btnLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		if (!isNetworkAvailable()) {
			Toast.makeText(getApplicationContext(), "Không thể kết nối đến máy chủ! Xin hãy chắc rằng máy của bạn đã bật kết nối Wifi/3G",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
				EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
				if (Singleton.myRestaurant.CheckLogin(txtUsername.getText().toString(), txtPassword.getText().toString())) {
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					startActivityForResult(intent, MAIN_ACTIVITY_LOGOUT);
				} else {
					AlertDialog.Builder builder = new Builder(LoginActivity.this);
					builder.setTitle("Đăng nhập thất bại");
					builder.setMessage("Sai tên đăng nhập hoặc mật khẩu!");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});
		new LoadData().execute();
	}

	class LoadData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Singleton.myRestaurant.getAccountList();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			btnLogin.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}

	private boolean doubleBackToExitPressedOnce = false;

	@Override
	public void onBackPressed() {

		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "Bấm BACK lần nữa để THOÁT", Toast.LENGTH_SHORT).show();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, 2000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == MAIN_ACTIVITY_LOGOUT) {
			EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
			txtPassword.setText("");
		}
	}

	// Internet state
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
