package com.atguigu.gulimall.search.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Expands common category/product keywords so singular/plural and aliases match the same intent.
 */
public final class SearchKeywordSynonyms {

    private static final Map<String, Set<String>> SYNONYM_GROUPS = Map.of(
            "phone", Set.of("phone", "phones", "mobile", "cellphone", "smartphone"),
            "computer", Set.of("computer", "computers", "laptop", "laptops", "pc", "notebook"),
            "tablet", Set.of("tablet", "tablets", "ipad"),
            "watch", Set.of("watch", "watches", "smartwatch", "smart watch"),
            "tv", Set.of("tv", "television", "televisions")
    );

    private SearchKeywordSynonyms() {
    }

    /**
     * Returns the original keyword plus any synonym variants (deduplicated, order preserved).
     */
    public static List<String> expand(String keyword) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String trimmed = keyword.trim();
        terms.add(trimmed);

        String normalized = trimmed.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Set<String>> entry : SYNONYM_GROUPS.entrySet()) {
            String anchor = entry.getKey();
            Set<String> group = entry.getValue();
            if (normalized.equals(anchor) || group.contains(normalized)) {
                terms.addAll(group);
                continue;
            }
            for (String alias : group) {
                if (normalized.contains(alias)) {
                    terms.addAll(group);
                    break;
                }
            }
        }
        return new ArrayList<>(terms);
    }
}
