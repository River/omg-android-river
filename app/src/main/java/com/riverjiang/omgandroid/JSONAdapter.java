package com.riverjiang.omgandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by river on 14-10-12.
 */
public class JSONAdapter extends BaseAdapter {

    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";

    Context mContext;
    LayoutInflater mLayoutInflater;
    JSONArray mJSONArray;

    public JSONAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mLayoutInflater = inflater;
        mJSONArray = new JSONArray();
    }

    @Override
    public int getCount() {
        return mJSONArray.length();
    }

    @Override
    public Object getItem(int i) {
        return mJSONArray.optJSONObject(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.row_book, null);

            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) view.findViewById(R.id.image_thumbnail);
            holder.authorTextView = (TextView) view.findViewById(R.id.text_author);
            holder.titleTextView = (TextView) view.findViewById(R.id.text_title);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        JSONObject bookObject = (JSONObject) getItem(i);

        if (bookObject.has("cover_i")) {
            String imageId = bookObject.optString("cover_i");
            String imageUrl = IMAGE_URL_BASE + imageId + "-L.jpg";

            Picasso.with(mContext).load(imageUrl).placeholder(R.drawable.ic_books).into(holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_books);
        }

        String title = "";
        String author = "";

        if (bookObject.has("title")) {
            title = bookObject.optString("title");
        }


        if (bookObject.has("author_name")) {
            author = bookObject.optJSONArray("author_name").optString(0);
        }

        holder.titleTextView.setText(title);
        holder.authorTextView.setText(author);

        return view;
    }

    public void updateData(JSONArray jsonArray) {
        mJSONArray = jsonArray;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView authorTextView;
    }
}
