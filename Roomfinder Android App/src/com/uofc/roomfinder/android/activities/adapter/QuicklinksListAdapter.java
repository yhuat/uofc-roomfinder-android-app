package com.uofc.roomfinder.android.activities.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uofc.roomfinder.R;

public class QuicklinksListAdapter extends ArrayAdapter<QuicklinkDataset> {

	public QuicklinksListAdapter(Context context, int resource, int textViewResourceId, List<QuicklinkDataset> objects) {
		super(context, resource, textViewResourceId, objects);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder holder = null;
		TextView title = null;
		ImageView i11 = null;
		QuicklinkDataset rowData = getItem(position);
		
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.quicklinks_list_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		
		//fill views with data
		holder = (ViewHolder) convertView.getTag();
		title = holder.gettitle();
		title.setText(rowData.title);
		i11 = holder.getImage();
		i11.setImageResource(rowData.imageId);
		
		return convertView;
	}

	private class ViewHolder {
		private View mRow;
		private TextView title = null;
		private ImageView i11 = null;

		public ViewHolder(View row) {
			mRow = row;
		}

		public TextView gettitle() {
			if (null == title) {
				title = (TextView) mRow.findViewById(R.id.title);
			}
			return title;
		}

		public ImageView getImage() {
			if (null == i11) {
				i11 = (ImageView) mRow.findViewById(R.id.img);
			}
			return i11;
		}
	}

}
