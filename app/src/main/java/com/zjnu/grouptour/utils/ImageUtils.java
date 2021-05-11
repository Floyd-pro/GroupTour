package com.zjnu.grouptour.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;

import static com.bumptech.glide.load.resource.bitmap.VideoDecoder.FRAME_OPTION;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author luchen
 * @Date 2021/4/1 10:00
 * @Description 图片工具类
 * <p>
 * Glide特点
 * 使用简单
 * 可配置度高，自适应程度高
 * 支持常见图片格式 Jpg png gif webp
 * 支持多种数据源  网络、本地、资源、Assets 等
 * 高效缓存策略    支持Memory和Disk图片缓存 默认Bitmap格式采用RGB_565内存使用至少减少一半
 * 生命周期集成   根据Activity/Fragment生命周期自动管理请求
 * 高效处理Bitmap  使用Bitmap Pool使Bitmap复用，主动调用recycle回收需要回收的Bitmap，减小系统回收压力
 * 这里默认支持Context，Glide支持Context,Activity,Fragment，FragmentActivity
 * <p>
 * PS:不要再非主线程里面使用Glide加载图片，如果真的使用了，请把context参数换成getApplicationContext
 */
public class ImageUtils {


    //默认加载
    public static void loadImage(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).into(mImageView);
    }

    //默认加载
    public static void loadImage(Context mContext, int path, ImageView mImageView) {
        Glide.with(mContext).load(path).into(mImageView);
    }

    //默认加载
    public static void loadImage(Context mContext, File path, ImageView mImageView) {
        Glide.with(mContext).load(path).into(mImageView);
    }

    //加载圆形图片
    public static void loadImageCircle(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).apply(bitmapTransform(new GlideCircleTransform())).into(mImageView);
    }

    //加载圆角图片
    public static void loadImageRound(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).apply(bitmapTransform(new GlideRoundTransform(20, 0))).into(mImageView);
    }

    //加载指定大小
    public static void loadImageSize(Context mContext, String path, int width, int height, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .override(width, height);
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    //填充
    public static void loadImageCrop(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop();
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    //设置加载中以及加载失败图片
    public static void loadImageLoding(Context mContext, String path, ImageView mImageView, int lodingImage, int errorImageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(lodingImage)
                .error(errorImageView);
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    //设置加载中以及加载失败图片并且指定大小
    public static void loadImageLodingSize(Context mContext, String path, int width, int height, ImageView mImageView, int lodingImage, int errorImageView) {
        RequestOptions options = new RequestOptions()
                .override(width, height)
                .placeholder(lodingImage)
                .error(errorImageView);
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    //清除图片缓存 （同一个地址，图片如果改变，则显示会改变，如果不清缓存，则会显示老数据 ）
    public static void loadImageClearCache(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    /**
     * 策略解说：
     * <p>
     * DiskCacheStrategy.NONE 	表示不缓存任何内容。
     * DiskCacheStrategy.DATA 	表示只缓存原始图片。
     * DiskCacheStrategy.RESOURCE 	表示只缓存转换过后的图片。
     * DiskCacheStrategy.ALL 	表示既缓存原始图片，也缓存转换过后的图片。
     * DiskCacheStrategy.AUTOMATIC：表示让Glide根据图片资源智能地选择使用哪一种缓存策略（默认选项）。
     */

    //设置缓存策略
    public static void loadImageDiskCache(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    /**
     * 会先加载缩略图
     */

    //设置缩略图支持
    public static void loadImageThumbnail(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).load(path).thumbnail(0.1f).into(mImageView);
    }

    //设置动态GIF加载方式 (默认也可以加载动图)
    public static void loadImageDynamicGif(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).asGif().load(path).into(mImageView);
    }

    //设置静态GIF加载方式(gif第一帧)
    public static void loadImageStaticGif(Context mContext, String path, ImageView mImageView) {
        Glide.with(mContext).asBitmap().load(path).into(mImageView);
    }

    //解决gif加载慢的问题
    public static void loadImageGifSlow(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA);
        Glide.with(mContext).load(path).apply(options).into(mImageView);
    }

    //Gif次数播放
    public static void loadImageCountGif(Context mContext, String path, final ImageView mImageView, final int count) {
        Glide.with(mContext).load(path).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady( Drawable drawable,  Transition<? super Drawable> transition) {
                if (drawable instanceof GifDrawable) {
                    Log.e("CXP", "gif：true");
                    GifDrawable gifDrawable = (GifDrawable) drawable;
                    gifDrawable.setLoopCount(count);
                    mImageView.setImageDrawable(drawable);
                    gifDrawable.start();
                } else {
                    Log.e("CXP", "gif：false");
                }
            }
        });
    }

    //判断图片类型
    public static void loadImageType(Context mContext, String path, final ImageView mImageView, final TextView tv) {
        Glide.with(mContext).load(path).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                if (drawable instanceof GifDrawable) {
                    Log.e("CXP", "gif：true");
                    tv.setText("gif：true");
                    GifDrawable gifDrawable = (GifDrawable) drawable;
                    mImageView.setImageDrawable(drawable);
                    gifDrawable.start();
                } else {
                    Log.e("CXP", "gif：false");
                    tv.setText("gif：false");
                    mImageView.setImageDrawable(drawable);
                }
            }
        });
    }


    //清理磁盘缓存
    public static void GuideClearDiskCache(Context mContext) {
        //理磁盘缓存 需要在子线程中执行
        Glide.get(mContext).clearDiskCache();
    }

    //清理内存缓存
    public static void GuideClearMemory(Context mContext) {
        //清理内存缓存  可以在UI主线程中进行
        Glide.get(mContext).clearMemory();
    }

    /**
     * 获取视频指定时间帧
     * @param context
     * @param url
     * @param imageView
     * @param frameTimeMicros  微秒，注意这里是微秒 1秒 = 1 * 1000 * 1000 微秒
     */
    public static void loadVideoScreenshot(final Context context, String url, ImageView imageView, long frameTimeMicros) {
        RequestOptions options = new RequestOptions()
                .frame(frameTimeMicros)
                .set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST);
        Glide.with(context).load(url).apply(options).into(imageView);
    }
}
