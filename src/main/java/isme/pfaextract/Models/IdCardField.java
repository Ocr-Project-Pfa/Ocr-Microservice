package isme.pfaextract.Models;

import java.awt.Rectangle;

public enum IdCardField {
    // Adjusted coordinates based on typical Moroccan ID layout
    HEADER(new Rectangle(10, 5, 80, 15)),      // Top section with "ROYAUME DU MAROC"
    CIN(new Rectangle(10, 30, 25, 8)),         // ID number
    FULL_NAME_FR(new Rectangle(20, 40, 35, 8)),
    FULL_NAME_AR(new Rectangle(55, 40, 35, 8)),
    DOB_FR(new Rectangle(20, 50, 30, 8)),
    DOB_AR(new Rectangle(55, 50, 30, 8)),
    POB_FR(new Rectangle(20, 60, 30, 8)),
    POB_AR(new Rectangle(55, 60, 30, 8));

    private final Rectangle region;

    IdCardField(Rectangle region) {
        this.region = region;
    }

    public Rectangle getRegion() {
        return region;
    }
}