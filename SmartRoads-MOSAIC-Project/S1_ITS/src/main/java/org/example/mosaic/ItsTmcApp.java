package org.example.mosaic;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.TrafficManagementCenterApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficManagementCenterOperatingSystem;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.InductionLoop;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.LaneAreaDetector;
import java.util.Collection;

public class ItsTmcApp extends AbstractApplication<TrafficManagementCenterOperatingSystem> implements TrafficManagementCenterApplication {

    private static final double CONGESTION_SPEED_MS = 5.0; 
    private static final double SPEED_CONGESTION_DENSITY = 20.0;
    private static final double HIGH_DENSITY_CONGESTION = 40.0;
    
    private int consecutiveDetections = 0;
    private boolean controlActivated = false;

    @Override
    public void onStartup() {
        getLog().info("TMC HighwayManagement started.");
    }

    @Override
    public void onShutdown() {
        getLog().info("TMC HighwayManagement shutting down.");
    }

    @Override
    public void processEvent(org.eclipse.mosaic.lib.util.scheduling.Event event) throws Exception {
    }

    @Override
    public void onInductionLoopUpdated(Collection<InductionLoop> inductionLoops) {
    }

    @Override
    public void onLaneAreaDetectorUpdated(Collection<LaneAreaDetector> laneAreaDetectors) {
        if (controlActivated) return; // Already triggered

        for (LaneAreaDetector detector : laneAreaDetectors) {
            double speed = detector.getMeanSpeed();
            double density = detector.getTrafficDensity();

            // Ignore empty readings
            if (speed < 0 || density < 1.0) continue;

            // Congestion Logic
            boolean isCongested = (speed <= CONGESTION_SPEED_MS && density >= SPEED_CONGESTION_DENSITY) || (density >= HIGH_DENSITY_CONGESTION);

            if (isCongested) {
                consecutiveDetections++;
                getLog().info("Congestion warning: speed=" + speed + ", density=" + density + ". Count: " + consecutiveDetections);
                
                if (consecutiveDetections >= 2) { 
                    getLog().info("VMS ACTIVATED at time " + getOs().getSimulationTime());
                    ItsGlobals.VMS_ACTIVE = true;
                    controlActivated = true;
                    break;
                }
            } else {
                consecutiveDetections = 0; // Reset
            }
        }
    }  
}
