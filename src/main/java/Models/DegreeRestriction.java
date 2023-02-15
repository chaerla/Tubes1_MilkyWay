package Models;

public class DegreeRestriction {
    /* Boolean[] degree : array of 360 degrees (boolean) */
    /* if true, degree[i] is restricted, else degree[i] is unrestricted */
    private Boolean[] degree;

    /* ctor */
    public DegreeRestriction() {
        this.degree = new Boolean[360];
        for (int i = 0; i < degree.length; i++) {
            this.degree[i] = false;
        }
    }

    /**
     * isDegreeRestricted
     * returns restriction status of degree
     * @param deg
     * @return
     */
    public Boolean getRestrictionStatus(int deg) {
        deg = normalizeDegree(deg);
        return this.degree[deg];
    }

    /**
     * setRestrictionStatus
     * sets new status of restriction for given index
     * @param deg
     * @param status
     */
    public void setRestrictionStatus(int deg, boolean status) {
        deg = normalizeDegree(deg);
        this.degree[deg] = status;
    }

    /**
     * restrictRange : add new restricted range
     * @param rangeStart
     * @param rangeFinish
     * value : 0-359
     */
    public void restrictRange(int rangeStart, int rangeFinish) {
        rangeStart = normalizeDegree(rangeStart);
        rangeFinish = normalizeDegree(rangeFinish);
        if (rangeStart > rangeFinish) {     /* ensure rangeStart <= rangeFinish */
            int temp = rangeStart;
            rangeStart = rangeFinish;
            rangeFinish = temp;
        }

        for (int i = rangeStart; i <= rangeFinish; i++) {
            this.degree[i] = true;
        }
    }

    /**
     * getNearestValidHeading
     * get nearest unrestricted heading, clockwise or counterclockwise
     * @param currentHeading
     * @param increment
     * @return
     */
    public int getNearestValidHeading(int currentHeading, int increment) {
        int rotateCwise = currentHeading;
        int CShift = 0, CCShift = 0;
        int rotateCCwise = currentHeading;

        // search clockwise
        while (this.degree[rotateCwise] == false) {
            rotateCwise = normalizeDegree(rotateCwise - increment);
            CShift += increment;
        }
        // search cclockwise
        while (this.degree[rotateCCwise] == false) {
            rotateCCwise = normalizeDegree(rotateCCwise + increment);
            CCShift += increment;
        }

        if (CShift < CCShift) {
            return rotateCwise;
        } else {
            return rotateCCwise;
        }
    }

    /**
     * normalizeDegree
     * returns degree in range 0-359
     * @param deg
     * @return
     */
    private int normalizeDegree(int deg) {
        deg %= 360;
        if (deg < 0) {
            deg += 360;
        }
        return deg;
    }
}
