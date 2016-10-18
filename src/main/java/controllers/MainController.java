package controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import models.CityMap;
import models.DeliveryRequest;
import models.Planning;
import services.xml.Parser;

import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {
    final private ReadOnlyObjectWrapper<MainControllerState> state = new ReadOnlyObjectWrapper<>();
    final private SimpleObjectProperty<CityMap> cityMap = new SimpleObjectProperty<>();
    final private SimpleObjectProperty<DeliveryRequest> deliveryRequest = new SimpleObjectProperty<>();
    final private SimpleObjectProperty<Planning> planning = new SimpleObjectProperty<>();
    final private Parser parserService = new Parser();
    final private SimpleDoubleProperty mapZoom = new SimpleDoubleProperty(1.0);

    @FXML
    private BorderPane root;
    @FXML
    private Button openCityMapButton;
    @FXML
    private Button openDeliveryRequestButton;
    @FXML
    private Button computePlanningButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setState(new WaitOpenCityMapState());
        this.openDeliveryRequestButton.disableProperty().bind(this.cityMap.isNull());
        this.computePlanningButton.disableProperty().bind(this.deliveryRequest.isNull());
    }

    protected Parent getRoot() {
        return this.root;
    }

    protected Parser getParserService() {
        return this.parserService;
    }

    // CityMap
    public SimpleObjectProperty<CityMap> cityMapProperty() {
        return this.cityMap;
    }

    public CityMap getCityMap() {
        return cityMapProperty().getValue();
    }

    public void setCityMap(CityMap cityMap) {
        cityMapProperty().setValue(cityMap);
    }

    // Planning
    public SimpleObjectProperty<Planning> planningProperty() {
        return this.planning;
    }

    public Planning getPlanning() {
        return planningProperty().getValue();
    }

    public void setPlanning(Planning planning) {
        planningProperty().setValue(planning);
    }

    // mapZoom
    public SimpleDoubleProperty mapZoomProperty() {
        return this.mapZoom;
    }

    public double getMapZoom() {
        return mapZoomProperty().getValue();
    }

    public void setMapZoom(double mapZoom) {
        mapZoomProperty().setValue(mapZoom);
    }

    public ObservableObjectValue<DeliveryRequest> deliveryRequestProperty() {
        return this.deliveryRequest;
    }

    public DeliveryRequest getDeliveryRequest() {
        return this.deliveryRequest.getValue();
    }

    public void setDeliveryRequest(DeliveryRequest deliveryRequest) {
        this.deliveryRequest.setValue(deliveryRequest);
    }

    @FXML
    protected ObservableObjectValue<MainControllerState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    protected MainControllerState getState() {
        return this.state.getValue();
    }

    private void setState(MainControllerState state) {
        this.state.setValue(state);
    }

    private void applyState(MainControllerState nextState) {
        MainControllerState currentState = this.getState();
        if (currentState == nextState) {
            return;
        }
        currentState.leaveState(this);
        nextState.enterState(this);
        this.setState(nextState);
    }

    // handlers
    public void onOpenCityMapButtonAction(ActionEvent actionEvent) {
        this.applyState(this.getState().onOpenCityMapButtonAction(this));
    }

    public void onOpenDeliveryRequestButtonAction(ActionEvent actionEvent) {
        this.applyState(this.getState().onOpenDeliveryRequestButtonAction(this));
    }

    public void onComputePlanningButtonAction(ActionEvent actionEvent) {
        this.applyState(this.getState().onComputePlanningButtonAction(this));
    }
}
