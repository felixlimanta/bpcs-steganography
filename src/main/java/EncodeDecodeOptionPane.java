import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class EncodeDecodeOptionPane {
  private JPanel panel;
  private JPasswordField keyField;
  private JSpinner thresholdSpinner;
  private JCheckBox encryptedMessageCheckbox;
  private JCheckBox randomEncodingCheckbox;
  private boolean encode;

  EncodeDecodeOptionPane(boolean encode) {
    this.encode = encode;
    panel = new JPanel(new GridLayout(6, 1));
    panel.add(new JLabel("Enter key:"));
    keyField = new JPasswordField(25);
    panel.add(keyField);
    panel.add(new JLabel("Enter BPCS threshold:"));
    thresholdSpinner = new JSpinner(new SpinnerNumberModel(Float.valueOf(0.3f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.01f)));
    panel.add(thresholdSpinner);
    encryptedMessageCheckbox = new JCheckBox(encode ? "Encrypt message before encoding" : "Message is encrypted before encoding", true);
    panel.add(encryptedMessageCheckbox);
    randomEncodingCheckbox = new JCheckBox(encode ? "Encode message at random blocks" : "Message is encoded at random blocks", true);
    panel.add(randomEncodingCheckbox);
  }

  int showDialog(Component parentComponent) {
    return JOptionPane.showConfirmDialog(parentComponent, panel, encode ? "Encode" : "Decode", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
  }

  String getKey() {
    return new String(keyField.getPassword());
  }

  float getThreshold() {
    return (float) thresholdSpinner.getValue();
  }

  boolean isEncryptedMessage() {
    return encryptedMessageCheckbox.isSelected();
  }

  boolean isRandomEncoding() {
    return randomEncodingCheckbox.isSelected();
  }
}
