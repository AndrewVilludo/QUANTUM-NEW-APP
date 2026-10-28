package com.example.quantumrealm;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import androidx.core.content.ContextCompat;

public final class LessonRichText {

    private LessonRichText() {
    }

    public static CharSequence format(Context context, String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String normalized = normalizeBullets(raw.replace("\r\n", "\n"));
        SpannableStringBuilder out = new SpannableStringBuilder();
        int i = 0;
        while (i < normalized.length()) {
            if (normalized.startsWith("**", i)) {
                int end = normalized.indexOf("**", i + 2);
                if (end > i) {
                    String inner = normalized.substring(i + 2, end);
                    int start = out.length();
                    out.append(inner);
                    out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 2;
                    continue;
                }
            }
            if (normalized.charAt(i) == '`') {
                int end = normalized.indexOf('`', i + 1);
                if (end > i) {
                    String inner = normalized.substring(i + 1, end);
                    int start = out.length();
                    out.append(inner);
                    int bg = ContextCompat.getColor(context, R.color.game_accent_soft);
                    out.setSpan(new BackgroundColorSpan(bg), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    out.setSpan(new TypefaceSpan("monospace"), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 1;
                    continue;
                }
            }
            out.append(normalized.charAt(i));
            i++;
        }
        return out;
    }

    private static String normalizeBullets(String text) {
        StringBuilder bullets = new StringBuilder();
        for (String line : text.split("\n", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
                bullets.append("• ").append(trimmed.substring(2).trim()).append('\n');
            } else {
                bullets.append(line).append('\n');
            }
        }
        if (bullets.length() > 0) {
            bullets.setLength(bullets.length() - 1);
        }
        return bullets.toString();
    }
}
