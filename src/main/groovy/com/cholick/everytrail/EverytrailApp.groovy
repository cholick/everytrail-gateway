package com.cholick.everytrail

import com.cholick.everytrail.external.MapsGateway
import com.cholick.everytrail.external.SearchGateway
import io.dropwizard.Application
import io.dropwizard.assets.AssetsBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class EverytrailApp extends Application<EverytrailAppConfiguration> {

    static final String MAPS_KEY = System.getenv('MAPS_KEY')
    static final String EVERYTRAIL_KEY = System.getenv('EVERYTRAIL_KEY')
    static final String EVERYTRAIL_SECRET = System.getenv('EVERYTRAIL_SECRET')

    public static void main(String[] args) throws Exception {
        new EverytrailApp().run(args)
    }

    @Override
    void initialize(Bootstrap<EverytrailAppConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle('/status', '/status'));
    }

    @Override
    void run(EverytrailAppConfiguration configuration, Environment environment) throws Exception {
        SearchResource searchResource = new SearchResource(
                new MapsGateway(MAPS_KEY),
                new SearchGateway(EVERYTRAIL_KEY, EVERYTRAIL_SECRET)
        )
        environment.jersey().register(searchResource)
    }

}
