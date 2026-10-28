package com.example.quantumrealm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnboardingSlideAdapter extends RecyclerView.Adapter<OnboardingSlideAdapter.Holder> {

    public static final class Slide {
        public final String emoji;
        public final String title;
        public final String body;

        public Slide(String emoji, String title, String body) {
            this.emoji = emoji;
            this.title = title;
            this.body = body;
        }
    }

    private final List<Slide> slides;

    public OnboardingSlideAdapter(List<Slide> slides) {
        this.slides = slides;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Slide slide = slides.get(position);
        holder.emoji.setText(slide.emoji);
        holder.title.setText(slide.title);
        holder.body.setText(slide.body);
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView emoji;
        final TextView title;
        final TextView body;

        Holder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.onboardingEmoji);
            title = itemView.findViewById(R.id.onboardingTitle);
            body = itemView.findViewById(R.id.onboardingBody);
        }
    }
}
