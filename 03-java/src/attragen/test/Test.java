
/*
 * Test.java
 *
 * Created on 2010-07-22, 20:50:02
 */

package attragen.test;

import attragen.formulas.DeJongMod;
import java.awt.geom.Point2D;
import java.util.*;

/**
 *
 * @author Rafal Hirsz
 */
public class Test extends javax.swing.JFrame implements Runnable {
    public static final int PARTICLECOUNT = 10000000;

    private DeJongMod formula;
    private Point2D.Double start;


    /** Creates new form Test */
    public Test() {
        initComponents();
        jProgressBar1.setMaximum(PARTICLECOUNT);

        double[] params = {
            0.04460616012687346,
            0.40564624373286484,
            -0.3382432096604129,
            -1.761516798900276
        };

        formula = new DeJongMod();
        formula.setParameters(params);

        start = new Point2D.Double(1.8529084053636606, 1.6638060931856447);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Start");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jProgressBar1.setValue(0);
     
        new Thread(this).start();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Test().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        List<Point2D.Double> pts = new ArrayList<Point2D.Double>();
        pts.add(start);

        // Simple array of objects
        //Point2D.Double[] pts = new Point2D.Double[PARTICLECOUNT];
        //pts[0] = start;

        Point2D.Double prev = start;
        Point2D.Double temp;

        for (int i=1; i<=PARTICLECOUNT; ++i) {
            temp = formula.calculatePoint(prev);
            pts.add(temp);
            prev = temp;
            jProgressBar1.setValue(i);
        }
    }

}