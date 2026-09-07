package org.example.mosaic;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.RoadSideUnitOperatingSystem;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.objects.v2x.GenericV2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.util.scheduling.Event;

public class RsuCitsApp extends AbstractApplication<RoadSideUnitOperatingSystem>
        implements CommunicationApplication {

    private static final double BROADCAST_RANGE_M = 5000.0;
    private boolean relayed = false;

    @Override
    public void onStartup() {
        getLog().info("[C-ITS RSU] app started. Waiting for equipped-vehicle incident warning.");

        getOs().getAdHocModule().enable(
            new AdHocModuleConfiguration()
                .addRadio()
                .channel(AdHocChannel.CCH)
                .distance(BROADCAST_RANGE_M)
                .create()
        );
    }

    @Override
    public void processEvent(Event event) {
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        long t = getOs().getSimulationTime() / 1_000_000_000L;

        if (!relayed) {
            MessageRouting routing = getOs()
                .getAdHocModule()
                .createMessageRouting()
                .geographical(new GeoCircle(getOs().getPosition(), BROADCAST_RANGE_M))
                .broadcast()
                .channel(AdHocChannel.CCH)
                .build();

            GenericV2xMessage warning = new GenericV2xMessage(routing, "INCIDENT_WARNING_RELAY", 200L);
            getOs().getAdHocModule().sendV2xMessage(warning);

            relayed = true;
            getLog().info("[C-ITS RSU] Relayed vehicle-triggered incident warning at t=" + t + "s.");
        }
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission transmission) {}

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {}

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {}

    @Override
    public void onShutdown() {}
}
