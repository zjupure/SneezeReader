package com.example.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.datamodel.Datainfo;
import com.example.liuchun.sneezereader.R;

import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class MyRecylcerAdapter extends RecyclerView.Adapter<MyRecylcerAdapter.ViewHolder> {
    public List<Datainfo> mDataset;
    public ItemFragment.ItemClickListener mListener;

    public MyRecylcerAdapter(List<Datainfo> dataset){
        mDataset = dataset;
    }

    public void setOnClickListener(ItemFragment.ItemClickListener listener){
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item_layout,parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //绑定数据
        int type = mDataset.get(position).getType();
        if(type != 3){
            //图卦和乐活页面, 呈现title
            holder.mTextView.setText(mDataset.get(position).getTitle());
        }else{
            //段子页面, 呈现内容
            holder.mTextView.setText(mDataset.get(position).getContent());
        }

    }

    @Override
    public int getItemCount() {
        return (mDataset == null) ? 0 : mDataset.size();
    }

    /**
     * 内部类,ViewHolder容器,对ViewHolder实现Item监听
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mTextView;

        public ViewHolder(View rootView){
            super(rootView);
            mTextView = (TextView)rootView.findViewById(R.id.item_title);

            rootView.setOnClickListener(ViewHolder.this);
        }

        @Override
        public void onClick(View v) {
            if(mListener != null){
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }
}
