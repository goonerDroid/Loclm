package com.sublime.loclm.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sublime.loclm.R;
import com.sublime.loclm.model.AutocompleteInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goonerdroid
 * on 27/11/17.
 */

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private List<AutocompleteInfo> placeList = new ArrayList<>();


    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    public void setData(List<AutocompleteInfo> dataList) {
        placeList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(PlaceAdapter.ViewHolder holder, int position) {
        AutocompleteInfo info = placeList.get(position);
        if (info != null) {
            holder.tvPrimaryAddress.setText(info.getPrimaryText());
            holder.tvSecondaryAddress.setText(info.getSecondaryText());
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_primary_name)
        TextView tvPrimaryAddress;
        @BindView(R.id.tv_secondary_name)
        TextView tvSecondaryAddress;


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
