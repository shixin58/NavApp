package com.passion.libnetwork;

import java.lang.reflect.Type;

public interface Converter<T> {
    T convert(String response, Type type);
    T convert(String response, Class clazz);
}
