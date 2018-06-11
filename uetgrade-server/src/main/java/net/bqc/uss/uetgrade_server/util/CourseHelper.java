package net.bqc.uss.uetgrade_server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseHelper {

    private static final Logger logger = LoggerFactory.getLogger(CourseHelper.class);
    private static final Pattern p = Pattern.compile("^([A-Z]{3})(\\s)(\\d{4}.*)"); // catch EMA 3048, EMA 3046 2

    /**
     * def normalize_course_code(raw_code):
         temp = raw_code
         m = re.match("^([A-Z]{3})(\s)(\d{4}.*)", raw_code)
         if m:
         raw_code = m.group(1) + m.group(3)

         if re.match("^[A-Z]{3}\d{4}$", raw_code):
         raw_code = raw_code + " 1"

         if temp is not raw_code:
         print "Normalize %s -> %s" % (temp, raw_code)
         return raw_code
     */
    public static String normalizeCourseCode(String rawCode) {
        if (rawCode == null) return rawCode;

        String normalizedCode = rawCode;
        Matcher m = p.matcher(normalizedCode);

        if (m.find()) //EMA 3048, EMA 3046 2 -> EMA3048, EMA3046 2
            normalizedCode = m.replaceFirst("$1$3");

        if (normalizedCode.matches("^[A-Z]{3}\\d{4}$")) // EMA3048 -> EMA3048 1
            normalizedCode = normalizedCode + " 1";

//        if (!rawCode.equals(normalizedCode))
//            logger.debug("Normalize code {} -> {}", rawCode, normalizedCode);

        return normalizedCode;
    }
}
