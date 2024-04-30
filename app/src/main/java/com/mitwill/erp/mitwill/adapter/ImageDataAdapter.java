package com.mitwill.erp.mitwill.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mitwill.erp.R;
import com.mitwill.erp.mitwill.model.AttachedImages;
import com.mitwill.erp.mitwill.model.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageDataAdapter extends RecyclerView.Adapter<ImageDataAdapter.ViewHolder> {
    private static final String TAG ="GMT" ;
    private Context context;
    ArrayList<HashMap<String,String> > arrImageInfoList;
    ImageItemClickListener imageItemClickListener;

    public ImageDataAdapter(Context context, ArrayList<HashMap<String, String>> arrImageInfoList, ImageItemClickListener listener) {
        this.arrImageInfoList = arrImageInfoList;
        this.context = context;
        this.imageItemClickListener = listener;
    }

    @Override
    public ImageDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(ImageDataAdapter.ViewHolder viewHolder, final int i) {
        Log.d(TAG, "onBindViewHolder: ");
        if (arrImageInfoList !=null && arrImageInfoList.get(i) !=null)
        {
            String imageURL=arrImageInfoList.get(i)
                    .get(AttachedImages.IMAGE_URL)+"/"+arrImageInfoList.get(i).get(AttachedImages.NAME);
            viewHolder.tv_android.setText(arrImageInfoList.get(i).get(AttachedImages.NAME));
            GlideApp.with(context)
                    .load(Uri.fromFile(new File(imageURL)))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .override(320, 640)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // log exception
                            Log.d(TAG, "onLoadFailed: " + e.getMessage());
                            return false; // important to return false so the error placeholder can be placed
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "onResourceReady: ");
                            return false;
                        }
                    })
                    .into(viewHolder.img_android);
            viewHolder.img_android .setScaleType(ImageView.ScaleType.FIT_XY);
            viewHolder.img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageItemClickListener.onItemClick(view, i);
                }
            });
        }
   }

    @Override
    public int getItemCount() {
        if (this.arrImageInfoList !=null  ) {
            return this.arrImageInfoList.size();
        }
        return  0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_android;
        private ImageView img_android,img_delete;
        public ViewHolder(View view) {
            super(view);
            tv_android = (TextView)view.findViewById( R.id.tv_android);
            img_android = (ImageView) view.findViewById(R.id.img_android);
            img_delete = (ImageView) view.findViewById(R.id.img_delete);
        }
    }
    public interface ImageItemClickListener {
        public void onItemClick(View v, int position);
    }

}