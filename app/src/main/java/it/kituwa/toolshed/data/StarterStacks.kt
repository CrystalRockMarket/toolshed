package it.kituwa.toolshed.data

/**
 * Bundles for the combinations people actually start with. Each one is a
 * complete, consistent compose file: the service names, networks, and
 * environment variables all line up, so it can be used as-is.
 */
internal object StarterStacks {
    val stacks = listOf(
        StarterStack(
            id = "vps-starter",
            name = "Small VPS starter",
            tagline = "A front door, an identity provider, and something that tells you when it breaks",
            description = "The four things every remote server wants and most first installs leave out: a reverse proxy that terminates certificates, single sign-on so every service shares one login, uptime monitoring from outside, and a dashboard to see what you have. PostgreSQL and Redis are included because the two applications both want them and one shared instance is cheaper and simpler than two.\n\nThis is the stack to copy onto a 4 GB VPS and grow from. It is deliberately not a collection of every service in the catalogue: it is the set that makes adding more services later painless.",
            serviceIds = listOf("traefik", "authentik", "uptime-kuma", "homarr", "postgres", "redis"),
            targetHardware = "2 vCPU, 4 GB RAM, 40 GB disk. Comfortable on a 2 GB VPS, tight on 1 GB.",
            difficulty = Difficulty.FOCUSSED,
            compose = VPS_STARTER_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Create the shared network before anything else",
                    "docker network create proxy. Every service in this stack joins it, and it is what lets Traefik discover them and let applications reach the database by name.",
                ),
                GuideStep(
                    "Write the .env with every secret generated",
                    "Use openssl rand -hex 32 for each of them. Nothing in this stack has a working default, and a placeholder value is a future outage.",
                ),
                GuideStep(
                    "Bring up just the databases first",
                    "docker compose up -d postgres redis, then wait a moment. Starting an application against a database that is still initialising produces a first-run error that looks like a configuration mistake.",
                ),
                GuideStep(
                    "Start Traefik and confirm the dashboard loads",
                    "Before adding applications, check Traefik binds to 80 and 443. Something else already listening is the most common reason this step fails.",
                ),
                GuideStep(
                    "Start Authentik and complete the bootstrap",
                    "The bootstrap email and password in .env create the first administrator. Do this before Uptime Kuma or Homarr are reachable from the internet, or they can be created by someone else.",
                ),
                GuideStep(
                    "Add Authentik as an OIDC provider for Homarr",
                    "This is the payoff of doing Authentik early. One provider, and Homarr loses its own password entirely.",
                ),
                GuideStep(
                    "Point Uptime Kuma at the public URL, from outside",
                    "Run the monitoring somewhere else. Monitoring the server from the server cannot detect the server being down, which is the failure you most want to catch.",
                ),
                GuideStep(
                    "Add a certificate-backed hostname for each service",
                    "One DNS record per service, with the Let's Encrypt email set in .env. The ACME account is what receives expiry warnings, and without an address you find out from a browser error.",
                ),
            ),
        ),
        StarterStack(
            id = "photo-vault",
            name = "Photo vault",
            tagline = "Phone backup, timeline, and face search on your own disk",
            description = "A complete photo setup: the Immich server with its machine learning container, the exact Postgres build it needs, Redis, and a reverse proxy so the mobile app can connect over HTTPS.\n\nThe storage question comes first and everything else is easy. Decide where the photos will live for the next decade, because that path is effectively permanent in Immich's database, and then run this as written.",
            serviceIds = listOf("immich", "postgres", "redis", "traefik"),
            targetHardware = "4 vCPU, 8 GB RAM. Photos need far more disk than RAM: budget 200 GB to start and grow it.",
            difficulty = Difficulty.FOCUSSED,
            compose = PHOTO_VAULT_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Choose a local path with room to grow",
                    "Not a network mount, not a path you plan to change. Immich stores the absolute path and changing it is a migration rather than an edit.",
                ),
                GuideStep(
                    "Set UPLOAD_LOCATION and DB_DATA_LOCATION in .env",
                    "Two different paths for two different things: the photos and the database. Keeping them separate means a bad migration cannot take both at once.",
                ),
                GuideStep(
                    "Do not swap the Postgres image for a plain one",
                    "Immich needs pgvecto.rs and VectorChord. The image tag in the compose file pins that build, and a plain postgres:latest fails in a way that looks like a schema problem.",
                ),
                GuideStep(
                    "Start it and register the administrator yourself",
                    "The first account created is the owner. Do it before the reverse proxy route is public.",
                ),
                GuideStep(
                    "Install the mobile app and start the backup",
                    "Set Wi-Fi-only if you are on a metered connection, then let the first pass run. For a large library this takes a long time and looks stalled while it is working.",
                ),
                GuideStep(
                    "Put it behind the proxy and add a daily database dump",
                    "The photos are files your normal backup covers. The database is the album structure, face data, and search index, and it needs pg_dump on a schedule.",
                ),
            ),
        ),
        StarterStack(
            id = "media-box",
            name = "Media box",
            tagline = "Films and music for the household, with hardware transcoding",
            description = "Two media servers sharing one reverse proxy: Jellyfin for video and Navidrome for music. The media directories are mounted read-only, so neither application can write to your files even if it has a bug.\n\nThe hardware transcoding lines in the Jellyfin service are commented because they only work on some machines. Uncomment them on Intel hardware and the difference between a smooth stream and a buffering spinner is roughly an order of magnitude.",
            serviceIds = listOf("jellyfin", "navidrome", "traefik"),
            targetHardware = "4 vCPU, 4 GB RAM, and as much disk as your collection needs. Intel graphics strongly recommended for transcoding.",
            difficulty = Difficulty.EASY,
            compose = MEDIA_BOX_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Organise the media into one folder per library",
                    "Films, television, and music in separate directories. Jellyfin's metadata scraping and artwork work far better with a library per genre than one mixed folder.",
                ),
                GuideStep(
                    "Mount the media read-only",
                    "The :ro suffix in the volume definitions is the single most valuable line in this stack. It costs nothing and prevents an entire category of mistake.",
                ),
                GuideStep(
                    "Enable hardware transcoding if the hardware supports it",
                    "Uncomment the devices line on Intel machines, then select the QuickSync encoder in the Jellyfin dashboard. Without it, playback problems are really just transcoding problems.",
                ),
                GuideStep(
                    "Complete both wizards",
                    "Jellyfin wants a server name and library definitions. Navidrome wants a music folder and an admin account. Both create an account on first visit and neither will prompt you again.",
                ),
                GuideStep(
                    "Install the Jellyfin app on the television",
                    "Direct play is the goal. A client that can play your files as they are means almost no server CPU is used, which is the difference between a Pi and a workstation.",
                ),
                GuideStep(
                    "Give each a hostname so remote access works",
                    "The apps are strict about HTTPS. A certificate from the proxy in this stack takes a minute per service.",
                ),
            ),
        ),
        StarterStack(
            id = "remote-access",
            name = "Remote access kit",
            tagline = "A way in, a way to block things, and a way to see attacks",
            description = "The perimeter of a home network: WireGuard through a web interface so you can reach your services from anywhere, network-wide DNS blocking, and behaviour-based intrusion detection sitting in front of your proxy.\n\nPut this up before you expose anything publicly. The order matters: the VPN first, so there is a private route to every service, and only then certificates on hostnames.",
            serviceIds = listOf("wg-easy", "pihole", "crowdsec", "traefik"),
            targetHardware = "2 vCPU, 2 GB RAM, 20 GB disk. Comfortable alongside the rest of your stack.",
            difficulty = Difficulty.FOCUSSED,
            compose = REMOTE_ACCESS_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Open UDP 51820 in the router and the firewall",
                    "WireGuard is UDP only. A blocked port shows as a handshake that never completes, and the troubleshooting paths lead away from the real problem.",
                ),
                GuideStep(
                    "Give the server a hostname before enrolling devices",
                    "Dynamic DNS is the usual answer for a home connection. The VPN endpoint should be a name that follows your IP, not an IP you have to edit on every device.",
                ),
                GuideStep(
                    "Set up Pi-hole with a secondary resolver as a fallback",
                    "The DNS change is the step that can take the household offline. A second resolver on the router means a failure of one does not take the network with it.",
                ),
                GuideStep(
                    "Enrol one phone, then confirm you can reach everything",
                    "If the tunnel connects and services are reachable, the hard part is done. Add the other devices afterwards, one configuration per device.",
                ),
                GuideStep(
                    "Install the bouncer, not just the parser",
                    "CrowdSec without a bouncer parses logs and blocks nothing. It appears to work because it is quiet, which is the worst possible failure mode.",
                ),
                GuideStep(
                    "Whitelist your private ranges before enabling remediation",
                    "A bouncer that blocks your own LAN can lock you out of your own services, including the proxy you would use to fix it. This check takes a minute and prevents a bad afternoon.",
                ),
                GuideStep(
                    "Add a bouncer for the proxy you actually run",
                    "The one in this stack is the Traefik bouncer. If you use nginx or Caddy, use the matching bouncer instead; a mismatched one silently enforces nothing.",
                ),
            ),
        ),
        StarterStack(
            id = "family-paperwork",
            name = "Family paperwork",
            tagline = "Scan, file, and find the household's documents",
            description = "A document setup that actually retires the filing cabinet. A scanner drops files into a watched folder, Paperless reads them and files them by correspondent and document type, Nextcloud shares the results with the family, and Stirling PDF handles the ad-hoc jobs that used to mean uploading a document to a website.\n\nEvery one of these handles private information. That is the argument for running them yourself, and also the reason to put the whole stack behind the VPN from the remote access stack rather than on a public port.",
            serviceIds = listOf("paperless-ng", "nextcloud", "stirling-pdf", "traefik"),
            targetHardware = "4 vCPU, 8 GB RAM, 50 GB disk plus whatever the documents need. The OCR work is the CPU cost.",
            difficulty = Difficulty.FOCUSSED,
            compose = FAMILY_PAPERWORK_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate the secrets and pick the OCR language",
                    "The Django secret key and the OCR language both go in .env. Wrong OCR language produces confidently incorrect text, which is worse than no text at all.",
                ),
                GuideStep(
                    "Point Paperless at a consume folder your scanner can reach",
                    "That shared directory is what makes the automation work. Without it you are re-uploading documents by hand, which is the whole job you were trying to remove.",
                ),
                GuideStep(
                    "Start Paperless and create a superuser",
                    "The first account is not an administrator. Create the superuser on the command line, then make a normal admin from it.",
                ),
                GuideStep(
                    "Start Nextcloud with a fresh database",
                    "Trusted domains must be set before the first request or the interface will warn on every page. The install warns rather than refuses, which means people get used to ignoring the warning.",
                ),
                GuideStep(
                    "Enable login on Stirling PDF immediately",
                    "It ships with authentication off so the first run is easy. An unauthenticated PDF tool on a network is an open file server.",
                ),
                GuideStep(
                    "Set up a rule for the first recurring document type",
                    "Anything from a bill sender tagged as a bill and filed under a tag. Doing this by hand afterwards is most of the work the software was meant to save.",
                ),
                GuideStep(
                    "Back up both databases nightly",
                    "Nextcloud and Paperless each have state that exists nowhere else: accounts and shares in one, the document index and workflow rules in the other. A file-level backup of the volumes is not a substitute.",
                ),
            ),
        ),
    )
}

private val VPS_STARTER_COMPOSE = """
networks:
  proxy:
    external: true

services:
  traefik:
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
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    networks:
      - proxy

  authentik:
    image: ghcr.io/goauthentik/server:${'$'}{AUTHENTIK_TAG:-latest}
    restart: unless-stopped
    ports:
      - '9000:9000'
    environment:
      - AUTHENTIK_SECRET_KEY=${'$'}{AUTHENTIK_SECRET_KEY}
      - AUTHENTIK_ENCRYPTION_KEY=${'$'}{AUTHENTIK_ENCRYPTION_KEY}
      - AUTHENTIK_POSTGRESQL__HOST=db
      - AUTHENTIK_POSTGRESQL__USER=${'$'}{POSTGRES_USER}
      - AUTHENTIK_POSTGRESQL__PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - AUTHENTIK_POSTGRESQL__NAME=authentik
      - AUTHENTIK_BOOTSTRAP_PASSWORD=${'$'}{AUTHENTIK_BOOTSTRAP_PASSWORD}
      - AUTHENTIK_BOOTSTRAP_EMAIL=${'$'}{AUTHENTIK_BOOTSTRAP_EMAIL}
    volumes:
      - ./authentik/media:/media
    depends_on:
      db:
        condition: service_healthy
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.authentik.rule=Host(`${'$'}{AUTHENTIK_HOSTNAME}`)
      - traefik.http.routers.authentik.entrypoints=websecure

  uptime-kuma:
    image: louislam/uptime-kuma:2
    restart: always
    ports:
      - '3001:3001'
    volumes:
      - ./uptime-kuma:/app/data
      # Read-only socket so Kuma can report container health.
      - /var/run/docker.sock:/var/run/docker.sock:ro
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.kuma.rule=Host(`${'$'}{KUMA_HOSTNAME}`)
      - traefik.http.routers.kuma.entrypoints=websecure

  homarr:
    image: ghcr.io/homarr-labs/homarr:${'$'}{HOMARR_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '7575:7575'
    environment:
      - SECRET_ENCRYPTION_KEY=${'$'}{SECRET_ENCRYPTION_KEY}
      - BASE_URL=https://${'$'}{HOMARR_HOSTNAME}
      - NEXTAUTH_URL=https://${'$'}{HOMARR_HOSTNAME}
    volumes:
      - ./homarr:/appdata
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.homarr.rule=Host(`${'$'}{HOMARR_HOSTNAME}`)
      - traefik.http.routers.homarr.entrypoints=websecure

  db:
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=authentik
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U ${'$'}{POSTGRES_USER}']
      interval: 10s
      retries: 5
    networks:
      - proxy

  redis:
    image: docker.io/library/redis:7-alpine
    restart: always
    command: >
      redis-server
        --requirepass ${'$'}{REDIS_PASSWORD}
        --maxmemory 256mb
        --maxmemory-policy allkeys-lru
    environment:
      - REDIS_PASSWORD=${'$'}{REDIS_PASSWORD}
    volumes:
      - ./redisdata:/data
    networks:
      - proxy

# .env needed by this stack:
#   ACME_EMAIL=you@example.com
#   AUTHENTIK_SECRET_KEY=          openssl rand -hex 32
#   AUTHENTIK_ENCRYPTION_KEY=      openssl rand -hex 32
#   AUTHENTIK_BOOTSTRAP_EMAIL=     you@example.com
#   AUTHENTIK_BOOTSTRAP_PASSWORD=  something long
#   SECRET_ENCRYPTION_KEY=         openssl rand -hex 32
#   POSTGRES_USER=authentik
#   POSTGRES_PASSWORD=             openssl rand -base64 32
#   REDIS_PASSWORD=                openssl rand -base64 32
#   AUTHENTIK_HOSTNAME=id.example.com
#   KUMA_HOSTNAME=uptime.example.com
#   HOMARR_HOSTNAME=home.example.com
"""

private val PHOTO_VAULT_COMPOSE = """
networks:
  proxy:
    external: true

services:
  traefik:
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
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    networks:
      - proxy

  immich-server:
    image: ghcr.io/immich-app/immich-server:${'$'}{IMMICH_VERSION:-release}
    restart: always
    volumes:
      - ${'$'}{UPLOAD_LOCATION}:/data
      - /etc/localtime:/etc/localtime:ro
    environment:
      - DB_DATA_LOCATION=/var/lib/postgresql/data
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.immich.rule=Host(`${'$'}{IMMICH_HOSTNAME}`)
      - traefik.http.routers.immich.entrypoints=websecure
      - traefik.http.services.immich.loadbalancer.server.port=2283

  immich-machine-learning:
    image: ghcr.io/immich-app/immich-machine-learning:${'$'}{IMMICH_VERSION:-release}
    restart: always
    volumes:
      - ml-cache:/cache
    networks:
      - proxy

  redis:
    image: docker.io/valkey/valkey:9
    restart: always
    healthcheck:
      test: redis-cli ping || exit 1
    networks:
      - proxy

  database:
    image: ghcr.io/immich-app/postgres:14-vectorchord0.4.3-pgvectors0.2.0
    restart: always
    shm_size: 128mb
    volumes:
      - ${'$'}{DB_DATA_LOCATION}:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
    networks:
      - proxy

volumes:
  ml-cache:

# .env needed by this stack:
#   UPLOAD_LOCATION=/srv/immich/uploads
#   DB_DATA_LOCATION=/srv/immich/database
#   POSTGRES_USER=immich
#   POSTGRES_PASSWORD=     openssl rand -base64 32
#   POSTGRES_DB=immich
#   IMMICH_VERSION=v1.119.0
#   IMMICH_HOSTNAME=photos.example.com
#   ACME_EMAIL=you@example.com
"""

private val MEDIA_BOX_COMPOSE = """
networks:
  proxy:
    external: true

services:
  traefik:
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
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    networks:
      - proxy

  jellyfin:
    image: jellyfin/jellyfin:${'$'}{JELLYFIN_VERSION:-latest}
    user: ${'$'}{PUID}:${'$'}{PGID}
    restart: unless-stopped
    network_mode: host
    volumes:
      - ./jellyfin/config:/config
      - ./jellyfin/cache:/cache
      # Read-only. Jellyfin never needs to write to your media.
      - ${'$'}{MEDIA_ROOT}/movies:/media/movies:ro
      - ${'$'}{MEDIA_ROOT}/tv:/media/tv:ro
    # Uncomment on Intel hardware for QuickSync transcoding.
    # devices:
    #   - /dev/dri:/dev/dri
    labels:
      - traefik.enable=true
      - traefik.http.routers.jellyfin.rule=Host(`${'$'}{JELLYFIN_HOSTNAME}`)
      - traefik.http.routers.jellyfin.entrypoints=websecure
      - traefik.http.services.jellyfin.loadbalancer.server.port=8096

  navidrome:
    image: deluan/navidrome:${'$'}{NAVIDROME_VERSION:-latest}
    user: ${'$'}{PUID}:${'$'}{PGID}
    restart: unless-stopped
    environment:
      - ND_MUSICFOLDER=/music
      - ND_SCANSCHEDULE=1h
    volumes:
      - ./navidrome/data:/data
      - ${'$'}{MEDIA_ROOT}/music:/music:ro
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.navidrome.rule=Host(`${'$'}{NAVIDROME_HOSTNAME}`)
      - traefik.http.routers.navidrome.entrypoints=websecure
      - traefik.http.services.navidrome.loadbalancer.server.port=4533

# .env needed by this stack:
#   MEDIA_ROOT=/srv/media
#   PUID=1000
#   PGID=1000
#   ACME_EMAIL=you@example.com
#   JELLYFIN_HOSTNAME=tv.example.com
#   NAVIDROME_HOSTNAME=music.example.com
"""

private val REMOTE_ACCESS_COMPOSE = """
networks:
  proxy:
    external: true

services:
  wg-easy:
    image: ghcr.io/wg-easy/wg-easy:${'$'}{WG_EASY_VERSION:-latest}
    restart: unless-stopped
    environment:
      - INSECURE=false
      - PASSWORD=${'$'}{WG_EASY_PASSWORD}
      - DEFAULT_DNS=${'$'}{PIHOLE_IP:-10.8.0.1}
    ports:
      - '51820:51820/udp'
      - '51821:51821/tcp'
      - '51821:51821/udp'
      - '8080:80'
    volumes:
      - ./wg-easy:/etc/wireguard
    cap_add:
      - NET_ADMIN
    sysctls:
      net.ipv4.ip_forward: 1
    networks:
      - proxy

  pihole:
    image: pihole/pihole:${'$'}{PIHOLE_VERSION:-latest}
    restart: unless-stopped
    environment:
      - TZ=${'$'}{TZ:-UTC}
      - FTLCONF_webserver_api_password=${'$'}{PIHOLE_PASSWORD}
    ports:
      - '53:53/tcp'
      - '53:53/udp'
      - '8081:80'
    volumes:
      - ./pihole/etc:/etc/pihole
      - ./pihole/dnsmasq:/var/lib/dnsmasq
    cap_add:
      - NET_ADMIN
    networks:
      - proxy

  crowdsec:
    image: crowdsecurity/crowdsec:${'$'}{CROWDSEC_VERSION:-latest}
    restart: always
    environment:
      - COLLECTIONS=${'$'}{CROWDSEC_COLLECTIONS:-crowdsecurity/traefik}
    volumes:
      - ./crowdsec/config:/etc/crowdsec
      - ./crowdsec/data:/var/lib/crowdsec/data
      # Mount the proxy log directory so it can parse real access logs.
      - /var/log/traefik:/var/log/traefik:ro
    networks:
      - proxy

  crowdsec-bouncer:
    image: crowdsecurity/traefik-bouncer:${'$'}{BOUNCER_VERSION:-latest}
    restart: always
    environment:
      - CROWDSEC_BOUNCER_API_KEY=${'$'}{CROWDSEC_BOUNCER_API_KEY}
      - CROWDSEC_AGENT_URL=http://crowdsec:8080
    depends_on:
      - crowdsec
    networks:
      - proxy

  traefik:
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
      - '--accesslog=true'
      - '--log.filePath=/var/log/traefik/access.log'
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
      - ./traefik-logs:/var/log/traefik
    networks:
      - proxy

volumes:
  traefik-logs:

# .env needed by this stack:
#   ACME_EMAIL=you@example.com
#   WG_EASY_PASSWORD=            something long
#   PIHOLE_PASSWORD=             openssl rand -hex 16
#   PIHOLE_IP=10.8.0.1
#   CROWDSEC_BOUNCER_API_KEY=    from 'cscli bouncers add'
#   CROWDSEC_COLLECTIONS=crowdsecurity/traefik
"""

private val FAMILY_PAPERWORK_COMPOSE = """
networks:
  proxy:
    external: true

services:
  traefik:
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
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./letsencrypt:/letsencrypt
    networks:
      - proxy

  paperless:
    image: ghcr.io/paperless-ngx/paperless-ngx:${'$'}{PAPERLESS_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '8000:8000'
    volumes:
      - ./paperless/data:/usr/src/paperless/data
      - ./paperless/media:/usr/src/paperless/media
      # Shared with your scanner's software. This is the automation path.
      - ./paperless/consume:/usr/src/paperless/consume
    environment:
      - PAPERLESS_REDIS=redis://broker:6379
      - PAPERLESS_DBHOST=paperless-db
      - PAPERLESS_DBNAME=paperless
      - PAPERLESS_DBUSER=paperless
      - PAPERLESS_DBPASS=${'$'}{PAPERLESS_DBPASS}
      - PAPERLESS_SECRET_KEY=${'$'}{PAPERLESS_SECRET_KEY}
      - PAPERLESS_OCR_LANGUAGE=${'$'}{PAPERLESS_OCR_LANGUAGE:-eng}
      - PAPERLESS_URL=https://${'$'}{PAPERLESS_HOSTNAME}
      - PAPERLESS_TIME_ZONE=${'$'}{TZ:-UTC}
    depends_on:
      paperless-db:
        condition: service_healthy
      broker:
        condition: service_started
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.paperless.rule=Host(`${'$'}{PAPERLESS_HOSTNAME}`)
      - traefik.http.routers.paperless.entrypoints=websecure
      - traefik.http.services.paperless.loadbalancer.server.port=8000

  paperless-db:
    image: docker.io/library/postgres:16
    restart: unless-stopped
    volumes:
      - ./paperless/pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=paperless
      - POSTGRES_USER=paperless
      - POSTGRES_PASSWORD=${'$'}{PAPERLESS_DBPASS}
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U paperless']
      interval: 10s
      retries: 5
    networks:
      - proxy

  broker:
    image: docker.io/valkey/valkey:8-alpine
    restart: unless-stopped
    volumes:
      - ./paperless/redisdata:/data
    networks:
      - proxy

  nextcloud:
    image: nextcloud:${'$'}{NEXTCLOUD_VERSION:-29-apache}
    restart: always
    ports:
      - '8090:80'
    volumes:
      - ./nextcloud:/var/www/html
      - /etc/localtime:/etc/localtime:ro
    environment:
      - MYSQL_HOST=nextcloud-db
      - MYSQL_DATABASE=nextcloud
      - MYSQL_USER=nextcloud
      - MYSQL_PASSWORD=${'$'}{NEXTCLOUD_DBPASS}
      - NEXTCLOUD_TRUSTED_DOMAINS=${'$'}{NEXTCLOUD_HOSTNAME}
      - REDIS_HOST=nextcloud-redis
      - PHP_MEMORY_LIMIT=512M
    depends_on:
      - nextcloud-db
      - nextcloud-redis
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.nextcloud.rule=Host(`${'$'}{NEXTCLOUD_HOSTNAME}`)
      - traefik.http.routers.nextcloud.entrypoints=websecure
      - traefik.http.services.nextcloud.loadbalancer.server.port=80

  nextcloud-db:
    image: mariadb:11.4
    restart: always
    command: --transaction-isolation=READ-COMMITTED --log-bin=binlog --binlog-format=ROW
    volumes:
      - ./nextcloud-db:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${'$'}{MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=nextcloud
      - MYSQL_USER=nextcloud
      - MYSQL_PASSWORD=${'$'}{NEXTCLOUD_DBPASS}
    networks:
      - proxy

  nextcloud-redis:
    image: redis:7-alpine
    restart: always
    networks:
      - proxy

  stirling:
    image: docker.io/stirlingtools/stirling-pdf:${'$'}{STIRLING_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '8080:8080'
    volumes:
      - ./stirling/config:/config
      - ./stirling/data:/usr/src/pdf-data
    environment:
      - SECURITY_ENABLELOGIN=true
      - TZ=${'$'}{TZ:-UTC}
    networks:
      - proxy
    labels:
      - traefik.enable=true
      - traefik.http.routers.stirling.rule=Host(`${'$'}{STIRLING_HOSTNAME}`)
      - traefik.http.routers.stirling.entrypoints=websecure
      - traefik.http.services.stirling.loadbalancer.server.port=8080

# .env needed by this stack:
#   ACME_EMAIL=you@example.com
#   PAPERLESS_SECRET_KEY=        openssl rand -hex 32
#   PAPERLESS_DBPASS=            openssl rand -base64 32
#   PAPERLESS_OCR_LANGUAGE=eng
#   PAPERLESS_HOSTNAME=paper.example.com
#   NEXTCLOUD_DBPASS=            openssl rand -base64 32
#   MYSQL_ROOT_PASSWORD=         openssl rand -base64 32
#   NEXTCLOUD_HOSTNAME=files.example.com
#   STIRLING_HOSTNAME=pdf.example.com
"""
