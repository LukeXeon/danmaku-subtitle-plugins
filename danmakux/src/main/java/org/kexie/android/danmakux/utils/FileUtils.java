package org.kexie.android.danmakux.utils;

import org.kexie.android.danmakux.converter.SubtitleParserFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

//文件工具类
@SuppressWarnings("WeakerAccess")
public final class FileUtils {
    private FileUtils() {
        throw new AssertionError();
    }

    public static String getFileNameNoExtension(File file) {
        if (file == null) return "";
        return getFileNameNoExtension(file.getPath());
    }

    public static String getFileNameNoExtension(String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }

    public static String getFileExtension(File file) {
        if (file == null) return "";
        return getFileExtension(file.getPath());
    }

    public static String getFileExtension(String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<File> scanDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException();
        }
        List<File> files = new LinkedList<>();
        for (String file : directory.list()) {
            if (SubtitleParserFactory.SUPPORT_FORMATS
                    .contains(getFileExtension(file))) {
                files.add(new File(file));
            }
        }
        return files;
    }

    public static List<File> getVideoSubtitles(File video) {
        List<File> list = scanDirectory(video.getParentFile());
        List<File> result = new LinkedList<>();
        String name = getFileNameNoExtension(video);
        for (File file : list) {
            if (Objects.equals(getFileNameNoExtension(file), name)) {
                result.add(file);
            }
        }
        return result;
    }

    public static List<File> getVideoSubtitles(String video) {
        return getVideoSubtitles(new File(video));
    }
}
