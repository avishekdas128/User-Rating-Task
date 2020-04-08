package com.example.userratingtask.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.userratingtask.R;
import com.example.userratingtask.models.RatingModel;

import java.util.List;

public class RatingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RatingModel> mList;

    public RatingsAdapter(List<RatingModel> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rating_item, parent, false);
        return new RatingsAdapter.RatingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RatingsViewHolder) holder).bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class RatingsViewHolder extends RecyclerView.ViewHolder {

        TextView ratingValue, dateTime;

        private RatingsViewHolder(@NonNull View itemView) {
            super(itemView);
            ratingValue = itemView.findViewById(R.id.rating_value);
            dateTime = itemView.findViewById(R.id.dateTime);
        }

        void bind(RatingModel model) {
            ratingValue.setText(model.getMinRating() + "-" + model.getMaxRating());
            dateTime.setText(model.getDateTime());
        }
    }
}
