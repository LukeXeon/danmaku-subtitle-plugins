package org.kexie.android.danmakux.utils;

import android.os.Build;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 方便获取泛型类型的工具类
 * 你可以像这样使用
 * <code>
 *     new TypeToken<IDataSource<InputStream>>() {}.getType()
 * </code>
 * @param <T> 泛型类型
 * @author Luke
 */
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

    /**
     * 获取{@link T}的{@link Type}
     * @return 返回参数化的泛型类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 比较两种{@link Type}
     * 视API等级不同,会把所有可能的手段都用上
     * 如果这样还不相等,就说明他们确实不是同一种类型
     * @param type1 类型1
     * @param type2 类型2
     * @return 比较结果
     */
    public static boolean deepEqualsType(Type type1, Type type2) {
        return Objects.deepEquals(type1, type2)
                || Objects.equals(type1.toString(), type2.toString())
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && Objects.equals(type1.getTypeName(), type2.getTypeName()));
    }

}