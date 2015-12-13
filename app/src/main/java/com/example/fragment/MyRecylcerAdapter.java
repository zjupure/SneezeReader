package com.example.fragment;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.datamodel.Article;
import com.example.sneezereader.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class MyRecylcerAdapter extends RecyclerView.Adapter<MyRecylcerAdapter.ViewHolder>{
    private List<Article> mDataset;
    private OnItemClickListener mListener;
    private int type = 0;

    public MyRecylcerAdapter(List<Article> dataset){
        mDataset = dataset;
    }

    public MyRecylcerAdapter(List<Article> dataset, int type){
        mDataset = dataset;
        this.type = type;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if(type == Article.TUGUA){
            //
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tugua_item_layout, parent, false);
        }else{
            //
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lehuo_item_layout, parent, false);
        }

        ViewHolder vh = new ViewHolder(v);
        //v.setOnClickListener(vh);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 加载真实的数据内容
        if(type == Article.TUGUA){

            holder.mTextView.setText(mDataset.get(position).getTitle());

            Uri uri = Uri.parse(mDataset.get(position).getImgurl());
            holder.mImageView.setImageURI(uri);
            //Log.d("RecyclerView", mDataset.get(position).getImgurl());
        }else if(type == Article.LEHUO){

            holder.mTextView.setText(mDataset.get(position).getTitle());
        }else if(type == Article.DUANZI){

            Spanned text = Html.fromHtml(mDataset.get(position).getDescription());
            holder.mTextView.setText(text);
        }

    }

    @Override
    public int getItemCount() {
        // add footer in the bottom
        return (mDataset == null) ? 0 : mDataset.size();
    }


    /**
     * Item单击接口
     */
    public  interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    /**
     * 内部类,ViewHolder容器,对ViewHolder实现Item监听
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;
        public SimpleDraweeView mImageView;

        public ViewHolder(View rootView){
            super(rootView);
            mTextView = (TextView)rootView.findViewById(R.id.item_title);
            mImageView = (SimpleDraweeView) rootView.findViewById(R.id.item_photo);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        mListener.onItemClick(v, getLayoutPosition());
                    }
                }
            });
        }
    }
}
