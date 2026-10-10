package org.example.badminton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

// เวลาทำการ: วันธรรมดา 13.00-21.00 น. / เสาร์ อาทิตย์ และวันหยุดนักขัตฤกษ์ 10.00-20.00 น.
public class OpeningHours {
    private static final int WEEKDAY_OPEN = 13;
    private static final int WEEKDAY_CLOSE = 21;
    private static final int HOLIDAY_OPEN = 10;
    private static final int HOLIDAY_CLOSE = 20;

    private ArrayList<LocalDate> publicHolidays;

    public OpeningHours() {
        publicHolidays = new ArrayList<LocalDate>();
    }

    public void addPublicHoliday(LocalDate date) {
        if (!publicHolidays.contains(date)) {
            publicHolidays.add(date);
            Collections.sort(publicHolidays);  // เรียงตามวันที่
        }
    }

    public ArrayList<LocalDate> getPublicHolidays() { return publicHolidays; }

    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidays.contains(date);
    }

    // เสาร์ อาทิตย์ หรือวันหยุดนักขัตฤกษ์
    public boolean isHoliday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || isPublicHoliday(date);
    }

    public int getOpenHour(LocalDate date) {
        if (isHoliday(date))
            return HOLIDAY_OPEN;
        else
            return WEEKDAY_OPEN;
    }

    public int getCloseHour(LocalDate date) {
        if (isHoliday(date))
            return HOLIDAY_CLOSE;
        else
            return WEEKDAY_CLOSE;
    }

    // ช่วงเวลาที่จองต้องอยู่ภายในเวลาเปิด-ปิดทั้งหมด
    public boolean isOpen(LocalDate date, int startHour, int hours) {
        return hours > 0 && startHour >= getOpenHour(date)
                && startHour + hours <= getCloseHour(date);
    }

    public String getDayName(LocalDate date) {
        switch (date.getDayOfWeek().getValue()) {
            case 1: return "จันทร์";
            case 2: return "อังคาร";
            case 3: return "พุธ";
            case 4: return "พฤหัสบดี";
            case 5: return "ศุกร์";
            case 6: return "เสาร์";
            default: return "อาทิตย์";
        }
    }

    public String describe(LocalDate date) {
        String type;
        if (isPublicHoliday(date))
            type = "วันหยุดนักขัตฤกษ์";
        else if (isHoliday(date))
            type = "วันหยุดเสาร์-อาทิตย์";
        else
            type = "วันธรรมดา";
        return "วัน" + getDayName(date) + " " + date + " (" + type + ") เปิด "
                + getOpenHour(date) + ":00 - " + getCloseHour(date) + ":00 น.";
    }
}
