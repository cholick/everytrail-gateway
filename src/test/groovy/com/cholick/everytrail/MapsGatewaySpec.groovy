package com.cholick.everytrail

import com.cholick.everytrail.domain.Coordinate
import com.cholick.everytrail.external.MapsGateway
import groovyx.net.http.RESTClient
import spock.lang.Specification

class MapsGatewaySpec extends Specification {

    def 'returns coordinates on success'() {
        given:
        MapsGateway mapsGateway = new MapsGateway('api_key')

        and:
        mapsGateway.mapsClient = Mock(RESTClient)
        def successResponse = [
                status: 200,
                data  : [
                        status : 'OK',
                        results: [
                                [geometry: [
                                        location: [lat: 42, lng: -100]
                                ]]
                        ]
                ]
        ]

        when:
        Coordinate coordinate = mapsGateway.geocode('Kirkland, WA')

        then:
        1 * mapsGateway.mapsClient.get(_ as Map) >> { args ->
            assert args.path[0] == '/maps/api/geocode/json'
            assert args.query.key[0] == 'api_key'
            assert args.query.address[0] == 'Kirkland, WA'
            return successResponse
        }

        and:
        coordinate.latitude == 42
        coordinate.longitude == -100
    }

    def 'throws exception on non-200'() {
        given:
        MapsGateway mapsGateway = new MapsGateway('api_key')
        mapsGateway.mapsClient = Mock(RESTClient)

        when:
        mapsGateway.geocode('Kirkland, WA')

        then:
        1 * mapsGateway.mapsClient.get(_ as Map) >> [status: 401]

        and:
        thrown(Exception)
    }

    def 'throws exception when Google returns non-OK status'() {
        given:
        MapsGateway mapsGateway = new MapsGateway('api_key')
        mapsGateway.mapsClient = Mock(RESTClient)

        when:
        mapsGateway.geocode('Kirkland, WA')

        then:
        1 * mapsGateway.mapsClient.get(_ as Map) >> [
                status: 200,
                data  : [status: 'Not OK']
        ]

        and:
        thrown(Exception)
    }

}
