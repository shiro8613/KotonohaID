package dev.shiro8613.kotonohaid;

public class FuzzySearch {
    public static boolean search(String targetString, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }

        int keywordIndex = 0;
        int keywordLength = keyword.length();
        int targetLength = targetString.length();

        for (int targetIndex = 0; targetIndex < targetLength; targetIndex++) {

            if (keyword.charAt(keywordIndex) == targetString.charAt(targetIndex)) {
                keywordIndex++;

                if (keywordIndex == keywordLength) {
                    return true;
                }
            }
        }

        return false;
    }
}
