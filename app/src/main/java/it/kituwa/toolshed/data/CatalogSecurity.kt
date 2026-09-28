package it.kituwa.toolshed.data

internal object Security {
    val services = listOf(
        Service(
            id = "authentik",
            name = "Authentik",
            tagline = "Single sign-on for everything you run",
            category = Category.SECURITY,
            blurb = "Authentik is an identity provider. You log in once, and every other service you run accepts that login instead of having its own password. When a service needs a new password, you change it in one place.\n\nThis is the single highest-leverage thing on this list. Once it is running, adding single sign-on to a service takes a few minutes, and the number of stored passwords drops with every one you convert.",
            bestFor = listOf(
                "One login for every self-hosted service",
                "Two-factor authentication you control",
                "Offboarding someone without changing ten passwords",
            ),
            homepage = "https://goauthentik.io",
            docsUrl = "https://docs.goauthentik.io/install-config/install/docker-compose",
            license = "MIT",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "9000",
            compose = AUTHENTIK_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate the secret key before the first start",
                    "Postgres needs an initial password and the application needs a secret key. The documented openssl command produces both. Starting without them leaves an instance that looks configured and is not.",
                ),
                GuideStep(
                    "Set the brand and the trusted host header early",
                    "The hostname is baked into the provider configuration and the links in emails. It is much easier to set now than to change after users exist.",
                ),
                GuideStep(
                    "Add a second factor before you invite anyone",
                    "Enrol a security key or TOTP for your own account first. Losing the only admin account of a single sign-on system is a genuinely bad afternoon.",
                ),
                GuideStep(
                    "Create an application and a provider for one service",
                    "The three-level model is applications, providers, and flows. One OIDC provider can serve many applications, which is the part that makes it worth the setup time.",
                ),
                GuideStep(
                    "Convert one low-risk service first",
                    "A dashboard such as Homarr or a status page. Get the whole flow working end to end before you put it in front of something you depend on.",
                ),
                GuideStep(
                    "Set up a recovery flow that does not depend on the server",
                    "Static recovery codes, not an email link. If the server is down, an email-based recovery will not help you get back into it.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Trusting proxy headers is a security decision",
                    "Authentik needs to know the scheme and the client address. Behind a reverse proxy this means trusting forwarded headers, which is only safe if nothing else can reach the container on the published port.",
                ),
                GuideStep(
                    "Do not publish the port to the internet behind a proxy",
                    "Bind the application to localhost and let the reverse proxy terminate TLS. If both the port and the proxy are public, you have two ways in and one is unprotected.",
                ),
                GuideStep(
                    "Upgrade path skips versions",
                    "Like most applications in this position, the supported upgrade path is sequential. Check the release notes for each version between where you are and where you want to be.",
                ),
                GuideStep(
                    "Enforce MFA on the outpost, not just the login",
                    "Password flows can bypass the second factor. If your services are on a private network and the requirement is strong, a policy in the application is the place to put it.",
                ),
            ),
            alsoConsider = listOf("outline", "grafana", "traefik", "crowdsec"),
        ),
        Service(
            id = "vault",
            name = "HashiCorp Vault",
            tagline = "Secrets management with an audit trail",
            category = Category.SECURITY,
            blurb = "Vault stores passwords, API keys, and certificates and hands them out only to authenticated workloads, with a record of who asked for what. It solves the problem of twelve services each holding a copy of the same database password.\n\nIt is also the most operationally demanding thing here. Vault has an unseal procedure, a storage backend, and a lease model, and a small mistake in those takes the whole thing offline.",
            bestFor = listOf(
                "One place for database passwords and API keys",
                "Short-lived credentials instead of static ones",
                "An audit trail of who read which secret",
            ),
            homepage = "https://www.vaultproject.io",
            docsUrl = "https://developer.hashicorp.com/vault/tutorials/get-started/standalone",
            license = "BUSL-1.1",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "8200",
            compose = VAULT_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Understand the storage backend is not a backup",
                    "File storage is the development configuration. It has no replication, no encryption of the data at rest, and it is not supported for production. Consul or integrated storage is the real answer.",
                ),
                GuideStep(
                    "Learn the unseal procedure before you need it",
                    "Vault is sealed when it restarts, and it needs a quorum of key shares to unseal. Practise it once, with the container running, so that the first real restart is not the first attempt.",
                ),
                GuideStep(
                    "Set the API address to what callers will use",
                    "Vault hands out addresses inside the seal, and a client that cannot resolve them fails in a way that looks like an authentication problem.",
                ),
                GuideStep(
                    "Start with one secret engine that does the work",
                    "The database secrets engine generates short-lived database credentials. It is the feature that pays for the operational overhead, and it needs no static password at all.",
                ),
                GuideStep(
                    "Use a Kubernetes-style auth method only if you have one",
                    "If your workloads are plain Docker Compose, the token auth method is a better fit than a JWT method designed for orchestrators.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The licence is BUSL, not open source",
                    "Vault moved to the Business Source License. It is free to run yourself and free in small teams, but it is not an open-source licence, so it cannot be bundled in an F-Droid app. Use it as a service you operate.",
                ),
                GuideStep(
                    "Losing the unseal keys means losing everything",
                    "With file storage, losing the keys means the data is unrecoverable. Store the keys and the recovery keys somewhere physically separate from the machine.",
                ),
                GuideStep(
                    "It stays sealed on every restart without an init container",
                    "A production setup auto-unseals with a key stored somewhere else, or a person unseals it on boot. Which of those you choose is a real security decision, not a convenience one.",
                ),
                GuideStep(
                    "Read-only tokens for anything that only reads",
                    "Vault policies are deny-by-default. A token scoped to one path and read-only is a small change that limits the damage of a leak considerably.",
                ),
            ),
            alsoConsider = listOf("authentik", "wg-easy", "pihole"),
        ),
        Service(
            id = "wg-easy",
            name = "WireGuard with wg-easy",
            tagline = "A VPN into your own network with a web interface",
            category = Category.SECURITY,
            blurb = "WireGuard is a fast, simple VPN. wg-easy gives it a web interface so you can add and remove devices without touching the server. It works well on a Pi and it is the first thing most people set up.\n\nOnce it is running you have a private network reachable from your phone, and every other service becomes reachable without publishing a single port.",
            bestFor = listOf(
                "Reaching home services from anywhere",
                "Avoiding a public port on anything",
                "A phone that talks to your server directly",
            ),
            homepage = "https://github.com/wg-easy/wg-easy",
            docsUrl = "https://wg-easy.io/getting-started/",
            license = "MIT",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 128, ramTypicalMb = 256, diskGb = 1, cores = 0.5),
            defaultPort = "51820",
            compose = WGEASY_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Allow the UDP port through the firewall",
                    "WireGuard is UDP 51820. If it is blocked, clients show a handshake that never completes, and the obvious troubleshooting path leads nowhere.",
                ),
                GuideStep(
                    "Set the host and the endpoint address",
                    "The endpoint is the public address clients connect to, which for most home setups is behind a dynamic DNS name. Hard-coding an IP breaks the day it changes.",
                ),
                GuideStep(
                    "Create a config per device, not per person",
                    "One configuration per device lets you revoke exactly one phone without touching everyone else's. This is the habit worth having from day one.",
                ),
                GuideStep(
                    "Point your other services at private addresses",
                    "Now that a tunnel exists, a service on 192.168.x.x is reachable from your phone and does not need a published port at all. This is the whole point.",
                ),
                GuideStep(
                    "Keep a way in that does not depend on the tunnel",
                    "If the VPN is the only route to the server and the VPN fails, you need physical or provider console access to fix it. Know how you would get in before you need it.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "wg-easy v15 changed the configuration format",
                    "The current major version stores its config in a database rather than a wg0.conf file, and the environment variable names changed. Match your compose file to the major version you actually run.",
                ),
                GuideStep(
                    "Reaching services on your LAN needs AllowedIPs and routing",
                    "The VPN address range is not enough. The AllowedIPs list must include your LAN, and the server needs packet forwarding enabled, or the tunnel works and nothing behind it is reachable.",
                ),
                GuideStep(
                    "DNS is the most common complaint",
                    "A tunnel that connects but cannot resolve names is a DNS configuration problem. Point the container's resolver at your own DNS service, which is Pi-hole on most of these setups.",
                ),
            ),
            alsoConsider = listOf("pihole", "adguard-home", "headscale", "traefik"),
        ),
        Service(
            id = "crowdsec",
            name = "CrowdSec",
            tagline = "Behaviour-based intrusion detection for your own services",
            category = Category.SECURITY,
            blurb = "CrowdSec reads your reverse proxy logs, works out which IPs are behaving like attackers, and blocks them. It learns from a blocklist community, so a botnet hammering someone else is often already known when it reaches you.\n\nIt is not a web application firewall and does not inspect request bodies. It reads logs and applies reputation, which makes it cheap to run and surprisingly effective against credential stuffing.",
            bestFor = listOf(
                "Blocking repeated failed logins",
                "Sharing reputation data with other operators",
                "Adding rate limiting that understands intent",
            ),
            homepage = "https://www.crowdsec.net",
            docsUrl = "https://docs.crowdsec.net/docs/getting_started/install/docker/",
            license = "MIT",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 5, cores = 1.0),
            defaultPort = "8080",
            compose = CROWDSEC_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Install the right bouncer for your proxy first",
                    "The bouncer is what actually blocks. Without one, CrowdSec parses logs and takes no action, which feels like it is working because it is quiet.",
                ),
                GuideStep(
                    "Use a collection for your proxy and the right one",
                    "The parser that understands your proxy's log format is selected by the collection name. Wrong collection means no parsing, which again looks like nothing happening.",
                ),
                GuideStep(
                    "Register the local agent to join the community blocklist",
                    "The console account is what links your instance to the shared intelligence. Without it you get local behaviour detection only, which is still useful but is not what the project is for.",
                ),
                GuideStep(
                    "Watch the decisions log to confirm it is acting",
                    "cscli decisions list is the fastest way to check that bans are being made and enforced. Check it after the first hour of real traffic.",
                ),
                GuideStep(
                    "Add a remediation component for known bad IPs",
                    "For an IP already known to the community, you do not need to wait for it to misbehave. A remediation component blocks it outright.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Log format is the whole integration",
                    "If your proxy's access log does not contain what the parser expects, CrowdSec sees nothing. Check the format in the acquisition list against your actual log file.",
                ),
                GuideStep(
                    "Be careful blocking your own network",
                    "A misconfigured remediation can ban your LAN, including the reverse proxy, and lock you out of your own services. Whitelist the private ranges before you enable it.",
                ),
                GuideStep(
                    "It is a bolt-on to the proxy, not a replacement",
                    "CrowdSec needs accurate logs, which only your reverse proxy has. If you are not logging properly at the proxy, start there instead.",
                ),
            ),
            alsoConsider = listOf("authentik", "traefik", "pihole", "wg-easy"),
        ),
        Service(
            id = "headscale",
            name = "Headscale",
            tagline = "Your own control server for a Tailscale network",
            category = Category.SECURITY,
            blurb = "Tailscale gives you a private network across your devices with no configuration and no open ports. It is the best answer for reaching your services from a phone. It also runs a coordination server, and some people would rather it was theirs.\n\nHeadscale is that server. You run it, register your devices, and your clients point at it instead of at Tailscale's.",
            bestFor = listOf(
                "A Tailscale-like mesh with no third party",
                "A private network across your own and friends' devices",
                "Reaching home services from a phone securely",
            ),
            homepage = "https://headscale.net",
            docsUrl = "https://headscale.net/stable/setup/installation/",
            license = "BSD-3-Clause",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 128, ramTypicalMb = 256, diskGb = 1, cores = 0.5),
            defaultPort = "8080",
            compose = HEADSCALE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Get the public key and private key out of the way",
                    "Headscale needs a noise private key and a noise public key, plus an IP prefix. The documented command generates them; skipping this produces a config that will not start.",
                ),
                GuideStep(
                    "Set the server URL to your real hostname",
                    "Clients are told where to register by this value. It must be the address they can reach, and it must match the certificate.",
                ),
                GuideStep(
                    "Register the first node by hand",
                    "Run headscale nodes register in the container, then apply the user and tag commands. Automatic key expiry has caught out a lot of first-time users.",
                ),
                GuideStep(
                    "Decide about MagicDNS and subnet routers",
                    "MagicDNS gives you hostnames inside the mesh. Subnet routers let the mesh reach your whole LAN, which is the feature that makes this feel like the VPN you wanted.",
                ),
                GuideStep(
                    "Keep the state directory backed up",
                    "The database holds every node, user, and ACL. Losing it means re-registering every device by hand, which on a phone fleet is an evening's work.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Not every Tailscale feature is implemented",
                    "Check the feature matrix before migrating. Some features people rely on are partial or absent, and finding that out after a migration is annoying.",
                ),
                GuideStep(
                    "Clients need a custom control server URL",
                    "Each app needs to be told where to register. The OS-level client takes a server flag; the GUI apps have a setting. Nothing works until that is changed.",
                ),
                GuideStep(
                    "ACLs are the part to spend time on",
                    "The default allows every node to reach every node. A sensible policy is a few lines and is the difference between a mesh and a flat network with extra steps.",
                ),
            ),
            alsoConsider = listOf("wg-easy", "pihole", "traefik"),
        ),
    )
}

private val AUTHENTIK_COMPOSE = """
services:
  server:
    container_name: authentik-server
    image: ghcr.io/goauthentik/server:${'$'}{AUTHENTIK_TAG:-latest}
    restart: unless-stopped
    ports:
      - '9000:9000'
    environment:
      - AUTHENTIK_SECRET_KEY=${'$'}{AUTHENTIK_SECRET_KEY}
      - AUTHENTIK_ENCRYPTION_KEY=${'$'}{AUTHENTIK_ENCRYPTION_KEY}
      - AUTHENTIK_POSTGRESQL__HOST=db
      - AUTHENTIK_POSTGRESQL__USER=${'$'}{AUTHENTIK_POSTGRESQL__USER}
      - AUTHENTIK_POSTGRESQL__PASSWORD=${'$'}{AUTHENTIK_POSTGRESQL__PASSWORD}
      - AUTHENTIK_POSTGRESQL__NAME=${'$'}{AUTHENTIK_POSTGRESQL__NAME}
      - AUTHENTIK_BOOTSTRAP_PASSWORD=${'$'}{AUTHENTIK_BOOTSTRAP_PASSWORD}
      - AUTHENTIK_BOOTSTRAP_EMAIL=${'$'}{AUTHENTIK_BOOTSTRAP_EMAIL}
      - AUTHENTIK_BRAND_NAME=Kituwa IT
      - AUTHENTIK_DISABLE_UPDATE_CHECK=true
    volumes:
      - ./media:/media
      - ./templates:/templates
    depends_on:
      - db
      - redis

  worker:
    container_name: authentik-worker
    image: ghcr.io/goauthentik/server:${'$'}{AUTHENTIK_TAG:-latest}
    command: worker
    restart: unless-stopped
    environment:
      - AUTHENTIK_SECRET_KEY=${'$'}{AUTHENTIK_SECRET_KEY}
      - AUTHENTIK_ENCRYPTION_KEY=${'$'}{AUTHENTIK_ENCRYPTION_KEY}
      - AUTHENTIK_POSTGRESQL__HOST=db
      - AUTHENTIK_POSTGRESQL__USER=${'$'}{AUTHENTIK_POSTGRESQL__USER}
      - AUTHENTIK_POSTGRESQL__PASSWORD=${'$'}{AUTHENTIK_POSTGRESQL__PASSWORD}
      - AUTHENTIK_POSTGRESQL__NAME=${'$'}{AUTHENTIK_POSTGRESQL__NAME}
    volumes:
      - ./media:/media
      - ./templates:/templates
    depends_on:
      - db
      - redis

  db:
    container_name: authentik_db
    image: docker.io/library/postgres:16-alpine
    restart: unless-stopped
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{AUTHENTIK_POSTGRESQL__USER}
      - POSTGRES_PASSWORD=${'$'}{AUTHENTIK_POSTGRESQL__PASSWORD}
      - POSTGRES_DB=${'$'}{AUTHENTIK_POSTGRESQL__NAME}

  redis:
    container_name: authentik_redis
    image: docker.io/library/redis:7-alpine
    restart: unless-stopped
"""

private val VAULT_COMPOSE = """
services:
  vault:
    container_name: vault
    image: hashicorp/vault:${'$'}{VAULT_VERSION:-latest}
    restart: unless-stopped
    ports:
      # Bound to localhost on purpose. Put a proxy in front of this.
      - '127.0.0.1:8200:8200'
    environment:
      - VAULT_DEV_ROOT_TOKEN_ID=${'$'}{VAULT_DEV_ROOT_TOKEN_ID}
      # 'server' enables TLS inside the seal. Use the address clients will use.
      - VAULT_ADDR=https://vault:8200
    cap_add:
      - IPC_LOCK
    volumes:
      - ./vault/data:/vault/data
      - ./vault/config:/vault/config
      - ./vault/certs:/vault/certs

  # Optional: only if you want Vault to start automatically after a restart.
  # Requires a key held somewhere other than this machine.
  # vault-unseal:
  #   container_name: vault-unseal
  #   image: hashicorp/vault:${'$'}{VAULT_VERSION:-latest}
  #   restart: unless-stopped
  #   depends_on:
  #     - vault
  #   environment:
  #     - VAULT_ADDR=https://vault:8200
  #     - VAULT_TOKEN=${'$'}{VAULT_DEV_ROOT_TOKEN_ID}
  #   volumes:
  #     - ./vault/init.json:/vault/init.json:ro
"""

private val WGEASY_COMPOSE = """
services:
  wg-easy:
    container_name: wg-easy
    image: ghcr.io/wg-easy/wg-easy:${'$'}{WG_EASY_VERSION:-latest}
    restart: unless-stopped
    environment:
      - INSECURE=false
      - PASSWORD=${'$'}{WG_EASY_PASSWORD}
      - DEFAULT_DNS=192.168.1.1
      - LANG=en
    ports:
      - '51820:51820/udp'
      - '51821:51821/tcp'
      - '51821:51821/udp'
      - '8080:80/tcp'
      - '8443:443/tcp'
    volumes:
      - ./config:/etc/wireguard
    cap_add:
      - NET_ADMIN
    sysctls:
      net.ipv4.ip_forward: 1
"""

private val CROWDSEC_COMPOSE = """
services:
  crowdsec:
    container_name: crowdsec
    image: crowdsecurity/crowdsec:${'$'}{CROWDSEC_VERSION:-latest}
    restart: always
    environment:
      - COLLECTIONS=${'$'}{CROWDSEC_COLLECTIONS:-crowdsecurity/nginx}
    # The agent API is not published to the host. Only the bouncer on this
    # network talks to it, which is the point of not publishing it.
    expose:
      - '8080'
    volumes:
      - ./config:/etc/crowdsec
      - ./data:/var/lib/crowdsec/data
      # Mount the proxy's log directory read-only so it can parse it.
      - /var/log/nginx:/var/log/nginx:ro
    networks:
      - proxy

  # The bouncer is what enforces the decisions. Without it nothing is blocked.
  crowdsec-bouncer:
    container_name: crowdsec-bouncer
    image: crowdsecurity/traefik-bouncer:${'$'}{BOUNCER_VERSION:-latest}
    restart: always
    environment:
      - CROWDSEC_BOUNCER_API_KEY=${'$'}{CROWDSEC_BOUNCER_API_KEY}
      - CROWDSEC_AGENT_URL=http://crowdsec:8080
    depends_on:
      - crowdsec
    networks:
      - proxy

networks:
  proxy:
    external: true
"""

private val HEADSCALE_COMPOSE = """
services:
  headscale:
    container_name: headscale
    image: headscale/headscale:${'$'}{HEADSCALE_VERSION:-latest}
    restart: always
    command: serve
    environment:
      - TZ=${'$'}{TZ:-UTC}
    volumes:
      - ./config:/etc/headscale
      - ./data:/var/lib/headscale
    ports:
      - '8080:8080'
"""
