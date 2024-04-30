package com.mitwill.erp.addons.stocks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mitwill.erp.R;
import com.mitwill.erp.addons.stocks.models.StockIncoterms;
import com.mitwill.erp.core.orm.ODataRow;
import com.mitwill.erp.core.support.addons.fragment.BaseFragment;
import com.mitwill.erp.core.support.addons.fragment.ISyncStatusObserverListener;
import com.mitwill.erp.core.support.drawer.ODrawerItem;
import com.mitwill.erp.core.support.list.OCursorListAdapter;
import com.mitwill.erp.core.utils.OControls;

import java.util.List;

/**
 * Created by st29 on 10/01/17.
 */

public class Stocks extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ISyncStatusObserverListener, SwipeRefreshLayout.OnRefreshListener, OCursorListAdapter.OnViewBindListener {
    public static final String TAG = Stocks.class.getSimpleName();

    private View mView;
    private ListView listView;
    private OCursorListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        setHasOptionsMenu( true );
        return inflater.inflate( R.layout.common_listview, container, false );
    }

    @Override
    public void onViewCreated( View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );
        mView = view;
        listView = (ListView)mView.findViewById( R.id.listview );
        listAdapter = new OCursorListAdapter( getActivity(), null, android.R.layout.simple_list_item_1 );
        listView.setAdapter( listAdapter );
        listAdapter.setOnViewBindListener( this );

        mView.findViewById( R.id.fabButton ).setVisibility( View.INVISIBLE );

        setHasSyncStatusObserver( TAG, this, db() );
        getLoaderManager().initLoader( 0, null, this );
    }

    @Override
    public void onViewBind( View view, Cursor cursor, ODataRow row ) {
        OControls.setText( view, android.R.id.text1, row.getString( "name" )  );
    }

    @Override
    public void onStatusChange( Boolean changed ) {
        if ( changed ) {
            getLoaderManager().restartLoader( 0, null, this );
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args ) {
        return new CursorLoader( getActivity(), db().uri(), null, null, null, null );
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor data ) {
        listAdapter.changeCursor( data );
        if ( data.getCount() > 0 ) {
            OControls.setGone( mView, R.id.loadingProgress );
            OControls.setVisible( mView, R.id.swipe_container );
            OControls.setGone( mView, R.id.data_list_no_item );
            setHasSwipeRefreshView( mView, R.id.swipe_container, this );
        } else {
            OControls.setGone( mView, R.id.loadingProgress );
            OControls.setGone( mView, R.id.swipe_container );
            OControls.setVisible( mView, R.id.data_list_no_item );
            setHasSwipeRefreshView( mView, R.id.data_list_no_item, this );
            OControls.setText( mView, R.id.title, getString(R.string.no_taks_found) );
            OControls.setText( mView, R.id.subTitle, R.string.swipe_to_check_new_task);
        }
        if ( db().isEmptyTable() ) {
            onRefresh();
        }
    }

    @Override
    public void onRefresh() {
        if ( inNetwork() ) {
            parent().sync().requestSync( StockIncoterms.AUTHORITY );
        }else{
            Snackbar snackbar = Snackbar
                    .make(mView, R.string.you_are_offline, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED);
            snackbar.show();
        }
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader ) {
        listAdapter.changeCursor( null );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public Class<StockIncoterms> database() {
        return StockIncoterms.class;
    }

    @Override
    public List<ODrawerItem> drawerMenus( Context context ) {
        return null;
/*
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add( new ODrawerItem( TAG )
                .setTitle( "Stocks" )
                .setIcon( R.drawable.ic_stock )
                .setInstance( new Stocks() ) );
        return menu;
*/
    }
}
