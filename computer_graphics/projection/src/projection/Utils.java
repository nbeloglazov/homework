package projection;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import clojure.lang.PersistentVector;

public class Utils {

    public static double vecMult(List<Double> p0, List<Double> p1, List<Double> p2, List<Double> p3) {
        double dx1 = p1.get(0) - p0.get(0);
        double dy1 = p1.get(1) - p0.get(1);
        double dx2 = p3.get(0) - p2.get(0);
        double dy2 = p3.get(1) - p2.get(1);
        return dx1 * dy2 - dx2 * dy1;
    }

    public static List<Double> lineSegmentIntersection(List<Double> l0, List<Double> l1, List<Double> s0, List<Double> s1) {
        double ldx = l1.get(0) - l0.get(0);
        double ldy = l1.get(1) - l0.get(1);
        double sdx = s1.get(0) - s0.get(0);
        double sdy = s1.get(1) - s0.get(1);
        double t = - (ldx * (s0.get(1) - l0.get(1))
                      -
                      ldy * (s0.get(0) - l0.get(0)))
                      / vecMult(l0, l1, s0, s1);
        double x = s0.get(0) + t * sdx;
        double y = s0.get(1) + t * sdy;
        return Arrays.asList(x, y);
    }

    public static boolean isVisible(List<Double> l0, List<Double> l1, List<Double> point, double sign) {
        return vecMult(l0, l1, l0, point) * sign >= 0;
    }

    public static List<List<Double>> lineSegment(List<Double> l0, List<Double> l1, List<Double> s0, List<Double> s1, double sign) {
        boolean s0Visible = isVisible(l0, l1, s0, sign);
        boolean s1Visible = isVisible(l0, l1, s1, sign);
        if (s0Visible && s1Visible) {
            return Arrays.asList(s1);
        } else if (s0Visible && !s1Visible) {
            return Arrays.asList(lineSegmentIntersection(l0, l1, s0, s1));
        } else if (!s0Visible && s1Visible) {
            return Arrays.asList(lineSegmentIntersection(l0, l1, s0, s1),
                                 s1);
        } else {
            return Collections.emptyList();
        }
    }

    public static List<List<Double>> linePolygon(List<Double> l0, List<Double> l1, double sign, List<List<Double>> polygon) {
        List<Double> firstPoint = null;
        List<Double> prevPoint = null;
        List<List<Double>> result = new ArrayList<List<Double>>(polygon.size());
        if (polygon.isEmpty()) {
            return Collections.emptyList();
        }
        for (List<Double> currentPoint : polygon) {
            if (prevPoint != null) {
                result.addAll(lineSegment(l0, l1, prevPoint, currentPoint, sign));
            }
            prevPoint = currentPoint;
            if (firstPoint == null) {
                firstPoint = currentPoint;
            }
        }
        result.addAll(lineSegment(l0, l1, prevPoint, firstPoint, sign));
        return result;
    }

    public static List<List<Double>> polygonPolygon(List<List<Double>> view, double sign, List<List<Double>> polygon) {
        List<Double> prevPoint = null;
        for (List<Double> currentPoint : view) {
            if (prevPoint != null) {
                polygon = linePolygon(prevPoint, currentPoint, sign, polygon);
                if (polygon.isEmpty()) {
                    return Collections.emptyList();
                }
            }
            prevPoint = currentPoint;
        }
        return polygon;
    }


}

