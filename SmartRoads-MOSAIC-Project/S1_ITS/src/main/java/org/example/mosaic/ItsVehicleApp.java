package org.example.mosaic;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.RoutingPosition;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.routing.CandidateRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import java.util.Random;

public class ItsVehicleApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    private static final double COMPLIANCE_RATE = 0.40; // 40% of drivers follow VMS
    private static final GeoPoint DIVERSION_POINT = GeoPoint.latLon(40.846386, 14.216081);
    
    private boolean followsVms;
    private boolean rerouted = false;

    @Override
    public void onStartup() {
        // Randomly determine if this driver complies with VMS
        followsVms = (new Random().nextDouble() <= COMPLIANCE_RATE);
        
        // Poll every 1 second
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + 1_000_000_000L, this);
    }

    @Override
    public void onShutdown() { }

    @Override
    public void processEvent(org.eclipse.mosaic.lib.util.scheduling.Event event) throws Exception {
        if (!rerouted && ItsGlobals.VMS_ACTIVE && followsVms) {
            getLog().info("Vehicle " + getOs().getId() + " saw VMS and is rerouting!");
            
            // Calculate a new route to the diversion point
            RoutingResponse response = getOs().getNavigationModule().calculateRoutes(new RoutingPosition(DIVERSION_POINT), new RoutingParameters());
            if (response != null && response.getBestRoute() != null) {
                CandidateRoute bestRoute = response.getBestRoute();
                getOs().getNavigationModule().switchRoute(bestRoute);
            }
            rerouted = true;
        }

        // Schedule next poll if haven't rerouted
        if (!rerouted) {
            getOs().getEventManager().addEvent(getOs().getSimulationTime() + 1_000_000_000L, this);
        }
    }

    @Override
    public void onVehicleUpdated(VehicleData prev, VehicleData current) {
    }
}
