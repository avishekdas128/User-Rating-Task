package com.example.userratingtask.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.userratingtask.R;
import com.example.userratingtask.adapter.RatingsAdapter;
import com.example.userratingtask.models.RatingModel;
import com.example.userratingtask.util.UserPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class PreviousRatingActivity extends AppCompatActivity {

    private RecyclerView rvRatings;
    private TextView noRatingsText;
    private List<RatingModel> ratingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_rating);
        rvRatings = findViewById(R.id.rvRatings);
        noRatingsText = findViewById(R.id.noRatingText);
        if (UserPreferenceManager.getPreferenceObjectRatings(getApplicationContext()) == null) {
            noRatingsText.setVisibility(View.VISIBLE);
            rvRatings.setVisibility(View.GONE);
        } else {
            ratingsList = UserPreferenceManager.getPreferenceObjectRatings(getApplicationContext());
            RatingsAdapter adapter = new RatingsAdapter(ratingsList);
            rvRatings.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            rvRatings.setAdapter(adapter);
        }
    }
}
