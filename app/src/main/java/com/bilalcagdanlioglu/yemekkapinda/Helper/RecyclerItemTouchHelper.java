package com.bilalcagdanlioglu.yemekkapinda.Helper;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapinda.Interface.RecyclerItemTouchHelperListener;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.CartViewHolder;
import com.bilalcagdanlioglu.yemekkapinda.ViewHolder.FavoritesViewHolder;

import org.jetbrains.annotations.NotNull;

public class RecyclerItemTouchHelper  extends ItemTouchHelper.SimpleCallback {

    private RecyclerItemTouchHelperListener listener;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super( dragDirs, swipeDirs );
        this.listener = listener;
    }

    @Override
    public boolean onMove( RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped( RecyclerView.ViewHolder viewHolder, int direction) {
        if(listener != null)
        {
            listener.onSwiped( viewHolder, direction,viewHolder.getAdapterPosition() );
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection( flags, layoutDirection );
    }

    @Override
    public void clearView( RecyclerView recyclerView,  RecyclerView.ViewHolder viewHolder) {
        if(viewHolder instanceof CartViewHolder){
            View foregroundView =((CartViewHolder)viewHolder).view_foreground;
            getDefaultUIUtil().clearView( foregroundView );
        }
        else if(viewHolder instanceof FavoritesViewHolder){
            View foregroundView =((FavoritesViewHolder)viewHolder).view_foreground;
            getDefaultUIUtil().clearView( foregroundView );
        }

    }

    @Override
    public void onChildDraw( Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(viewHolder instanceof CartViewHolder){
            View foregroundView =((CartViewHolder)viewHolder).view_foreground;
            getDefaultUIUtil().onDraw( c,recyclerView,foregroundView,dX,dY,actionState,isCurrentlyActive );
        }
        else if(viewHolder instanceof FavoritesViewHolder){
            View foregroundView =((FavoritesViewHolder)viewHolder).view_foreground;
            getDefaultUIUtil().onDraw( c,recyclerView,foregroundView,dX,dY,actionState,isCurrentlyActive );
        }

    }

    @Override
    public void onSelectedChanged( RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder !=null){
            if(viewHolder instanceof CartViewHolder){
                View foregroundView =((CartViewHolder)viewHolder).view_foreground;
                getDefaultUIUtil().onSelected( foregroundView );
            }
            else if(viewHolder instanceof FavoritesViewHolder){
                View foregroundView =((FavoritesViewHolder)viewHolder).view_foreground;
                getDefaultUIUtil().onSelected( foregroundView );
            }

        }
    }

    @Override
    public void onChildDrawOver(@NonNull @NotNull Canvas c, @NonNull @NotNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(viewHolder !=null){
            View foregroundView =((CartViewHolder)viewHolder).view_foreground;
            getDefaultUIUtil().onDrawOver( c,recyclerView,foregroundView,dX,dY,actionState,isCurrentlyActive );
        }
        else if(viewHolder instanceof FavoritesViewHolder){
            View foregroundView =((FavoritesViewHolder)viewHolder).view_foreground;
            getDefaultUIUtil().onDrawOver( c,recyclerView,foregroundView,dX,dY,actionState,isCurrentlyActive );
        }

    }


}
