package com.cholick.everytrail.external

import com.cholick.everytrail.domain.Coordinate
import groovyx.net.http.RESTClient

class MapsGateway {

    final String mapsKey

    RESTClient mapsClient = new RESTClient("https://maps.googleapis.com")

    MapsGateway(String mapsKey) {
        this.mapsKey = mapsKey
    }

    Coordinate geocode(String address) {
        def response = mapsClient.get(
                path: "/maps/api/geocode/json",
                query: [key: mapsKey, address: address]
        )

        if (response.status == 200 && response.data.status == 'OK') {
            Map coordinate = response.data.results[0].geometry.location
            return new Coordinate(latitude: coordinate.lat, longitude: coordinate.lng)
        } else {
            throw new Exception()
        }
    }

}
