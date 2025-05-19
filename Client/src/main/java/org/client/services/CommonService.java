package org.client.services;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class CommonService {
  public static void showError(Label label, String message) {
    Platform.runLater(() -> {
      label.setText(message);
      label.setStyle("-fx-text-fill: red;");
    });
  }

  public static void showSuccess(Label label, String message) {
    Platform.runLater(() -> {
      label.setText(message);
      label.setStyle("-fx-text-fill: green;");
    });
  }

  public static void clearLabel(Label label) {
    Platform.runLater(() ->
            label.setText(""));
  }
}
