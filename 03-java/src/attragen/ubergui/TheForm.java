package attragen.ubergui;

import attragen.core.*;
import attragen.gui.AttractorFilter;
import attragen.gui.DataHandler;
import attragen.gui.PreviewForm;
import attragen.renderers.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.lang.ArrayUtils;
import tips4java.HSLColor;

/**
 *
 * @author Rafal
 */
public class TheForm extends attragen.gui.AbstractGeneratorForm {

    private final String[] exclude = {"Sierpinski"};

    private BufferedImage buffer;
    private PreviewForm prev;
    private Random rnd;
    private String formula;
    private int index = 0;
    private int stepPercent = 0;

    /** Creates new form TheForm */
    public TheForm() {
        initComponents();

        theChooser.addChoosableFileFilter(new AttractorFilter());
        
        prev = new PreviewForm();
        prev.setParentForm(this);
        
        rnd = new Random();

        buffer = (BufferedImage)createImage(512, 512);

        gen = new Generator();
        gen.setResolution(512, 512);
        gen.setQuality(0.1);
        gen.setCompositeLevel(2);
        gen.setRenderer(new ImageRenderer(buffer));

        gen.addEventListener(new GeneratorListener() {
            @Override public void initialized(BeginEvent evt) {
                progress.setValue(0);
                stepPercent = 0;
            }
            @Override public void stepOccured(StepEvent evt) {
                double percent = (double)evt.getProgress() / (double)evt.getMaxIterations();
                progress.setValue(stepPercent + (int)(percent*33));
                prev.panel.repaint();
            }
            @Override public void generationStarted(GenerateEvent evt) {
                progress.setValue(33);
                stepPercent = 33;
            }
            @Override public void compositingStarted(CompositeEvent evt) {
                progress.setValue(66);
                stepPercent = 66;
            }
            @Override public void finished(FinishEvent evt) {
                progress.setValue(100);
                prev.panel.repaint();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        theChooser = new javax.swing.JFileChooser();
        theButton = new javax.swing.JButton();
        bSave = new javax.swing.JButton();
        progress = new javax.swing.JProgressBar();

        theChooser.setCurrentDirectory(new File(attragen.Application.getPath()));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AttraGen - UBER GUI!");

        theButton.setFont(theButton.getFont().deriveFont(theButton.getFont().getStyle() | java.awt.Font.BOLD, theButton.getFont().getSize()+37));
        theButton.setText("PIERDUT!!!!!");
        theButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theButtonActionPerformed(evt);
            }
        });

        bSave.setText("Zapisz dane!");
        bSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bSave, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(progress, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(theButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(theButton, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bSave, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void theButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_theButtonActionPerformed
        if (gen.isRunning()) {
            gen.abort();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

        try {
            String[] formulas = attragen.formulas.Formula.listFormulas();
            do {
                formula = formulas[rnd.nextInt(formulas.length)];
            } while (ArrayUtils.contains(exclude, formula));
            gen.setFormula(formula);
            prev.setTitle(String.format("Numer %d - %s", ++index, formula));
        } catch (ClassNotFoundException e){}

        gen.setBaseColor(HSLColor.toRGB(rnd.nextInt(360), 90, 60));
        gen.randomizeParameters();
        gen.randomizeStartPoint();

        prev.panel.setImage(buffer);
        prev.setVisible(true);

        new Thread(gen, "PIERDUT").start();
    }//GEN-LAST:event_theButtonActionPerformed

    private void bSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSaveActionPerformed
        int result = theChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = theChooser.getSelectedFile();

            String path = f.getPath();
            if (!path.endsWith(".attr")) path += ".attr";

            f = new File(path);

            try {
                DataHandler.save(f, formula, gen.getParameters(), gen.getStartPoint());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Nie można było zapisać atraktora.", this.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_bSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bSave;
    private javax.swing.JProgressBar progress;
    private javax.swing.JButton theButton;
    private javax.swing.JFileChooser theChooser;
    // End of variables declaration//GEN-END:variables

}