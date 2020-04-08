package com.example.userratingtask.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.userratingtask.R;
import com.example.userratingtask.models.RatingModel;
import com.example.userratingtask.util.UserPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button rating, previousRatings;
    private Intent i;
    private List<RatingModel> ratingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rating = findViewById(R.id.rating);
        previousRatings = findViewById(R.id.previous_ratings);
        if (getIntent() != null && getIntent().getIntExtra("maxRating", 0) != 0) {
            rating.setText("Rating (" + getIntent().getIntExtra("minRating", 0) + "-" + getIntent().getIntExtra("maxRating", 0) + ")");
        } else {
            if (UserPreferenceManager.getPreferenceObjectRatings(getApplicationContext()) != null) {
                ratingsList = UserPreferenceManager.getPreferenceObjectRatings(getApplicationContext());
                RatingModel model = ratingsList.get(ratingsList.size() - 1);
                rating.setText("Rating (" + model.getMinRating() + "-" + model.getMaxRating() + ")");
            }
        }
        rating.setOnClickListener(v -> {
            i = new Intent(this, RatingActivity.class);
            startActivity(i);
        });
        previousRatings.setOnClickListener(v -> {
            i = new Intent(this, PreviousRatingActivity.class);
            startActivity(i);
        });
    }
}
