package com.example.demo.util;

import org.springframework.web.util.HtmlUtils;
import java.util.regex.Pattern;

/**
 * XSS 공격 방어를 위한 유틸리티 클래스
 */
public class XSSUtils {

    // 위험한 HTML 태그들을 감지하는 패턴
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\\r\\n]*=[\\r\\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\\r\\n]*=[\\r\\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onmouseover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    /**
     * XSS 공격 가능성이 있는 문자열인지 검사
     */
    public static boolean containsXSS(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * HTML 특수 문자를 이스케이프 처리
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Spring의 HtmlUtils를 사용한 HTML 이스케이프
        String sanitized = HtmlUtils.htmlEscape(input);

        // 추가적인 필터링
        sanitized = sanitized.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
        sanitized = sanitized.replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "");
        sanitized = sanitized.replaceAll("(?i)<.*?\\s+on\\w+\\s*=.*?>", "");

        return sanitized;
    }

    /**
     * 입력값 검증 - XSS 공격이 감지되면 예외 발생
     */
    public static void validateInput(String input, String fieldName) {
        if (containsXSS(input)) {
            throw new IllegalArgumentException(fieldName + "에 허용되지 않는 문자가 포함되어 있습니다.");
        }
    }

    /**
     * 안전한 텍스트로 변환 (표시용)
     */
    public static String toSafeText(String input) {
        if (input == null) {
            return "";
        }
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * HTML 이스케이프를 해제 (필요한 경우)
     */
    public static String unescapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return HtmlUtils.htmlUnescape(input);
    }
}