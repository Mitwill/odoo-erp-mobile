package com.mitwill.erp.Pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mitwill.erp.addons.colordesignorder.models.ColorDesignOrder;
import com.mitwill.erp.addons.colordesignorderlines.models.ColorDesignOrderLine;
import com.mitwill.erp.addons.res.ResPartner;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.orm.fields.OColumn;

import java.util.Map;

public class SampleColorDesignOrder  implements Parcelable {

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
    private static final String TAG = SampleColorDesignOrder.class.getSimpleName();


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

    private SampleColorDesignOrder() {
    }

    public SampleColorDesignOrder( Parcel in ) {
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
     *   Get the SampleColorDesignOrder Model for the local SaleOrder Object
     * */
    public SampleColorDesignOrder( ODataRow dataRow, ColorDesignOrderLine colorDesignOrderLine ) {
        this.set_id( dataRow.getInt( ColorDesignOrder._ID ) );
        this.setId( dataRow.getInt( ColorDesignOrder.ID ) );
        this.setCreate_date( dataRow.get( ColorDesignOrder.CREATE_DATE ).toString() );
        this.setWrite_date( dataRow.get( ColorDesignOrder.WRITE_DATE ).toString() );
        this.setName( dataRow.getString( ColorDesignOrder.NAME ) );
        this.set_create_date( dataRow.get( ColorDesignOrder._WRITE_DATE ).toString() );

        ODataRow partnerRow = dataRow.getM2ORecord( ColorDesignOrder.PARTNER_ID ).browse();
        this.setPartner_id( partnerRow.getInt( OColumn.ROW_ID ) );
        this.setPartnerName( partnerRow.get( ResPartner.NAME ).toString() );
        this.setQuantity( colorDesignOrderLine.getQuantity( dataRow.getInt( ColorDesignOrder._ID ) ) );
        this.setGuid( dataRow.getString( ColorDesignOrder.GUID ) );
        this.setDesignOrder( dataRow.getBoolean(ColorDesignOrder.IS_DESIGN_ORDER));
    }

    /*
     *   Parse the data from the server for the sample sale order
     * */
    public SampleColorDesignOrder( Map<String, Object> mapSampleOrder ) {

        this.setId( ( Double.valueOf( (double)mapSampleOrder.get( SampleColorDesignOrder.ID ) ).intValue() ) );
        this.setCreate_date( (String)mapSampleOrder.get( SampleColorDesignOrder.CREATE_DATE ) );

        if ( mapSampleOrder.get( SampleColorDesignOrder.WRITE_DATE ) != null ) {
            this.setWrite_date( mapSampleOrder.get( SampleColorDesignOrder.WRITE_DATE ).toString() );
        }

        if ( mapSampleOrder.get( SampleColorDesignOrder.NAME ) != null ) {
            this.setName( (String)mapSampleOrder.get( SampleColorDesignOrder.NAME ) );
        }

        if ( mapSampleOrder.get( SampleColorDesignOrder.GUID ) != null ) {
            this.setGuid( mapSampleOrder.get( SampleColorDesignOrder.GUID ).toString() );
        }
        if ( mapSampleOrder.get( SampleColorDesignOrder.PARTNER_ID ) instanceof Double ) {
            this.setPartner_id( Double.valueOf( (double)mapSampleOrder.get( SampleColorDesignOrder.PARTNER_ID ) ).intValue() );
            this.setPartnerName( (String)mapSampleOrder.get( SampleColorDesignOrder.PARTNER_NAME ) );
        }
        Log.d( TAG, "SampleColorDesignOrder: "+ mapSampleOrder.get( SampleColorDesignOrder.QUANTITY ) );
        float value =  Double.valueOf( (double)mapSampleOrder.get( SampleColorDesignOrder.QUANTITY ) ).floatValue();
        Log.d( TAG, "SampleColorDesignOrder: value"+ value );
        this.setQuantity(value );
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SampleColorDesignOrder createFromParcel( Parcel in ) {
            return new SampleColorDesignOrder( in );
        }

        public SampleColorDesignOrder[] newArray( int size ) {
            return new SampleColorDesignOrder[size];
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