package com.example.learntoeic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learntoeic.R;
import com.example.learntoeic.databinding.ItemContainerWordBinding;
import com.example.learntoeic.listeners.IWordListener;
import com.example.learntoeic.models.Word;

import java.util.List;

public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.WordViewHolder> {
    private final List<Word> words;
    private final Context context;
    private final IWordListener listener;
    public WordsAdapter(List<Word> words, Context context, IWordListener listener) {
        this.words = words;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WordViewHolder viewHolder = new WordViewHolder(ItemContainerWordBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        holder.setData(words.get(position), position+1);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder{
        private ItemContainerWordBinding binding;

        public WordViewHolder(ItemContainerWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void setData(Word word, int order) {
            if(word.flag) {
                binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.color_2));
            } else {
                binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.color_1));
            }

            if(word.selectStatus) {
                binding.textOrder.setVisibility(View.GONE);
                binding.checkBox.setVisibility(View.VISIBLE);
                binding.checkBox.setChecked(word.flag);

                binding.getRoot().setOnLongClickListener(null);
            } else {
                binding.textOrder.setVisibility(View.VISIBLE);
                binding.checkBox.setVisibility(View.GONE);
                binding.textOrder.setText(String.valueOf(order));

                binding.getRoot().setOnLongClickListener(l -> {
                    listener.onLongClickListener(word);
                    return true;
                });
            }
            binding.textWordEnglish.setText(word.en);
            binding.textWordVietnamese.setText(word.vi);
            binding.textType.setText(word.getTypeString());

            binding.checkBox.setOnClickListener(l -> listener.onWordCheckboxClick(binding.checkBox.isChecked(), word));
        }
    }
}
