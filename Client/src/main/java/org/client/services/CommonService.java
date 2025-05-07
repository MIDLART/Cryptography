package org.client.services;

import javafx.scene.control.Label;

public class CommonService {
  public static void showError(Label label, String message) {
    label.setText(message);
    label.setStyle("-fx-text-fill: red;");
  }

  public static void showSuccess(Label label, String message) {
    label.setText(message);
    label.setStyle("-fx-text-fill: green;");
  }

  public static void clearLabel(Label label) {
    label.setText("");
  }
}
