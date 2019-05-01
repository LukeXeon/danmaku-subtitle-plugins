package org.kexie.android.danmakux.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LRCFormat extends Format {

    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌词上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";

    /**
     * 专辑
     */
    private final static String LEGAL_AL_PREFIX = "[al:";

    private final static String LEGAL_TOTAL_PREFIX = "[total:";


    @Override
    public Subtitle parse(String fileName, InputStream input, Charset charset) throws IOException, FormatException {
        Subtitle subtitle = new Subtitle();
        if (input != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(input, charset));

            // 这里面key为该行歌词的开始时间，方便后面排序
            TreeMap<Integer, Section> lineInfoTemp = new TreeMap<>();
            Map<String, Object> lyricsTags = new HashMap<>();
            String lineInfo;
            while ((lineInfo = br.readLine()) != null) {

                // 解析歌词
                //parserLineInfos(lyricsLineInfosTemp,
                //      lyricsTags, lineInfo);

            }
            input.close();
            input = null;
            // 重新封装
            TreeMap<Integer, Section> lyricsLines = new TreeMap<>();

            int index = 0;
            for (Section section : lineInfoTemp.values()) {
                lyricsLines.put(index++, section);
            }
            // 设置歌词的标签类
            //subtitle.setLyricsTags(lyricsTags);
            //
            //subtitle.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return null;
    }

    @Override
    public Object transformation(Subtitle tto) {
        throw new UnsupportedOperationException("No implement");
    }
}
