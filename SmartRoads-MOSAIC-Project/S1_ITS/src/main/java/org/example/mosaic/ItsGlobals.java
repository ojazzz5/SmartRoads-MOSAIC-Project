package org.example.mosaic;

public class ItsGlobals {
    // This flag is shared between the TMC and the vehicles.
    // When the TMC detects a shockwave, it turns this ON.
    public static volatile boolean VMS_ACTIVE = false;
}
