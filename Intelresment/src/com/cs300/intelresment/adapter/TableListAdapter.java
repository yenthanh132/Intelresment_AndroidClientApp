package com.cs300.intelresment.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cs300.intelresment.R;
import com.cs300.intelresment.data.Table;

public class TableListAdapter extends BaseAdapter {

	private Activity context;
	private List<Table> TableList;

	static class ViewHolder {
		public TextView tvName;
	}

	public TableListAdapter(Activity context, List<Table> TableList) {
		this.context = context;
		this.TableList = TableList;
	}

	@Override
	public int getCount() {
		return TableList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		View gridView = view;

		if (view == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			gridView = inflater.inflate(R.layout.table_row_view, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvName = (TextView) gridView.findViewById(R.id.tvName);
			gridView.setTag(viewHolder);
		}
		ViewHolder viewHolder = (ViewHolder) gridView.getTag();
		Table table = (Table) TableList.get(position);
		viewHolder.tvName.setText(table.name);
		return gridView;
	}

}
