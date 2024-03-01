package com.example.learntoeic.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.learntoeic.R;
import com.example.learntoeic.animations.ZoomAnimation;
import com.example.learntoeic.databinding.ActivityLearnBinding;
import com.example.learntoeic.models.Word;
import com.example.learntoeic.utilities.Const;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearnActivity extends AppCompatActivity {
    private ActivityLearnBinding binding;
    private List<Word> words;
    private int correct = 0;
    private int wrong = 0;
    private int now = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLearnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
        processData();
    }

    private void init() {
        words = new ArrayList<>();
        update();
    }

    private void setListeners() {
        binding.btnEnd.setOnClickListener(l -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnNext.setOnClickListener(l -> {
            String en = binding.inputWordEnglish.getText().toString().trim();
            if(en.isEmpty()) {
                showToast("Bạn chưa nhập từ tiếng anh");
                return;
            }
            if(en.equalsIgnoreCase(words.get(now).en))
            {
                correct++;
                if(now+1 == words.size()) showResult();
                else {
                    now++;
                    setData(words.get(now));
                }
            } else {
                wrong++;
                binding.textCorrect.setText(words.get(now).en);
                binding.textWrong.setText(binding.inputWordEnglish.getText().toString());

                binding.viewCover.setVisibility(View.VISIBLE);
                binding.llWrongWord.setVisibility(View.VISIBLE);
                ZoomAnimation.animateZoomIn(binding.llWrongWord);
            }
            binding.inputWordEnglish.setText(null);
            update();
        });

        binding.btnCloseResult.setOnClickListener(l -> {
            if(now+1 == words.size()) showResult();
            else {
                now++;
                setData(words.get(now));
            }
            binding.viewCover.setVisibility(View.GONE);
            binding.llWrongWord.setVisibility(View.GONE);
        });
    }

    private void showResult() {

    }

    private void isLoading(Boolean loading) {
        if(loading) {
            binding.viewCover.setVisibility(View.VISIBLE);
            binding.progressBarLoading.setVisibility(View.VISIBLE);
        } else {
            binding.viewCover.setVisibility(View.GONE);
            binding.progressBarLoading.setVisibility(View.GONE);
        }
    }

    private void update() {
        binding.textCorrectCounter.setText((String.valueOf(correct)));
        binding.textWrongCounter.setText((String.valueOf(wrong)));
        binding.progressBar.setProgress(now);
    }

    private void setData(Word word) {
        binding.textMean.setText(word.vi);
        binding.textType.setText(word.getTypeString());
    }

    private void processData() {
        isLoading(true);
        String unitId = getIntent().getStringExtra(Const.KEY_UNIT_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Const.KEY_COLLECTION_WORDS)
                        .whereEqualTo(Const.KEY_UNIT_ID, unitId).get()
                        .addOnSuccessListener(qs -> {
                            isLoading(false);
                            if(qs.getDocuments().size()==0) showToast("Không có từ nào");
                            else {
                                for(DocumentSnapshot ds : qs.getDocuments()) {
                                    Word word = new Word(
                                            ds.getString(Const.KEY_WORD_EN),
                                            ds.getString(Const.KEY_WORD_VN),
                                            ds.getString(Const.KEY_WORD_TYPE)
                                    );
                                    words.add(word);
                                }
                                binding.progressBar.setMax(words.size());
                                Collections.shuffle(words);
                                setData(words.get(0));
                            }
                        })
                .addOnFailureListener(e -> {
                    showToast(e.getMessage());
                    getOnBackPressedDispatcher().onBackPressed();
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}