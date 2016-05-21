package com.github.hiteshsondhi88.sampleffmpeg;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.sampleffmpeg.activities.CompressActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = CompressActivity.class
)
@SuppressWarnings("unused")
public class DaggerDependencyModule {

    private final Context context;

    public DaggerDependencyModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    FFmpeg provideFFmpeg() {
        return FFmpeg.getInstance(context.getApplicationContext());
    }

}
