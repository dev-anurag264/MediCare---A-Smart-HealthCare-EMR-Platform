package com.medicare_health_systems.entity;

import java.util.Set;

public enum AppointmentStatus {
    PENDING {
        @Override
        public Set<AppointmentStatus> validTransitions() {
            return Set.of(CONFIRMED, CANCELLED);
        }
    },
    CONFIRMED {
        @Override
        public Set<AppointmentStatus> validTransitions() {
            return Set.of(COMPLETED, CANCELLED, NO_SHOW);
        }
    },
    COMPLETED {
        @Override
        public Set<AppointmentStatus> validTransitions() {
            return Set.of(); // Terminal state — no further transitions
        }
    },
    CANCELLED {
        @Override
        public Set<AppointmentStatus> validTransitions() {
            return Set.of(); // Terminal state
        }
    },
    NO_SHOW {
        @Override
        public Set<AppointmentStatus> validTransitions() {
            return Set.of(); // Terminal state
        }
    };

    public abstract Set<AppointmentStatus> validTransitions();


    public boolean canTransitionTo(AppointmentStatus next) {
        return validTransitions().contains(next);
    }
}
