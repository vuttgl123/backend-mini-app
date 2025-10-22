package example.backend_mini_app.shared.util;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@UtilityClass
public class LunarUtils {

    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;

    /**
     * Chuyển đổi từ âm lịch sang dương lịch
     *
     * @param year năm âm lịch
     * @param month tháng âm lịch (1-12)
     * @param day ngày âm lịch (1-30)
     * @return Solar object hoặc null nếu không hợp lệ
     */
    public static Solar toSolar(int year, int month, int day) {
        return toSolar(year, month, day, false);
    }

    /**
     * Chuyển đổi từ âm lịch sang dương lịch (có hỗ trợ tháng nhuận)
     *
     * @param year năm âm lịch
     * @param month tháng âm lịch (1-12)
     * @param day ngày âm lịch (1-30)
     * @param isLeapMonth có phải tháng nhuận không
     * @return Solar object hoặc null nếu không hợp lệ
     */
    public static Solar toSolar(int year, int month, int day, boolean isLeapMonth) {
        try {
            validateLunarDate(year, month, day);

            Lunar lunar = isLeapMonth ? Lunar.fromYmd(year, -month, day) : Lunar.fromYmd(year, month, day);

            return lunar.getSolar();
        } catch (Exception e) {
            log.error("Failed to convert lunar to solar: {}/{}/{} (leap: {})",
                    year, month, day, isLeapMonth, e);
            return null;
        }
    }

    /**
     * Chuyển đổi từ dương lịch sang âm lịch
     *
     * @param year năm dương lịch
     * @param month tháng dương lịch (1-12)
     * @param day ngày dương lịch (1-31)
     * @return Lunar object hoặc null nếu không hợp lệ
     */
    public static Lunar toLunar(int year, int month, int day) {
        try {
            validateSolarDate(year, month, day);

            Solar solar = Solar.fromYmd(year, month, day);
            return solar.getLunar();
        } catch (Exception e) {
            log.error("Failed to convert solar to lunar: {}/{}/{}", year, month, day, e);
            return null;
        }
    }

    /**
     * Chuyển đổi từ LocalDate sang âm lịch
     */
    public static Lunar toLunar(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return toLunar(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Lấy ngày âm lịch hiện tại
     */
    public static Lunar getCurrentLunar() {
        LocalDate today = LocalDate.now();
        return toLunar(today);
    }

    /**
     * Kiểm tra xem có phải tháng nhuận không
     */
    public static boolean isLeapMonth(Lunar lunar) {
        return lunar != null && lunar.getMonth() < 0;
    }

    /**
     * Lấy tên tháng âm lịch (có xử lý tháng nhuận)
     */
    public static String getLunarMonthName(Lunar lunar) {
        if (lunar == null) {
            return "";
        }

        int month = Math.abs(lunar.getMonth());
        String monthName = "Tháng " + month;

        if (isLeapMonth(lunar)) {
            monthName = "Tháng nhuận " + month;
        }

        return monthName;
    }

    private static void validateLunarDate(int year, int month, int day) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(
                    String.format("Lunar year must be between %d and %d", MIN_YEAR, MAX_YEAR));
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Lunar month must be between 1 and 12");
        }
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("Lunar day must be between 1 and 30");
        }
    }

    private static void validateSolarDate(int year, int month, int day) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(
                    String.format("Solar year must be between %d and %d", MIN_YEAR, MAX_YEAR));
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Solar month must be between 1 and 12");
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Solar day must be between 1 and 31");
        }

        try {
            LocalDate.of(year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid solar date: " + year + "/" + month + "/" + day);
        }
    }
}