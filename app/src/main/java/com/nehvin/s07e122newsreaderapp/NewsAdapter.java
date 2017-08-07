package com.nehvin.s07e122newsreaderapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Vineet K Jain on 03-Aug-17.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private static final String TAG = NewsAdapter.class.getSimpleName();
    public Context context;
    public List<NewsDetailsView> list;
    final private ListItemClickListener mOnItemClickListener;

    public NewsAdapter(Context context, List<NewsDetailsView> list, ListItemClickListener listener) {

        this.context = context;
        this.list = list;
        mOnItemClickListener = listener;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewPosition) {
//        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.number_list_item;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        NewsViewHolder viewHolder = new NewsViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        holder.title.setText(list.get(position).title);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Cache of the children views for a list item.
     */
    class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView title;


        public NewsViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_item_number);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnItemClickListener.onListItemClick(clickedPosition);
        }
    }

    public interface ListItemClickListener
    {
        void onListItemClick(int clickedItemIndex);
    }
}