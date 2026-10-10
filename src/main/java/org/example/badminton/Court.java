package org.example.badminton;

public class Court {
    public static final double PRICE_PER_HOUR = 180.0;

    private int courtNo;

    public Court(int courtNo) {
        this.courtNo = courtNo;
    }

    public int getCourtNo() { return courtNo; }
    public double getPricePerHour() { return PRICE_PER_HOUR; }

    @Override
    public String toString() {
        return "สนาม " + courtNo;
    }
}
