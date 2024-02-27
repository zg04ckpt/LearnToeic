package com.example.learntoeic.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learntoeic.databinding.ItemContainerUnitBinding;
import com.example.learntoeic.listeners.IUnitListener;
import com.example.learntoeic.models.Unit;

import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> {
    private final List<Unit> units;
    private final IUnitListener unitListener;

    public UnitAdapter(List<Unit> unitNames, IUnitListener unitListener) {
        this.units = unitNames;
        this.unitListener = unitListener;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UnitViewHolder(ItemContainerUnitBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        holder.setData(units.get(position));
    }

    @Override
    public int getItemCount() {
        return units.size();
    }

    class UnitViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUnitBinding _binding;

        public UnitViewHolder(ItemContainerUnitBinding binding) {
            super(binding.getRoot());
            _binding = binding;
        }

        public void setData(Unit unit) {
            _binding.textUnitName.setText(unit.name);
            _binding.getRoot().setOnClickListener(l -> unitListener.onUnitClicked(unit));
            _binding.getRoot().setOnLongClickListener(l -> {
                unitListener.onUnitLongClicked(unit);
                return true;
            });
        }
    }
}
