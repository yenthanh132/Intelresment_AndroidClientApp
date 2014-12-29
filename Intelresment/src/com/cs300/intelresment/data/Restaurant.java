package com.cs300.intelresment.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs300.intelresment.web.Downloader;

public class Restaurant {
	public String name, address, telephone;
	public int ID;

	public List<Deal> listDeal = new ArrayList<Deal>();
	public List<Food> listFood = new ArrayList<Food>();
	public List<Table> listTable = new ArrayList<Table>();
	public List<Account> listAccount = new ArrayList<Account>();

	Restaurant() {
		ID = 0;
	}

	Restaurant(int ID) {
		this.ID = ID;
	}

	private Date LastUpdatedTime = null;

	public boolean getTableUpdate() {
		String path = "http://haikhanh.me:1080/table/getUpdateSinceTime/";
		JSONObject dataJSON = new JSONObject();
		try {
			if (LastUpdatedTime != null)
				dataJSON.put("datetime", new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(LastUpdatedTime));
			String ret = Downloader.PostData(path, dataJSON.toString());
			JSONObject obj = new JSONObject(ret);
			if (obj.has("tableUpdated")) {
				JSONArray arr = obj.getJSONArray("tableUpdated");
				for (int i = 0; i < arr.length(); ++i) {
					JSONObject objTable = arr.getJSONObject(i);
					for (int j = 0; j < listTable.size(); ++j)
						if (listTable.get(j).ID == objTable.getInt("id")) {
							if (objTable.isNull("billId"))
								listTable.get(j).billID = 0;
							else
								listTable.get(j).billID = objTable.getInt("billId");
							break;
						}
				}
				LastUpdatedTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(obj.getString("lastUpdated"));
				return true;
			} else
				return false;

		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean getTableForceUpdate() {
		String path = "http://haikhanh.me:1080/table/getUpdateSinceTime/";
		JSONObject dataJSON = new JSONObject();
		try {
			String ret = Downloader.PostData(path, dataJSON.toString());
			JSONObject obj = new JSONObject(ret);
			if (obj.has("tableUpdated")) {
				JSONArray arr = obj.getJSONArray("tableUpdated");
				for (int i = 0; i < arr.length(); ++i) {
					JSONObject objTable = arr.getJSONObject(i);
					for (int j = 0; j < listTable.size(); ++j)
						if (listTable.get(j).ID == objTable.getInt("id")) {
							if (objTable.isNull("billId"))
								listTable.get(j).billID = 0;
							else
								listTable.get(j).billID = objTable.getInt("billId");
							break;
						}
				}
				LastUpdatedTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(obj.getString("lastUpdated"));
				return true;
			} else
				return false;

		} catch (Exception e) {
			return false;
		}
	}

	public void getAccountList() {
		String data = Downloader.GetData("http://haikhanh.me:1080/user/");
		listAccount.clear();
		try {
			JSONArray arr = new JSONArray(data);
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject obj = arr.getJSONObject(i);
				Account acc = new Account();
				acc.username = obj.getString("username");
				acc.password = obj.getString("password");
				listAccount.add(acc);
			}
		} catch (Exception e) {

		}
	}

	public boolean CheckLogin(String username, String password) {
		try {
			// Create MD5 Hash
			final String MD5 = "MD5";
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(password.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}

			String hashPass = hexString.toString();
			for (int i = 0; i < listAccount.size(); ++i)
				if (listAccount.get(i).username.equals(username) && listAccount.get(i).password.equals(hashPass))
					return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Bill getBillByID(int billID) {
		Bill bill = new Bill();
		String data = Downloader.GetData("http://haikhanh.me:1080/bill/" + billID);
		try {
			JSONObject obj = new JSONObject(data);
			bill.ID = billID;
			bill.orderDate = obj.getString("orderDate");
			bill.paymentDate = obj.getString("paymentDate");
			if (!obj.getString("paymentPrice").equals("null"))
				bill.paymentPrice = (float) obj.getDouble("paymentPrice");
			else
				bill.paymentPrice = 0;

			bill.isOnline = obj.getBoolean("isOnline");
			bill.customerName = obj.getString("customerName");
			bill.customerPhone = obj.getString("customerName");
			bill.customerAddress = obj.getString("customerAddress");

			// Get ordered food list
			data = Downloader.GetData("http://haikhanh.me:1080/bill/getFood/" + billID);
			obj = new JSONObject(data);
			JSONArray arr = obj.getJSONArray("listOfFood");
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject obj2 = arr.getJSONObject(i);
				int foodID = obj2.getInt("foodId");
				int foodCount = obj2.getInt("number");
				for (int j = 0; j < listFood.size(); ++j)
					if (foodID == listFood.get(j).ID) {
						// Found the corresponding food
						Food orderedFood = new Food(listFood.get(j));
						orderedFood.count = foodCount;
						bill.listOrderedFood.add(orderedFood);
						break;
					}
			}
		} catch (Exception e) {

		}
		return bill;
	}

	public void addNewBill(Bill bill) {
		if (bill.ID > 0)
			return;
		String path = "http://haikhanh.me:1080/bill/";
		JSONObject dataJSON = new JSONObject();
		try {
			dataJSON.put("paymentPrice", 0);
			dataJSON.put("orderDate", new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
			String retJSON = Downloader.PostData(path, dataJSON.toString());

			JSONObject obj = new JSONObject(retJSON);
			bill.ID = obj.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addFoodToBill(Bill bill) {
		if (bill.ID == 0)
			return;
		// Delete all the food first
		String path = "http://haikhanh.me:1080/bill/destroyFood/" + bill.ID;
		JSONObject dataJSON = new JSONObject();
		try {
			dataJSON.put("billId", bill.ID);
			Downloader.PostData(path, dataJSON.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start adding food
		path = "http://haikhanh.me:1080/bill/addFood/";
		for (int i = 0; i < bill.listOrderedFood.size(); ++i) {
			Food food = bill.listOrderedFood.get(i);
			JSONObject obj = new JSONObject();
			try {
				obj.put("billId", bill.ID);
				obj.put("foodId", food.ID);
				obj.put("number", food.count);
				Downloader.PostData(path, obj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addBillToTable(int tableID, int billID) {
		Downloader.PostData("http://haikhanh.me:1080/table/" + tableID + "/assignWithBill/" + billID, "");
		// String path = "http://haikhanh.me:1080/table/" + tableID;
		// JSONObject data = new JSONObject();
		// try {
		// data.put("billId", billID);
		// Downloader.PutData(path, data.toString());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}

	public void getFoodList() {
		String data = Downloader.GetData("http://haikhanh.me:1080/food/");
		listFood.clear();
		try {
			JSONArray arr = new JSONArray(data);
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject obj = arr.getJSONObject(i);
				Food food = new Food();
				if (!obj.getString("id").equals("null"))
					food.ID = obj.getInt("id");
				else
					food.ID = 0;

				if (!obj.getString("price").equals("null"))
					food.price = obj.getInt("price");
				else
					food.price = 0;

				food.count = 0;
				food.name = obj.getString("name");
				food.description = obj.getString("description");
				food.imageSmallUrl = obj.getString("imageSmall");
				food.imageLargeUrl = obj.getString("imageLarge");

				listFood.add(food);
			}
		} catch (Exception e) {

		}
	}

	public Table getTableByID(int tableID) {
		String data = Downloader.GetData("http://haikhanh.me:1080/table/" + tableID);
		try {
			JSONObject obj = new JSONObject(data);
			Table table = new Table();
			if (!obj.isNull("billId"))
				table.billID = obj.getInt("billId");
			else
				table.billID = 0;

			if (!obj.isNull("id"))
				table.ID = obj.getInt("id");
			else
				table.ID = 0;

			if (!obj.isNull("name"))
				table.name = obj.getString("name");
			else
				table.name = "";

			if (!obj.isNull("description"))
				table.description = obj.getString("description");
			else
				table.description = "";

			return table;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean moveTableFromIDToID(int tableFromID, int tableToID) {
		Table table1 = getTableByID(tableFromID);
		Table table2 = getTableByID(tableToID);
		if (table2.billID != 0)
			return false;
		if (table1.billID == 0)
			return false;
		addBillToTable(table2.ID, table1.billID);
		addBillToTable(table1.ID, 0);
		return true;
	}

	public void getTableList() {
		String data = Downloader.GetData("http://haikhanh.me:1080/table");
		listTable.clear();
		try {
			JSONArray arr = new JSONArray(data);
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject obj = arr.getJSONObject(i);
				Table table = new Table();
				if (!obj.isNull("billId"))
					table.billID = obj.getInt("billId");
				else
					table.billID = 0;

				if (!obj.isNull("id"))
					table.ID = obj.getInt("id");
				else
					table.ID = 0;

				if (!obj.isNull("name"))
					table.name = obj.getString("name");
				else
					table.name = "";

				if (!obj.isNull("description"))
					table.description = obj.getString("description");
				else
					table.description = "";

				listTable.add(table);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
