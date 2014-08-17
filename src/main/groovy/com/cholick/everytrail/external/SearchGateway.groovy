package com.cholick.everytrail.external

import com.cholick.everytrail.domain.Coordinate
import groovyx.net.http.HTTPBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SearchGateway {
    private static Logger log = LoggerFactory.getLogger(this)

    HTTPBuilder everytrailRequest

    SearchGateway(String everytrailKey, String everytrailSecret) {
        everytrailRequest = new HTTPBuilder("http://${everytrailKey}:${everytrailSecret}@www.everytrail.com/")
        everytrailRequest.client.params.setParameter("http.connection.timeout", new Integer(25 * 1000))
        everytrailRequest.client.params.setParameter("http.socket.timeout", new Integer(25 * 1000))
    }

    Map search(Coordinate origin, Integer within) {
        Map everyTrailRequestParams = [
                path : 'api/index/search',
                query: [
                        lat       : origin.latitude, lon: origin.longitude,
                        proximity : within,
                        activities: '5',  //restrict to hikes
                        limit     : 30
                ]
        ]

        def response = everytrailRequest.get(everyTrailRequestParams)
        def statusMessage = response.@status.text()
        if (statusMessage == 'success') {
            Integer returned = Integer.parseInt(response.guides.@returnedCount.text())
            if (returned) {
                Integer childIndex = Math.random() * returned
                def hike = response.guides.guide[childIndex]
                return buildHikeResponse(hike)
            } else {
                throw new NoHikesException()
            }
        } else {
            log.error(response.text())
            throw new Exception()
        }
    }

    private Map buildHikeResponse(hike) {
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

    static class NoHikesException extends RuntimeException {}

}
