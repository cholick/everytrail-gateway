package com.cholick.everytrail.domain

class Coordinate {

    Double latitude
    Double longitude

    Coordinate() {}

    Coordinate(Double latitude, Double longitude) {
        this.latitude = latitude
        this.longitude = longitude
    }

    @Override
    String toString() {
        return "${latitude}, ${longitude}"
    }

}
