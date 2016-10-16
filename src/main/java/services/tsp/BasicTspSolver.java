package services.tsp;

import models.*;

import java.util.*;

public class BasicTspSolver extends AbstractTspSolver {
    /**
     * The constructor for a basic TSP solver.
     * It doesn't need anything for now.
     */
    BasicTspSolver() {
        // Nothing to do
    }

    /**
     * Solve the TSP problem for the given DeliveryGraph.
     * IMPORTANT: we need to assume that nodes have an ID between 0 and graph.size().
     * @param graph The (complete) graph representing all delivery points and the warehouse.
     * @return The delivery plan (Planning) associated to the given DeliveryGraph.
     */
    @Override
    public Planning solve(DeliveryGraph graph) {
        // Initialize solver parameters
        this.bestSolutionCost = Integer.MAX_VALUE;
        this.bestSolution = new AbstractWayPoint[graph.size()];
        // Initialize unseen nodes
        ArrayList<AbstractWayPoint> unseen = graph.getNodes();
        // Initialize seen nodes
        ArrayList<AbstractWayPoint> seen = new ArrayList<AbstractWayPoint>(graph.size());
        // Let's say that the first seen node is the first one of the graph
        seen.add(graph.iterator().next().getKey());
        unseen.remove(graph.iterator().next().getKey());
        // Get the cost for all routes
        Map<AbstractWayPoint, Map<AbstractWayPoint, Integer>> costs = new HashMap<>();
        graph.iterator().forEachRemaining((startPoint) -> {
            costs.put(startPoint.getKey(), new HashMap<>());
            startPoint.getValue().entrySet().forEach((endPoint) -> {
                costs.get(startPoint).put(endPoint.getKey(), endPoint.getValue().getDuration());
            });
        });
        // Get the time needed to deliver each way point
        Map<AbstractWayPoint, Integer> deliveryDurations = graph.getDeliveryDurations();
        // Compute solution
        branchAndBound(graph.iterator().next().getKey(), unseen, seen, 0, costs, deliveryDurations);
        // Construct Planning based on the previous result
        List<Route> routes = new ArrayList<>(graph.size());
        for(int i = 0; i < graph.size(); i++) {
            routes.add(graph.getRoute(this.bestSolution[i], this.bestSolution[(i+1)%graph.size()]));
        }
        return new Planning(routes);
    }

    private void branchAndBound(AbstractWayPoint lastSeenNode, ArrayList<AbstractWayPoint> unseen, ArrayList<AbstractWayPoint> seen, int seenCost, Map<AbstractWayPoint, Map<AbstractWayPoint, Integer>> costs, Map<AbstractWayPoint, Integer> deliveryDurations) {
        /*if (unseen.size() == 0){ // tous les sommets ont ete visites
            seenCost += costs[lastSeenNodeId][0];
            if (seenCost < this.bestSolutionCost){ // on a trouve une solution meilleure que meilleureSolution
                seen.toArray(this.bestSolution);
                this.bestSolutionCost = seenCost;
            }
        } else if (seenCost + this.bound(lastSeenNodeId, unseen, costs, deliveryDurations) < this.bestSolutionCost){
            Iterator<Integer> it = iterator(lastSeenNodeId, unseen, costs);
            while (it.hasNext()){
                Integer prochainSommet = it.next();
                seen.add(prochainSommet);
                unseen.remove(prochainSommet);
                branchAndBound(prochainSommet, unseen, seen, seenCost + costs[lastSeenNodeId][prochainSommet] + deliveryDurations[prochainSommet], costs, duree);
                unseen.add(prochainSommet);
                seen.remove(prochainSommet);
            }
        }*/
    }

    private int bound(int lastSeenNodeId, ArrayList<Integer> unseen, int[][] costs, int[] deliveryDurations) {
        // TODO ?
        return 0;
    }

    protected Iterator<Integer> iterator(Integer lastSeenNodeId, ArrayList<Integer> unseen, int[][] costs) {
        // TODO
        return null;
    }
}
