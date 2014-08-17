package com.cholick.everytrail

import com.cholick.everytrail.domain.Coordinate
import com.cholick.everytrail.domain.Query
import com.cholick.everytrail.external.MapsGateway
import com.cholick.everytrail.external.SearchGateway
import com.cholick.everytrail.external.SearchGateway.NoHikesException
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
    private static Logger log = LoggerFactory.getLogger(this)

    MapsGateway mapsGateway
    SearchGateway searchGateway

    SearchResource(MapsGateway mapsGateway, SearchGateway searchGateway) {
        this.mapsGateway = mapsGateway
        this.searchGateway = searchGateway
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Map hike(Query query) {
        log.info query as String

        Coordinate origin
        try {
            origin = mapsGateway.geocode(query.address)
        } catch (Exception e) {
            log.error(e.message, e)
            return [error: "I can't geocode that address"]
        }

        Map response
        try {
            response = searchGateway.search(origin, query.within ?: 100)
        } catch (NoHikesException nhe) {
            response = [error: "Everytrail didn't find any matching hikes"]
        } catch (Exception e) {
            response = [error: "Everytrail didn't respond to my query (their API can be hit & miss)"]
        }
        return response
    }

}
