package com.mitwill.erp.mitwill.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mitwill.erp.R;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.adapter.ImagesAdapter;
import com.mitwill.erp.mitwill.model.AttachedImages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pl.aprilapps.easyphotopicker.EasyImage;

public class ImageAttachmentsActivity extends AppCompatActivity implements View.OnClickListener, EasyImage.Callbacks {
    private static final String TAG = ImageAttachmentsActivity.class.getSimpleName();
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final int PERMISSIONS_REQUEST_CAMERA = 2;

    protected RecyclerView recyclerView;
    private ImagesAdapter imagesAdapter;
    private Button btnSaveImage = null;
    private TextView emptyView;
    private Toolbar myToolbar;

    AttachedImages images;
    private ArrayList<File> photos = new ArrayList<>();
    private int orderId = 0;
    private String res_model = "";
    private Boolean isImageListEdited = false;
    private Boolean isPermissionEnabled = false;
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_attachments);

        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        myToolbar.setTitle("Images");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey(OConstants.KEY_ORDER_ID)) {
                this.setOrderId(extras.getInt(OConstants.KEY_ORDER_ID));
            }
            if (extras.containsKey(OConstants.KEY_RES_MODEL_NAME)){
                this.setRes_Model(extras.getString(OConstants.KEY_RES_MODEL_NAME));
            }
        }

        initview();
        loadLocalImages();
    }

    private void loadLocalImages() {
        List<ODataRow> draftSampleOrders = images.select(null,
                "res_id = ? and res_model = ? ",
                new String[]{getOrderId()  + "", res_model + ""});

        for (ODataRow dataRow : draftSampleOrders) {
            photos.add(new File(dataRow.getString("images_url")));
        }
        imagesAdapter.notifyDataSetChanged();
        HandleEnableButton();
    }

    private void initview() {
        images = new AttachedImages(this, null);
        emptyView = (TextView ) findViewById(R.id.empty_view);
        btnSaveImage = findViewById(R.id.activity_display_image_btn_select_image);
        btnSaveImage.setOnClickListener(this);
        recyclerView = findViewById(R.id.recyclerView);
        imagesAdapter = new ImagesAdapter(this, photos,imageItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imagesAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void handleRecyclerViewVisibility() {
        if (photos.size() <= 0){
            recyclerView.setVisibility( View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    ImagesAdapter.ImageItemClickListener imageItemClickListener =new ImagesAdapter.ImageItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            switch (v.getId())
            {
                case R.id.imageView:
                    Bundle data = new Bundle();
                    data.putString("Filename",String.valueOf(photos.get(position).getPath()));
                    Intent intent = new Intent(ImageAttachmentsActivity.this, ShowImageActivity.class);
                    intent.putExtras(data);
                    intent.putExtra("photos", photos);
                    startActivity(intent);
                    break;
                case R.id.img_delete:
                    showAlertImageDelete(position);
                    break;
            }

        }
    };
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public String getRes_Model() {
        return res_model;
    }

    public void setRes_Model(String res_model) {
        this.res_model = res_model;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_attachments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (orderId > 0 &&  isImageListEdited) {
                    showAlertImageDraft();
                } else {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.menu_camera:

                if (recyclerView.getAdapter().getItemCount() == 3) {
                    Toast.makeText(this, "You can not select more than 3 images", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (isPermissionEnabled) {
                    EasyImage.openCamera(this, 0);
                } else {
                    requestCameraPermission();
                }

                return true;

            case R.id.menu_gallery:
                if (recyclerView.getAdapter().getItemCount() == 3) {
                    Toast.makeText(this, "You can not select more than 3 images", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                    EasyImage.openGallery(this, 0);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE },
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                 } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            }
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE },
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            }
            else {
                return true;
            }

        } else {
            return true;
        }
    }

    private void requestCameraPermission() {

        if ( ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
                Log.d(TAG, "requestPermission showing the dialog: ");

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
                Log.d(TAG, "requestPermission: ");
            }
        } else {
            isPermissionEnabled = true;
            EasyImage.openCamera(this, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = true;
                    EasyImage.openCamera(this, 0);
                } else {
                    Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EasyImage.openGallery(this, 0);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
    @Override
    public void onBackPressed() {
        if (orderId > 0 &&  isImageListEdited) {
            showAlertImageDraft();
        } else {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    private void showAlertImageDelete(final int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_image)
                .setMessage(R.string.sure_to_discard_image)
                .setCancelable(false)
                .setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        photos.remove(position);
                        isImageListEdited=true;
                        imagesAdapter.notifyDataSetChanged();
                        HandleEnableButton();
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }
    private void showAlertImageDraft() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.want_to_save_changes)
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        boolean isSaved=handleSaveButtonImages();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .create();
        alertDialog.show();
    }
    private void discardImages(DialogInterface dialog) {
        boolean isDeleted= deleteExistImage(orderId);
        dialog.dismiss();

        Intent intent = new Intent();
        intent.putExtra(OConstants.KEY_DELETE_ORDER, orderId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean deleteExistImage(int orderId) {
        boolean result = false;
        List<ODataRow> dataRows = images.select(null, "res_id = ?", new String[]{orderId + ""});

        for (ODataRow dataRow : dataRows) {
            this.images.delete(dataRow.getInt(OColumn.ROW_ID), true);
        }
        return result;
    }
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private File saveToInternalStorage(File sourceImage) throws IOException {
        Log.d(TAG, "saveToInternalStorage: ");
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp +".png";
        // Create imageDir
        File mypath = new File(directory, fileName);
        copy(sourceImage, mypath);
        return mypath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, this);
    }

    @Override
    public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
        Log.d(TAG, "onImagePickerError: ");
    }


    public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
        Log.d(TAG, "onImagePicked: ");
        try {
            File path = saveToInternalStorage(imageFile);
            photos.add(path);
            imagesAdapter.notifyDataSetChanged();
            isImageListEdited=true;
            HandleEnableButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void HandleEnableButton() {
        if (photos.size() > 0) {
            emptyView.setVisibility(View.GONE);
            btnSaveImage.setBackgroundColor(getResources().getColor(R.color.android_green));
            btnSaveImage.setEnabled(true);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            btnSaveImage.setBackgroundColor(getResources().getColor(R.color.base_chatter_view_note_background));
            btnSaveImage.setEnabled(false);
        }
    }

    @Override
    public void onCanceled(EasyImage.ImageSource source, int type) {
        Log.d(TAG, "onCanceled: ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_display_image_btn_select_image:
                Toast.makeText(this,getString(R.string.images_saved), Toast.LENGTH_SHORT).show();
                boolean isSaved=handleSaveButtonImages();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
    private boolean handleSaveButtonImages() {
        boolean isUploaded=false;
        boolean isDeleted= deleteExistImage(orderId);
        for (File file:photos) {
            String filename=file.getName();
            OValues orderImagesValues = new OValues();
            orderImagesValues.put(AttachedImages.ID, 0);
            orderImagesValues.put(AttachedImages.RES_ID, orderId);
            orderImagesValues.put(AttachedImages.IMAGE_URL, file.getPath());
//            orderImagesValues.put(AttachedImages.IMAGE_ENCODE, map.get(AttachedImages.IMAGE_ENCODE));
            orderImagesValues.put(AttachedImages.NAME, file.getName());
            orderImagesValues.put(AttachedImages.UNIQUE_ID,  UUID.randomUUID().toString());
            orderImagesValues.put(AttachedImages.RES_MODEL, res_model);
            orderImagesValues.put(AttachedImages.IS_UPLOADED, "false");
            orderImagesValues.put(AttachedImages.NUMBER_OF_TRY, 0);

            AttachedImages attachedImages = new AttachedImages(this, null);
            int images = attachedImages.insert(orderImagesValues);
            if(images>=0){
                isUploaded= true;
            }
        }
        return isUploaded;
    }
}
