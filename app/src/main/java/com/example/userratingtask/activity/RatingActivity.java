package com.example.userratingtask.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.userratingtask.R;
import com.example.userratingtask.models.RatingModel;
import com.example.userratingtask.util.RangeSeekBar;
import com.example.userratingtask.util.UserPreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RatingActivity extends AppCompatActivity {

    private Button submit;
    private RangeSeekBar seekBar;
    private List<RatingModel> ratingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        submit = findViewById(R.id.submit);
        seekBar = findViewById(R.id.seekbar);
        submit.setOnClickListener(v -> {
            if (seekBar.getSelectedMaxValue().equals(seekBar.getSelectedMinValue())) {
                Toast.makeText(this, "Maximum and minimum range cannot be same!", Toast.LENGTH_SHORT).show();
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aa", Locale.getDefault());
            String currentDateTime = sdf.format(new Date());
            RatingModel rating = new RatingModel(seekBar.getSelectedMinValue(), seekBar.getSelectedMaxValue(), currentDateTime);
            if (UserPreferenceManager.getPreferenceObjectRatings(getApplicationContext()) == null) {
                ratingsList.add(rating);
            } else {
                ratingsList = UserPreferenceManager.getPreferenceObjectRatings(getApplicationContext());
                ratingsList.add(rating);
            }
            UserPreferenceManager.setPreferenceObjectRatings(getApplicationContext(), ratingsList);
            Toast.makeText(this, "Rating (" + rating.getMinRating() + "-" + rating.getMaxRating() + ") Submitted!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("minRating", seekBar.getSelectedMinValue());
            i.putExtra("maxRating", seekBar.getSelectedMaxValue());
            startActivity(i);
            finishAffinity();
        });
    }
}
