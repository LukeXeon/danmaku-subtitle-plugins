package org.kexie.android.danmakux.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

//方便获取泛型类型的工具类
@SuppressWarnings("WeakerAccess")
public abstract class TypeToken<T> {
    private final Type type;

    public TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
            return;
        }
        throw new IllegalArgumentException("No generics found!");
    }

    public Type getType() {
        return type;
    }
}