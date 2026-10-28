package com.example.quantumrealm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

public class LessonSlidePagerAdapter extends RecyclerView.Adapter<LessonSlidePagerAdapter.SlideViewHolder> {

    private final JSONArray slides;

    public LessonSlidePagerAdapter(JSONArray slides) {
        this.slides = slides != null ? slides : new JSONArray();
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        try {
            JSONObject slide = slides.getJSONObject(position);
            int step = slide.optInt("step", position + 1);
            int total = slide.optInt("total", slides.length());
            String emoji = slide.optString("emoji", "📖");
            String title = slide.optString("title", "Slide " + step);
            String content = slide.optString("content", "");

            holder.emoji.setText(emoji);
            holder.stepLabel.setText(holder.itemView.getContext().getString(
                    R.string.lesson_step_of, step, total));
            holder.title.setText(title);
            holder.content.setText(LessonRichText.format(holder.itemView.getContext(), content));
        } catch (Exception ignored) {
            holder.title.setText("");
            holder.content.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return slides.length();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        final TextView emoji;
        final TextView stepLabel;
        final TextView title;
        final TextView content;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.slideEmoji);
            stepLabel = itemView.findViewById(R.id.slideStepLabel);
            title = itemView.findViewById(R.id.slideTitle);
            content = itemView.findViewById(R.id.slideContent);
        }
    }
}
