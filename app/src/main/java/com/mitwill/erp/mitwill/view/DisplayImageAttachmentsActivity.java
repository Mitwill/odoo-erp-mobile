package com.mitwill.erp.mitwill.view;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.core.content.FileProvider;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mitwill.erp.R;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.support.OdooCompatActivity;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.adapter.ImageDataAdapter;
import com.mitwill.erp.mitwill.model.AttachedImages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DisplayImageAttachmentsActivity extends OdooCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DisplayImageAttachmentsActivity.class.getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int CAMERA_REQUEST = 1888;
    private static final int SELECT_IMAGE = 1889;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private Boolean isPermissionEnabled = false;
    public static AttachedImages attachedImages= null;

    RecyclerView recyclerView ;
    private TextView emptyView;
    private Button btnSaveImage = null;
    private Bundle extras;
    private int orderId = 0;
    private Boolean res_model = false;
    private Boolean isImageListEdited = false;
    private ProgressDialog dialog;
    File photoFile;
    ImageDataAdapter adapter;
    AttachedImages images;

    ArrayList<HashMap<String,String> > arrImageInfoList =new ArrayList<>();

    private Toolbar myToolbar;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_display_image_attachments );
        myToolbar = findViewById( R.id.my_toolbar);
        myToolbar.setTitleTextColor(getResources().getColor( R.color.colorWhite));
        myToolbar.setTitle("Images" );
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator( R.drawable.ic_arrow_back);

        initViews();
        extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey(OConstants.KEY_ORDER_ID)) {
                this.setOrderId(extras.getInt(OConstants.KEY_ORDER_ID));
            }
            if (extras.containsKey(OConstants.KEY_IS_COLOR_DESIGN_ORDER)){
                this.setRes_Model(extras.getBoolean(OConstants.KEY_IS_COLOR_DESIGN_ORDER));
            }
        }
        loadLocalData();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
    private void loadLocalData() {
        LoadLocalImageData  loadImageData = new LoadLocalImageData();
        loadImageData.execute();
    }
    class LoadLocalImageData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d(TAG, "doInBackground: ");
                List<ODataRow> draftSampleOrders = images.select(null,
                        "res_id = ? ",
                        new String[]{ getOrderId() + ""});
                Log.d(TAG, "loadLocalData: "+draftSampleOrders.size());
                for (ODataRow dataRow : draftSampleOrders) {
                    Log.d(TAG, "loadLocalData: "+dataRow);
                    HashMap<String,String>  hashMap =new HashMap<>();
//                    hashMap.put(AttachedImages.IMAGE_ENCODE, (String) dataRow.get(AttachedImages.IMAGE_ENCODE));
                    hashMap.put(AttachedImages.NAME, (String) dataRow.get(AttachedImages.NAME));
                    hashMap.put(AttachedImages.IMAGE_URL,(String) dataRow.get(AttachedImages.IMAGE_URL));
                    arrImageInfoList.add(hashMap);

                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                            fillImageList();
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: Error"+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (DisplayImageAttachmentsActivity.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                return;
            }
            hideProgressDialog();
        }
    }
    private void initViews() {
        images = new AttachedImages(this, null);
        emptyView = (TextView ) findViewById(R.id.empty_view);
        btnSaveImage= findViewById(R.id.activity_display_image_btn_select_image);
        recyclerView = (RecyclerView) findViewById( R.id.card_recycler_view );
        recyclerView.setHasFixedSize( true );
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager( getApplicationContext(), 2 );
        recyclerView.setLayoutManager( layoutManager );
        adapter = new ImageDataAdapter( getApplicationContext(), arrImageInfoList, imageItemClickListener);
        recyclerView.setAdapter(adapter);
        if (adapter!=null ) {
            recyclerView.setVisibility( View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        btnSaveImage.setOnClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        attachedImages= new AttachedImages(this, null);

        getSupportLoaderManager().initLoader(0, null, this);
    }
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public Boolean  getRes_Model() {
        return res_model;
    }

    public void setRes_Model(Boolean res_model) {
        this.res_model = res_model;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate( R.menu.menu_attachments, menu);
        return true;
    }@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (orderId > 0 &&  isImageListEdited) {
                    if (adapter.getItemCount()>0) {
                        showAlertImageDraft();
                    }else{
                        boolean isDeleted= deleteExistImage(orderId);
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.menu_camera:
                if (arrImageInfoList.size() == 3) {
                    Toast.makeText(this, "You can not select more than 3 images", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (isPermissionEnabled) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
                return true;

            case R.id.menu_gallery:
                if (arrImageInfoList.size() == 3) {
                    Toast.makeText(this, "You can not select more than 3 images", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                    openGallery();
                }
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }

        dialog.setMessage(getString(R.string.title_please_wait));
        dialog.setCancelable(false);
        dialog.show();
    }
    private void hideProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }
    Uri fileProvider=null;
    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri("photo.jpg");
        fileProvider = FileProvider.getUriForFile(this, "com.odoo.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAMERA_REQUEST);
        }

    }
    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            }
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
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
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
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
                        MY_PERMISSIONS_REQUEST_CAMERA);
                Log.d(TAG, "requestPermission showing the dialog: ");

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                Log.d(TAG, "requestPermission: ");
            }
        } else {
            isPermissionEnabled = true;
            openCamera();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = true;
                    openCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:data "+data+"  requestCode:"+requestCode+"   resultCode:"+resultCode);
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: "+new Date().toString());
                new LoadImageDataTask(Uri.fromFile(photoFile)).execute();

            }
            if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    new LoadImageDataTask(data.getData()).execute();
                }
            }
        }else{
            Log.d(TAG, "onActivityResult: canceled");
        }
    }


    private class LoadImageDataTask extends AsyncTask<Void, Void, Bitmap> {

        private Uri imagePath;

        LoadImageDataTask(Uri imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: "+new  Date().toString());
            showProgressDialog();
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                Log.d(TAG, "doInBackground: "+new  Date().toString());
                InputStream imageStream = getContentResolver().openInputStream(imagePath);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                Log.d(TAG, "doInBackground: "+imagePath);
//                getImage( bitmap );
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        fillImageList();
//                    }
//                });
                return bitmap;

            } catch (FileNotFoundException e) {
                Log.d(TAG, "doInBackground: "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute: "+new  Date().toString());
            super.onPostExecute(bitmap);
            HashMap<String,String>  imageHashMap = createImageFile(bitmap);
            updateListItem(imageHashMap);
            hideProgressDialog();
            Log.d(TAG, "onPostExecute: 2"+new  Date().toString());
//            fillImageList();
        }
    }

    public void updateListItem ( HashMap<String, String> imageHashMap) {
        showProgressDialog();
        this.arrImageInfoList.add(imageHashMap);
        recyclerView.setVisibility( View.VISIBLE);
        adapter.notifyDataSetChanged();
        if (adapter!=null && adapter.getItemCount()<=0){
            recyclerView.setVisibility( View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        if( this.arrImageInfoList.size()>0){
            enableOrderButton();
        }else{
            disableOrderButton();
        }
        hideProgressDialog();
    }

    private void getImage(Bitmap photo ) {
        arrImageInfoList.add(createImageFile(photo));
        isImageListEdited=true;
//        fillImageList();
    }

    private HashMap<String, String> createImageFile(Bitmap photo) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp +".png";
        File documentsFile = getApplicationContext().getFilesDir();
        String strPathToUpload = documentsFile.getAbsolutePath() + File.separator +"MitwillImages";
        File dir = new File(strPathToUpload);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(new File(strPathToUpload), imageFileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String imagesEncode= "";

       return handleImageData(imagesEncode,imageFileName,strPathToUpload);
    }

    private HashMap<String, String> handleImageData(String imagesEncode, String imageFileName, String strPathToUpload) {
        HashMap<String,String>  hashMap =new HashMap<>();
        hashMap.put(AttachedImages.NAME,imageFileName);
        hashMap.put(AttachedImages.IMAGE_ENCODE,imagesEncode);
        hashMap.put(AttachedImages.IMAGE_URL,strPathToUpload);
        return hashMap;
    }

    ImageDataAdapter.ImageItemClickListener imageItemClickListener =new ImageDataAdapter.ImageItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            showAlertImageDelete(position);
        }
    };

    private void fillImageList() {

//        adapter = new ImageDataAdapter( getApplicationContext(), arrImageInfoList, imageItemClickListener);
        showProgressDialog();
        recyclerView.setAdapter( adapter );
        if (adapter!=null && adapter.getItemCount()<=0){
            recyclerView.setVisibility( View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        if( this.arrImageInfoList.size()>0){
            enableOrderButton();
        }else{
            disableOrderButton();
        }
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                Log.d(TAG, "onGlobalLayout: "+new  Date().toString());
//                hideProgressDialog();
            }
        });
//        hideProgressDialog();

    }

    private void showAlertImageDelete(final int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage(R.string.sure_to_discard_image)
                .setCancelable(false)
                .setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteImageTask(position).execute();
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
    class DeleteImageTask extends AsyncTask<Integer, Void, Void> {

        int imgPosition;
        public DeleteImageTask(int position) {
            imgPosition=position;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            Log.d("DeleteImageTask", "onPreExecute: ");
        }
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("DeleteImageTask", "doInBackground: ");
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.d("DeleteImageTask", "run: ");
                    arrImageInfoList.remove(imgPosition);
                    isImageListEdited=true;
                    adapter.notifyDataSetChanged();
                    if (arrImageInfoList.size() <=0){
                        recyclerView.setVisibility( View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        disableOrderButton();
                    };
                    Log.d("DeleteImageTask", "run: Finish");
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog();
            Log.d("DeleteImageTask", "onPostExecute: ");

        }
    }
    private void disableOrderButton() {
        btnSaveImage.setBackgroundColor(getResources().getColor(R.color.base_chatter_view_note_background));
        btnSaveImage.setEnabled(false);
    }

    private void enableOrderButton() {
        btnSaveImage.setBackgroundColor(getResources().getColor(R.color.android_green));
        btnSaveImage.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (orderId > 0 &&  isImageListEdited) {
            if (adapter.getItemCount()>0) {
                showAlertImageDraft();
            }else{
                boolean isDeleted= deleteExistImage(orderId);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    private void showAlertImageDraft() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.label_discard)
                .setMessage(R.string.sure_to_discard_image)
                .setCancelable(false)
                .setPositiveButton(R.string.label_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        discardImages(dialog);
                    }
                })
                .setNegativeButton(R.string.save_image, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SaveImageData  saveImageData = new SaveImageData();
                        saveImageData.execute();
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
    @Override
    public void onClick( View v ) {
        Log.d(TAG, "onClick: ");

        int id = v.getId();
        switch (id) {
            case R.id.activity_display_image_btn_select_image:
                SaveImageData  saveImageData = new SaveImageData();
                saveImageData.execute();
                break;
        }
   }
    class SaveImageData extends AsyncTask<Void, Void, Void> {
        boolean isImageSaved=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
                Log.d(TAG, "doInBackground: ");
                isImageSaved= handleSaveButtonImages();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            hideProgressDialog();
            if (isImageSaved) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }else{
                Log.d(TAG, "onPostExecute: ");
            }
        }
    }
    private boolean handleSaveButtonImages() {
        boolean isUploaded=false;
        boolean isDeleted= deleteExistImage(orderId);
        for(HashMap map: arrImageInfoList) {
            String resmodel= "color.design.order";
            if (res_model){
                resmodel= "color.design.order";
            }else {
                resmodel= "sale.order";
            }

            OValues orderImagesValues = new OValues();
            orderImagesValues.put(AttachedImages.ID, 0);
            orderImagesValues.put(AttachedImages.RES_ID, orderId);
            orderImagesValues.put(AttachedImages.IMAGE_URL, map.get(AttachedImages.IMAGE_URL));
//            orderImagesValues.put(AttachedImages.IMAGE_ENCODE, map.get(AttachedImages.IMAGE_ENCODE));
            orderImagesValues.put(AttachedImages.NAME, map.get(AttachedImages.NAME));
            orderImagesValues.put(AttachedImages.UNIQUE_ID,  UUID.randomUUID().toString());
            orderImagesValues.put(AttachedImages.RES_MODEL, resmodel);
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

    private boolean deleteExistImage(int orderId) {
        boolean result = false;
        List<ODataRow> dataRows = images.select(null, "res_id = ?", new String[]{orderId + ""});

        for (ODataRow dataRow : dataRows) {
            this.images.delete(dataRow.getInt(OColumn.ROW_ID), true);
           /* String imagePath=dataRow.getString(AttachedImages.IMAGE_URL)+"/"+dataRow.getString(AttachedImages.NAME);
            File file = new File(imagePath);
            if (file.exists()) {
                if (file.delete()) {
                    result=true;
                    Log.d(TAG, "file Deleted :" + imagePath);
                } else {
                    Log.d(TAG, "file not Deleted :" + imagePath);
                }
            }*/
        }
        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String select = null;
        if (orderId > 0) {
            select = "(" + "id = " + orderId + ")";
        } else {
            select = "( id = -1)";
        }
        return  new CursorLoader(this, images.uri(), null, select, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        adapter.notifyDataSetChanged();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}