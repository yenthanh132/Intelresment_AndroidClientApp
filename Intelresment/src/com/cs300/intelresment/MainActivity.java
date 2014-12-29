package com.cs300.intelresment;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cs300.intelresment.adapter.TableAdapter;
import com.cs300.intelresment.data.Singleton;

public class MainActivity extends Activity {

	protected static final int TABLE_ACTIVITY_CLOSE = 11;
	private GridView gvTable;
	private TableAdapter tableAdapter;
	private Timer timer;
	private TimerTask taskUpdateTable;
	private ImageButton btnBack, btnRefresh;

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gvTable = (GridView) findViewById(R.id.gvTable);

		gvTable.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				Intent intent = new Intent(MainActivity.this, TableActivity.class);
				intent.putExtra("tableID", pos);
				startActivityForResult(intent, TABLE_ACTIVITY_CLOSE);
			}
		});

		btnBack = (ImageButton) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});

		btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new RefreshData().execute();
			}
		});

		timer = new Timer();
		taskUpdateTable = new TimerTask() {

			@Override
			public void run() {
				if (Singleton.myRestaurant.getTableUpdate()) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (tableAdapter != null)
								tableAdapter.notifyDataSetChanged();
						}
					});

				}
			}
		};
		timer.schedule(taskUpdateTable, 1000, 1000);

		new LoadData().execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == TABLE_ACTIVITY_CLOSE) {
			tableAdapter.notifyDataSetChanged();
		}
	}

	private boolean doubleBackToExitPressedOnce = false;

	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setTitle("Đăng xuất?");
		builder.setMessage("Bạn có muốn đăng xuất khỏi hệ thống?");
		builder.setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void CreateDialog() {
		if (dialog != null)
			return;
		dialog = new ProgressDialog(this);
		dialog.setTitle("Intelresment");
		dialog.setMessage("Đang xử lí...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.hide();
	}

	@Override
	protected void onDestroy() {
		if (taskUpdateTable != null) {
			taskUpdateTable.cancel();
			taskUpdateTable = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		super.onDestroy();
	}

	class LoadData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Singleton.myRestaurant.getTableList();
			Singleton.myRestaurant.getFoodList();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			tableAdapter = new TableAdapter(MainActivity.this, Singleton.myRestaurant.listTable);
			gvTable.setAdapter(tableAdapter);
			dialog.hide();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			CreateDialog();
			dialog.show();
			super.onPreExecute();
		}

	}

	class RefreshData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Singleton.myRestaurant.getTableForceUpdate();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			tableAdapter.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), "Đã refresh dữ liệu thành công!", Toast.LENGTH_SHORT).show();
			dialog.hide();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			CreateDialog();
			dialog.show();
			super.onPreExecute();
		}

	}

}
