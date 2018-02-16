package stima;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GUIMain {
  private final int MAX_IMAGE_HEIGHT = 400;

  private JButton encodeButton;
  private JButton decodeButton;
  private JLabel loadedImage;
  private JButton loadImageButton;
  private JButton saveImageButton;
  private JButton loadMessageButton;
  private JButton saveMessageButton;
  private JLabel loadedMessage;
  private JPanel contentPane;
  private JFileChooser loadImageFileChooser;
  private JFileChooser saveImageFileChooser;
  private JFileChooser loadMessageFileChooser;
  private JFileChooser saveMessageFileChooser;

  private ImageEncodeDecode imageEncodeDecode;

  private void loadImage() {
    loadedImage.setIcon(new ImageIcon(imageEncodeDecode.getImage().getScaledInstance(-1, MAX_IMAGE_HEIGHT, Image.SCALE_SMOOTH)));
    loadedImage.setText("");
  }

  private void loadMessage() {
    loadedMessage.setText("Loaded message: " + imageEncodeDecode.getMessageFilename());
  }

  private void updateButtons() {
    saveImageButton.setEnabled(imageEncodeDecode.isImageLoaded());
    saveMessageButton.setEnabled(imageEncodeDecode.isMessageLoaded());
    encodeButton.setEnabled(imageEncodeDecode.isImageLoaded() && imageEncodeDecode.isMessageLoaded());
    decodeButton.setEnabled(imageEncodeDecode.isImageLoaded());
  }

  private GUIMain (ImageEncodeDecode _imageEncodeDecode) {
    imageEncodeDecode = _imageEncodeDecode;
    if (imageEncodeDecode.isImageLoaded()) {
      loadImage();
    }
    if (imageEncodeDecode.isMessageLoaded()) {
      loadMessage();
    }
    updateButtons();

    FileNameExtensionFilter bmpPngFilter = new FileNameExtensionFilter("BMP and PNG images", "bmp", "png");

    loadImageFileChooser = new JFileChooser();
    loadImageFileChooser.setDialogTitle(loadImageButton.getText());
    loadImageFileChooser.setDialogType(JFileChooser.FILES_ONLY);
    loadImageFileChooser.setFileFilter(bmpPngFilter);

    saveImageFileChooser = new JFileChooser();
    saveImageFileChooser.setDialogTitle(saveImageButton.getText());
    loadImageFileChooser.setDialogType(JFileChooser.FILES_ONLY);
    saveImageFileChooser.setFileFilter(bmpPngFilter);

    loadMessageFileChooser = new JFileChooser();
    loadMessageFileChooser.setDialogTitle(loadMessageButton.getText());
    loadMessageFileChooser.setDialogType(JFileChooser.FILES_ONLY);

    saveMessageFileChooser = new JFileChooser();
    saveMessageFileChooser.setDialogTitle(saveMessageButton.getText());
    saveMessageFileChooser.setDialogType(JFileChooser.FILES_ONLY);

    loadImageButton.addActionListener(e -> {
      if (loadImageFileChooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          imageEncodeDecode.loadImage(loadImageFileChooser.getSelectedFile());
          loadImage();
          updateButtons();
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(contentPane, "Cannot load image", loadImageFileChooser.getDialogTitle(), JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    saveImageButton.addActionListener(e -> {
      saveImageFileChooser.setSelectedFile(new File(imageEncodeDecode.getImageFilename()));
      if (saveImageFileChooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          imageEncodeDecode.saveImage(saveImageFileChooser.getSelectedFile());
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(contentPane, "Cannot save image", saveImageFileChooser.getDialogTitle(), JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    loadMessageButton.addActionListener(e -> {
      if (loadMessageFileChooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          imageEncodeDecode.loadMessage(loadMessageFileChooser.getSelectedFile());
          loadMessage();
          updateButtons();
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(contentPane, "Cannot load message", loadMessageFileChooser.getDialogTitle(), JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    saveMessageButton.addActionListener(e -> {
      saveMessageFileChooser.setSelectedFile(new File(imageEncodeDecode.getMessageFilename()));
      if (saveMessageFileChooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          imageEncodeDecode.saveMessage(saveMessageFileChooser.getSelectedFile());
        }
        catch (IOException ex) {
          JOptionPane.showMessageDialog(contentPane, "Cannot save message", saveMessageFileChooser.getDialogTitle(), JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    encodeButton.addActionListener(e -> {
      JPasswordField passwordField = new JPasswordField();
      if (JOptionPane.showConfirmDialog(contentPane, passwordField, "Encode - Enter stego-key", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
        JDialog dialog = new JDialog();
        dialog.setTitle("BPCS Steganography");
        dialog.setContentPane(new GUIMain(imageEncodeDecode.encodeImage(new String(passwordField.getPassword()))).contentPane);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
      }
    });

    decodeButton.addActionListener(e -> {
      JPasswordField passwordField = new JPasswordField();
      if (JOptionPane.showConfirmDialog(contentPane, passwordField, "Decode - Enter stego-key", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
        JDialog dialog = new JDialog();
        dialog.setTitle("BPCS Steganography");
        dialog.setContentPane(new GUIMain(imageEncodeDecode.decodeImage(new String(passwordField.getPassword()))).contentPane);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
      }
    });
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      JOptionPane.showMessageDialog(null, "Cannot set to system Look and Feel", "Look and Feel", JOptionPane.WARNING_MESSAGE);
    }

    JFrame frame = new JFrame("BPCS Steganography");
    frame.setContentPane(new GUIMain(new ImageEncodeDecode()).contentPane);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}