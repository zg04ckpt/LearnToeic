package com.example.learntoeic.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.learntoeic.adapters.UnitAdapter;
import com.example.learntoeic.animations.ZoomAnimation;
import com.example.learntoeic.databinding.ActivityMainBinding;
import com.example.learntoeic.listeners.IUnitListener;
import com.example.learntoeic.models.Unit;
import com.example.learntoeic.utilities.Const;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity implements IUnitListener {
    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private UnitAdapter adapter;
    List<Unit> units;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();
        setContentView(binding.getRoot());
        setListeners();
        init();
        setUnitListener();
    }

    private void setUnitListener() {
        db.collection(Const.KEY_COLLECTION_UNITS).addSnapshotListener(unitListener);
    }

    private final EventListener<QuerySnapshot> unitListener = ((value, error) -> {
        if(error != null)
        {
            showToast(error.getMessage());
            return;
        }
        if(value != null) {
            for (DocumentChange dc : value.getDocumentChanges()) {
                if(dc.getType() == DocumentChange.Type.ADDED) {
                    units.add(new Unit(
                            dc.getDocument().getId(),
                            dc.getDocument().getString(Const.KEY_UNIT_NAME),
                            dc.getDocument().getDate(Const.KEY_UNIT_CREATED_TIME)));
                } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                    String id = dc.getDocument().getId();
                    for(int i=0; i<units.size(); ++i)
                    {
                        if(units.get(i).id.equals(id))
                        {
                            Unit updatedUnit = new Unit(
                                id,
                                dc.getDocument().getString(Const.KEY_UNIT_NAME),
                                dc.getDocument().getDate(Const.KEY_UNIT_CREATED_TIME)
                            );
                            units.set(i, updatedUnit);
                            break;
                        }
                    }
                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                    units.removeIf(u -> u.id.equals(dc.getDocument().getId()));
                }
            }
            units.sort(Comparator.comparing(u -> u.createdDate));
            adapter.notifyDataSetChanged();
            setIsLoading(false);
        }
    });

    private void init() {
        setIsLoading(true);
        units = new ArrayList<>();
        adapter = new UnitAdapter(units, this);
        binding.rvUnits.setAdapter(adapter);
    }

    private void setListeners() {
        binding.fabAddUnit.setOnClickListener(v -> {
            binding.layoutAddUnit.setVisibility(View.VISIBLE);
            ZoomAnimation.animateZoomIn(binding.layoutAddUnit);
        });
        binding.buttonClose.setOnClickListener(v -> {
            binding.layoutAddUnit.setVisibility(View.GONE);
            ZoomAnimation.animateZoomOut(binding.layoutAddUnit);
        });
        binding.buttonAdd.setOnClickListener(v -> addUnit());
    }

    private void addUnit() {
        if(binding.inputUnitName.getText().toString().isEmpty()) return;
        HashMap<String, Object> units = new HashMap<>();
        units.put(Const.KEY_UNIT_NAME, binding.inputUnitName.getText().toString());
        units.put(Const.KEY_UNIT_CREATED_TIME, new Date());
        db.collection(Const.KEY_COLLECTION_UNITS).add(units)
                .addOnSuccessListener(task -> {
                    binding.layoutAddUnit.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void setIsLoading(Boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvUnits.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.rvUnits.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnitClicked(Unit unit) {
        Intent intent = new Intent(getApplicationContext(), ShowListActivity.class);
        intent.putExtra(Const.KEY_UNIT, unit);
        startActivity(intent);
    }

    @Override
    public void onUnitLongClicked(Unit unit) {
        binding.layoutUpdateUnit.setVisibility(View.VISIBLE);

        ZoomAnimation.animateZoomIn(binding.layoutUpdateUnit);
        binding.inputUnitNameUpdate.setText(unit.name);
        binding.buttonCloseUpdate.setOnClickListener(l -> binding.layoutUpdateUnit.setVisibility(View.GONE));
        binding.buttonUpdateUnit.setOnClickListener(l -> {
            String newName = binding.inputUnitNameUpdate.getText().toString().trim();
            if(newName.isEmpty()) showToast("Tên mới rỗng");
            else
            {
                updateUnit(unit.id, newName);
                binding.layoutUpdateUnit.setVisibility(View.GONE);
            }
        });
        binding.buttonDeleteUnit.setOnClickListener(l -> showAlert("Xác nhận muốn xóa unit", unit.id));
    }

    private void updateUnit(String id, String name) {
        HashMap<String, Object> upd = new HashMap<>();
        upd.put(Const.KEY_UNIT_NAME, name);
        db.collection(Const.KEY_COLLECTION_UNITS).document(id)
                .update(upd)
                .addOnSuccessListener(t -> showToast("Cập nhật thành công"))
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void showAlert(String message, String id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            deleteUnit(id);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            binding.layoutUpdateUnit.setVisibility(View.GONE);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteUnit(String id) {
        db.collection(Const.KEY_COLLECTION_UNITS).document(id).delete()
                .addOnFailureListener(e -> showToast(e.getMessage()));

        db.collection(Const.KEY_COLLECTION_WORDS)
                .whereEqualTo(Const.KEY_UNIT_ID, id)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        for (DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()) {
                            db.collection(Const.KEY_COLLECTION_WORDS).document(ds.getId()).delete();
                        }
                        showToast("Xóa unit thành công");
                        binding.layoutUpdateUnit.setVisibility(View.GONE);
                    } catch (Exception e) {
                        showToast(e.getMessage());
                    }
                });

    }
}