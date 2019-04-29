package org.kexie.android.danmakux.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("WeakerAccess")
public abstract class TypeToken<T> {
    private final Type type;

    public TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("No generics found!");
    }

    public Type getType() {
        return type;
    }
}