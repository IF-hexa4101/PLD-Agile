package models;

public class DeliveryAddress extends AbstractWayPoint {

    final private int deliveryDuration;

    @Override
    public int getDuration() {
        return this.deliveryDuration;
    }

    public DeliveryAddress(Intersection intersection, int deliveryDuration) {
        super(intersection);
        this.deliveryDuration = deliveryDuration;
    }

    @Deprecated
    public int getDeliveryDuration() {
        return deliveryDuration;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeliveryAddress))
            return false;

        DeliveryAddress other = (DeliveryAddress) obj;
        return this.deliveryDuration == other.deliveryDuration && super.equals(other);
    }

    @Override
    public String toString() {
        //return "DeliveryAddress [intersection=" + intersection + ", deliveryDuration=" + deliveryDuration + "]";
        return "" + getId();
    }
}
