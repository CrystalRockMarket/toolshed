package it.kituwa.toolshed.data

internal object Networking {
    val services = listOf(
        Service(
            id = "traefik",
            name = "Traefik",
            tagline = "The reverse proxy that reads your labels",
            category = Category.NETWORKING,
            blurb = "Traefik is the front door for a Docker stack. It terminates TLS, routes by hostname, and discovers new containers on its own, so adding a service does not mean editing a config file.\n\nFor a self-hosted stack it is the component that turns forty containers into forty addresses you can put certificates on. It is also the most common source of 'why is my service 502', so read the router rules before you add twenty services.",
            bestFor = listOf(
                "Automatic HTTPS for every container",
                "One hostname per service, discovered by label",
                "Routing by path, header, or container name",
            ),
            homepage = "https://traefik.io",
            docsUrl = "https://doc.traefik.io/traefik/getting-started/quick-start/",
            license = "MIT",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 5, cores = 1.0),
            defaultPort = "80",
            compose = TRAEFIK_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Join every container to one network",
                    "The shared external network is what makes discovery work. Create it once with docker network create proxy, and attach both Traefik and the services.",
                ),
                GuideStep(
                    "Publish ports 80 and 443 on the host",
                    "If another service already has them, Traefik will not bind and the error is not obvious. Check what is already listening before starting.",
                ),
                GuideStep(
                    "Use a real ACME account with an email address",
                    "Let's Encrypt expiry notices go to that address. Without it you will find out about a renewal failure from a browser error on a Tuesday.",
                ),
                GuideStep(
                    "Label every service you want exposed",
                    "The two labels are the router rule and the service itself. A container with no labels is simply not reachable through the proxy, which is a good default.",
                ),
                GuideStep(
                    "Add the dashboard only if you will use it",
                    "The dashboard is genuinely useful for debugging. It is also an unauthenticated admin view by default, so do not expose it without basic auth.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The 502 is almost always a wrong port or a wrong network",
                    "The router rule must match the container port, not the published one, and the container must be on the same network as Traefik. Those two facts explain nearly every 502.",
                ),
                GuideStep(
                    "HTTP to HTTPS redirection catches health checks",
                    "If you redirect all HTTP, and a health check or a monitoring probe follows the redirect, you get timeouts. Exclude internal paths from the redirect.",
                ),
                GuideStep(
                    "Certificate storage must be a volume",
                    "Without a persisted acme.json, Traefik requests new certificates on every restart and you will hit Let's Encrypt rate limits within a day.",
                ),
                GuideStep(
                    "Middlewares stack, and order matters",
                    "When several labels apply, Traefik applies them in a defined order that is not the order you wrote them. If auth is not applying, check the middleware list before the rule.",
                ),
            ),
            alsoConsider = listOf("authentik", "crowdsec", "homarr", "uptime-kuma"),
        ),
        Service(
            id = "pihole",
            name = "Pi-hole",
            tagline = "Network-wide ad blocking and DNS",
            category = Category.NETWORKING,
            blurb = "Pi-hole answers DNS for your whole network and drops requests to known advertising domains. It is one of the highest-value services on this list: it works for every device, including the ones you cannot install software on.\n\nIt is also the service most likely to break your internet if you get it wrong, because a broken DNS resolver takes the whole household offline. Set up a secondary resolver before you change the router.",
            bestFor = listOf(
                "Blocking ads for every device on the network",
                "A local DNS server with query logging",
                "Rewriting ad domains to a local sinkhole",
            ),
            homepage = "https://pi-hole.net",
            docsUrl = "https://docs.pi-hole.net/docker/",
            license = "MIT",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 128, ramTypicalMb = 256, diskGb = 5, cores = 0.5),
            defaultPort = "8080",
            compose = PIHOLE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set a static IP or a DHCP reservation",
                    "A Pi-hole that changes address means the router points at nothing after a reboot. Reserve the address on the router, or set a static address in the container's network.",
                ),
                GuideStep(
                    "Change the router DNS last, and only to Pi-hole",
                    "Pointing the router at Pi-hole and leaving a public resolver as the fallback gives you a working escape hatch. Using a public resolver as the primary defeats the purpose.",
                ),
                GuideStep(
                    "Add a second DNS server for safety",
                    "A second Pi-hole, or a secondary resolver on the router, means a failure of one does not take the network offline. This is the single most important step on this page.",
                ),
                GuideStep(
                    "Add your own hosts entries for local names",
                    "Instead of using local hostnames everywhere, add entries for your services in Pi-hole. One place to change when a service moves.",
                ),
                GuideStep(
                    "Blocklists are the point, and they are not all equal",
                    "The default list is small. Add a reputable list, add nothing ad-blocking on your own work domains, and check the query log when a site breaks.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "A blocked domain that a site depends on will break that site",
                    "When something will not load, check the Pi-hole query log before debugging the site. The answer is usually there, and it is a per-domain allow entry.",
                ),
                GuideStep(
                    "Docker DNS can bypass Pi-hole entirely",
                    "Containers that resolve other containers through Docker's internal resolver never ask Pi-hole. This surprises people who have everything else working perfectly.",
                ),
                GuideStep(
                    "Upstream resolvers matter for privacy",
                    "Pi-hole still forwards what it does not block to an upstream resolver. Choose one you are comfortable with, because that is where the remaining queries go.",
                ),
                GuideStep(
                    "Keep the gravity database on a volume",
                    "It is a downloaded list of blocklists. Recomputing it from scratch takes a long time, and losing it means blocking nothing until it finishes.",
                ),
            ),
            alsoConsider = listOf("adguard-home", "wg-easy", "traefik", "homarr"),
        ),
        Service(
            id = "adguard-home",
            name = "AdGuard Home",
            tagline = "The friendlier alternative DNS with filtering",
            category = Category.NETWORKING,
            blurb = "AdGuard Home does what Pi-hole does with a nicer interface and better per-client settings. It is the easier choice if you want to pause filtering for one device or see which device is making the noisy queries.\n\nChoose one or the other, not both. Two DNS servers on one network, each blocking independently, is a support problem you do not need.",
            bestFor = listOf(
                "Network-wide ad blocking with per-device control",
                "DNS with a UI that non-technical people can use",
                "Encrypted DNS to upstream resolvers",
            ),
            homepage = "https://adguard.com/en/adguard-home/overview.html",
            docsUrl = "https://github.com/AdguardTeam/AdGuardHome/wiki/Docker",
            license = "GPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 5, cores = 0.5),
            defaultPort = "3000",
            compose = ADGUARD_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Bind to port 53 on the host",
                    "AdGuard Home listens on DNS port 53 directly. If systemd-resolved or another service has it, the container will not start, and the error mentions the port rather than the conflict.",
                ),
                GuideStep(
                    "Set the web interface port to something else",
                    "The setup wizard defaults to port 80, which is very often taken. Pick 3000 for the UI and keep DNS on 53.",
                ),
                GuideStep(
                    "Complete the wizard before pointing the router at it",
                    "The wizard sets the upstream resolver and the filtering defaults. Doing it after you change DNS means an interval where nothing resolves.",
                ),
                GuideStep(
                    "Add client rules for your own services",
                    "Rewrite rules let you give your services short names. Combined with a local hostname, it means a single place to update when an address changes.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "It is a full DNS server, not a proxy",
                    "Every device on the network must be told to use it. That includes printers, TVs, and anything that resolves a hostname, which is more than you expect.",
                ),
                GuideStep(
                    "Encrypted upstream DNS has a catch",
                    "If you use DNS-over-HTTPS upstream, the resolver must be able to reach it, and the resolver needs a certificate it can validate. Self-signed certificates in particular cause silent failures.",
                ),
                GuideStep(
                    "Per-client settings are the reason to pick it over Pi-hole",
                    "Being able to disable filtering for one child's device, or for your own development machine, in five seconds is worth a lot in daily use.",
                ),
            ),
            alsoConsider = listOf("pihole", "traefik", "wg-easy"),
        ),
        Service(
            id = "portainer",
            name = "Portainer",
            tagline = "A web interface for Docker",
            category = Category.NETWORKING,
            blurb = "Portainer gives you a graphical way to see containers, networks, volumes, and logs, and to start and stop things without a terminal. It is the fastest way for a non-specialist to get comfortable with a Docker host.\n\nIt is also full root on that host, exposed through a browser. If you install it, put it behind single sign-on immediately, and use a restricted socket proxy if you can.",
            bestFor = listOf(
                "Seeing everything running on a host",
                "Managing containers for people who are not comfortable with the CLI",
                "Inspecting logs and resource use visually",
            ),
            homepage = "https://www.portainer.io",
            docsUrl = "https://docs.portainer.io/start/installation/docker",
            license = "zlib",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 128, ramTypicalMb = 256, diskGb = 2, cores = 0.5),
            defaultPort = "9443",
            compose = PORTAINER_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Install it before you need it",
                    "The first time you need it is the worst time, because it is usually when something is broken. Install it on a working host first and learn it while nothing is on fire.",
                ),
                GuideStep(
                    "Create the admin account immediately",
                    "Portainer's initial setup window is short. If you miss it, recovering the account means removing the bind mount, which is an unpleasant hour.",
                ),
                GuideStep(
                    "Only ever mount one endpoint",
                    "If Portainer can see more than one Docker socket, it can manage every host you connect. One Portainer per host is the sane arrangement.",
                ),
                GuideStep(
                    "Set up an admin password in the environment",
                    "PORT_ADMIN_PASSWORD lets you set it deterministically, which is what you want for a scripted or containerised install.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "This is root on the host, through a web browser",
                    "The Docker socket is equivalent to root. Combined with a weak password, or no password on a port-forwarded 9000, that is a complete compromise. Authenticate it, and do not expose the old 9000 port at all.",
                ),
                GuideStep(
                    "The 9000 port has no authentication by design",
                    "It exists only for the very first request that bootstraps the TLS certificate. Publish 9443 instead and never publish 9000.",
                ),
                GuideStep(
                    "The business edition is where the good features are",
                    "Role-based access, registries, and environment management are paid. For a single household host, the community edition is genuinely enough.",
                ),
                GuideStep(
                    "A socket proxy is the honest hardening",
                    "Systems such as tecnativa's socket proxy expose a subset of Docker API calls. It adds a component, and it turns a full root equivalent into something scoped.",
                ),
            ),
            alsoConsider = listOf("homarr", "dockge", "uptime-kuma"),
        ),
        Service(
            id = "homarr",
            name = "Homarr",
            tagline = "A dashboard for the services you actually use",
            category = Category.NETWORKING,
            blurb = "Homarr is a start page for your own services. You add tiles for everything you run, and it becomes the one address you open on your phone or a wall-mounted tablet to see whether things are up.\n\nIt can also read the Docker socket and show container status directly. That is convenient and it is also root access, so the same caution as Portainer applies.",
            bestFor = listOf(
                "One page linking to every service you run",
                "A wall tablet showing what is up",
                "Status tiles for the whole stack",
            ),
            homepage = "https://homarr.dev",
            docsUrl = "https://homarr.dev/docs/installation/docker",
            license = "MIT",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 2, cores = 0.5),
            defaultPort = "7575",
            compose = HOMARR_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate the secret encryption key yourself",
                    "Homarr stores credentials for its integrations encrypted with this key. Generating it with openssl rand -hex 32 and keeping it in .env is the documented approach.",
                ),
                GuideStep(
                    "Set the base URL to the real address",
                    "This is used in the login redirects and in the cookie settings. If it does not match what the browser uses, login loops forever.",
                ),
                GuideStep(
                    "Mount the appdata directory as a volume",
                    "Board layouts, tiles, and users are all in there. Without a volume, every restart resets the dashboard you just arranged.",
                ),
                GuideStep(
                    "Add the Docker socket only if you want live status",
                    "Without it, Homarr still works perfectly as a link board. With it, you get container state at the cost of a host-root equivalent on your network.",
                ),
                GuideStep(
                    "Set the user and group ids to match your other services",
                    "Running as a different user from the rest of your stack makes permission problems appear only when a volume is shared.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The Docker socket is optional and is a real risk",
                    "Read-only mounting of the socket is better than nothing but is still significant access. If you only want a link board, leave it out.",
                ),
                GuideStep(
                    "Board layouts are the reason people keep it",
                    "Dragging tiles into a layout and having it stick across devices is a small thing that makes it the page you actually open.",
                ),
                GuideStep(
                    "The status ping is an HTTP request from the container",
                    "A service that requires authentication shows as down or redirects. For those, use a tile without a status ping rather than a misleading red dot.",
                ),
            ),
            alsoConsider = listOf("portainer", "uptime-kuma", "authentik", "dockge"),
        ),
    )
}

private val TRAEFIK_COMPOSE = """
services:
  traefik:
    container_name: traefik
    image: traefik:${'$'}{TRAEFIK_VERSION:-v3.3}
    restart: always
    command:
      - '--providers.docker=true'
      - '--providers.docker.exposedbydefault=false'
      - '--providers.docker.network=proxy'
      - '--entrypoints.web.address=:80'
      - '--entrypoints.web.http.redirections.entrypoint.to=websecure'
      - '--entrypoints.web.http.redirections.entrypoint.scheme=https'
      - '--entrypoints.websecure.address=:443'
      - '--entrypoints.websecure.http.tls.certresolver=letsencrypt'
      - '--certificatesresolvers.letsencrypt.acme.email=${'$'}{ACME_EMAIL}'
      - '--certificatesresolvers.letsencrypt.acme.storage=/letsencrypt/acme.json'
      - '--certificatesresolvers.letsencrypt.acme.tlschallenge=true'
      - '--api.dashboard=true'
      - '--api.insecure=false'
      - '--ping=true'
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    networks:
      - proxy

  # Example of a routed service. Add these two labels to any container that
  # should be reachable at a hostname.
  #   labels:
  #     - traefik.enable=true
  #     - traefik.http.routers.example.rule=Host(`example.com`)
  #     - traefik.http.routers.example.entrypoints=websecure
  #     - traefik.http.services.example.loadbalancer.server.port=1234

networks:
  proxy:
    external: true
"""

private val PIHOLE_COMPOSE = """
services:
  pihole:
    container_name: pihole
    image: pihole/pihole:${'$'}{PIHOLE_VERSION:-latest}
    restart: unless-stopped
    environment:
      - TZ=${'$'}{TZ:-UTC}
      - FTLCONF_webserver_api_password=${'$'}{FTLCONF_WEBSERVER_API_PASSWORD}
      # Web interface on 8080 so it does not fight for port 80 with a proxy.
      - FTLCONF_webserver_bind_interfaces=0.0.0.0
    ports:
      - '53:53/tcp'
      - '53:53/udp'
      - '8080:80'
    volumes:
      - ./etc-pihole:/etc/pihole
      - ./dns:/var/lib/dnsmasq
    # Needed for FTL to see interface statistics.
    cap_add:
      - NET_ADMIN
    dns:
      - 127.0.0.1
"""

private val ADGUARD_COMPOSE = """
services:
  adguardhome:
    container_name: adguardhome
    image: adguard/adguardhome:${'$'}{ADGUARD_VERSION:-latest}
    restart: always
    ports:
      - '53:53/tcp'
      - '53:53/udp'
      - '3000:3000'
      - '80:80'
    volumes:
      - ./work:/opt/adguardhome/work
      - ./conf:/opt/adguardhome/conf
"""

private val PORTAINER_COMPOSE = """
services:
  portainer:
    container_name: portainer
    image: portainer/portainer-ce:${'$'}{PORTAINER_VERSION:-latest}
    restart: always
    ports:
      - '9443:9443'
      # 8000 is for the edge agent tunnel. Only expose it if you use agents.
      - '127.0.0.1:8000:8000'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - portainer_data:/data
    environment:
      - PORT_ADMIN_PASSWORD=${'$'}{PORTAINER_ADMIN_PASSWORD}

volumes:
  portainer_data:
"""

private val HOMARR_COMPOSE = """
services:
  homarr:
    container_name: homarr
    image: ghcr.io/homarr-labs/homarr:${'$'}{HOMARR_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '7575:7575'
    environment:
      # Generate with: openssl rand -hex 32
      - SECRET_ENCRYPTION_KEY=${'$'}{SECRET_ENCRYPTION_KEY}
      - BASE_URL=${'$'}{BASE_URL}
      - NEXTAUTH_URL=${'$'}{BASE_URL}
      - AUTH_COOKIE_PREFIX=homarr
      - AUTH_LOGOUT_REDIRECT_URL=${'$'}{BASE_URL}/auth/login
      - PUID=${'$'}{PUID:-1000}
      - PGID=${'$'}{PGID:-1000}
      - TZ=${'$'}{TZ:-UTC}
    volumes:
      - ./appdata:/appdata
      # Read-only and only if you want live container status on your tiles.
      # This is host root access. Read it before you add it.
      - /var/run/docker.sock:/var/run/docker.sock:ro
"""
