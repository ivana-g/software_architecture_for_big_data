# Provenance metrics

In this exercise we'll look at introducing service level indicators within the Provenance codebase.

### The Exercise

Capture Provenance metrics!

- create a Prometheus job
- using Grafana, create a Prometheus data source
- create a Grafana dashboard

### Quick start

After downloading the codebase, you'll notice that the `provenance-metrics` project includes a few example metrics.
Search for `article-requests` to review the `Meter` metric.

Then review the Dropwizard [metrics](https://metrics.dropwizard.io/) Getting Started page to gain a general understanding of
the new code. Dropwizard Metrics is a Java framework for developing ops-friendly, high-performance, RESTful web
services built by Coda Hale and the engineering crew at Yammer.

Build the provenance-metrics project.

 ```bash
./gradlew clean build
 ```

Run the server using the below command.

```bash
java -jar applications/provenance-server/build/libs/provenance-server-1.0-SNAPSHOT.jar
```

### Prometheus

We'll be using [Prometheus](https://prometheus.io/) to store our metrics data. Prometheus is an open-source monitoring
application built by the engineers at SoundCloud.

Install Prometheus and ensure that Prometheus is configured with our new metrics endpoint.

```bash
brew install prometheus
powershell with chocolatey: choco install prometheus -y
```

Modify `/usr/local/etc/prometheus.yml` (windows: C:\ProgramData\chocolatey\lib\prometheus\tools\prometheus-2.2.1.windows-amd64) to match the example below. For homebrew modify `/opt/homebrew/etc/prometheus.yml`.

```yaml
  - job_name: 'dropwizard'
    metrics_path: '/metrics'
    scrape_interval: 5s
    scheme: http
    static_configs:
      - targets: [ 'localhost:8881' ]
```

Restart prometheus.

```bash
brew services restart prometheus
powershell: 
cd C:\ProgramData\chocolatey\lib\prometheus\tools\prometheus-2.2.1.windows-amd64\ 
.\prometheus.exe --config.file=prometheus.yml

C:\ProgramData\chocolatey\lib\prometheus\tools\prometheus-2.2.1.windows-amd64
```

Upon success, you should see our Dropwizard endpoint `http://localhost:8881/metrics` **UP** on the
Prometheus [Status Targets](http://localhost:9090/targets) page.

![Prometheus target](docs/images/prometheus.png)

### Grafana

Grafana allows you to query, visualize and alert on metrics from a variety of data sources. We'll be
using [Grafana](https://grafana.com/) to display our metrics data stored in Prometheus.

First, install and run Grafana.

```bash
brew install grafana
powershell with chocolatey: choco install grafana -y

brew services restart grafana
windows run `grafana-server.exe` from C:\ProgramData\chocolatey\lib\grafana\tools\grafana-v11.5.4\bin 
```

Then use the [web application](http://localhost:3000) provided by Grafana to set up and
configure our Prometheus data source.

![Prometheus data source](docs/images/data-source.png)

Add your newly created prometheus data source `http://localhost:3000/datasources/new`. Use `http://localhost:9090` for your grafana data source http url.

Next, create a new dashboard and graph our `article_requests_total` data.

Use the query below to display requests per second.

```
irate(article_requests_total[5m])
```

![Grafana query](docs/images/query.png)

Curl the provenance articles endpoint to start tracking metrics. User a bash script similar to the example below to drive
multiple requests per second.

```bash
while [ true ]; do for i in {1..10}; do curl -v -H "Accept: application/json" http://localhost:8881/articles; done; sleep 5; done
``` 

Your dashboard should start recording data.

![Grafana dashboard](docs/images/dashboard.png)

Capture a screenshot of your Prometheus targets page and Grafana dashboard to complete the exercise. 

Hope you enjoy the exercise!

Thanks,

The IC Team

© 2022 by Initial Capacity, Inc. All rights reserved.

## Summary on the instructions by Initial Capacity
1. Run the provenance server : `java -jar .\applications\provenance-server\build\libs\provenance-server-1.0-SNAPSHOT.jar`
   1.1. Verify it is working in the browser: http://localhost:8881/metrics , you should see dropwizard‐style metrics 

2. Configure Prometheus (follow the installation steps up if not installed). 
   
   Executables and config files are located (on windows, installed with choco) in: `C:\ProgramData\chocolatey\lib\prometheus\tools\prometheus-2.2.1.windows-amd64\`
   2.1. Edit `scrape_configs` in prometheus.yml 
        ```
          - job_name: 'dropwizard'
              metrics_path: '/metrics'
              scrape_interval: 5s
              scheme: http
              static_configs:
                - targets: ['localhost:8881']
        ```
   2.3. Start Prometheus `.\prometheus.exe --config.file=prometheus.yml`

        Verify: Prometheus will start on port 9090 by default

        Navigate to http://localhost:9090/targets and you should see a target dropwizard → UP

3. Start grafana (follow the instruction steps up if not installed)

   Executables are located (on windows, installed with choco) in: `C:\ProgramData\chocolatey\lib\grafana\tools\grafana-v11.5.4\bin`

   3.1. Start grafana: .\grafana-server.exe

        Verify: Grafana’s UI runs on http://localhost:3000
                
                Log in with admin / admin (you’ll be prompted to change the password).

4. Hook Grafana to Prometheus
   
   4.1. Add a `Data Source`
        
        Data Source: Prometheus

        HTTP URL: http://localhost:9090

5. Build Dashboard in Grafana
   
   Add new dashboard

   Query code window: irate(article_requests_total[5m])

   

        