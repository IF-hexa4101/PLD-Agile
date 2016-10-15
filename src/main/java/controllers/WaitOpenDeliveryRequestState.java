package controllers;


import com.sun.istack.internal.NotNull;
import javafx.stage.FileChooser;
import models.DeliveryRequest;

import java.io.File;

public class WaitOpenDeliveryRequestState extends WaitOpenCityMapState {
    public void enterState(@NotNull MainController mainController) {
        mainController.setCurrentDeliveryRequest(null); // Reset any previous delivery request
    }

    public void leaveState(@NotNull MainController mainController) {

    }

    public @NotNull MainControllerState onOpenDeliveryRequestButtonAction(@NotNull MainController mainController) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Delivery Request");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Delivery Request file (*.xml)", "xml"));
        File deliverRequestFile = fileChooser.showOpenDialog(mainController.getRoot().getScene().getWindow());

        if (deliverRequestFile == null) { // User cancelled operation
            return this;
        }

        DeliveryRequest currentDeliveryRequest = mainController.getParserService().getDeliveryRequest(deliverRequestFile, mainController.getCurrentCityMap());

        mainController.setCurrentDeliveryRequest(currentDeliveryRequest);

        return new ReadyState();
    }
}
