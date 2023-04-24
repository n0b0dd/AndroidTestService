package com.kosign.reminderdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kosign.reminderdemo.data.Item;

import java.util.List;

public class RecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> items;

    public void setItems(List<Item> items){
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0){
            return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item,parent, false));
        }else {
            return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item,parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (items.get(position).getItemType() == 0){
//            ((HeaderHolder) holder).textView.setText(items.get(position).getMeeting().getRoom());
        }
        else {

        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType() == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected class ReservationHolder extends RecyclerView.ViewHolder {


        public ReservationHolder(@NonNull View itemView) {
            super(itemView);

        }

    }

    protected class HeaderHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public HeaderHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView4);

        }

    }


}
