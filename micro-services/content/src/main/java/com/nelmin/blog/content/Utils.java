package com.nelmin.blog.content;

import org.springframework.data.repository.util.ClassUtils;

import java.util.List;

public class Utils {

    public static String[] getSortProperties(List<String> properties, Class<?> clazz) {
        return properties.stream()
                .filter(it -> ClassUtils.hasProperty(clazz, it))
                .map(Utils::camelToSnake)
                .toArray(String[]::new);
    }

    public static String camelToSnake(String str) {
        StringBuilder result = new StringBuilder();

        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
