/*
 * ProfileForm.java
 *
 * Created on 2010-02-07, 13:53:09
 */

package attragen.gui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author evol
 */
public class ProfileForm extends javax.swing.JFrame implements PropertyChangeListener {
    private String currentProfileName;
    private boolean saveProfile = false;
    private boolean automaticChange = false;
    private MainForm parentform;

    /** Creates new form ProfileForm */
    public ProfileForm() {
        initComponents();

        tResX.addPropertyChangeListener("value", this);
        tResY.addPropertyChangeListener("value", this);
        tQuality.addPropertyChangeListener("value", this);

        try {
            // Populate the combobox
            String[] profiles = Profile.getProfiles();
            for (String profile: profiles) {
                cbProfile.addItem(profile);
            }

            updateCurrentProfileName();
            updateForm();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    public Profile getCurrentProfile() {
        Profile profile = new Profile(currentProfileName);

        try {
            profile.load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
        }

        return profile;
    }

    private void updateForm() {
        Profile profile = getCurrentProfile();

        Dimension res = profile.getResolution();

        automaticChange = true;
        tResX.setValue(res.width);
        tResY.setValue(res.height);
        tQuality.setValue(profile.getQuality());
        sComposite.setValue(profile.getCompositeLevel());
        automaticChange = false;
    }

    private void updateProfile() {
        if (saveProfile) {
            Profile profile = getCurrentProfile();

            profile.setResolution(((Number)tResX.getValue()).intValue(),((Number)tResY.getValue()).intValue());
            profile.setQuality(((Number)tQuality.getValue()).intValue());
            profile.setCompositeLevel(sComposite.getValue());

            try {
                profile.save();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
            } finally {
                saveProfile = false;
            }
        }
    }

    private void updateCurrentProfileName() {
        currentProfileName = (String)cbProfile.getSelectedItem();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!automaticChange) {
            if (evt.getOldValue() != evt.getNewValue()) {
                saveProfile = true;
            }
        }
    }

    public void setParentForm(MainForm form) {
        parentform = form;
    }

    public Dimension getResolution() {
        return new Dimension(((Number)tResX.getValue()).intValue(), ((Number)tResY.getValue()).intValue());
    }
    public int getQuality() {
        return ((Number)tQuality.getValue()).intValue();
    }
    public int getCompositeLevel() {
        return sComposite.getValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bRemove = new javax.swing.JButton();
        bAdd = new javax.swing.JButton();
        cbProfile = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        sComposite = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        bClose = new javax.swing.JButton();
        tResY = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        tResX = new javax.swing.JFormattedTextField();
        tQuality = new javax.swing.JFormattedTextField();

        setTitle("Edytor profili");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        bRemove.setText("Usuń");
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });

        bAdd.setText("Dodaj...");
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddActionPerformed(evt);
            }
        });

        cbProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfileActionPerformed(evt);
            }
        });

        jLabel1.setText("Profil:");

        jLabel3.setText("Rozdzielczość:");

        jLabel4.setText("%");

        jLabel5.setText("Jakość:");

        sComposite.setMajorTickSpacing(1);
        sComposite.setMaximum(3);
        sComposite.setPaintTicks(true);
        sComposite.setSnapToTicks(true);
        sComposite.setValue(0);
        sComposite.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sCompositeStateChanged(evt);
            }
        });

        jLabel6.setText("Stopień kolorowania:");

        bClose.setText("Zamknij");
        bClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCloseActionPerformed(evt);
            }
        });

        tResY.setColumns(4);
        tResY.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        tResY.setText("1024");

        jLabel2.setText("x");

        tResX.setColumns(4);
        tResX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        tResX.setText("1024");

        tQuality.setColumns(3);
        tQuality.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        tQuality.setText("200");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(18, 18, 18)
                        .add(sComposite, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 230, Short.MAX_VALUE)
                        .add(tResX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tResY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 308, Short.MAX_VALUE)
                        .add(tQuality, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bClose))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(tResY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(tResX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(tQuality, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sComposite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(bClose)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cbProfile, 0, 209, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bAdd)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bRemove)
                .addContainerGap())
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bRemove)
                    .add(cbProfile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(bAdd))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfileActionPerformed
        updateProfile();
        updateCurrentProfileName();
        updateForm();
    }//GEN-LAST:event_cbProfileActionPerformed

    private void bCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCloseActionPerformed
        updateProfile();
        setVisible(false);
    }//GEN-LAST:event_bCloseActionPerformed

    private void sCompositeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sCompositeStateChanged
        if (!automaticChange) {
            saveProfile = true;
        }
    }//GEN-LAST:event_sCompositeStateChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        updateProfile();
    }//GEN-LAST:event_formWindowClosing

    private void bAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        String newname = JOptionPane.showInputDialog(this, "Nazwa profilu", "Dodaj profil", JOptionPane.PLAIN_MESSAGE);
        if ((newname != null) && (!newname.isEmpty())) {
            Profile profile = new Profile(newname);
            try {
                profile.save();
                cbProfile.addItem(newname);
                parentform.cbProfile.addItem(newname);
                parentform.cbProfile.setSelectedItem(newname);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), this.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_bAddActionPerformed

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        int answer = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć ten profil?", "Usuń profil", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            String currname = currentProfileName;
            if (Profile.delete(currname)) {
                cbProfile.removeItem(currname);
                parentform.cbProfile.removeItem(currname);
                cbProfile.setSelectedIndex(parentform.cbProfile.getSelectedIndex());
            }
        }
    }//GEN-LAST:event_bRemoveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bClose;
    private javax.swing.JButton bRemove;
    public javax.swing.JComboBox cbProfile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSlider sComposite;
    private javax.swing.JFormattedTextField tQuality;
    private javax.swing.JFormattedTextField tResX;
    private javax.swing.JFormattedTextField tResY;
    // End of variables declaration//GEN-END:variables

}