package org.kexie.android.danmakux.converter;

import org.kexie.android.danmakux.io.Chardet;
import org.kexie.android.danmakux.io.Jdk18BufferedInputStream;
import org.kexie.android.danmakux.model.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LyricParser {

    private LyricParser() {
        throw new AssertionError();
    }

    private static final Lyric LYRIC_NO_FOUND = new Lyric(0, "没有发现歌词");
    private static final Lyric LYRIC_HAS_ERROR = new Lyric(0, "歌词加载出错");

    public static List<Lyric> loadFile(File file) {
        if (!file.exists() || file.isFile()) {
            return Collections.singletonList(LYRIC_NO_FOUND);
        }
        InputStream input = null;
        try {
            input = Jdk18BufferedInputStream
                    .newInstance(new FileInputStream(file));
            Charset charset = Chardet.bestGuess(input);
            List<Lyric> lrcList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
            String line = reader.readLine();
            while (line != null) {
                List<Lyric> lyrics = parseLine(line);
                lrcList.addAll(lyrics);
                line = reader.readLine();
            }
            Collections.sort(lrcList);
            return lrcList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonList(LYRIC_HAS_ERROR);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Lyric> parseLine(String str) {
        ArrayList<Lyric> list = new ArrayList<>();
        String[] arr = str.split("]");
        String content = arr[arr.length - 1];

        for (int i = 0; i < arr.length - 1; i++) {
            if (arr.length > i) {
                int startTime = parseTime(arr[i]);
                Lyric lrcBean = new Lyric(startTime, content);
                list.add(lrcBean);
            }
        }
        return list;
    }

    /**
     * 对歌词时间可能出现汉字或英文字母做了处理
     *
     * @param s s
     * @return d
     */
    private static int parseTime(String s) {
        int mtime = 0;
        boolean containsChinese = isContainsEnglishAndChinese(s);
        String braces = "[";
        if (containsChinese) {
            return 0;
        } else {
            String[] arr = s.split(":");
            String min = arr[0].substring(1);
            if (min.contains(braces)) {
                min = "00";
            }
            if (arr.length > 1) {
                String sec = arr[1];
                boolean b = isContainsEnglishAndChinese(sec);
                mtime = (int) (Integer.parseInt(min) * 60 * 1000
                        + Float.parseFloat(b ? "1" : sec) * 1000);
            }
            return arr.length > 1 ? mtime : 1;
        }

    }

    private static boolean isContainsEnglishAndChinese(String str) {
        String regex = "^[a-z0-9A-Z\\\\u4e00-\u9fa5]+$";
        return str.matches(regex);
    }
}
