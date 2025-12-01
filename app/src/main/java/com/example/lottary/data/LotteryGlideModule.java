package com.example.lottary.data;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

/**
 * A class needed to display photos from the database using Glide
 * @see <a href="https://firebaseopensource.com/projects/firebase/firebaseui-android/storage/readme">
 *     Using Firebase UI to display images from Firebase Storage</a>
 */
@GlideModule
public class LotteryGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2)
                .build();
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));
    }

    /**
     * Enable handling of storage references
     * @param context An Application {@link android.content.Context}.
     * @param glide The Glide singleton that is in the process of being initialized.
     * @param registry An {@link com.bumptech.glide.Registry} to use to register components.
     */
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference.class, InputStream.class,
                new FirebaseImageLoader.Factory());
    }
}
