# MTD Identifier Lookup

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Download](https://api.bintray.com/packages/hmrc/releases/mtd-identifier-lookup/images/download.svg)](https://bintray.com/hmrc/releases/mtd-identifier-lookup/_latestVersion)

This protected micro-service processes authenticated requests to retrieve the MDT Income Tax identifier (**MTD IT ID**) for a NINO.

sbt "~run 9769"

# Endpoint Definitions (APIs)

## Lookup MTD IT ID using a related NINO

**Method**: GET

**URL**: `/mtd-identifier-lookup/nino/:nino`

|Path Parameter|Description|
|-|-|
|`nino`|Then national insurance number for the user.|

**Query Parameters**: N/A

### Success Response

**Status**: OK (200)

#### Definition

##### Response Object

|Data Item|Type|Mandatory|
|-|-|-|
|mtdbsa|`String`|Yes|

#### Example

**Status**: OK (200)
**Json Body**: 
```
{
  "mtdbsa": "XQIT00000000001"
}
```

### Error Responses

|HTTP Code|Reason|
|-|-|
|400|Invalid `nino` supplied|
|403|Not authorised to perform operation|
|500|Downstream error|

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Running the application

### Running from Nexus/Bintray
To update from Nexus and start all services from the RELEASE version instead of snapshot
```
sm --start MTD_ID_LOOKUP -f
```

### To manually run the application locally:

Kill the service ```sm --stop MTD_ID_LOOKUP``` *(if it's already running)*. Then run:
```
sbt 'run 9769'
```

## Testing the application

To test the application fully (Unit Tests, Component Tests *(integration)*, Scala Style Checker and Scala Coverage report) execute:
```
sbt clean scalastyle coverage test it:test coverageOff coverageReport
```
*(To run only a subset of the tests ommit the desired sbt options accordingly)*

---
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

