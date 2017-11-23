[![Build status](https://api.travis-ci.org/btlines/grpcmonix.svg?branch=master)](https://travis-ci.org/btlines/grpcmonix)
[![Dependencies](https://app.updateimpact.com/badge/852442212779298816/grpcmonix.svg?config=compile)](https://app.updateimpact.com/latest/852442212779298816/grpcmonix)
[![License](https://img.shields.io/:license-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![GRPCMonixGenerator](https://api.bintray.com/packages/beyondthelines/maven/grpcmonixgenerator/images/download.svg) ](https://bintray.com/beyondthelines/maven/grpcmonixgenerator/_latestVersion)
[![GRPCMonixRuntime](https://api.bintray.com/packages/beyondthelines/maven/grpcmonixruntime/images/download.svg) ](https://bintray.com/beyondthelines/maven/grpcmonixruntime/_latestVersion)

# GRPC Monix

Use Monix's Tasks and Observables to implement your GRPC services instead of Java's StreamObservers.

- Unary calls return a `Task[T]` for the response returned by the server
- Server streaming calls return an `Observable[T]` for the elements returned by the server
- Client streaming calls take an `Observable[T]` for the elements emitted by the client and return a `Task[U]` for the server response
- Bidi streaming calls take an `Observable[T]` for the elements emitted by the client and return an `Observable[U]` for the elements returned by the server

## Installation

You need to enable [`sbt-protoc`](https://github.com/thesamet/sbt-protoc) plugin to generate source code for the proto definitions.
You can do it by adding a `protoc.sbt` file into your `project` folder with the following lines:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.9")

resolvers += Resolver.bintrayRepo("beyondthelines", "maven")

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.0-pre5",
  "beyondthelines"         %% "grpcmonixgenerator" % "0.0.2"
)
```

Here we add a dependency to the GRPCMonix protobuf generator. 

Then we need to trigger the generation from the `build.sbt`:

```scala
PB.targets in Compile := Seq(
  // compile your proto files into scala source files
  scalapb.gen() -> (sourceManaged in Compile).value,
  // generate the GRPCMonix source code
  grpcmonix.generators.GrpcMonixGenerator() -> (sourceManaged in Compile).value
)

resolvers += Resolver.bintrayRepo("beyondthelines", "maven")

libraryDependencies += "beyondthelines" %% "grpcmonixruntime" % "0.0.2"
```

### Usage

You're now ready to implement your GRPC service using Monix's Tasks and Observables.

To implement your service's business logic you simply extend the GRPC monix generated trait.

E.g. for the RouteGuide service: 

```scala
class RouteGuideMonixService(features: Seq[Feature]) extends RouteGuideGrpcMonix.RouteGuide {
  // Unary call
  override def getFeature(request: Point): Task[Feature] = ???
  // Server streaming
  override def listFeatures(request: Rectangle): Observable[Feature] = ???
  // Client streaming
  override def recordRoute(points: Observable[Point]): Task[RouteSummary] = ???
  // Bidi streaming
  override def routeChat(notes: Observable[RouteNote]): Observable[RouteNote] = ???
}
```

The server creation is similar except you need to provide a Monix's `Scheduler` instead of an `ExecutionContext` when binding the service 

```scala
val server = ServerBuilder
  .forPort(8980)
  .addService(
    RouteGuideGrpcMonix.bindService(
      new RouteGuideMonixService(features), // the service implemented above
      monix.execution.Scheduler.global
    )
  )
  .build()    
```

Tasks and Observables are also available on the client side:

```scala
val channel = ManagedChannelBuilder
  .forAddress("localhost", 8980)
  .usePlainText(true)
  .build()

val stub = RouteGuideGrpcMonix.stub(channel) // only an async stub is provided

// Unary call
val feature: Task[Feature] = stub.getFeature(408031728, -748645385)
// Server streaming
val request = Rectangle(
  lo = Some(Point(408031728, -748645385)),
  hi = Some(Point(413700272, -742135189))
)
val features: Observable[Feature] = stub.listFeatures(request)
// Client streaming
val route: Observable[Feature] = Observable
  .fromIterable(features.map(_.getLocation)) 
  .delayOnNext(100.millis)
val summary: Task[RouteSummary] = stub.recordRoute(route)
// Bidi streaming
val notes: Observable[RouteNote] = Observable(
  RouteNote(message = "First message", location = Some(Point(0, 0))),
  RouteNote(message = "Second message", location = Some(Point(0, 1))),
  RouteNote(message = "Third message", location = Some(Point(1, 0))),
  RouteNote(message = "Fourth message", location = Some(Point(1, 1)))
).delayOnNext(1.second)
val allNotes = stub.routeChat(notes)
```

