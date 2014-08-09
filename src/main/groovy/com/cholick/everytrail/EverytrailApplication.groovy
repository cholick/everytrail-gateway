package com.cholick.everytrail

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class EverytrailApplication extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new EverytrailApplication().run(args)
    }

    @Override
    void initialize(Bootstrap<AppConfiguration> bootstrap) {
    }

    @Override
    void run(AppConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(SearchResource)
    }

}
