package attragen.core;

/**
 * Robert Penner easing equations
 * Ported to Java
 * @author Rafał Hirsz
 */
public class Tween {
    static double linear(double time, double begin, double change, double duration) {
        return change*time/duration+begin;
    }
    static double cubicInOut(double time, double begin, double change, double duration) {
        if ((time /= duration/2) < 1) return change/2*Math.pow(time, 3) + begin;
        return change/2*(Math.pow(time-2, 3)+2) + begin;
    }
    static double elasticOut(double time, double begin, double change, double duration) {
        if (time == 0) return begin;
        if ((time/=duration) == 1) return begin + change;

        double period = duration * 0.3;
        double amplitude = change;
        double s = period / 4;

        return amplitude * Math.pow(2, -10*time) * Math.sin((time*duration-s)*(2*Math.PI)/period) + change + begin;
    }
}
