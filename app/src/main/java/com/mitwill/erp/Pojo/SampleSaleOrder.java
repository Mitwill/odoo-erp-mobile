package com.mitwill.erp.Pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mitwill.erp.addons.saleorderlines.models.SaleOrderLine;
import com.mitwill.erp.addons.saleorder.models.SaleOrder;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.fields.OColumn;

import java.util.Map;

/**
 * Created by st29 on 06/03/18.
 */

public class SampleSaleOrder implements Parcelable {

    public static final String ID = "id";
    public static final String _ID = "_id";
    public static final String PARTNER_NAME = "partnerName";
    public static final String PARTNER_ID = "partner_id";
    public static final String CREATE_DATE = "create_date";
    public static final String NAME = "name";
    public static final String QUANTITY = "quantity";
    public static final String WRITE_DATE = "write_date";
    private static final String GUID = "guid";
    public static final String IS_DESIGN_ORDER = "isDesignOrder";
    private static final String TAG = SampleSaleOrder.class.getSimpleName();


    private int _id = -1;

    private String partnerName = "";

    private int id = 0;

    private int partner_id;

    private String create_date = "";

    private String name = "";

    private float quantity = 0;

    private String write_date = "";

    private String _create_date = "";

    private String guid = "";

    private Boolean isDesignOrder = false;

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName( String partnerName ) {
        this.partnerName = partnerName;
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public int getPartner_id() {
        return partner_id;
    }

    public void setPartner_id( int partner_id ) {
        this.partner_id = partner_id;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date( String create_date ) {
        this.create_date = create_date;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity( float quantity ) {
        this.quantity = quantity;
    }

    public String getWrite_date() {
        return write_date;
    }

    public void setWrite_date( String write_date ) {
        this.write_date = write_date;
    }

    @Override
    public String toString() {
        return "ClassPojo [partnerName = "+partnerName+", id = "+id+", partner_id = "+partner_id+", create_date = "+create_date+", name = "+name+", quantity = "+quantity+", write_date = "+write_date+"]";
    }

    public int get_id() {
        return _id;
    }

    public void set_id( int _id ) {
        this._id = _id;
    }

    public boolean isDraft() {
        if ( this.getId() == 0 ) {
            return true;
        } else {
            return false;
        }
    }

    public String get_create_date() {
        return _create_date;
    }

    public void set_create_date( String _create_date ) {
        this._create_date = _create_date;
    }

    private SampleSaleOrder() {
    }

    public SampleSaleOrder( Parcel in ) {
        this._id = in.readInt();
        this.id = in.readInt();
        this.partnerName = in.readString();
        this.partner_id = in.readInt();
        this.create_date = in.readString();
        this.write_date = in.readString();
        this.name = in.readString();
        this.quantity = in.readFloat();
        this.isDesignOrder = in.readByte() != 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags ) {
        dest.writeInt( this._id );
        dest.writeInt( this.id );
        dest.writeString( this.partnerName );
        dest.writeInt( this.partner_id );
        dest.writeString( this.create_date );
        dest.writeString( this.write_date );
        dest.writeString( this.name );
        dest.writeFloat( this.quantity );
        dest.writeByte((byte) (this.isDesignOrder ? 1 : 0));
    }

    /*
    *   Get the SampleSaleOrder Model for the local SaleOrder Object
    * */
    public SampleSaleOrder( ODataRow dataRow, SaleOrderLine saleOrderLine ) {
        this.set_id( dataRow.getInt( SaleOrder._ID ) );
        this.setId( dataRow.getInt( SaleOrder.ID ) );
        this.setCreate_date( dataRow.get( SaleOrder.CREATE_DATE ).toString() );
        this.setWrite_date( dataRow.get( SaleOrder.WRITE_DATE ).toString() );
        this.setName( dataRow.getString( SaleOrder.NAME ) );
        this.set_create_date( dataRow.get( SaleOrder._WRITE_DATE ).toString() );

        ODataRow partnerRow = dataRow.getM2ORecord( SaleOrder.PARTNER_ID ).browse();
        this.setPartner_id( partnerRow.getInt( OColumn.ROW_ID ) );
        this.setPartnerName( partnerRow.get( ResPartner.NAME ).toString() );
        this.setQuantity( saleOrderLine.getQuantity( dataRow.getInt( SaleOrder._ID ) ) );
        this.setGuid( dataRow.getString( SaleOrder.GUID ) );
        this.setDesignOrder( dataRow.getBoolean(SaleOrder.IS_DESIGN_ORDER));
    }

    /*
    *   Parse the data from the server for the sample sale order
    * */
    public SampleSaleOrder( Map<String, Object> mapSampleOrder ) {

        this.setId( ( Double.valueOf( (double)mapSampleOrder.get( SampleSaleOrder.ID ) ).intValue() ) );
        this.setCreate_date( (String)mapSampleOrder.get( SampleSaleOrder.CREATE_DATE ) );

        if ( mapSampleOrder.get( SampleSaleOrder.WRITE_DATE ) != null ) {
            this.setWrite_date( mapSampleOrder.get( SampleSaleOrder.WRITE_DATE ).toString() );
        }

        if ( mapSampleOrder.get( SampleSaleOrder.NAME ) != null ) {
            this.setName( (String)mapSampleOrder.get( SampleSaleOrder.NAME ) );
        }

        if ( mapSampleOrder.get( SampleSaleOrder.GUID ) != null ) {
            this.setGuid( mapSampleOrder.get( SampleSaleOrder.GUID ).toString() );
        }
        if ( mapSampleOrder.get( SampleSaleOrder.PARTNER_ID ) instanceof Double ) {
            this.setPartner_id( Double.valueOf( (double)mapSampleOrder.get( SampleSaleOrder.PARTNER_ID ) ).intValue() );
            this.setPartnerName( (String)mapSampleOrder.get( SampleSaleOrder.PARTNER_NAME ) );
        }
        Log.d( TAG, "SampleSaleOrder: "+ mapSampleOrder.get( SampleSaleOrder.QUANTITY ) );
        float value =  Double.valueOf( (double)mapSampleOrder.get( SampleSaleOrder.QUANTITY ) ).floatValue();
        Log.d( TAG, "SampleSaleOrder: value"+ value );
        this.setQuantity(value );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SampleSaleOrder createFromParcel( Parcel in ) {
            return new SampleSaleOrder( in );
        }

        public SampleSaleOrder[] newArray( int size ) {
            return new SampleSaleOrder[size];
        }
    };

    public String getGuid() {
        return guid;
    }

    public void setGuid( String guid ) {
        this.guid = guid;
    }

    public Boolean getDesignOrder() {
        return isDesignOrder;
    }

    public void setDesignOrder(Boolean designOrder) {
        isDesignOrder = designOrder;
    }
}

