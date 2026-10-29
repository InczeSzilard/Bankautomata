package com.mycompany.bankautomata.rule;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Test clock whose time we can move forward by hand (e.g. to the next day). */
class AllithatoOra extends Clock {

    private static final ZoneId ZONA = ZoneId.of("Europe/Bucharest");
    private Instant most;

    AllithatoOra(LocalDateTime kezdet) {
        this.most = kezdet.atZone(ZONA).toInstant();
    }

    void elore(Duration mennyivel) {
        most = most.plus(mennyivel);
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return most;
    }
}