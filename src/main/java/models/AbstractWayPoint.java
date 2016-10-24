package models;

import com.google.java.contract.Requires;

public abstract class AbstractWayPoint implements Comparable<AbstractWayPoint> {

    final protected Intersection intersection;
    private int deliveryTimeStart;
    private int deliveryTimeEnd;

    public AbstractWayPoint(Intersection intersection) {
        this.intersection = intersection; // TODO clone to avoid a later modification?
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
        return getIntersection().getX();
    }

    public int getY() {
        return getIntersection().getY();
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

    /**
     * @param timeOfPassage
     * @return true if tha time of passage is more than deliveryTimeStart and
     * time of passage plus delivery duration is less than deliveryTimeEnd
     * if timeOfPassage is greater than a day (86400 sec) is modulus by 86400
     * is used.
     */
    public boolean canBePassed(int timeOfPassage){
        timeOfPassage = timeOfPassage%86400;
        return deliveryTimeStart < timeOfPassage && deliveryTimeEnd > (timeOfPassage+this.getDuration());
    }

    protected int getId() {
        return this.intersection.getId();
    }
}
