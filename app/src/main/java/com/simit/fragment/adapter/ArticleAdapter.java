package com.simit.fragment.adapter;

import android.graphics.drawable.Animatable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.image.ImageInfo;
import com.simit.database.Article;
import com.simit.activity.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by liuchun on 2015/7/16.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder>{
    private static final String TAG = "ArticleAdapter";

    private List<Article> mArticles;
    private OnItemClickListener mListener;
    private int type = 0;

    public ArticleAdapter(List<Article> articles, int type){
        this.mArticles = articles;
        this.type = type;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        // 根据文章类型渲染不同的背景图
        if(type == Article.TUGUA){
            // 图卦页面需要带子图
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tugua_item_layout, parent, false);
        }else{
            // 乐活、段子页面是纯粹的文字
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lehuo_item_layout, parent, false);
        }

        // 塞入容器缓存
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 加载真实的数据内容
        Article article = mArticles.get(position);
        if(type == Article.TUGUA){

            holder.mTextView.setText(article.getTitle());

            //loadImage(holder.mImageView, article.getImgUrl());
            holder.mImageView.setImageURI(article.getImgUrl());

        }else if(type == Article.LEHUO){

            holder.mTextView.setText(article.getTitle());
        }else if(type == Article.DUANZI){

            Spanned text = Html.fromHtml(article.getDescription());
            holder.mTextView.setText(text);
        }

    }

    @Override
    public int getItemCount() {

        return (mArticles == null) ? 0 : mArticles.size();
    }

    /**
     * 加载图片，自适应大小
     * @param imageView
     * @param uriString
     */
    private void loadImage(final SimpleDraweeView imageView, String uriString){

        final ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>(){
            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

                updateImageSize(imageView, imageInfo);
            }

            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {

                updateImageSize(imageView, imageInfo);
            }
        };

        final DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uriString)
                .setOldController(imageView.getController())
                .build();

        imageView.setController(controller);
    }

    /**
     * 更新图片的尺寸
     * @param imageView
     * @param imageInfo
     */
    private void updateImageSize(SimpleDraweeView imageView, ImageInfo imageInfo){

        if(imageInfo != null){
            Log.d(TAG, "ImageInfo: width=" + imageInfo.getWidth() + ", heigh=" + imageInfo.getHeight());

            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            final int imageWidth = imageView.getMeasuredWidth();
            final int imageHeight = imageView.getMeasuredHeight();

            lp.width = imageWidth;
            lp.height = imageWidth * imageInfo.getHeight() / imageInfo.getWidth();

            imageView.setLayoutParams(lp);
            imageView.setAspectRatio((float)imageInfo.getWidth() / imageInfo.getHeight());
        }

    }


    /**
     * Item事件接口
     */
    public  interface OnItemClickListener {
        /**
         * 单击事件
         * @param view
         * @param position
         */
        void onItemClick(View view, int position);

        /**
         * 长按事件
         * @param view
         * @param position
         */
        void onItemLongClick(View view, int position);
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
            // 单击操作
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        mListener.onItemClick(v, getLayoutPosition());
                    }
                }
            });
            // 长按操作
            rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(mListener != null){
                        mListener.onItemLongClick(v, getLayoutPosition());
                    }
                    return false;
                }
            });
        }
    }

}
