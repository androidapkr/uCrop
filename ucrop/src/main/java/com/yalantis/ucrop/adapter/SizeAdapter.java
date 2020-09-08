package com.yalantis.ucrop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.widget.AspectRatioImageView;

import java.util.ArrayList;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.ItemHolder> {

    private ArrayList<AspectRatio> list;

    public SizeAdapter(Context context) {
        list = new ArrayList<>();
        list.add(new AspectRatio("1:1", 1, 1));
        list.add(new AspectRatio("3:4", 3, 4));
        list.add(new AspectRatio("3:2", 3, 2));
        list.add(new AspectRatio("16:9", 16, 9));
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.child_crop_size, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.imageView.setXYSize(list.get(position).getAspectRatioX() / list.get(position).getAspectRatioY());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        final AspectRatioImageView imageView;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewRatioChild);

        }
    }
}
