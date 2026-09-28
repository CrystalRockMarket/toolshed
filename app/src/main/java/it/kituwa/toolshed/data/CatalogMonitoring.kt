package it.kituwa.toolshed.data

internal object Monitoring {
    val services = listOf(
        Service(
            id = "uptime-kuma",
            name = "Uptime Kuma",
            tagline = "Uptime monitoring with a notification when it breaks",
            category = Category.MONITORING,
            blurb = "Uptime Kuma watches your services from the outside and tells you when they stop answering. It has a good status page, sensible notification integrations, and a dashboard you can glance at.\n\nThe thing to understand is that it is a monitor for things it can reach. A monitor inside your own network cannot tell you the internet is down, and a monitor on a VPS cannot tell you the power is off in the spare room.",
            bestFor = listOf(
                "Knowing when a service or host stops responding",
                "A status page for your family's services",
                "Notifications to Telegram, email, or a webhook",
            ),
            homepage = "https://github.com/louislam/uptime-kuma",
            docsUrl = "https://github.com/louislam/uptime-kuma/wiki/%F0%9F%94%A7-How-To-Install",
            license = "MIT",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 5, cores = 0.5),
            defaultPort = "3001",
            compose = UPTIMEKUMA_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Create the admin account on first visit",
                    "The setup page appears once and then never again. If you skip it, resetting means deleting the database volume and starting over, which is the correct fix and a mildly annoying one.",
                ),
                GuideStep(
                    "Monitor from outside where the traffic arrives",
                    "If Uptime Kuma runs on the same host as the thing it monitors, a host reboot takes down both and the monitor reports nothing. A cheap external host or a friend's connection is far more useful.",
                ),
                GuideStep(
                    "Use the Docker health check in your compose files",
                    "Uptime Kuma can check a container's health directly. It is the fastest signal you can get and it is not a substitute for checking the service over HTTP.",
                ),
                GuideStep(
                    "Add the status page to your dashboard",
                    "The status page is a separate URL and it is a genuinely good artefact for a household: one page that says what is up right now.",
                ),
                GuideStep(
                    "Set up one notification channel and test it",
                    "A notification you have not seen arrive is not a notification. Send a test notification before you rely on it at three in the morning.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The database is a single SQLite file",
                    "Which makes backups trivial: copy the data directory. It also means one instance, no clustering, and no remote replicas.",
                ),
                GuideStep(
                    "1-minute intervals are the finest granularity",
                    "Anything more frequent will be rate-limited by the remote services you are checking and will not tell you anything useful about them.",
                ),
                GuideStep(
                    "Certificate expiry monitoring is worth adding",
                    "Kuma can check for expiring TLS certificates. This is the check that quietly catches the problem before it becomes an outage, and no other tool on this list does it as a first-class feature.",
                ),
            ),
            alsoConsider = listOf("grafana", "prometheus", "homarr", "traefik"),
        ),
        Service(
            id = "grafana",
            name = "Grafana",
            tagline = "Dashboards for the metrics you already collect",
            category = Category.MONITORING,
            blurb = "Grafana turns time series data into dashboards that answer questions. Point it at Prometheus, InfluxDB, Postgres, or almost anything, and you can build panels and alerts without much effort.\n\nGrafana does not collect anything. It is the display layer, which means it is only as good as what you feed it. A beautiful dashboard over an empty database is a work of fiction.",
            bestFor = listOf(
                "Visualising metrics from several sources",
                "Alerting on thresholds with a notification channel",
                "One screen showing the state of everything",
            ),
            homepage = "https://grafana.com",
            docsUrl = "https://grafana.com/docs/grafana/latest/setup-grafana/installation/docker/",
            license = "AGPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 10, cores = 1.0),
            defaultPort = "3000",
            compose = GRAFANA_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the admin password in the environment",
                    "The default admin and admin gets you into a lot of trouble. Set GF_SECURITY_ADMIN_PASSWORD and change the default before the port is reachable.",
                ),
                GuideStep(
                    "Mount the data volume before the first start",
                    "Dashboards and users are in a SQLite database. Without a volume, every restart loses everything you built.",
                ),
                GuideStep(
                    "Add Prometheus as a data source",
                    "The URL is the Prometheus service name inside the Docker network, not localhost. Getting this wrong is the most common Grafana error and the error message is not helpful about it.",
                ),
                GuideStep(
                    "Start with one dashboard, not ten",
                    "Build the one screen you will actually look at. A dashboard nobody opens is the most common outcome of monitoring projects.",
                ),
                GuideStep(
                    "Set up a notification channel and route alerts to it",
                    "Contact points in Grafana 9 and later, and alert rules that reference them. An alert without a contact point is a decoration.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "SQLite is fine until it is not",
                    "For an all-in-one with embedded Prometheus it is fine. Add Loki and a Postgres datasource and you should move the Grafana database to Postgres, which is a documented migration.",
                ),
                GuideStep(
                    "Grafana's own login is not single sign-on by default",
                    "It supports OIDC. If you already run Authentik, connecting it removes a password and gives you central disable-user.",
                ),
                GuideStep(
                    "Dashboards are JSON and worth keeping in git",
                    "Export a dashboard as JSON and commit it. A dashboard that only exists inside a database you just restored is a setback you will feel.",
                ),
            ),
            alsoConsider = listOf("prometheus", "uptime-kuma", "homarr"),
        ),
        Service(
            id = "prometheus",
            name = "Prometheus",
            tagline = "The metrics database and scraper at the centre of the stack",
            category = Category.MONITORING,
            blurb = "Prometheus is what collects metrics. It scrapes an endpoint every fifteen seconds, stores the history locally, and evaluates alerting rules against it. Grafana draws the result.\n\nTwo properties matter and both are deliberate. It stores everything locally, so it needs a disk and a retention policy. And it only knows what it is told to ask for, so a metric that is not exposed is invisible no matter how much you need it.",
            bestFor = listOf(
                "Collecting metrics from containers and hosts",
                "Time-series history for capacity planning",
                "Alerting on thresholds that matter to you",
            ),
            homepage = "https://prometheus.io",
            docsUrl = "https://prometheus.io/docs/prometheus/latest/installation/docker/",
            license = "Apache-2.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 30, cores = 1.0),
            defaultPort = "9090",
            compose = PROMETHEUS_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Decide on a retention policy before you start",
                    "The default keeps fifteen days, which fills a disk in a surprisingly short time on a busy stack. Set it explicitly and set it to fit the disk you have.",
                ),
                GuideStep(
                    "Write the scrape config to look at the right targets",
                    "Inside Docker networking, other containers are reachable by service name on their own port, plus a metrics path that is not always the obvious one.",
                ),
                GuideStep(
                    "Use the node exporter for the host",
                    "Containers are easy to scrape because most expose metrics. Host metrics need the node exporter mounted against the host filesystem and PID namespace.",
                ),
                GuideStep(
                    "Add cAdvisor for per-container CPU and memory",
                    "Without it you can see that the host is busy but not which container is responsible, which is the question you will actually have.",
                ),
                GuideStep(
                    "Add Grafana as a data source and test a panel",
                    "A query in the Prometheus UI first, then in Grafana. It separates problems with the metric from problems with the dashboard.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "It is a single point of truth and a single point of failure",
                    "Prometheus does not replicate. If the disk dies, the history is gone. Back up the data directory, and consider remote write to a longer-term store.",
                ),
                GuideStep(
                    "Cardinality will hurt you before anything else",
                    "A label with a unique value per request or per user makes the number of time series explode and the process run out of memory. This is the classic self-inflicted Prometheus outage.",
                ),
                GuideStep(
                    "The node exporter needs those host mounts",
                    "The read-only mounts and the PID namespace are not optional. Without them you get partial metrics that look real and are subtly wrong.",
                ),
                GuideStep(
                    "Recording rules make dashboards faster and cheaper",
                    "Pre-computing a rate over five minutes and graphing that is faster than recomputing it on every dashboard refresh, and it is the standard practice.",
                ),
            ),
            alsoConsider = listOf("grafana", "uptime-kuma", "alertmanager"),
        ),
        Service(
            id = "alertmanager",
            name = "Alertmanager",
            tagline = "Grouping and routing so you are not woken 400 times",
            category = Category.MONITORING,
            blurb = "Alertmanager sits between Prometheus and your phone, and it does the unglamorous work that makes alerting bearable. It groups related alerts into one notification, silences known problems, and routes by label.\n\nA Prometheus with an alert but no Alertmanager sends you one message per alert, and the moment something goes genuinely wrong you will mute all of them.",
            bestFor = listOf(
                "Grouping related alerts into one notification",
                "Silencing a known problem for a few hours",
                "Routing alerts to different people by label",
            ),
            homepage = "https://prometheus.io/docs/alerting/latest/alertmanager/",
            docsUrl = "https://prometheus.io/docs/alerting/latest/configuration/",
            license = "Apache-2.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 128, ramTypicalMb = 256, diskGb = 1, cores = 0.5),
            defaultPort = "9093",
            compose = ALERTMANAGER_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Write a config file before you start",
                    "A default config with no routes matches nothing and you will conclude the whole system is broken. Define at least one route with a receiver.",
                ),
                GuideStep(
                    "Start with one receiver, then split them",
                    "One destination that works is worth more than five that you have not tested. Split by label once you know the plumbing is sound.",
                ),
                GuideStep(
                    "Point Prometheus at it explicitly",
                    "The alerting section of the Prometheus config needs the Alertmanager address. It is not discovered automatically.",
                ),
                GuideStep(
                    "Learn the silences before you need them",
                    "A silence is a temporary, expiring, annotated suppression. Setting one with a reason and an expiry is the difference between a managed system and one people disable.",
                ),
                GuideStep(
                    "Group by the right labels",
                    "Grouping by job means one notification for all problems in that service. Grouping by nothing means a separate message for every single alert, which is the thing you are trying to avoid.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "No authentication built in",
                    "Alertmanager has no user accounts. If you expose its UI, put it behind a proxy with authentication or keep it on an internal network. Do not put it on a public port as shipped.",
                ),
                GuideStep(
                    "group_wait and repeat_interval are the tuning knobs",
                    "The first defaults to thirty seconds and repeat interval to four hours. Both are fine defaults, and both are wrong if your alerts are noisy.",
                ),
                GuideStep(
                    "Silences are for humans, not for suppressing truth",
                    "A silence hides an alert from you and from Prometheus. It does not fix anything, and the clock on the silence is there to remind you of that.",
                ),
            ),
            alsoConsider = listOf("prometheus", "grafana", "uptime-kuma"),
        ),
        Service(
            id = "plausible",
            name = "Plausible",
            tagline = "Privacy-respecting web analytics you host",
            category = Category.MONITORING,
            blurb = "Plausible answers 'how many people used my site' without tracking anyone across the internet. No cookies, no personal data, no fingerprinting, and the whole thing is one Go binary on your own hardware.\n\nFor a self-hosted site this is the rare tool that is both simpler and more private than the alternative. The community edition is free; the paid features are the parts you probably do not need.",
            bestFor = listOf(
                "Traffic numbers for a site you control",
                "Analytics with no cookies and no personal data",
                "A self-hosted alternative to Google Analytics",
            ),
            homepage = "https://plausible.io",
            docsUrl = "https://github.com/plausible/community-edition#docker-instructions",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 10, cores = 1.0),
            defaultPort = "8000",
            compose = PLAUSIBLE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Use the community edition image",
                    "ghcr.io/plausible/community-edition is the free one. The commercial image requires a licence key, and the community edition has no visitor-level breakdown.",
                ),
                GuideStep(
                    "ClickHouse is not optional",
                    "Plausible stores events in ClickHouse. The compose file includes it, and it is the largest part of the memory footprint.",
                ),
                GuideStep(
                    "Set the base URL and the site domain",
                    "The domain setting is what makes the dashboard filter work. Set it to the hostname without a scheme, or nothing is recorded.",
                ),
                GuideStep(
                    "Add the snippet to your sites",
                    "One script tag, deferred. Check the dashboard shows a real pageview within a minute, and only then add it to the rest of your sites.",
                ),
                GuideStep(
                    "Decide how long you keep the data",
                    "Events accumulate. Prune the events table periodically, or a busy site will eventually make the dashboard slow for reasons that are not obvious.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "It cannot show you individual visitors",
                    "No cross-session tracking means no person-level journeys. For most sites that is a feature, and it is a hard limit of the design, not a setting.",
                ),
                GuideStep(
                    "Bots are not filtered automatically in the community edition",
                    "Expect some automated traffic in the numbers. The paid edition has bot filtering; on the community edition, be suspicious of very round numbers.",
                ),
                GuideStep(
                    "Self-hosting beats the hosted version for a small site",
                    "The hosted version is cheap and has no maintenance. The self-hosted version is free, has your data, and adds one more thing to keep updated. Either is a defensible choice.",
                ),
            ),
            alsoConsider = listOf("grafana", "traefik", "homarr"),
        ),
    )
}

private val UPTIMEKUMA_COMPOSE = """
services:
  uptime-kuma:
    container_name: uptime-kuma
    image: louislam/uptime-kuma:${'$'}{UPTIME_KUMA_VERSION:-2}
    restart: always
    ports:
      - '3001:3001'
    volumes:
      - ./data:/app/data
      # Read-only socket: lets Kuma inspect container health without write access.
      - /var/run/docker.sock:/var/run/docker.sock:ro
"""

private val GRAFANA_COMPOSE = """
services:
  grafana:
    container_name: grafana
    image: grafana/grafana:${'$'}{GRAFANA_VERSION:-latest}
    restart: always
    ports:
      - '3000:3000'
    environment:
      - GF_SECURITY_ADMIN_USER=${'$'}{GF_SECURITY_ADMIN_USER:-admin}
      - GF_SECURITY_ADMIN_PASSWORD=${'$'}{GF_SECURITY_ADMIN_PASSWORD}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SERVER_ROOT_URL=${'$'}{GF_SERVER_ROOT_URL}
    volumes:
      - ./data:/var/lib/grafana
      - ./config:/etc/grafana
"""

private val PROMETHEUS_COMPOSE = """
services:
  prometheus:
    container_name: prometheus
    image: prom/prometheus:${'$'}{PROMETHEUS_VERSION:-latest}
    restart: always
    ports:
      - '9090:9090'
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      # Keep 30 days. The 15-day default can fill a small disk quickly.
      - '--storage.tsdb.retention.time=30d'
    volumes:
      - ./prometheus:/prometheus
      - ./config:/etc/prometheus

  node-exporter:
    container_name: node-exporter
    image: prom/node-exporter:${'$'}{NODE_EXPORTER_VERSION:-latest}
    restart: always
    command:
      - '--path.rootfs=/host'
    volumes:
      - /:/host:ro,rslave
    pid: host

  cadvisor:
    container_name: cadvisor
    image: gcr.io/cadvisor/cadvisor:${'$'}{CADVISOR_VERSION:-latest}
    restart: always
    privileged: true
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:ro
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
"""

private val ALERTMANAGER_COMPOSE = """
services:
  alertmanager:
    container_name: alertmanager
    image: prom/alertmanager:${'$'}{ALERTMANAGER_VERSION:-latest}
    restart: always
    ports:
      - '9093:9093'
    volumes:
      - ./alertmanager:/alertmanager
      - ./config:/etc/alertmanager
"""

private val PLAUSIBLE_COMPOSE = """
services:
  plausible:
    container_name: plausible
    image: ghcr.io/plausible/community-edition:${'$'}{PLAUSIBLE_VERSION:-latest}
    restart: always
    ports:
      - '8000:8000'
    environment:
      - BASE_URL=${'$'}{BASE_URL}
      - SECRET_KEY_BASE=${'$'}{SECRET_KEY_BASE}
      - DATABASE_URL=postgres://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@db:5432/${'$'}{POSTGRES_DB}
      - CLICKHOUSE_DATABASE_URL=http://clickhouse:8123/${'$'}{CLICKHOUSE_DATABASE:-plausible_events_db}
    depends_on:
      - db
      - clickhouse

  db:
    container_name: plausible_db
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=plausible_db

  clickhouse:
    container_name: plausible_clickhouse
    image: clickhouse/clickhouse-server:${'$'}{CLICKHOUSE_VERSION:-24.8-alpine}
    restart: always
    ulimits:
      nofile:
        soft: 262144
        hard: 262144
    volumes:
      - ./clickhouse:/var/lib/clickhouse
"""
