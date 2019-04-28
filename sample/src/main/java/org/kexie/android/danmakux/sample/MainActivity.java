package org.kexie.android.danmakux.sample;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.kexie.android.danmakux.subtitle.converter.SubtitleParserFactory;
import org.kexie.android.danmakux.subtitle.format.Format;

import java.io.InputStream;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Logger.addLogAdapter(new AndroidLogAdapter());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action",
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DanmakuView danmakuView = findViewById(R.id.danmaku);

        try {
            AssetManager assetManager = getAssets();

            Logger.d(assetManager.list("standards"));

            InputStream inputStream = assetManager
                    .open("standards/SRT/Avengers.2012.Eng.Subs.srt");

            ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

            loader.load(inputStream);

            IDataSource<?> dataSource = loader.getDataSource();

            Format format = Format.forName(Format.FORMAT_ASS);

            BaseDanmakuParser danmakuParser = SubtitleParserFactory
                    .create(format);

            danmakuParser.load(dataSource);
            danmakuView.enableDanmakuDrawingCache(true);
            danmakuParser.setConfig(DanmakuContext.create())
                    .setDisplayer(new AndroidDisplayer())
                    .getDanmakus();

            danmakuView.prepare(danmakuParser, DanmakuContext.create());
            danmakuView.start();
            danmakuView.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
