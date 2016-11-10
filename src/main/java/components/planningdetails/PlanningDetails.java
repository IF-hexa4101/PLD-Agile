package components.planningdetails;

import components.events.AddWaypointAction;
import components.events.CancelAddWaypointAction;
import components.events.RemoveWaypointAction;
import components.events.SaveDeliveryAddress;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import models.AbstractWaypoint;
import models.Planning;
import models.Route;
import org.jetbrains.annotations.NotNull;
import services.map.IMapService;

import java.io.IOException;

public class PlanningDetails extends ScrollPane {
    @FXML
    protected VBox planningDetailsVBox;
    private final SimpleObjectProperty<Planning> planning = new SimpleObjectProperty<>(this, "planning", null);
    private final SimpleObjectProperty<IMapService> mapService = new SimpleObjectProperty<>(this, "mapService", null);
    private final ReadOnlyObjectWrapper<IPlanningDetailsState> state = new ReadOnlyObjectWrapper<>(this, "state", new DefaultState(this));

    public PlanningDetails() {
        super();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/components/planningdetails/PlanningDetails.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.refreshView();
        this.planningProperty().addListener(this::onPlanningChange);
        this.addEventHandler(AddWaypointAction.TYPE, this::onAddWaypointButtonAction);
        this.addEventHandler(RemoveWaypointAction.TYPE, this::onRemoveWaypointButtonAction);
        this.addEventHandler(CancelAddWaypointAction.TYPE, this::onCancelAddWaypointButtonAction);
        this.addEventHandler(SaveDeliveryAddress.TYPE, this::onSaveNewWaypoint);
    }

    public final SimpleObjectProperty<Planning> planningProperty() {
        return this.planning;
    }

    public final void setPlanning(Planning value) {
        this.planningProperty().setValue(value);
    }

    public final Planning getPlanning() {
        return this.planningProperty().getValue();
    }

    public SimpleObjectProperty<IMapService> mapServiceProperty() {
        return this.mapService;
    }

    public IMapService getMapService() {
        return this.mapServiceProperty().getValue();
    }

    public void setMapService(IMapService value) {
        this.mapServiceProperty().setValue(value);
    }

    @NotNull
    public ReadOnlyObjectProperty<IPlanningDetailsState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    @NotNull
    public IPlanningDetailsState getState() {
        return this.state.getValue();
    }

    protected void setState(@NotNull IPlanningDetailsState value) {
        this.state.setValue(value);
    }

    protected void onPlanningChange(ObservableValue<? extends Planning> observable, Planning oldValue, Planning newValue) {
        this.changeState(this.getState().onPlanningChange(observable, oldValue, newValue));
    }

    protected void onPlanningWaypointsChange(ListChangeListener.Change<? extends AbstractWaypoint> listChange) {
        this.changeState(this.getState().onPlanningWaypointsChange(listChange));
    }

    public void onAddWaypointButtonAction(AddWaypointAction action) {
        System.out.println("Adding");
        this.changeState(this.getState().onAddWaypointAction(action));
    }

    public void onCancelAddWaypointButtonAction(CancelAddWaypointAction action) {
        System.out.println("Cancelling");
        this.changeState(this.getState().onCancelAddWaypointAction(action));
    }

    public void onRemoveWaypointButtonAction(RemoveWaypointAction action) {
        this.changeState(this.getState().onRemoveWaypointAction(action));
    }

    public void onSaveNewWaypoint(SaveDeliveryAddress action) {
        this.changeState(this.getState().onSaveNewWaypoint(action));
    }

    protected void changeState(@NotNull IPlanningDetailsState nextState) {
        IPlanningDetailsState currentState = this.getState();
        if (currentState == nextState) {
            return;
        }
        currentState.leaveState(nextState);
        this.setState(nextState);
        nextState.enterState(currentState);
    }

    protected void waypointsToPlanningDetails() {
        final ObservableList<Node> itemNodes = this.planningDetailsVBox.getChildren();
        itemNodes.clear();
        final Planning planning = this.getPlanning();
        if (planning == null) {
            return;
        }
        final ObservableList<Route> routes = planning.getRoutes();
        if (routes == null) {
            return;
        }

        int index = 0;
        for (Route item : routes) {
            final PlanningDetailsItem node = new PlanningDetailsItem();
            node.setIndex(index++);
            node.setItem(item);
            node.setPlanning(planning);
            itemNodes.add(node);
        }
    }

    protected void refreshView() {
        this.getState().refreshView();
    }
}
