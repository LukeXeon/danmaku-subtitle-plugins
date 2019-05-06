package org.kexie.android.danmakux.model;

import android.support.annotation.NonNull;

public class Lyric implements Comparable<Lyric> {
    private int startTime;
    private String content;

    public Lyric(int startTime, String content) {
        this.startTime = startTime;
        this.content = content;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(@NonNull Lyric musicLrcBean) {
        return startTime - musicLrcBean.getStartTime();
    }

    @NonNull
    @Override
    public String toString() {
        return "MusicLyric{" +
                "startTime=" + startTime +
                ", content='" + content + '\'' +
                '}';
    }
}
