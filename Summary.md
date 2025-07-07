## Application Continuum

__Naming, organizing, and reducing circular dependencies__ are the three most important things you'll ever do as a programmer.

We go through application continuum through the following commits: 
```
git tag -ln --sort=v:refname

v1    First commit
v2    Functional groups
v3    Feature groups (Bounded context)
v4    Components
v5    Applications
v6    Services
v7    Databases
v8    Versioning
v9    Service Discovery
v10   Circuit Breaker
```

Commit explained below: 
```
v1 – Monolith Start: A flat directory with minimal structure.

v2 – Functional Groups: Model/Controller/DAO folders to organize functionality.

v3 – Feature/Bouned Contexts: Per-domain packages (e.g., Timesheets) to improve cohesion.

v4 – Components: Fully modular code with isolated builds and independent testing support.

v5 – Applications: Distinct applications (Allocations, Backlog, Timesheets, Registration) built from components.

v6 – Services: Each app becomes a microservice with REST communication.

v7 – Databases: Dedicated database per service, ensuring autonomy and schema independence.

v8 – Versioning: API versioning introduced (via Accept headers) for backward compatibility.

v9 – Service Discovery: Dynamic discovery replaces hard-coded endpoints.

v10 – Circuit Breaker: Adds resilience patterns to handle service failures gracefully
```

### Service Discovery in AppContinuum

In the v9 "Service Discovery" stage, AppContinuum transitions from hard-coded endpoints to dynamic resolution:

    - Service Registry

        Services register themselves (with host/port) into a shared registry (like Redis) at startup and periodically.

    - Client-side discovery

        Clients query the registry to look up current addresses for other services instead of using fixed URLs.

    - Resilience

        Enables rolling deployments and scaling — services can join or leave freely without updating client code.

        Offers load balancing across instances with random or round-robin selection from discovered addresses.

Technically:

    The app includes a ServiceRegistry interface and Redis-backed implementation.

    Services perform SETEX heartbeats to announce availability.

    Lookups fetch active service addresses via KEYSCAN or SMEMBERS, then choose an instance.

```
class InstanceDataGateway(val pool: JedisPool, val timeToLiveInMillis: Long) {
    fun heartbeat(appId: String, url: String): InstanceRecord {
        val resource = pool.resource
        resource.psetex("$appId:$url", timeToLiveInMillis, url)
        resource.close()
        return InstanceRecord(appId, url)
    }

    fun findBy(appId: String): List<InstanceRecord> {
        val list = mutableListOf<InstanceRecord>()
        val resource = pool.resource
        resource.keys("$appId:*")
                .map { pool.resource.get(it) }
                .mapTo(list) { InstanceRecord(appId, it) }
        resource.close()
        return list
    }
}
```

### Circuit Breaker in AppContinuum

In the v10 "Circuit Breaker" commit, the app integrates resilience logic to handle failures:

    - Circuit Breaker Proxy

        Outgoing calls to services are wrapped by a proxy or decorator that monitors failures.

    - Failure Threshold & Timeouts

        If calls to a particular service fail repeatedly (e.g., timeout or error), the circuit “opens” and stops calling the service.

    - Half-Open & Recovery

        After a cooldown period, it allows limited test calls. On success, it “closes” again; on failure, it stays open.

Technically:

    Circuit Breaker APIs include isAllowed() and recordSuccess() / recordFailure().

    When open, calls return fast error or fallback response.

    On half-open, a few probes are allowed before full reopening.

```
class CircuitBreaker(val timeoutInMillis: Long = 200, val maxFailures: Int = 3, val retryIntervalInMillis: Long = 300) {

    fun <T> withCircuitBreaker(function: () -> T, fallback: () -> T): T {

        if (open() && !shouldRetry()) return fallback()

        val future = Executors.newSingleThreadExecutor().submit(function)

        return try {
            future.get(timeoutInMillis, TimeUnit.MILLISECONDS).apply {
                reset()
            }
        } catch (e: Exception) {
            fail()
            fallback()
        } finally {
            future.cancel(true)
        }
    }
```




