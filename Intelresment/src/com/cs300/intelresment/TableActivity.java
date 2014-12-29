package com.cs300.intelresment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cs300.intelresment.adapter.OrderedFoodAdapter;
import com.cs300.intelresment.adapter.TableListAdapter;
import com.cs300.intelresment.data.Bill;
import com.cs300.intelresment.data.Singleton;
import com.cs300.intelresment.data.Table;

public class TableActivity extends Activity {

	private static final int ORDER_ACTIVITY_RESPONDE = 21;
	private Table table;
	private Bill bill;
	private OrderedFoodAdapter foodAdapter;
	private TextView tvTableName, tvOrderDate;
	private GridView gvOrderedFood;
	private ImageButton btnBack, btnRefresh, btnMenu;
	private Button btnChooseFood, btnOrder;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table);

		tvTableName = (TextView) findViewById(R.id.tvTableName);
		tvOrderDate = (TextView) findViewById(R.id.tvOrderDate);
		gvOrderedFood = (GridView) findViewById(R.id.gvOrderedFood);
		btnBack = (ImageButton) findViewById(R.id.btnBack);
		btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
		btnMenu = (ImageButton) findViewById(R.id.btnMenu);
		btnChooseFood = (Button) findViewById(R.id.btnChooseFood);
		btnOrder = (Button) findViewById(R.id.btnOrder);

		Bundle bundle = getIntent().getExtras();
		int pos = bundle.getInt("tableID");
		table = Singleton.myRestaurant.listTable.get(pos);

		tvTableName.setText(table.name);
		doTableRefresh();

		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		btnRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				doTableRefresh();
				Toast.makeText(getApplicationContext(), "Đã refresh thành công dữ liệu của bàn ăn", Toast.LENGTH_SHORT).show();
			}
		});

		btnChooseFood.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(TableActivity.this, OrderActivity.class);
				Singleton.tmpBill = bill;
				startActivityForResult(intent, ORDER_ACTIVITY_RESPONDE);
			}
		});

		btnOrder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (bill.listOrderedFood.size() == 0) {
					AlertDialog.Builder builder = new Builder(TableActivity.this);
					builder.setTitle("Thông báo");
					builder.setMessage("Chưa có món ăn nào được chọn!");
					builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
					return;
				}
				if (bill.ID == 0 && table.billID != 0) {
					AlertDialog.Builder builder = new Builder(TableActivity.this);
					builder.setTitle("Thông báo");
					builder.setMessage("Bàn này đã được đặt nên không thể gửi yêu cầu đặt mới! Hãy refresh để xem danh sách món ăn mới.");
					builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
					return;
				}
				new ProcessBill().execute();
			}
		});

		btnMenu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				PopupMenu popup = new PopupMenu(TableActivity.this, btnMenu);
				popup.getMenuInflater().inflate(R.menu.popup_tablemenu, popup.getMenu());
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.btnMove:
							if (table.billID == 0) {
								AlertDialog.Builder builder = new Builder(TableActivity.this);
								builder.setTitle("Lỗi!");
								builder.setMessage("Bàn ăn hiện không có hóa đơn để di chuyển!");
								builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								builder.create().show();
								return true;
							}

							final Dialog dialogTablePicker = new Dialog(TableActivity.this);
							dialogTablePicker.setContentView(R.layout.dialog_tablepicker);
							dialogTablePicker.setTitle("Di chuyển bàn ăn");

							final List<Table> freeTableList = new ArrayList<Table>();
							freeTableList.clear();
							for (int i = 0; i < Singleton.myRestaurant.listTable.size(); ++i)
								if (Singleton.myRestaurant.listTable.get(i).billID == 0) {
									freeTableList.add(Singleton.myRestaurant.listTable.get(i));
								}
							TableListAdapter adapter = new TableListAdapter(TableActivity.this, freeTableList);
							ListView lvTable = (ListView) dialogTablePicker.findViewById(R.id.lvTable);
							lvTable.setAdapter(adapter);
							lvTable.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
									Integer[] params = new Integer[] { table.ID, freeTableList.get(pos).ID };
									new ProcessMoveTable().execute(params);
									dialogTablePicker.dismiss();
								}
							});
							dialogTablePicker.show();
							return true;

						case R.id.btnRemove:
							if (table.billID == 0) {
								AlertDialog.Builder builder = new Builder(TableActivity.this);
								builder.setTitle("Lỗi!");
								builder.setMessage("Bàn ăn hiện không có hóa đơn để xóa!");
								builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								builder.create().show();
								return true;
							}

							AlertDialog.Builder builder = new Builder(TableActivity.this);
							builder.setTitle("Xác nhận");
							builder.setMessage("Bạn có chắc chắn muốn xóa hóa đơn cho bàn ăn này?");
							builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Integer[] params = new Integer[] { table.ID };
									new ProcessRemoveTable().execute(params);

									dialog.dismiss();
								}
							});
							builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int arg1) {
									dialog.dismiss();
								}
							});
							builder.create().show();
							return true;
						default:
							return false;
						}
					}
				});
				popup.show();
			}
		});

	}

	private void doTableRefresh() {
		if (table.billID == 0) {
			tvOrderDate.setText("[Bàn trống]");
			bill = new Bill();
			bill.ID = 0;
			foodAdapter = new OrderedFoodAdapter(TableActivity.this, bill.listOrderedFood);
			gvOrderedFood.setAdapter(foodAdapter);
		} else {
			new LoadData().execute();
		}
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == ORDER_ACTIVITY_RESPONDE) {

			if (resultCode == RESULT_OK) {
				foodAdapter.notifyDataSetChanged();
			}
		}
	}

	class ProcessMoveTable extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			dialog.hide();
			if (result == true) {
				Toast.makeText(getApplicationContext(), "Di chuyển bàn ăn thành công!", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				AlertDialog.Builder builder = new Builder(TableActivity.this);
				builder.setTitle("Lỗi!");
				builder.setMessage("Không thể di chuyển bàn ăn theo yêu cầu.");
				builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			CreateDialog();
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			if (Singleton.myRestaurant.moveTableFromIDToID(params[0], params[1])) {
				return true;
			}
			return false;
		}

	}

	class ProcessRemoveTable extends AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			dialog.hide();
			Toast.makeText(getApplicationContext(), "Xóa hóa đơn thành công!", Toast.LENGTH_SHORT).show();
			finish();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			CreateDialog();
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			Singleton.myRestaurant.addBillToTable(params[0], 0);
			return null;
		}

	}

	class ProcessBill extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Singleton.myRestaurant.addNewBill(bill);
			Singleton.myRestaurant.addFoodToBill(bill);
			Singleton.myRestaurant.addBillToTable(table.ID, bill.ID);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dialog.hide();
			Toast.makeText(getApplicationContext(), "Đặt món thành công cho " + table.name, Toast.LENGTH_SHORT).show();
			finish();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			CreateDialog();
			dialog.show();
			super.onPreExecute();
		}

	}

	class LoadData extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			CreateDialog();
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			bill = Singleton.myRestaurant.getBillByID(table.billID);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Date date = null;
			try {
				date = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(bill.orderDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (date != null)
				tvOrderDate.setText(new SimpleDateFormat("HH:mm").format(date));
			foodAdapter = new OrderedFoodAdapter(TableActivity.this, bill.listOrderedFood);
			gvOrderedFood.setAdapter(foodAdapter);

			dialog.hide();
			super.onPostExecute(result);
		}

	}

}
