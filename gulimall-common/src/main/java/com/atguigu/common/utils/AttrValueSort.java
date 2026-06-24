package com.atguigu.common.utils;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural order for attribute facet values (RAM/Capacity GB/TB, release years, etc.).
 */
public final class AttrValueSort {

    private static final Pattern GB_NUM = Pattern.compile("(\\d+)\\s*GB", Pattern.CASE_INSENSITIVE);
    private static final Pattern TB_NUM = Pattern.compile("(\\d+)\\s*TB", Pattern.CASE_INSENSITIVE);

    private AttrValueSort() {}

    public static Comparator<String> forAttrName(String attrName) {
        if (attrName == null || attrName.isBlank()) {
            return AttrValueSort::compareNatural;
        }
        String n = attrName.trim().toLowerCase(Locale.ROOT);
        if ("ram".equals(n) || "memory".equals(n) || "capacity".equals(n) || "version".equals(n)) {
            return AttrValueSort::compareStorageLike;
        }
        return AttrValueSort::compareNatural;
    }

    public static int compareNatural(String a, String b) {
        int storage = compareStorageLike(a, b);
        long ka = storageSortKey(a);
        long kb = storageSortKey(b);
        if (ka != Long.MAX_VALUE - 1 || kb != Long.MAX_VALUE - 1) {
            return Long.compare(ka, kb);
        }
        Double na = tryParseDouble(a);
        Double nb = tryParseDouble(b);
        if (na != null && nb != null) {
            return na.compareTo(nb);
        }
        return String.CASE_INSENSITIVE_ORDER.compare(a, b);
    }

    static int compareStorageLike(String a, String b) {
        long na = storageSortKey(a);
        long nb = storageSortKey(b);
        if (na != nb) {
            return Long.compare(na, nb);
        }
        return String.CASE_INSENSITIVE_ORDER.compare(a, b);
    }

    private static long storageSortKey(String value) {
        if (value == null || value.isBlank()) {
            return Long.MAX_VALUE;
        }
        String v = value.trim();
        Matcher tb = TB_NUM.matcher(v);
        if (tb.find()) {
            return Long.parseLong(tb.group(1)) * 1024L;
        }
        Matcher gb = GB_NUM.matcher(v);
        if (gb.find()) {
            return Long.parseLong(gb.group(1));
        }
        return Long.MAX_VALUE - 1;
    }

    private static Double tryParseDouble(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
