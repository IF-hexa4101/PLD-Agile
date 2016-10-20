package components.mapcanvas;

import com.google.java.contract.Ensures;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import models.CityMap;
import models.DeliveryAddress;
import models.DeliveryRequest;
import models.Intersection;
import models.Planning;
import models.Route;
import models.StreetSection;
import models.Warehouse;

import java.util.List;

public class MapCanvasController extends Canvas {
    private static final CityMap DEFAULT_CITY_MAP = null;
    private static final DeliveryRequest DEFAULT_DELIVERY_REQUEST = null;
    private static final Planning DEFAULT_PLANNING = null;
    private static final double DEFAULT_ZOOM = 1.0;
    private static final double DEFAULT_OFFSET_X = 0.0;
    private static final double DEFAULT_OFFSET_Y = 0.0;

    private SimpleDoubleProperty zoom;
    private SimpleDoubleProperty offsetX;
    private SimpleDoubleProperty offsetY;
    private SimpleObjectProperty<CityMap> cityMap;
    private SimpleObjectProperty<DeliveryRequest> deliveryRequest;
    private SimpleObjectProperty<Planning> planning;

    public MapCanvasController() {
        widthProperty().addListener(event -> draw());
        heightProperty().addListener(event -> draw());
        zoomProperty().addListener(event -> draw());
        offsetXProperty().addListener(event -> draw());
        offsetYProperty().addListener(event -> draw());
        cityMapProperty().addListener(event -> draw());
        deliveryRequestProperty().addListener(event -> draw());
        planningProperty().addListener(event -> draw());
    }

    @SuppressWarnings("restriction")
    private void clear() {
        double width = getWidth();
        double height = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setTransform(1, 0,  0, 1,0, 0);
        gc.clearRect(0, 0, width, height);
    }

    @SuppressWarnings("restriction")
    private void updateTransform() {
        double width = getWidth();
        double height = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setTransform(1, 0,  0, 1, 0, 0);
        gc.clearRect(0, 0, width, height);

    	CityMap map = getCityMap();
        List<Intersection> intersections = map.getIntersections();
        double xmax = 0;
        double ymax = 0;
        double xmin = 0;
        double ymin = 0;

		for (Intersection inter : intersections) {
			xmax = Math.max(xmax, inter.getX());
			ymax = Math.max(ymax, inter.getY());
			xmin = Math.min(xmin, inter.getX());
			ymin = Math.min(ymin, inter.getY());
		}

        double mapWidth = xmax-xmin;
        double mapHeight = ymax-ymin;

        double zoomX = width/mapWidth;
        double zoomY = height/mapHeight;

        double zoom = Math.min(zoomX,zoomY);

        gc.scale(zoom, zoom);
        
        gc.translate(-xmin-8, -ymin-8); //offset
    }


    @SuppressWarnings("restriction")
	private void draw() {
    	clear();

    	if (getCityMap() == null) {
            return;
        }

    	updateTransform();

        drawCityMap();
        if(getDeliveryRequest() == null) {
        	return;
        }
        drawDeliveryRequest();
        if(getPlanning() == null){
        	return;
        }
        drawPlanning();
    }


    @SuppressWarnings("restriction")
    @Ensures("getCityMap() != null")
	private void drawCityMap(){
        GraphicsContext gc = getGraphicsContext2D();
        CityMap map = getCityMap();
        List<Intersection> intersections = map.getIntersections();

		List<StreetSection> streetSections = map.getStreetSections();
		for (StreetSection section : streetSections) {
			gc.setLineWidth(2);
			gc.setStroke(Color.GREY);
			gc.strokeLine(section.getStartIntersection().getX(), section.getStartIntersection().getY(),
					section.getEndIntersection().getX(), section.getEndIntersection().getY());
		}

		for (Intersection inter : intersections){
			gc.fillOval(inter.getX()-5, inter.getY()-5, 10, 10);
		}
    }

    @SuppressWarnings("restriction")
    @Ensures("getDeliveryRequest() != null")
	private void drawDeliveryRequest(){
    	GraphicsContext gc = getGraphicsContext2D();
    	DeliveryRequest deliveryRequest = getDeliveryRequest();

    	Iterable<DeliveryAddress> listDeliveryAddresses = deliveryRequest.getDeliveryAddresses();
    	Warehouse warehouse = deliveryRequest.getWareHouse();

    	for(DeliveryAddress delivery : listDeliveryAddresses){
    		gc.setFill(Color.BLUE);
    		gc.fillOval(delivery.getIntersection().getX()-9, delivery.getIntersection().getY()-9, 18, 18);
    	}
    	gc.setFill(Color.RED);
    	gc.fillOval(warehouse.getIntersection().getX()-9, warehouse.getIntersection().getY()-9, 18, 18);
    	gc.setFill(Color.BLACK);
    }

	@SuppressWarnings("restriction")
    @Ensures("getPlanning() != null")
	private void drawPlanning(){

    	GraphicsContext gc = getGraphicsContext2D();

		Planning planning = getPlanning();
		System.out.println(planning.getFullTime());
		//planning.

    	Iterable<Route> listRoutes = planning.getRoutes();

    	int number = 1;
    	for(Route route : listRoutes){
    		gc.setStroke(Color.ORANGE);
    		List<StreetSection> streetSections = route.getStreetSections();
    		for (StreetSection section : streetSections) {
    			gc.setLineWidth(4);
    			gc.setStroke(Color.ORANGE);
    			gc.strokeLine(section.getStartIntersection().getX(), section.getStartIntersection().getY(),
    					section.getEndIntersection().getX(), section.getEndIntersection().getY());
    		}
    		gc.setLineWidth(3);
    		gc.setStroke(Color.BLACK);
    		gc.strokeText(""+number, route.getStartWaypoint().getIntersection().getX(), route.getStartWaypoint().getIntersection().getY());
    		gc.setStroke(Color.WHITE);
    		gc.setLineWidth(1);
    		gc.strokeText(""+number, route.getStartWaypoint().getIntersection().getX(), route.getStartWaypoint().getIntersection().getY());
    		number++;
    	}

    }


    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double width) {
        return width;
    }

    @Override
    public double prefHeight(double height) {
        return height;
    }

    /**
     * The cityMap to display
     *
     * @return The cityMap property
     */
    public final SimpleObjectProperty<CityMap> cityMapProperty() {
        if (cityMap == null) {
            cityMap = new SimpleObjectProperty<>(this, "cityMap", DEFAULT_CITY_MAP);
        }
        return cityMap;
    }

    /**
     * Set the city map
     *
     * @param value
     */
    public final void setCityMap(CityMap value) {
        cityMapProperty().setValue(value);
    }

    public final CityMap getCityMap() {
        return cityMap == null ? DEFAULT_CITY_MAP : cityMap.getValue();
    }

    /**
     * The deliveryRequest with waypoints to display
     *
     * @return The cityMap property
     */
    public final SimpleObjectProperty<DeliveryRequest> deliveryRequestProperty() {
        if (deliveryRequest == null) {
            deliveryRequest = new SimpleObjectProperty<>(this, "deliveryRequest", DEFAULT_DELIVERY_REQUEST);
        }
        return deliveryRequest;
    }

    /**
     * Set the delivery request
     *
     * @param value
     */
    public final void setDeliveryRequest(DeliveryRequest value) {
        deliveryRequestProperty().setValue(value);
    }

    public final DeliveryRequest getDeliveryRequest() {
        return deliveryRequest == null ? DEFAULT_DELIVERY_REQUEST : deliveryRequest.getValue();
    }

    /**
     * The cityMap to display
     *
     * @return The planning property
     */
    public final SimpleObjectProperty<Planning> planningProperty() {
        if (planning == null) {
            planning = new SimpleObjectProperty<>(this, "planning", DEFAULT_PLANNING);
        }
        return planning;
    }

    /**
     * Set the planning
     *
     * @param value
     */
    public final void setPlanning(Planning value) {
        planningProperty().bind(new SimpleObjectProperty<Planning>(value));
    }

    public final Planning getPlanning() {
        return planning == null ? DEFAULT_PLANNING : planning.getValue();
    }

    /**
     * The zoom factor to use.
     *
     * @return The zoom property
     */
    public final DoubleProperty zoomProperty() {
        if (zoom == null) {
            zoom = new SimpleDoubleProperty(this, "zoom", DEFAULT_ZOOM);
        }
        return zoom;
    }

    /**
     * Set the zoom factor of the map
     *
     * @param value
     */
    public final void setZoom(double value) {
        zoomProperty().bind(new SimpleDoubleProperty(value));
    }

    public final double getZoom() {
        return zoom == null ? DEFAULT_ZOOM : zoom.getValue();
    }

    /**
     * The offsetX of the map
     *
     * @return offsetX property
     */
    public final DoubleProperty offsetXProperty() {
        if (offsetX == null) {
            offsetX = new SimpleDoubleProperty(this, "offsetX", DEFAULT_OFFSET_X);
        }
        return offsetX;
    }

    /**
     * Set the offset x of the map
     *
     * @param value offsetX value
     */
    public final void setOffsetX(double value) {
        offsetXProperty().setValue(value);
    }

    public final double getOffsetX() {
        return offsetX == null ? DEFAULT_OFFSET_X : offsetX.getValue();
    }

    /**
     * The offset y of the map
     *
     * @return offsetY property
     */
    public final DoubleProperty offsetYProperty() {
        if (offsetY == null) {
            offsetY = new SimpleDoubleProperty(this, "offsetY", DEFAULT_OFFSET_Y);
        }
        return offsetY;
    }

    /**
     * Set the offset y of the map
     *
     * @param value offsetY value
     */
    public final void setOffsetY(double value) {
        offsetYProperty().setValue(value);
    }

    public final double getOffsetY() {
        return offsetY == null ? DEFAULT_OFFSET_Y : offsetY.getValue();
    }

//    public void mousePressed(MouseEvent mouseEvent) {
//        System.out.println("Start drag");
//    }
//
//    public void mouseDragged(MouseEvent mouseEvent) {
//        System.out.println("dragged");
//    }
//
//    public void mouseReleased(MouseEvent mouseEvent) {
//        System.out.println("Released");
//    }
}
