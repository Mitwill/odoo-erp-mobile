
package com.mitwill.erp.mitwill.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import androidx.annotation.Nullable;
import android.util.Log;
import com.mitwill.erp.App;
import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.OValues;
import com.mitwill.erp.core.orm.fields.OColumn;
import com.mitwill.erp.core.rpc.helper.OArguments;
import com.mitwill.erp.core.rpc.helper.utils.gson.OdooResult;
import com.mitwill.erp.core.rpc.listeners.IOdooResponse;
import com.mitwill.erp.core.rpc.listeners.OdooError;
import com.mitwill.erp.datas.OConstants;
import com.mitwill.erp.mitwill.model.AttachedImages;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageUploadService extends Service {

    private static final String TAG = "ImageUploadService";
    private int order_id=0;
    private String resModel="";
    AttachedImages attachedImages;
    ColorDesignOrder colorDesignOrder;
    SaleOrder saleOrder;
    Boolean isNetwork=false;
    Boolean isSampleOrder=false;
    Boolean isImageAttached = false;
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate:  MyService Created ");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (intent !=null){
            order_id = intent.getIntExtra("order_id", 0);
            isSampleOrder = intent.getBooleanExtra("isSampleOrder",false);
            resModel = intent.getStringExtra("resModelName");
            attachedImages = new AttachedImages(this, null);
            isNetwork = inNetwork();
            saleOrder = new SaleOrder( this, null );
            colorDesignOrder = new ColorDesignOrder(this, null);
            if (inNetwork()) {
                uploadImageAsyncTask uploadImage = new uploadImageAsyncTask();
                uploadImage.execute();
            }
        }

        return Service.START_STICKY;
    }
    public class uploadImageAsyncTask extends AsyncTask<Void, Void, Void> {

        OdooResult odooResult = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: ");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: ");

            //getting the arguments for1 the order
            OArguments arguments= getArguments();
            if (isImageAttached) {
                HashMap<String, Object> data = getData();

                odooResult = attachedImages
                        .getServerDataHelper()
                        .getOdoo()
                        .withRetryPolicy(10000, 3)
                        .callMethod(resModel, OConstants.WS_ADD_ATTACHMENTS, arguments, data);

            }
            Log.d(TAG, "doInBackground:" + odooResult);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: ");
            if (odooResult != null) {
                if (odooResult.containsKey("error")) {
                    Log.d(TAG, "onPostExecute: "+odooResult.containsKey("error"));

                }else{
                    List<Map<String, Object>> resultData = odooResult.getArray("result");
                    for (Map<String, Object> resDate: resultData) {
                        boolean isImageUpdated = updateImageUploded(resDate);
                    }
                }
            }
            SendColorDesignOrderEmailerTask orderEmailerTask = new SendColorDesignOrderEmailerTask();
            orderEmailerTask.execute(order_id);
        }
    }

    public class SendColorDesignOrderEmailerTask extends AsyncTask<Integer, Void, Void> {

        private final String TAG = SendColorDesignOrderEmailerTask.class.getSimpleName();

        private Boolean is_uploaded=false;
        @Override
        protected Void doInBackground( Integer... params ) {

            OArguments arguments = new OArguments();
            arguments.add( params[0] );
            HashMap<String, Object> data = new HashMap<>();
            if (resModel.equals("color.design.order")) {
                colorDesignOrder.getServerDataHelper()
                        .getOdoo()
                        .withRetryPolicy(30000, 0)
                        .callMethod(resModel,
                                OConstants.WS_SEND_COLOR_DESIGN_ORDERS_DETAILS,
                                arguments,
                                data,
                                new IOdooResponse() {

                                    @Override
                                    public void onResponse(OdooResult response) {
                                        Log.d(TAG, "onResponse: " + response);
                                        deleteImageForOrderId(order_id);
                                    }

                                    @Override
                                    public void onError(OdooError error) {
                                        Log.d(TAG, "onError: " + error.getMessage());
                                    }
                                });
            }else{
                if (isSampleOrder) {
                    arguments.add(true);
                }else{
                    arguments.add(false);
                }
                saleOrder.getServerDataHelper()
                    .getOdoo()
                    .withRetryPolicy(30000, 0)
                    .callMethod(resModel,
                            OConstants.WS_SEND_QUOTATION_ORDER_DETAILS,
                            arguments,
                            data,
                            new IOdooResponse() {

                                @Override
                                public void onResponse(OdooResult response) {
                                    Log.d(TAG, "onResponse: " + response);
                                    deleteImageForOrderId(order_id);
                                }

                                @Override
                                public void onError(OdooError error) {
                                    Log.d(TAG, "onError: " + error.getMessage());
                                }
                            });
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: ");
            /*if (is_uploaded) {
                boolean result = deleteImageForOrderId(order_id);
            }*/

        }
    }
    private boolean deleteImageForOrderId(int orderId) {
        boolean result = false;
        List<ODataRow> dataRows = this.attachedImages.select(null, "id = ? and is_uploaded = ? ", new String[]{orderId + "","true"});

        for (ODataRow dataRow : dataRows) {
            this.attachedImages.delete(dataRow.getInt(OColumn.ROW_ID), true);
            String imagePath=dataRow.getString(AttachedImages.IMAGE_URL)+"/"+dataRow.getString(AttachedImages.NAME);
            File file = new File(imagePath);
            if (file.exists()) {
                if (file.delete()) {
                    result=true;
                    Log.d(TAG, "file Deleted :" + imagePath);
                } else {
                    Log.d(TAG, "file not Deleted :" + imagePath);
                }
            }
        }
        return result;
    }
    private HashMap<String, Object> getData() {
        HashMap<String, Object> data = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            Log.d(TAG, "doInBackground:JsonObject " + jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  data;
    }

    private OArguments getArguments() {
        OArguments arguments  = new OArguments();

        //getting the arguments for the orderlines
        List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        if (order_id > 0) {
            for (HashMap<String, Object> data : getImageList(order_id)) {
                JSONObject obj = new JSONObject(data);
                jsonObjects.add(obj);
            }
        }

        JSONArray test = new JSONArray(jsonObjects);
        arguments.add(test);
        return  arguments;
    }

    private boolean updateImageUploded(Map<String, Object> resultData) {

        boolean result = false;
        List<ODataRow> dataRows = attachedImages.select(null, "unique_id = ?", new String[]{resultData.get("unique_id") + ""});

        for (ODataRow dataRow : dataRows) {
            OValues oValues = new OValues();
            oValues.put("is_uploaded", "true");
            result=  attachedImages.update(dataRow.getInt(OColumn.ROW_ID),oValues);
        }
        return result;

    }

    public  ArrayList<HashMap<String, Object>> getImageList(int order_id) {
        Log.d(TAG, "getImageList: ");
        List<ODataRow> imgesListData = attachedImages.select(null,
                "id = ? ",
                new String[]{ order_id+ ""});//local order id

        ArrayList<HashMap<String, Object>> contentValues = new ArrayList<>();
        for (ODataRow dataRow : imgesListData) {
            isImageAttached=true;
            String imageURL=dataRow.getString(AttachedImages.IMAGE_URL);
            Uri url=  Uri.fromFile(new File(imageURL));
            Bitmap bitmapfilename=compressImage(imageURL);
            Log.d(TAG, "getImageList: filename"+bitmapfilename);
            OValues oValues = new OValues();
            oValues.put(AttachedImages.NAME,dataRow.getString(AttachedImages.NAME));
            oValues.put(AttachedImages.IMAGE_ENCODE,getBase64EncodedImage(imageURL,bitmapfilename));
            oValues.put(AttachedImages.RES_ID,dataRow.getString(AttachedImages.ID));
            oValues.put(AttachedImages.RES_MODEL,dataRow.getString(AttachedImages.RES_MODEL));
            oValues.put(AttachedImages.UNIQUE_ID,dataRow.getString(AttachedImages.UNIQUE_ID));
            resModel=dataRow.getString(AttachedImages.RES_MODEL);
            contentValues.add(oValues.toDataRow().getAll());
        }
        return contentValues;

    }
    public Bitmap compressImage(String imageUri) {

//        String filePath = getRealPathFromURI(imageUri);
        String filePath=imageUri;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
//        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;


//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 1280.0f;
        float maxWidth = 960.0f;

        if (actualHeight ==0){
            actualHeight= (int) maxHeight;
        }
        if (actualWidth ==0){
            actualWidth= (int) maxWidth;
        }


        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

//        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            Log.d(TAG, "compressImage: "+exception.getMessage());
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = imageUri;
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        return filename;
        return  scaledBitmap;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
    public String getBase64EncodedImage(String imageURL, Bitmap bitmapfilename) {

        Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(new File(imageURL)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmapfilename.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] byteImage = baos.toByteArray();

        return Base64.encodeToString(byteImage, 0);

    }

    public boolean inNetwork() {
        App app = (App) this.getApplicationContext();
        isNetwork=app.inNetwork();
        return isNetwork; 
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
