package Models;

public class DegreeRange {
    private int startDegree;
    private int endDegree;

    public DegreeRange(int startDegree, int endDegree) {
        this.startDegree = normalizeDegree(startDegree);
        this.endDegree = normalizeDegree(endDegree);
    }

    public int getStartDegree() {
        return startDegree;
    }

    public int getEndDegree() {
        return endDegree;
    }

    public void setStartDegree(int startDegree) {
        this.startDegree = normalizeDegree(startDegree);
    }

    public void setEndDegree(int endDegree) {
        this.endDegree = normalizeDegree(endDegree);
    }

    public boolean isInRange(int degree) {
        degree = normalizeDegree(degree);
        if (startDegree <= endDegree) {
            return degree >= startDegree && degree <= endDegree;
        } else {
            return degree >= startDegree || degree <= endDegree;
        }
    }

    private int normalizeDegree(int degree) {
        degree %= 360;
        if (degree < 0) {
            degree += 360;
        }
        return degree;
    }
}
