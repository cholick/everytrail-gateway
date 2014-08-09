package com.cholick.everytrail

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path('/search')
@Produces(MediaType.APPLICATION_JSON)
class SearchResource {
    private static final Logger log = LoggerFactory.getLogger(this)

    static final MAPS_KEY = System.getenv('MAPS_KEY')
    static final EVERYTRAIL_KEY = System.getenv('EVERYTRAIL_KEY')
    static final EVERYTRAIL_SECRET = System.getenv('EVERYTRAIL_SECRET')

    static final Map<String, Number> DEFAULT_ORIGIN = [lat: 47.6814875, lng: -122.2087353]

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Map hike(Map post) {
        log.info post as String

        Map origin = DEFAULT_ORIGIN
        if (post.address) {
            try {
                origin = geocode(post.address)
            } catch (Exception e) {
                log.error(e.message, e)
                return [
                        error: "I can't geocode that address"
                ]
            }
        }

        def everytrailRequest = new HTTPBuilder("http://${EVERYTRAIL_KEY}:${EVERYTRAIL_SECRET}@www.everytrail.com/")
        def everyTrailRequestParams = [
                path : 'api/index/search',
                query: [
                        lat      : origin.lat, lon: origin.lng,
                        proximity: post.within ?: 100,
                        activities: '5',
                        limit    : 30
                ]
        ]

        try {
            def response = everytrailRequest.get(everyTrailRequestParams)
            def statusMessage = response.@status.text()
            if (statusMessage == 'success') {
                Integer returned = Integer.parseInt(response.guides.@returnedCount.text())
                if (returned) {
                    Integer childIndex = Math.random() * returned
                    def hike = response.guides.guide[childIndex]
                    return buildJson(hike)
                } else {
                    return [error: "Everytrail didn't find any matching hikes"]
                }
            } else {
                log.error(response.text())
                return [error: "Everytrail didn't respond to my query"]
            }
        } catch (Exception e) {
            log.error(e.message, e)
            return [error: "Everytrail didn't respond to my query"]
        }

    }

    private Map geocode(String address) {
        def geocoder = new RESTClient("https://maps.googleapis.com")
        def response = geocoder.get(
                path: "/maps/api/geocode/json",
                query: [key: MAPS_KEY, address: address]
        )

        if (response.status == 200 && response.data.status == 'OK') {
            return response.data.results[0].geometry.location
        } else {
            throw new Exception()
        }
    }

    private Map buildJson(hike) {
        Map json = [
                title: hike.title.text(),
                url  : 'http://www.everytrail.com/guide/' + hike.url.text(),
                lat  : hike.location.@lat.text(),
                lon  : hike.location.@lon.text()

        ]
        if (Integer.parseInt(hike.containsPictures.text())) {
            json.image = hike.picture[0].fullsize.text()
        }
        return json
    }

}
