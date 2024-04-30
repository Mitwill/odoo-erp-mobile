package com.mitwill.erp.mitwill.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mitwill.erp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    private List<File> imagesFiles;
    ImageItemClickListener imageItemClickListener;
    public ImagesAdapter(Context context, List<File> imagesFiles, ImageItemClickListener listener) {
        this.context = context;
        this.imagesFiles = imagesFiles;
        this.imageItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ViewHolder(inflater.inflate(R.layout.image_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Picasso.get()
                .load(imagesFiles.get(position))
                .fit()
                .into(holder.imageView);
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageItemClickListener.onItemClick(view, position);
            }
        });
        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageItemClickListener.onItemClick(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesFiles.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView,img_delete;;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            img_delete = itemView.findViewById(R.id.img_delete);
        }
    }
    public interface ImageItemClickListener {
        public void onItemClick(View v, int position);
    }
}

