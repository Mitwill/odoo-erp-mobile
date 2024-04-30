package com.mitwill.erp.mitwill.view;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.mitwill.erp.R;
import java.io.File;
import java.util.ArrayList;

public class ShowImageActivity extends AppCompatActivity {
    SubsamplingScaleImageView imageView;
    ArrayList<File> photos;
    private Toolbar myToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        Bundle bundle = getIntent().getExtras();
        String imagePath= bundle.getString("Filename");

        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        photos= (ArrayList<File>)getIntent().getSerializableExtra("photos");
        imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);

        imageView.setImage(ImageSource.uri(imagePath));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        return true;
    }
}
