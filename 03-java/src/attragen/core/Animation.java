package attragen.core;

import java.awt.geom.Point2D;

/**
 *
 * @author Rafał Hirsz
 */
public class Animation implements Runnable {
    private Generator gen;
    private int count = 250;
    private double[] startData;
    private double[] endData;
    private Point2D.Double startPoint;
    private Point2D.Double endPoint;

    private String output = System.getProperty("user.dir");

    protected javax.swing.event.EventListenerList listenerList =
            new javax.swing.event.EventListenerList();
    
    private boolean running = false;
    private boolean busy = false;
    private int currentFrame = 0;

    // The Listener
    private class FrameExporter implements GeneratorListener {
        private Animation dis;

        public FrameExporter(Animation a) {
            dis = a;
        }

        public void initialized(BeginEvent evt) {}
        public void stepOccured(StepEvent evt) {}
        public void generationStarted(GenerateEvent evt) {}
        public void compositingStarted(CompositeEvent evt) {}
        public void finished(FinishEvent evt) {
            try {
                Thread.sleep(70);
            } catch (InterruptedException e) {}

            String name = output + System.getProperty("file.separator")
                        + String.format("%04d.png", currentFrame);
            gen.getRenderer().toFile(name);

            currentFrame++;
            StepEvent e = new StepEvent(dis);
            e.setProgress(currentFrame, count);
            fireStepEvent(e);

            busy = false;
            //notifyAll();
        }
    }
    private FrameExporter exporter;

    // SETTERS
    public void setGenerator(Generator generator) {
        this.gen = generator;
        exporter = new FrameExporter(this);
        gen.addEventListener(exporter);
    }

    public void setFrameCount(int count) {
        this.count = count;
    }

    public void setData(double[] start, double[] end) throws RuntimeException {
        if (start.length != end.length) throw new RuntimeException("Ilość parametrów nie jest równa.");

        startData = start;
        endData = end;
    }

    public void setPoints(Point2D.Double start, Point2D.Double end) {
        startPoint = start;
        endPoint = end;
    }

    public void setOutput(String dir) {
        output = dir;
    }

    // PUBLIC METHODS
    public void abort() {
        running = false;
        gen.abort();
    }

    public void generate() {
        running = true;
        currentFrame = 0;
        fireBeginEvent(new BeginEvent(this));

        generateFrame();
    }

    public void run() {
        generate();
    }

    public boolean isRunning() {
        return running;
    }

    // PRIVATE METHODS
    private void generateFrame() {
        if (!running) return;

        while ((currentFrame < count) && running) {
            if (busy) {
                continue;
            }

            gen.setParameters(getParams(currentFrame));
            gen.setStartPoint(getPoint(currentFrame));
            new Thread(gen, "Animation Generator").start();
            busy = true;

            /*while (gen.isRunning()) { /* EXPLODE * }

            String name = output + System.getProperty("file.separator")
                                 + String.format("%04d.png", currentFrame);
            gen.getRenderer().toFile(name);

            currentFrame++;*/
        }

        gen.removeEventListener(exporter);
        fireFinishEvent(new FinishEvent(this));
        running = false;
    }

    private double[] getParams(int frame) {
        double[] params = new double[startData.length];

        for (int i=0; i<startData.length; i++) {
            params[i] = Tween.cubicInOut(frame, startData[i], (endData[i] - startData[i]), count);
        }

        return params;
    }

    private Point2D.Double getPoint(int frame) {
        Point2D.Double point = new Point2D.Double();

        point.x = Tween.cubicInOut(frame, startPoint.x, (endPoint.x - startPoint.x), count);
        point.y = Tween.cubicInOut(frame, startPoint.y, (endPoint.y - startPoint.y), count);

        return point;
    }

    // EVENT HANDLING
    public void addEventListener(AnimationListener listener) {
        listenerList.add(AnimationListener.class, listener);
    }
    public void removeEventListener(AnimationListener listener) {
        listenerList.remove(AnimationListener.class, listener);
    }
    private void fireBeginEvent(BeginEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == AnimationListener.class) {
                ((AnimationListener)listeners[i+1]).initialized(evt);
            }
        }
    }
    private void fireStepEvent(StepEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == AnimationListener.class) {
                ((AnimationListener)listeners[i+1]).stepOccured(evt);
            }
        }
    }
    private void fireFinishEvent(FinishEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == AnimationListener.class) {
                ((AnimationListener)listeners[i+1]).finished(evt);
            }
        }
    }
}
