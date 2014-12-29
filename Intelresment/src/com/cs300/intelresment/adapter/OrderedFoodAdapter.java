package com.cs300.intelresment.adapter;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cs300.intelresment.R;
import com.cs300.intelresment.data.Food;
import com.cs300.intelresment.data.Table;
import com.cs300.intelresment.web.ImageLoader;

public class OrderedFoodAdapter extends BaseAdapter {

	private Activity context;
	private List<Food> FoodList;
	private ImageLoader imageLoader;

	static class ViewHolder {
		public TextView tvID, tvName, tvPrice, tvPriceDeal, tvNumber;
		public ImageView ivImg;
	}

	public OrderedFoodAdapter(Activity context, List<Food> FoodList) {
		this.context = context;
		this.FoodList = FoodList;
		sortFood();
		imageLoader = new ImageLoader(context);
	}

	@Override
	public int getCount() {
		return FoodList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	private void sortFood() {
		int n = FoodList.size();
		for (int i = 0; i < n; ++i)
			for (int j = i + 1; j < n; ++j)
				if (FoodList.get(i).ID > FoodList.get(j).ID) {
					Food tmp = FoodList.get(i);
					FoodList.set(i, FoodList.get(j));
					FoodList.set(j, tmp);
				}
	}

	@Override
	public void notifyDataSetChanged() {
		sortFood();
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		View gridView = view;

		if (view == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			gridView = inflater.inflate(R.layout.food_row_view, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvID = (TextView) gridView.findViewById(R.id.tvID);
			viewHolder.tvName = (TextView) gridView.findViewById(R.id.tvName);
			viewHolder.tvPrice = (TextView) gridView.findViewById(R.id.tvPrice);
			viewHolder.tvPriceDeal = (TextView) gridView.findViewById(R.id.tvPriceDeal);
			viewHolder.tvNumber = (TextView) gridView.findViewById(R.id.tvNumber);
			viewHolder.ivImg = (ImageView) gridView.findViewById(R.id.ivImg);
			gridView.setTag(viewHolder);
		}
		ViewHolder viewHolder = (ViewHolder) gridView.getTag();
		Food food = (Food) FoodList.get(position);
		viewHolder.tvID.setText(String.format("%03d", food.ID));
		viewHolder.tvName.setText(food.name);

		viewHolder.tvPrice.setText(String.format("%2.0f", food.price) + " VND");
		viewHolder.tvPriceDeal.setText("");
		viewHolder.tvNumber.setText(String.valueOf(food.count));
		imageLoader.DisplayImage(food.imageSmallUrl, viewHolder.ivImg);

		return gridView;
	}
}
