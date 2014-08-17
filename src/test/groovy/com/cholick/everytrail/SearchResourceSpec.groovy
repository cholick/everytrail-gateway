package com.cholick.everytrail

import com.cholick.everytrail.domain.Coordinate
import com.cholick.everytrail.domain.Query
import com.cholick.everytrail.external.MapsGateway
import com.cholick.everytrail.external.SearchGateway
import io.dropwizard.testing.junit.ResourceTestRule
import org.junit.Rule
import org.slf4j.Logger
import spock.lang.Specification

class SearchResourceSpec extends Specification {

    SearchResource resource = new SearchResource(Mock(MapsGateway), Mock(SearchGateway))
    @Rule
    ResourceTestRule resources = ResourceTestRule.builder().addResource(resource).build()

    def setup() {
        resource.log = Mock(Logger)
    }

    def 'hike json on success'() {
        given:
        resource.mapsGateway.geocode(_) >> new Coordinate(90, 0)
        resource.searchGateway.search(_ as Coordinate, _ as Integer) >> [
                title: 'Some hike'
        ]

        when:
        Map hike = resource.hike(new Query(address: 'Kirkland, WA', within: 25))

        then:
        hike == [title: 'Some hike']
    }

    def 'error on gecoding failure'() {
        given:
        resource.mapsGateway.geocode(_) >> {
            throw new Exception()
        }

        when:
        Map hike = resource.hike(new Query(address: 'Kirkland, WA', within: 25))

        then:
        hike
        hike.error
        hike.error.contains('geocode')
    }

    def 'error on no results'() {
        given:
        resource.mapsGateway.geocode(_) >> new Coordinate(90, 0)
        resource.searchGateway.search(_ as Coordinate, _ as Integer) >> {
            throw new SearchGateway.NoHikesException()
        }

        when:
        Map hike = resource.hike(new Query(address: 'Kirkland, WA', within: 25))

        then:
        hike
        hike.error
        hike.error.contains('Everytrail')
    }

    def 'error on Everytrail API failure'() {
        given:
        resource.mapsGateway.geocode(_) >> new Coordinate(90, 0)
        resource.searchGateway.search(_ as Coordinate, _ as Integer) >> {
            throw new Exception()
        }

        when:
        Map hike = resource.hike(new Query(address: 'Kirkland, WA', within: 25))

        then:
        hike
        hike.error
        hike.error.contains('Everytrail')
    }

}
