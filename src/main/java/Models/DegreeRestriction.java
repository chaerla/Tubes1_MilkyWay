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
     * 
     * @param deg
     * @return
     */
    public Boolean isDegValid(int deg) {
        deg = normalizeDegree(deg);
        return !this.degree[deg];
    }

    /**
     * setRestrictionStatus
     * sets new status of restriction for given index
     * 
     * @param deg
     * @param status
     */
    public void setRestrictionStatus(int deg, boolean status) {
        deg = normalizeDegree(deg);
        this.degree[deg] = status;
    }

    /**
     * restrictRange
     * add new heading restriction
     * 
     * @param heading
     * @param delta
     */
    public void restrictRange(int heading, int delta) {
        int i = 0;
        while (i <= delta) {
            degree[normalizeDegree(heading + i)] = true;
            degree[normalizeDegree(heading - i)] = true;
            i++;
        }
    }

    /**
     * getNearestValidHeading
     * get nearest unrestricted heading, clockwise or counterclockwise
     * 
     * @param currentHeading
     * @param increment
     * @return
     */
    public int getNearestValidHeading(int currentHeading, int increment) {
        int rotateCwise = currentHeading;
        int CShift = 0, CCShift = 0;
        int rotateCCwise = currentHeading;

        // search clockwise
        while (this.degree[rotateCwise]) {
            rotateCwise = normalizeDegree(currentHeading - CShift);
            CShift++;
        }
        // search cclockwise
        while (this.degree[rotateCCwise]) {
            rotateCCwise = normalizeDegree(currentHeading + CCShift);
            CCShift++;
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
     * 
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
