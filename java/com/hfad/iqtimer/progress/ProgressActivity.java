package com.hfad.iqtimer.progress;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.hfad.iqtimer.R;


public class ProgressActivity extends AppCompatActivity {


    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

}
}