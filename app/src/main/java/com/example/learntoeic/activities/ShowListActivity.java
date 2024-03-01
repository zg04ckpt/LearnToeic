package com.example.learntoeic.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.learntoeic.R;
import com.example.learntoeic.adapters.WordsAdapter;
import com.example.learntoeic.animations.ZoomAnimation;
import com.example.learntoeic.databinding.ActivityShowListBinding;
import com.example.learntoeic.listeners.IWordListener;
import com.example.learntoeic.models.Unit;
import com.example.learntoeic.models.Word;
import com.example.learntoeic.utilities.Const;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ShowListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener, IWordListener {
    private ActivityShowListBinding binding;
    private WordsAdapter adapter;
    private List<Word> words;
    private Unit unit;
    private FirebaseFirestore db;
    private Boolean isDeleteStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
        setWordListener();
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        unit = (Unit)getIntent().getSerializableExtra(Const.KEY_UNIT);
        binding.textUnitName.setText(unit.name);
        words = new ArrayList<>();

        adapter = new WordsAdapter(words, getApplicationContext(), this);
        binding.rvWords.setAdapter(adapter);
        binding.rvWords.setVisibility(View.VISIBLE);
        isDeleteStatus = false;
    }

    private void setListeners() {
        binding.icBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.fabAddWord.setOnClickListener(v -> {
            if(isDeleteStatus) cancelDeleteWordStatus();
            else setAddWordLayoutVisibility(true);
        });

        binding.buttonClose.setOnClickListener(v -> {
            setAddWordLayoutVisibility(false);
        });

        binding.buttonType.setOnClickListener(v -> showPopup());

        binding.buttonAdd.setOnClickListener(v -> {
            if(checkWordValid()) {
                addWord();
            }
        });

        binding.fabRemoveWord.setOnClickListener(l -> {
            if(isDeleteStatus) cancelDeleteWordStatus();
            else deleteWord();
        });

        binding.checkBoxAll.setOnClickListener(l -> {
            if(binding.checkBoxAll.isChecked()) {
                for (Word w:words) w.setFlag(true);
            }
            else {
                for (Word w:words) w.setFlag(false);
            }
            adapter.notifyDataSetChanged();
        });

        binding.fabLearn.setOnClickListener(l -> {
            Intent intent = new Intent(getApplicationContext(), LearnActivity.class);
            intent.putExtra(Const.KEY_UNIT_ID, unit.id);
            startActivity(intent);
        });

        binding.inputWordEnglish.setOnFocusChangeListener((view, l) -> {
            //check spell
        });
    }

    private void setSelectionMode(Boolean active) {
        isDeleteStatus = active;
        for(Word w : words) w.selectStatus = active;
        adapter.notifyDataSetChanged();
    }

    private void cancelDeleteWordStatus() {
        boolean deleted = false;
        for(int i=words.size()-1; i>=0; --i){
            if(words.get(i).flag)
            {
                db.collection(Const.KEY_COLLECTION_WORDS).document(words.get(i).id).delete();
                deleted = true;
            }
        }

        binding.fabAddWord.setImageResource(R.drawable.ic_add);
        binding.fabAddWord.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_4)));

        binding.fabRemoveWord.setImageResource(R.drawable.ic_delete);
        binding.textOrder.setVisibility(View.VISIBLE);
        binding.checkBoxAll.setVisibility(View.GONE);
        for(Word w : words) w.flag = false;
        setSelectionMode(false);

        if(!deleted) showToast("Chưa có từ nào được chọn");
        else showToast("Xóa thành công");
    }

    private void deleteWord() {
        binding.fabRemoveWord.setImageResource(R.drawable.ic_confirm);
        binding.textOrder.setVisibility(View.GONE);
        binding.checkBoxAll.setVisibility(View.VISIBLE);

        binding.fabAddWord.setImageResource(R.drawable.ic_close);
        binding.fabAddWord.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_red)));

        setSelectionMode(true);
    }

    private void addWord() {
        String en = binding.inputWordEnglish.getText().toString().trim();
        String vi = binding.inputWordVietnamese.getText().toString().trim();
        String ty = binding.textType.getText().toString();

        db.collection(Const.KEY_COLLECTION_WORDS)
                .whereEqualTo(Const.KEY_WORD_EN, en)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots.getDocuments().size() > 0) {
                        showToast("Từ đã tồn tại");
                    } else {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(Const.KEY_UNIT_ID, unit.id);
                        data.put(Const.KEY_WORD_EN, en);
                        data.put(Const.KEY_WORD_VN, vi);
                        data.put(Const.KEY_WORD_TYPE, ty);
                        db.collection(Const.KEY_COLLECTION_WORDS).add(data)
                                .addOnSuccessListener(task -> {
                                    showToast("Thêm từ thành công!");
                                    binding.inputWordEnglish.setText(null);
                                    binding.inputWordVietnamese.setText(null);
                                    binding.textType.setText("<none>");
                                    setAddWordLayoutVisibility(false);
                                })
                                .addOnFailureListener(e -> showToast(e.getMessage()));
                    }
                }).addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void setWordListener() {
        db.collection(Const.KEY_COLLECTION_WORDS).addSnapshotListener(wordListener);
    }

    private final EventListener<QuerySnapshot> wordListener = ((value, error) -> {
        if(error != null)
        {
            showToast(error.getMessage());
            return;
        }
        if(value != null)
        {
            for (DocumentChange dc : value.getDocumentChanges()) {
                if(dc.getType() == DocumentChange.Type.ADDED && dc.getDocument().getString(Const.KEY_UNIT_ID).equals(unit.id)) {
                    words.add(new Word(
                            dc.getDocument().getId(),
                        dc.getDocument().getString(Const.KEY_WORD_EN),
                        dc.getDocument().getString(Const.KEY_WORD_VN),
                            dc.getDocument().getString(Const.KEY_WORD_TYPE),
                        false,false));
                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                    words.removeIf(w -> Objects.equals(w.id, dc.getDocument().getId()));
                }
            }
            words.sort(Comparator.comparing((Word w) -> w.en));
            adapter.notifyDataSetChanged();
        }
    });

    private void setAddWordLayoutVisibility(Boolean isVisible) {
        if(isVisible) {
            binding.layoutAddWord.setVisibility(View.VISIBLE);
            ZoomAnimation.animateZoomIn(binding.layoutAddWord);
        } else {
            ZoomAnimation.animateZoomOut(binding.layoutAddWord);
            binding.layoutAddWord.setVisibility(View.GONE);
        }
    }

    private Boolean checkWordValid() {
        String en = binding.inputWordEnglish.getText().toString().trim();
        if(en.isEmpty())
        {
            showToast("Chưa nhập từ tiếng anh");
            return false;
        }
        if(binding.inputWordVietnamese.getText().toString().trim().isEmpty())
        {
            showToast("Chưa nhập nghĩa tiếng việt");
            return false;
        }

        if(binding.textType.getText().toString().equals(getResources().getString(R.string.text_none)))
        {
            showToast("Chưa chọn từ loại");
            return false;
        }
        return true;
    }

    private void showPopup() {
        PopupMenu menu = new PopupMenu(this, binding.llType);
        menu.setOnMenuItemClickListener(this::onMenuItemClick);
        menu.inflate(R.menu.menu_select_word_type);
        menu.show();
    }

    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        binding.textType.setText(item.getTitle().toString());
        binding.textType.setTypeface(null, Typeface.BOLD_ITALIC);
        binding.textType.setTextColor(Color.BLACK);
        binding.layoutAddWord.clearFocus();
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClickListener(Word word) {
        word.setFlag(true);
        deleteWord();
    }

    @Override
    public void onWordCheckboxClick(Boolean isChecked, Word word) {
        word.setFlag(isChecked);
        adapter.notifyDataSetChanged();
    }
}