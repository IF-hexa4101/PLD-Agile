package models;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;

import java.util.*;

public class CityMap {

    /**
     * Key: `intersectionId` Value: The associated Intersection
     */
    private Map<Integer, Intersection> intersections;

    /**
     * Key: `startIntersectionId` Value: Map: - Key: `endIntersectionId` - Value: `StreetSection`, the StreetSection from the intersection
     * with the id `startIntersectionId` to the intersection with the id `endIntersectionId`
     */
    private Map<Integer, Map<Integer, StreetSection>> streetSections;

    public CityMap(Iterable<Intersection> intersections, Iterable<StreetSection> streetSections) {
        this.intersections = new TreeMap<Integer, Intersection>();
        this.streetSections = new TreeMap<Integer, Map<Integer, StreetSection>>();

        for (Intersection intersection : intersections) {
            addIntersection(intersection);
        }
        for (StreetSection streetSection : streetSections) {
            addStreetSection(streetSection);
        }
    }

    @Requires({"intersection != null", "!intersections.containsKey(intersection.getId())"})
    @Ensures({"intersections.containsKey(intersection.getId())", "intersections.containsValue(intersection)",
        "intersections.get(intersection.getId()) == intersection"})
    private void addIntersection(Intersection intersection) {
        intersections.put(intersection.getId(), intersection);
    }

    @Requires({"streetSection != null", "intersections.containsKey(streetSection.getStartIntersection().getId())",
        "intersections.containsKey(streetSection.getEndIntersection().getId())"})
    private void addStreetSection(StreetSection streetSection) {
        Map<Integer, StreetSection> streetSectionsFromStartIntersection = streetSections.get(streetSection.getStartIntersection().getId());
        if (streetSectionsFromStartIntersection == null) {
            streetSectionsFromStartIntersection = new TreeMap<Integer, StreetSection>();
        }

        streetSectionsFromStartIntersection.put(streetSection.getEndIntersection().getId(), streetSection);
        streetSections.put(streetSection.getStartIntersection().getId(), streetSectionsFromStartIntersection);
    }

    // TODO Complexity to improve by using a min-heap for the greys intersections
    @Requires({"startWaypoint != null", "endWaypoints != null",
        "intersections.containsValue(startWaypoint.getIntersection())"})
    protected List<Route> shortestPath(AbstractWaypoint startWaypoint, List<AbstractWaypoint> endWaypoints) {
        Map<Integer, Integer> index = new TreeMap<Integer, Integer>();
        int counter = 0;
        for (Map.Entry<Integer, Intersection> entry : intersections.entrySet()) {
            index.put(entry.getKey(), counter++);
        }

        Intersection[] predecessors = new Intersection[intersections.size()];
        int[] durations = new int[intersections.size()];
        Map<Integer, Intersection> blacks = new TreeMap<Integer, Intersection>();
        List<Intersection> greys = new LinkedList<Intersection>();
        Map<Integer, Intersection> whites = new TreeMap<Integer, Intersection>(intersections);

        whites.remove(startWaypoint.getId());
        greys.add(startWaypoint.getIntersection());
        for (int i = 0; i < durations.length; i++) {
            durations[i] = Integer.MAX_VALUE;
        }
        durations[index.get(startWaypoint.getId())] = 0;

        while (greys.size() != 0) {
            Intersection minimalGreyIntersection = getMinimalGreyIntersection(index, greys, durations);
            for (Intersection successor : getNeighbourIntersection(minimalGreyIntersection)) {
                if (!blacks.containsKey(successor.getId())) {
                    StreetSection streetSection = getStreetSection(minimalGreyIntersection, successor);
                    release(index, streetSection, predecessors, durations);
                    if (whites.containsKey(successor.getId())) {
                        whites.remove(successor.getId());
                        greys.add(successor);
                    }
                }
            }
            greys.remove(minimalGreyIntersection);
            blacks.put(minimalGreyIntersection.getId(), minimalGreyIntersection);
        }

        return computeReturn(index, predecessors, startWaypoint, endWaypoints);
    }

    // TODO Improve complexity
    @Requires({"greys != null", "durations != null", "greys.size() <= durations.length"})
    @Ensures({"greys.contains(result)"})
    private Intersection getMinimalGreyIntersection(Map<Integer, Integer> index, List<Intersection> greys, int[] durations) {
        int minDuration = Integer.MAX_VALUE;
        Intersection minimalGreyIntersection = null;

        for (Intersection greyIntersection : greys) {
            if (durations[index.get(greyIntersection.getId())] < minDuration) {
                minDuration = durations[index.get(greyIntersection.getId())];
                minimalGreyIntersection = greyIntersection;
            }
        }
        return minimalGreyIntersection;
    }

    @Requires({
        "streetSection != null",
        "predecessors != null",
        "durations != null",
        "predecessors.length == durations.length"})
    @Ensures({"durations[index.get(streetSection.getEndIntersection().getId())] <= "
        + "durations[index.get(streetSection.getStartIntersection().getId())] + streetSection.getDuration()"})
    private void release(Map<Integer, Integer> index, StreetSection streetSection, /* IN/OUT */ Intersection[] predecessors, int[] durations) {
        int idStartIntersection = streetSection.getStartIntersection().getId();
        int idEndIntersection = streetSection.getEndIntersection().getId();

        if (durations[index.get(idEndIntersection)] > durations[index.get(idStartIntersection)] + streetSection.getDuration()) {
            durations[index.get(idEndIntersection)] = durations[index.get(idStartIntersection)] + streetSection.getDuration();
            predecessors[index.get(idEndIntersection)] = streetSection.getStartIntersection();
        }
    }

    @Requires({"endWaypoints != null", "startWaypoint != null"})
    private List<Route> computeReturn(Map<Integer, Integer> index, Intersection[] predecessors, AbstractWaypoint startWaypoint, List<AbstractWaypoint> endWaypoints) {
        List<Route> result = new ArrayList<Route>();
        for (AbstractWaypoint endWaypoint : endWaypoints) {
            List<StreetSection> streetSectionsInCurrentRoute = new LinkedList<StreetSection>();

            Intersection currentIntersection = endWaypoint.getIntersection();
            Intersection precedentIntersection = predecessors[index.get(currentIntersection.getId())];

            while (precedentIntersection != null) {
                streetSectionsInCurrentRoute.add(0, getStreetSection(precedentIntersection, currentIntersection));
                currentIntersection = precedentIntersection;
                precedentIntersection = predecessors[index.get(currentIntersection.getId())];
            }

            result.add(new Route(startWaypoint, endWaypoint, streetSectionsInCurrentRoute));
        }
        return result;
    }

    @Requires({"intersections.containsValue(intersection)"})
    private Collection<Intersection> getNeighbourIntersection(Intersection intersection) {
        Map<Integer, StreetSection> outStreetSections = streetSections.get(intersection.getId());
        List<Intersection> neighbourIntersections = new ArrayList<Intersection>();

        if (outStreetSections != null) {
            for (Map.Entry<Integer, StreetSection> entry : outStreetSections.entrySet()) {
                neighbourIntersections.add(getIntersection(entry.getKey()));
            }
        }
        return neighbourIntersections;
    }

    @Requires({"request != null", "request.getWarehouse() != null", "request.getDeliveryAddresses() != null"})
    public DeliveryGraph computeDeliveryGraph(DeliveryRequest request) {
        List<AbstractWaypoint> pointsContainedInRequest = new LinkedList<AbstractWaypoint>();
        pointsContainedInRequest.add(request.getWarehouse());

        Iterable<DeliveryAddress> adressContainedInRequest = request.getDeliveryAddresses();
        for (DeliveryAddress adress : adressContainedInRequest) {
            pointsContainedInRequest.add(adress);
        }

        Map<AbstractWaypoint, Map<AbstractWaypoint, Route>> mappedRoutes = new TreeMap<AbstractWaypoint, Map<AbstractWaypoint, Route>>();
        for (int i = 0; i < pointsContainedInRequest.size(); i++) {
            //always get first element as it will be deleted and pushed back at the end of the list
            AbstractWaypoint startPoint = pointsContainedInRequest.get(0);
            Map<AbstractWaypoint, Route> routesFromGivenStartPoint = new TreeMap<AbstractWaypoint, Route>();
            //remove start point from list to prevent zero point route
            pointsContainedInRequest.remove(0);//remove startPoint from the list
            List<Route> shortestPathRoutes = shortestPath(startPoint, pointsContainedInRequest);
            pointsContainedInRequest.add(startPoint);//add startPoint back to the list (at the end)

            for (Route route : shortestPathRoutes) {
                routesFromGivenStartPoint.put(route.getEndWaypoint(), route);
            }
            mappedRoutes.put(startPoint, routesFromGivenStartPoint);
        }
        return new DeliveryGraph(request.getCityMap(), mappedRoutes);
    }

    public List<Intersection> getIntersections() {
        List<Intersection> listIntersection = new ArrayList<Intersection>();
        for (Intersection value : intersections.values()) {
            listIntersection.add(value);
        }
        return listIntersection;
    }

    public List<StreetSection> getStreetSections() {
        List<StreetSection> listStreetSection = new ArrayList<StreetSection>();
        for (Map<Integer, StreetSection> value : streetSections.values()) {
            for (StreetSection section : value.values())
                listStreetSection.add(section);
        }
        return listStreetSection;
    }

    /**
     * Returns the intersection with the supplied id.
     *
     * @param idIntersection The id of intersection to retrieve. The id must be in the map.
     * @return The intersection with the supplied id
     */
    @Requires({"intersections.containsKey(idIntersection)"})
    public Intersection getIntersection(int idIntersection) {
        return intersections.get(idIntersection);
    }

    public boolean isIntersectionInCityMap(int idIntersection) {
        return intersections.containsKey(idIntersection);
    }

    @Requires({"intersections.containsValue(startIntersection)", "intersections.containsValue(endIntersection)"})
    @Ensures({"result.getStartIntersection().equals(startIntersection)", "result.getEndIntersection().equals(endIntersection)"})
    private StreetSection getStreetSection(Intersection startIntersection, Intersection endIntersection) {
        Map<Integer, StreetSection> outStreetSections = streetSections.get(startIntersection.getId());
        if (outStreetSections != null) {
            return outStreetSections.get(endIntersection.getId());
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CityMap)) {
            return false;
        } else if (this == obj)
            return true;
        CityMap other = (CityMap) obj;

        if (intersections.size() != other.intersections.size())
            return false;
        for (Map.Entry<Integer, Intersection> entry : intersections.entrySet()) {
            if (!entry.getValue().equals(other.intersections.get(entry.getKey())))
                return false;
        }

        if (streetSections.size() != other.streetSections.size())
            return false;
        for (Map.Entry<Integer, Map<Integer, StreetSection>> entrySubMap : streetSections.entrySet()) {
            Map<Integer, StreetSection> otherSubMap = other.streetSections.get(entrySubMap.getKey());
            if (otherSubMap == null && entrySubMap.getValue() != null || otherSubMap != null && entrySubMap.getKey() == null)
                return false;
            if (otherSubMap == null)
                continue; // The two sub item are null, so they are equals
            if (otherSubMap.size() != entrySubMap.getValue().size())
                return false;

            for (Map.Entry<Integer, StreetSection> entry : entrySubMap.getValue().entrySet()) {
                if (!entry.getValue().equals(otherSubMap.get(entry.getKey())))
                    return false;
            }
        }

        return true;
    }
}
