package attragen.core;

import attragen.formulas.*;
import attragen.renderers.*;
import ec.util.MersenneTwisterFast;
import java.awt.Color;
import java.awt.geom.Point2D;
import tips4java.HSLColor;

/**
 * The actual attractor generator
 * @author Rafał Hirsz
 */
public class Generator implements Runnable {
    private static final int MAXATTR = 10000;
    private static final int STEP = 2500;
    private static final int PRECALCCYCLES = 5000;
    private static final int ITERATIONS = 10000000; // 2kk * 5
    private static final double STEPFACTOR = 0.3; // 1.5 / 5 = 0.3

    private int w = 512;
    private int h = 512;
    private double[] params;
    private int maxIterations;
    private Formula formula;
    private Renderer renderer;
    private double quality = 0.2;
    private int composite = 0;
    private Color color = Color.RED;

    private double valueStep;
    private boolean random;
    private int num = 0;
    private MersenneTwisterFast rnd = new MersenneTwisterFast();
    private long timer;

    private boolean pointSet = false;
    private double startx, starty;

    private double xmin = 1e32;
    private double xmax = -1e32;
    private double ymin = 1e32;
    private double ymax = -1e32;

    private volatile boolean running = false;
    private volatile boolean firstRun = true;

    protected javax.swing.event.EventListenerList listenerList =
            new javax.swing.event.EventListenerList();

    public Generator() {

    }

    // Setters
    public void setResolution(int x, int y) {
        w = x;
        h = y;
        calcMaxIterations();
    }
    public void setParameters(double[] params) {
        if (params.length == 0) {
            randomizeParameters();
        } else {
            this.params = params;
            random = false;
        }
    }
    public void setFormula(String name) {
        try {
            Class cl = Class.forName("attragen.formulas." + name);
            formula = (Formula) cl.newInstance();
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        }
    }
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
        renderer.setParent(this);
    }
    public void setQuality(double q) {
        quality = q;
        valueStep = STEPFACTOR / q;
        calcMaxIterations();
    }
    public void setCompositeLevel(int level) {
        composite = level;
    }
    public void setBaseColor(Color col) {
        color = col;
    }
    public void setStartPoint(Point2D.Double p) {
        pointSet = true;
        startx = p.x;
        starty = p.y;
    }

    // Getters
    public int getIterationStep() {
        return 2500;
    }
    public int getMaxIterations() {
        return maxIterations;
    }
    public double[] getParameters() {
        return params;
    }
    public Point2D.Double getStartPoint() {
        return new Point2D.Double(startx, starty);
    }
    public Renderer getRenderer() {
        return renderer;
    }
    
    // Some helpers
    private void calcMaxIterations() {
        maxIterations = (int)(ITERATIONS * quality * (w*h)/262144);
    }

    public void randomizeParameters() {
        int count = formula.parameterCount();

        java.util.Random seedrnd = new java.util.Random();
        int seed = Math.abs(seedrnd.nextInt());

        params = new double[count];
        rnd = new MersenneTwisterFast(seed);
        for (int i=0; i<count; i++) {
            params[i] = 4 * (rnd.nextDouble() - 0.5);
        }
 
        random = true;
    }

    public void randomizeStartPoint() {
        pointSet = true;
        startx = (rnd.nextDouble() - 0.5);
        starty = (rnd.nextDouble() - 0.5);
    }

    // Event handling
    public void addEventListener(GeneratorListener listener) {
        listenerList.add(GeneratorListener.class, listener);
    }

    public void removeEventListener(GeneratorListener listener) {
        listenerList.remove(GeneratorListener.class, listener);
    }

    private void fireBeginEvent(BeginEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == GeneratorListener.class) {
                ((GeneratorListener)listeners[i+1]).initialized(evt);
            }
        }
    }
    private void fireCompositeEvent(CompositeEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == GeneratorListener.class) {
                ((GeneratorListener)listeners[i+1]).compositingStarted(evt);
            }
        }
    }
    private void fireFinishEvent(FinishEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == GeneratorListener.class) {
                ((GeneratorListener)listeners[i+1]).finished(evt);
            }
        }
    }
    private void fireGenerateEvent(GenerateEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == GeneratorListener.class) {
                ((GeneratorListener)listeners[i+1]).generationStarted(evt);
            }
        }
    }
    private void fireStepEvent(StepEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == GeneratorListener.class) {
                ((GeneratorListener)listeners[i+1]).stepOccured(evt);
            }
        }
    }

    // Generation
    @Override
    public void run() {
        running = true;
        timer = System.currentTimeMillis();
        boolean done = false;
        num = 0;

        renderer.prepare();

        while ((!done) && running) {
            num++;

            // Abort on attractor limit
            if (num > MAXATTR) {
                running = false;
                return;
            }

            fireBeginEvent(new BeginEvent(this));

            done = preCalc();
            if (!done) {
                randomizeParameters();
                randomizeStartPoint();
            }
        }

        firstRun = false;
        running = false;
    }

    /**
     * Aborts the generation process
     */
    public void abort() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    /**
     * Calculates the bounds and checks if the attractor makes sense.
     * @return A boolean
     */
    private boolean preCalc() {
        if (params == null) randomizeParameters();
        formula.setParameters(params);

        if (!pointSet) randomizeStartPoint();
        double x1 = startx;
        double y1 = starty;

        double lyapunov = 0;

        double xe = x1 + (rnd.nextDouble() - 0.5) / 1000;
        double ye = y1 + (rnd.nextDouble() - 0.5) / 1000;
        double dx = x1 - xe;
        double dy = y1 - ye;
        double d0 = Math.sqrt(dx*dx + dy*dy);
        double dd;

        xmin = 1e32;
        xmax = -1e32;
        ymin = 1e32;
        ymax = -1e32;

        // Check if the generation makes sense
        int i = 1;
        int j;
        Point2D.Double p, ep;
        boolean loop = true;
        boolean fail;
        do {
            fail = false;
            j = 0;

            // Generate in steps
            while ((j < STEP) && (i < PRECALCCYCLES) && running) {
                p = formula.calculatePoint(new Point2D.Double(x1, y1));

                // Calculate bounds
                if (p.x < xmin) xmin = p.x;
                if (p.y < ymin) ymin = p.y;
                if (p.x > xmax) xmax = p.x;
                if (p.y > ymax) ymax = p.y;

                // Check only if randomly generated
                if (random) {
                    if ((xmin < -1e10) || (ymin < -1e10) || (xmax > 1e10) || (ymax > 1e10)) {
                        fail = true;
                        break;
                    }

                    ep = formula.calculatePoint(new Point2D.Double(xe, ye));

                    dx = p.x - x1;
                    dy = p.y - y1;
                    if ((Math.abs(dx) < 1e-10) && (Math.abs(dy) < 1e-10)) {
                        fail = true;
                        break;
                    }

                    // Calculate the Lyapunov exponent
                    if (i > 1000) {
                        dx = p.x - ep.x;
                        dy = p.y - ep.y;
                        dd = Math.sqrt(dx*dx + dy*dy);
                        lyapunov += Math.log(Math.abs(dd / d0));
                        xe = p.x + d0 * dx / dd;
                        ye = p.y + d0 * dy / dd;
                    }
                }

                x1 = p.x;
                y1 = p.y;
                i++;
                j++;
            }

            if ((i < PRECALCCYCLES) && !fail) {
                StepEvent evt = new StepEvent(this);
                evt.setProgress(i, maxIterations);
                fireStepEvent(evt);
            } else {
                if (random) {
                    if (!fail) {
                        if (Math.abs(lyapunov) < 10) {
                            // neutrally stable
                            fail = true;
                        } else if (lyapunov < 0) {
                            // periodic
                            fail = true;
                        } // else: chaotic
                    }
                }
                loop = false;
            }
        } while (loop && running);

        // Actual generation
        if (((!fail) || (!random)) && running) {
            generate();
        }
        return !fail;
    }

    /**
     * Generates and draws the attractor.
     */
    private void generate() {
        fireGenerateEvent(new GenerateEvent(this));

        renderer.setData(new double[w][h]);
        renderer.setStep(valueStep);

        // Set the drawing range
        double xrange = (xmax - xmin) / 0.95;
        double yrange = (ymax - ymin) / 0.95;

        double x1 = startx;
        double y1 = starty;

        int i = 0;
        boolean loop = true;
        do {
            int j = 0;

            while ((j < STEP) && (i < maxIterations) && running) {
                Point2D.Double p = formula.calculatePoint(new Point2D.Double(x1, y1));

                double hxy = 0;
                if ((composite > 0) && (i > 100)) {
                    double dx = (p.x - x1) / (xmax-xmin);
                    double dy = (p.y - y1) / (ymax-ymin);
                    hxy = dx*dx + dy*dy;
                }

                // Calculate the point coordinates
                double fx = (p.x - xmin) / xrange + 0.025;
                double fy = (p.y - ymin) / yrange + 0.025;

                if (i > 100) {
                    renderer.addPixel(fx, fy);
                    
                    if (composite > 0) {
                        if (i < STEP*25) {
                            float[] start = HSLColor.fromRGB(color);
                            double bhue = (double)start[0] + (hxy * 120);
                            while (bhue > 360) bhue -= 360;

                            HSLColor hsl = new HSLColor((float)bhue, start[1], start[2]);
                            renderer.setPointColor(fx, fy, hsl.getRGB());
                        }
                    }
                }
                x1 = p.x;
                y1 = p.y;
                i++;
                j++;
            }

            if (i < maxIterations) {
                if ((i % (STEP*50)) == 0) {
                    renderer.render();
                }

                StepEvent evt = new StepEvent(this);
                evt.setProgress(i, maxIterations);
                fireStepEvent(evt);
            } else {
                renderer.render();
                
                if (composite > 0) {
                    StepEvent evt;

                    fireCompositeEvent(new CompositeEvent(this));

                    // Step 1 
                    renderer.colorize();

                    evt = new StepEvent(this);
                    evt.setProgress(1, 2);
                    fireStepEvent(evt);

                    // Step 2
                    if (composite > 1) {
                        renderer.postprocess(composite-1);
                    }
                    evt = new StepEvent(this);
                    evt.setProgress(2, 2);
                    fireStepEvent(evt);

                    fireFinishEvent(new FinishEvent(this));
                } else {
                    fireFinishEvent(new FinishEvent(this));
                }

                loop = false;
            }
        } while (loop && running);
    }
}
