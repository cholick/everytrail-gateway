#### Everytrail Gateway

This is a simple service that queries the EveryTrail API.

/search responds to post requests of the form
```
{
    "address": "Boulder, CO",
    "within": 25
}
```

The address is first geocoded by [Google's API](https://developers.google.com/maps/documentation/geocoding/).
Then the coordinates are sent to Everytrail's search API endpoint. A single, random hike is chosen and returned. For example,
```
{
    title: "Longs Peak / Chasm Lake"
    url: "http://www.everytrail.com/guide/longs-peak-chasm-lake"
    lat: "40.27203664"
    lon: "-105.55691061"
    image: "http://images.everytrail.com/pics/fullsize/4591328-CIMG2322.JPG"
}
```

The purpose of this service is for a Hubot plugin which will find
a hike when asked. Here's the plugin source:

```coffee
# Description:
#   Search Everytrail for a hike.
#
# Dependencies:
#   None
#
# Commands:
#   hubot hike me around <address | city, state | zip> - Find a hike
#   hubot hike me around <address | city, state | zip> within <mile radius> - Find a hike
#
# Author:
#   *Matt Cholick*

module.exports = (robot) ->
  robot.respond /hike me around (.*)/i, (msg) ->
    split = msg.match[1].split('within')

    within = 50
    if split.length > 1
      within = split[1].match(/\s*(\d*).*/)[1] || within

    query =
      address: split[0]
      within: within

    #msg.http('http://localhost:9000/search')
    msg.http('http://everytrail-gateway.herokuapp.com/search')
      .header('Content-Type', 'application/json')
      .post(JSON.stringify(query)) (err, res, body) ->
        json = JSON.parse(body)
        if json.error
          msg.send json.error
        else
          hike = "#{json.title}: #{json.url}/map"
          hike = "#{json.image}\n#{hike}" if json.image
          msg.send hike
```

##### Setup

Add environment variables containing API keys:
```
export MAPS_KEY=
export EVERYTRAIL_KEY=
export EVERYTRAIL_SECRET=
```

To run the project as it will be on Heroku, create a script like the following
**local_jar.sh**

```
#!/bin/sh

export MAPS_KEY=
export EVERYTRAIL_KEY=
export EVERYTRAIL_SECRET=

export PORT=8000

./gradlew stage
java -jar -Ddw.server.applicationConnectors[0].port=$PORT everytrail.jar server app.yaml

```

##### Deployment

```
git push heroku master
```
