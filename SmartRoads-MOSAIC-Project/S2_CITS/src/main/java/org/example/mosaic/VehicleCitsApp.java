package org.example.mosaic;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.GenericV2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.routing.RoutingParameters;
import org.eclipse.mosaic.lib.routing.RoutingResponse;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.routing.CandidateRoute;

public class VehicleCitsApp extends AbstractApplication<VehicleOperatingSystem>
        implements CommunicationApplication {

    private static final double BROADCAST_RANGE_M = 500.0; // Cars broadcast short range
    private static final double STUCK_SPEED_MS = 5.0;
    private static final long CHECK_INTERVAL = 1_000_000_000L;
    private static final long SECOND = 1_000_000_000L;

    private static final GeoPoint DIVERSION_POINT = GeoPoint.latLon(40.846386, 14.216081);

    private boolean warningBroadcast = false;
    private boolean alreadyRerouted = false;

    @Override
    public void onStartup() {
        getOs().getAdHocModule().enable(
            new AdHocModuleConfiguration()
                .addRadio()
                .channel(AdHocChannel.CCH)
                .distance(BROADCAST_RANGE_M)
                .create()
        );

        getOs().getEventManager().addEvent(
            getOs().getSimulationTime() + CHECK_INTERVAL, this
        );
    }

    @Override
    public void processEvent(Event event) {
        long t = getOs().getSimulationTime() / SECOND;

        if (!warningBroadcast) {
            double speed = getOs().getNavigationModule().getVehicleData().getSpeed();

            if (t >= 600 && speed >= 0 && speed < STUCK_SPEED_MS) {
                MessageRouting routing = getOs().getAdHocModule()
                    .createMessageRouting()
                    .geographical(new GeoCircle(getOs().getPosition(), BROADCAST_RANGE_M))
                    .broadcast()
                    .channel(AdHocChannel.CCH)
                    .build();

                getOs().getAdHocModule().sendV2xMessage(
                    new GenericV2xMessage(routing, "INCIDENT_WARNING", 200L)
                );

                warningBroadcast = true;
                getLog().info("[C-ITS Vehicle] " + getOs().getId() + " detected blockage and broadcast warning!");
            }
        }

        long next = getOs().getSimulationTime() + CHECK_INTERVAL;
        if (next < 3600L * SECOND) {
            getOs().getEventManager().addEvent(next, this);
        }
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        if (alreadyRerouted) {
            return;
        }

        long t = getOs().getSimulationTime() / SECOND;
        getLog().info("[C-ITS Vehicle] " + getOs().getId() + " received V2X warning at t=" + t + "s. Rerouting.");

        RoutingResponse response = getOs().getNavigationModule()
            .calculateRoutes(new org.eclipse.mosaic.lib.routing.RoutingPosition(DIVERSION_POINT), new RoutingParameters());

        if (response != null && response.getBestRoute() != null) {
            CandidateRoute bestRoute = response.getBestRoute();
            boolean switched = getOs().getNavigationModule().switchRoute(bestRoute);

            if (switched) {
                alreadyRerouted = true;
            }
        }
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {}

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {}

    @Override
    public void onMessageTransmitted(V2xMessageTransmission transmission) {}

    @Override
    public void onShutdown() {}
}
