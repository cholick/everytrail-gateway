package com.cholick.everytrail

import com.cholick.everytrail.domain.Coordinate
import com.cholick.everytrail.external.SearchGateway
import groovyx.net.http.HTTPBuilder
import spock.lang.Specification

class SearchGatewaySpec extends Specification {

    SearchGateway searchGateway

    def setup() {
        searchGateway = new SearchGateway('key!', 'secret!')
        searchGateway.everytrailRequest = Mock(HTTPBuilder)
    }

    def 'returns hike json on success'() {
        given:
        String xml = SearchGatewaySpec.classLoader.getResourceAsStream('search.xml').text
        def successResponse = new XmlSlurper().parseText(xml)

        when:
        Map hike = searchGateway.search(new Coordinate(latitude: 42, longitude: -100), 25)

        then:
        1 * searchGateway.everytrailRequest.get(_ as Map) >> { args ->
            assert args.path[0] == 'api/index/search'
            assert args.query.lat[0] == 42
            assert args.query.lon[0] == -100
            assert args.query.proximity[0] == 25
            assert args.query.activities[0] == '5'
            return successResponse
        }

        and:
        hike == [
                title: 'Poo-Poo Point via Chirico Trail',
                url  : 'http://www.everytrail.com/guide/poo-poo-point-via-chirico-trail',
                lat  : '47.500433',
                lon  : '-122.021448',
                image: 'http://images.everytrail.com/pics/fullsize/389092-IMG_0077.jpg'
        ] || hike == [
                title: 'Twin Falls Trail - Olallie State Park',
                url  : 'http://www.everytrail.com/guide/twin-falls-trail-olallie-state-park',
                lat  : '47.445116',
                lon  : '-121.697557',
                image: 'http://images.everytrail.com/pics/fullsize/2264579-IMG_1464.jpg'
        ]
    }

    def 'throws no hikes exception when no results'() {
        given:
        String xml = SearchGatewaySpec.classLoader.getResourceAsStream('no_results.xml').text
        def failureResponse = new XmlSlurper().parseText(xml)

        when:
        searchGateway.search(new Coordinate(latitude: 42, longitude: -100), 25)

        then:
        1 * searchGateway.everytrailRequest.get(_ as Map) >> failureResponse

        and:
        thrown(SearchGateway.NoHikesException)
    }

    def 'throws exception on failure'() {
        given:
        String xml = SearchGatewaySpec.classLoader.getResourceAsStream('failure.xml').text
        def failureResponse = new XmlSlurper().parseText(xml)

        when:
        searchGateway.search(new Coordinate(latitude: 42, longitude: -100), 25)

        then:
        1 * searchGateway.everytrailRequest.get(_ as Map) >> failureResponse

        and:
        thrown(Exception)
    }

}
