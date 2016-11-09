package components.mapcanvas;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import models.CityMap;
import models.DeliveryAddress;
import models.DeliveryRequest;
import models.Intersection;
import models.Planning;
import models.Route;
import models.StreetSection;
import models.Warehouse;

import java.awt.Rectangle;
import java.util.List;

public class MapCanvas extends Canvas {
    private static final CityMap DEFAULT_CITY_MAP = null;
    private static final DeliveryRequest DEFAULT_DELIVERY_REQUEST = null;
    private static final Planning DEFAULT_PLANNING = null;
    private static final double DEFAULT_ZOOM = 1.0;
    private static final double DEFAULT_OFFSET_X = 8.0;
    private static final double DEFAULT_OFFSET_Y = 8.0;
    private static final int MARGIN_ERROR = 0;
    private static final double DEFAULT_INTERSECTION_SIZE = 10;
    private static final double DEFAULT_DELIVERY_SIZE = 18;

    private SimpleDoubleProperty zoom;
    private SimpleDoubleProperty offsetX;
    private SimpleDoubleProperty offsetY;
    private SimpleObjectProperty<CityMap> cityMap;
    private SimpleObjectProperty<DeliveryRequest> deliveryRequest;
    private SimpleObjectProperty<Planning> planning;
    private List<Intersection> intersections;
    private Iterable<DeliveryAddress> listDeliveryAddresses;
    private double calZoom;
    private final ListChangeListener<Route> planningChangeListener;

    @SuppressWarnings("restriction")
    public MapCanvas() {
        final MapCanvas self = this;

        this.planningChangeListener = change -> self.draw();

        widthProperty().addListener(event -> draw());
        heightProperty().addListener(event -> draw());
        zoomProperty().addListener(event -> draw());
        offsetXProperty().addListener(event -> draw());
        offsetYProperty().addListener(event -> draw());
        cityMapProperty().addListener(event -> draw());
        deliveryRequestProperty().addListener(event -> draw());
        planningProperty().addListener((observableValue, oldPlanning, newPlanning) -> {
            if (oldPlanning != null) {
                oldPlanning.routesProperty().removeListener(self.planningChangeListener);
            }
            if (newPlanning != null) {
                newPlanning.routesProperty().addListener(self.planningChangeListener);
            }
            self.draw();
        });

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                double eventX = e.getX();
                double eventY = e.getY();
                eventX /= calZoom;
                eventY /= calZoom;
                eventX += DEFAULT_OFFSET_X;
                eventY += DEFAULT_OFFSET_Y;

                if(getDeliveryRequest() != null){
                	Warehouse warehouse = getDeliveryRequest().getWarehouse();
                	if (eventX < warehouse.getX() + DEFAULT_DELIVERY_SIZE / 2 && eventX > warehouse.getX() - DEFAULT_DELIVERY_SIZE / 2
                            && eventY < warehouse.getY() + DEFAULT_DELIVERY_SIZE / 2 && eventY > warehouse.getY() - DEFAULT_DELIVERY_SIZE / 2){
                		WarehouseSelectionEvent deliver = new WarehouseSelectionEvent(warehouse, e.getX(), e.getY());
                        fireEvent(deliver);
                        IntersectionSelectionEvent nullIntersect = new IntersectionSelectionEvent(null, e.getX(), e.getY());
                        fireEvent(nullIntersect);
                        return;
                	}
                    listDeliveryAddresses = getDeliveryRequest().getDeliveryAddresses();
                    for(DeliveryAddress delivery : listDeliveryAddresses){
                        if (eventX < delivery.getX() + DEFAULT_DELIVERY_SIZE / 2 && eventX > delivery.getX() - DEFAULT_DELIVERY_SIZE / 2
                            && eventY < delivery.getY() + DEFAULT_DELIVERY_SIZE / 2 && eventY > delivery.getY() - DEFAULT_DELIVERY_SIZE / 2) {
                            DeliverySelectionEvent deliver = new DeliverySelectionEvent(delivery, e.getX(), e.getY());
                            fireEvent(deliver);
                            IntersectionSelectionEvent nullIntersect = new IntersectionSelectionEvent(null, e.getX(), e.getY());
                            fireEvent(nullIntersect);
                            return;
                        }
                    }
                }

                for (Intersection inter : intersections) {
                    if (eventX < inter.getX() + DEFAULT_INTERSECTION_SIZE / 2 && eventX > inter.getX() - DEFAULT_INTERSECTION_SIZE / 2
                        && eventY < inter.getY() + DEFAULT_INTERSECTION_SIZE / 2 && eventY > inter.getY() - DEFAULT_INTERSECTION_SIZE / 2) {
                        IntersectionSelectionEvent intersect = new IntersectionSelectionEvent(inter, e.getX(), e.getY());
                        fireEvent(intersect);
                        DeliverySelectionEvent nullDeliver = new DeliverySelectionEvent(null, e.getX(), e.getY());
                        fireEvent(nullDeliver);
                        return;
                    }
                }
                IntersectionSelectionEvent intersect = new IntersectionSelectionEvent(null, e.getX(), e.getY());
                fireEvent(intersect);
            }
        });
    }

    @SuppressWarnings("restriction")
    private void clear() {
        double width = getWidth();
        double height = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.clearRect(0, 0, width, height);
    }

    @SuppressWarnings("restriction")
    private void updateTransform() {
        double width = getWidth();
        double height = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.clearRect(0, 0, width, height);

        CityMap map = getCityMap();
        intersections = map.getIntersections();

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

        double mapWidth = xmax - xmin;
        double mapHeight = ymax - ymin;

        double zoomX = width / mapWidth;
        double zoomY = height / mapHeight;

        double zoom = Math.min(zoomX, zoomY);

        gc.scale(zoom, zoom);
        calZoom = zoom;

        gc.translate(-xmin - DEFAULT_OFFSET_X, -ymin - DEFAULT_OFFSET_Y); //offset
    }


    @SuppressWarnings("restriction")
    private void draw() {
        clear();

        if (getCityMap() == null) {
            return;
        }

        updateTransform();

        drawCityMap();
        if (getDeliveryRequest() == null) {
            return;
        }
        drawDeliveryRequest();
        if (getPlanning() == null) {
            return;
        }
        drawPlanning();
    }


    @SuppressWarnings("restriction")
    @Requires("getCityMap() != null")
    private void drawCityMap() {
        GraphicsContext gc = getGraphicsContext2D();
        CityMap map = getCityMap();
        intersections = map.getIntersections();

        List<StreetSection> streetSections = map.getStreetSections();
        for (StreetSection section : streetSections) {
            gc.setLineWidth(2);
            gc.setStroke(Color.GREY);
            gc.strokeLine(section.getStartIntersection().getX(), section.getStartIntersection().getY(),
                section.getEndIntersection().getX(), section.getEndIntersection().getY());
        }

        for (Intersection inter : intersections) {
            gc.fillOval(inter.getX() - DEFAULT_INTERSECTION_SIZE / 2, inter.getY() - DEFAULT_INTERSECTION_SIZE / 2,
                DEFAULT_INTERSECTION_SIZE, DEFAULT_INTERSECTION_SIZE);
        }
    }

    @SuppressWarnings("restriction")
    @Requires("getDeliveryRequest() != null")
    private void drawDeliveryRequest() {
        GraphicsContext gc = getGraphicsContext2D();
        DeliveryRequest deliveryRequest = getDeliveryRequest();

        Iterable<DeliveryAddress> listDeliveryAddresses = deliveryRequest.getDeliveryAddresses();
        Warehouse warehouse = deliveryRequest.getWarehouse();

        for (DeliveryAddress delivery : listDeliveryAddresses) {
            gc.setFill(Color.BLUE);
            gc.fillOval(delivery.getIntersection().getX() - DEFAULT_DELIVERY_SIZE / 2, delivery.getIntersection().getY() - DEFAULT_DELIVERY_SIZE / 2,
                DEFAULT_DELIVERY_SIZE, DEFAULT_DELIVERY_SIZE);
        }
        gc.setFill(Color.RED);
        gc.fillOval(warehouse.getIntersection().getX() - DEFAULT_DELIVERY_SIZE / 2, warehouse.getIntersection().getY() - DEFAULT_DELIVERY_SIZE / 2,
            DEFAULT_DELIVERY_SIZE, DEFAULT_DELIVERY_SIZE);
        gc.setFill(Color.BLACK);
    }

    @SuppressWarnings("restriction")
    @Requires("getPlanning() != null")
    private void drawPlanning() {
    	Color currentColor =  Color.BLUE;
    	
        GraphicsContext gc = getGraphicsContext2D();

        Planning planning = getPlanning();

        Iterable<Route> listRoutes = planning.getRoutes();

        int number = 1;
        int countSections = 1;
        int totalSections = 0;
        for (Route route : listRoutes) {
            List<StreetSection> streetSections = route.getStreetSections();
            
               totalSections += streetSections.size();
            
        }
        
        for (Route route : listRoutes) {
            gc.setStroke(currentColor);
            List<StreetSection> streetSections = route.getStreetSections();
            
            for (StreetSection section : streetSections) {
                gc.setLineWidth(4);
                
                currentColor = getColor(currentColor, countSections++, totalSections);
                
                gc.setStroke(currentColor);
                gc.strokeLine(section.getStartIntersection().getX(), section.getStartIntersection().getY(),
                    section.getEndIntersection().getX(), section.getEndIntersection().getY());
                drawArrowBetweenStreetSection(section, currentColor);
            }
        }
        drawDeliveryRequest();
        for(Route route : listRoutes){
            gc.setLineWidth(3);
            gc.setStroke(Color.BLACK);
            gc.strokeText("" + number, route.getStartWaypoint().getIntersection().getX(), route.getStartWaypoint().getIntersection().getY());
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeText("" + number, route.getStartWaypoint().getIntersection().getX(), route.getStartWaypoint().getIntersection().getY());
            number++;
        }

    }
    
    public Color getColor(Color color, int step, int nbStep){
    	
    	if(step*3 < nbStep){
    		color =  new Color((color.getRed()+0.05)%1, color.getGreen(), color.getBlue(), color.getOpacity());
    	}
    	else if(step*3 < 2*nbStep){
    		color =  new Color(color.getRed(), (color.getGreen()+0.05)%1, color.getBlue(), color.getOpacity());
    	}
    	else{
    		color =  new Color(color.getRed(), color.getGreen(), (color.getBlue()+0.05)%1, color.getOpacity());
    	}
    	
    	
    	
    	return color;
    }
    
    
    public void drawArrowBetweenStreetSection(StreetSection street, Color color){

        double xStart = street.getStartIntersection().getX();
        double xEnd = street.getEndIntersection().getX();
        double yStart = street.getStartIntersection().getY();
        double yEnd = street.getEndIntersection().getY();
        
        double alpha = Math.atan2(yStart-yEnd,xStart-xEnd);
        System.out.println("alpha = " +alpha);
        
        double xthird = (xStart + 2*xEnd)/3;
        double ythird = (yStart + 2*yEnd)/3;
        
        double lengthCross = 10 ;
        double xCross1 = xthird + lengthCross * Math.cos(alpha+Math.PI/6);
        double yCross1 = ythird + lengthCross * Math.sin(alpha+Math.PI/6);
        double xCross2 = xthird + lengthCross * Math.cos(alpha+11*Math.PI/6);
        double yCross2 = ythird + lengthCross * Math.sin(alpha+11*Math.PI/6);
        
        GraphicsContext gc = getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.strokeLine(xthird, ythird, xCross1, yCross1);
        gc.strokeLine(xthird, ythird, xCross2, yCross2);
            
        
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

    /*
    public void mousePressed(MouseEvent mouseEvent) {
        System.out.println("Start drag"+mouseEvent.getX()+" "+mouseEvent.getY());

    }

    public void mouseDragged(MouseEvent mouseEvent) {
        System.out.println("dragged");
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        System.out.println("Released");
    }*/
}
