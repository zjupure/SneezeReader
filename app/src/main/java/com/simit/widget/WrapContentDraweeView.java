package com.simit.widget;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

/**
 * Created by liuchun on 16/9/3.
 *
 * <pre>
 * Works when either height or width is set to wrap_content
 * The view is resized based on the image fetched
 * The solution is from stackoverflow
 * <href>http://stackoverflow.com/questions/33955510/facebook-fresco-using-wrap-conent/34075281#34075281</href>
 * </pre>
 */
public class WrapContentDraweeView extends SimpleDraweeView {

    /** we set a listener and update the view's aspect when the image is loaded */
    private final ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {

        @Override
        public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
            updateViewSize(imageInfo);
        }

        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
            updateViewSize(imageInfo);
        }
    };


    public WrapContentDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public WrapContentDraweeView(Context context) {
        super(context);
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WrapContentDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setImageURI(Uri uri, Object callerContext) {
        DraweeController controller = ((PipelineDraweeControllerBuilder)getControllerBuilder())
                .setControllerListener(controllerListener)
                .setCallerContext(callerContext)
                .setUri(uri)
                .setOldController(getController())
                .build();
        setController(controller);
    }

    /**
     * 更新DraweeView的AspectRatio
     * @param imageInfo
     */
    private void updateViewSize(@Nullable ImageInfo imageInfo){

        if(imageInfo != null){
            setAspectRatio((float)imageInfo.getWidth() / imageInfo.getHeight());
        }
    }
}
