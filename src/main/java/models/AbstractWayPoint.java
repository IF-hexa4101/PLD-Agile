package models;

import com.google.java.contract.Requires;

public abstract class AbstractWayPoint implements Comparable<AbstractWayPoint> {

    final protected Intersection intersection;
    private int deliveryTimeStart;
    private int deliveryTimeEnd;

    public AbstractWayPoint(Intersection intersection, int deliveryTimeStart, int deliveryTimeEnd) {
        this.intersection = intersection; // TODO clone to avoid a later modification?
        this.deliveryTimeStart = deliveryTimeStart;
        this.deliveryTimeEnd = deliveryTimeEnd;
    }
    
    public AbstractWayPoint(Intersection intersection) {
        this.intersection = intersection; // TODO clone to avoid a later modification?
        this.deliveryTimeStart = 0;
        this.deliveryTimeEnd = 86400;
    }

    public abstract int getDuration();
    
    public int getDeliveryTimeStart() {
    	return deliveryTimeStart;
    }
    
    public int getDeliveryTimeEnd() {
    	return deliveryTimeEnd;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public int getX() {
        return intersection.getX();
    }

    public int getY() {
        return intersection.getY();
    }
    
    protected int getId() {
        return intersection.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractWayPoint))
            return false;

        AbstractWayPoint other = (AbstractWayPoint) obj;
        return getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return new Integer(this.getId()).hashCode();
    }

    @Override
    @Requires({ "other != null" })
    public int compareTo(AbstractWayPoint other) {
        return getId() - other.getId();
    }
}
