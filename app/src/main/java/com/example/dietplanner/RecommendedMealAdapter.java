package com.example.dietplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dietplanner.data.RecommendedItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecommendedMealAdapter extends RecyclerView.Adapter<RecommendedMealAdapter.ViewHolder> {
    private List<RecommendedItem> items = new ArrayList<>();
    private OnItemChangedListener listener;

    public interface OnItemChangedListener {
        void onItemChanged();
    }

    public RecommendedMealAdapter(OnItemChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_recommended_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedItem item = items.get(position);
        holder.textViewName.setText(item.name);
        holder.textViewQuantity.setText(String.format(Locale.US, "%dg", item.quantity));
        holder.textViewCalories.setText(String.format(Locale.US, "%.0f kcal", item.calories));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateRecommendation(List<RecommendedItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewQuantity;
        TextView textViewCalories;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewRecommendedName);
            textViewQuantity = itemView.findViewById(R.id.textViewRecommendedQuantity);
            textViewCalories = itemView.findViewById(R.id.textViewRecommendedCalories);
        }
    }
}