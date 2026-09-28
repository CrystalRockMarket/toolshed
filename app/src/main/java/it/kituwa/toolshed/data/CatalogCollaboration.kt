package it.kituwa.toolshed.data

internal object Collaboration {
    val services = listOf(
        Service(
            id = "mattermost",
            name = "Mattermost",
            tagline = "Team chat that keeps your messages on your server",
            category = Category.COLLABORATION,
            blurb = "Mattermost is the self-hosted chat for organisations that need to keep their own communications. It has channels, threads, file sharing, voice, and a plugin system, and it is used by agencies and government bodies for exactly the reason you would guess.\n\nThe Team Edition is free and has no message limit. The paid editions add compliance features, not chat.",
            bestFor = listOf(
                "Internal team chat on your own infrastructure",
                "A Slack replacement with no per-seat pricing",
                "Organisations with data residency requirements",
            ),
            homepage = "https://mattermost.com",
            docsUrl = "https://docs.mattermost.com/deployment-guide/server/deploy-containers.html",
            license = "MIT",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "8065",
            compose = MATTERMOST_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "The Team Edition image is the one you want",
                    "mattermost/mattermost-team-edition is free with no message cap. The enterprise image requires a licence file and will not start without one.",
                ),
                GuideStep(
                    "Wait for the database before the first request",
                    "The app starts faster than Postgres finishes migrating. A healthcheck with a proper start period stops the first request from erroring.",
                ),
                GuideStep(
                    "Create the first account through the system console",
                    "The initial account is created in the system console, not the sign-up page, and it is a full administrator. Do this before anyone can reach the login page.",
                ),
                GuideStep(
                    "Set the site URL before inviting anyone",
                    "Notifications, email links, and image proxying all use it. Getting it wrong produces broken links in emails and a warning banner on every page.",
                ),
                GuideStep(
                    "Put TLS in front of it",
                    "WebSocket connections do not survive a plain HTTP hop, and you will get a chat that connects and then silently stops receiving messages.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "File storage is the thing that fills the disk",
                    "Attachments live in a mounted volume by default. Ten people sharing screenshots will fill a small disk faster than any database will.",
                ),
                GuideStep(
                    "Elasticsearch and the compliance exports are optional",
                    "Neither is in the compose file here, and neither is needed. They exist for very large instances and regulated deployments.",
                ),
                GuideStep(
                    "The mobile app connects to a specific site URL",
                    "Each server is a separate entry in the app. If you change hostname, the old entry stops working and the new one needs to be added by hand.",
                ),
                GuideStep(
                    "Upgrades need a database migration and a version match",
                    "The app and schema versions move together. Take a database backup before every upgrade and do not skip versions.",
                ),
            ),
            alsoConsider = listOf("rocketchat", "element", "authentik"),
        ),
        Service(
            id = "rocketchat",
            name = "Rocket.Chat",
            tagline = "Feature-heavy chat with omnichannel inboxes",
            category = Category.COLLABORATION,
            blurb = "Rocket.Chat is a chat server with a lot of features: live chat widgets, omnichannel inboxes that connect to social and messaging platforms, and a large plugin ecosystem. It is popular with customer-facing support teams.\n\nIt is also heavier and more demanding than Mattermost for the same basic chat, and the free tier is limited. Read the licensing before you build on it.",
            bestFor = listOf(
                "Chat plus a customer support inbox",
                "Embedding a live chat widget on a site",
                "A Slack-style interface on your own server",
            ),
            homepage = "https://www.rocket.chat",
            docsUrl = "https://docs.rocket.chat/docs/quick-start-guide",
            license = "MIT",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 4096, ramTypicalMb = 8192, diskGb = 30, cores = 4.0),
            defaultPort = "3000",
            compose = ROCKETCHAT_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Give MongoDB its own volume and its own memory",
                    "MongoDB will use all the memory it can see and starve the app. The wiredTiger cache size setting in the compose file is there to prevent exactly that.",
                ),
                GuideStep(
                    "Use MongoDB as a replica set, even a single node",
                    "Oplog tailing and the change streams Rocket.Chat relies on require a replica set. A standalone mongod works for a moment and then breaks in confusing ways.",
                ),
                GuideStep(
                    "Register the admin through the setup wizard",
                    "The first-run wizard creates the administrator. Complete it in the browser straight after the first start, because the endpoint is public.",
                ),
                GuideStep(
                    "Set the site URL and the Rocket.Chat origin",
                    "Two separate settings, both required. The origin is what the container sees; the site URL is what users see. Confusing them is a common cause of blank pages behind a proxy.",
                ),
                GuideStep(
                    "Add a reverse proxy that understands WebSockets",
                    "Caddy and Traefik do this automatically. A misconfigured nginx location without the upgrade headers will connect and then go silent.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Check the licence tier before you rely on it",
                    "The community edition has limits on active users and on some features. For a large internal deployment, the paid tier applies, and the free tier's limits are documented.",
                ),
                GuideStep(
                    "Omnichannel needs outbound network access",
                    "Connecting to WhatsApp, Instagram, or email requires the container to reach the internet and often a verified business account. This is where a self-hosted deployment gets complicated.",
                ),
                GuideStep(
                    "MongoDB backups must be consistent",
                    "A filesystem copy of a running MongoDB is not a valid backup. Use mongodump, and test restoring it.",
                ),
            ),
            alsoConsider = listOf("mattermost", "element", "zammad"),
        ),
        Service(
            id = "element",
            name = "Matrix (Synapse + Element)",
            tagline = "Federated, encrypted chat you can host anywhere",
            category = Category.COLLABORATION,
            blurb = "Matrix is a protocol for federated messaging, which means people on different servers can talk to each other with end-to-end encryption. Synapse is the reference server, and Element is the default client.\n\nIt is the only option here that talks to the wider decentralised network, and that is a genuine feature rather than a novelty. It is also the hardest of the chat options to run well.",
            bestFor = listOf(
                "Talking to people on other Matrix servers",
                "Encrypted chat without a central operator",
                "A self-hosted Slack alternative with federation",
            ),
            homepage = "https://matrix.org",
            docsUrl = "https://matrix-org.github.io/synapse/latest/setup/installation.html",
            license = "AGPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "8008",
            compose = MATRIX_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Use the sliding sync port too, or your clients will crawl",
                    "Synapse 1.104 and Element X need port 8009 for the new sync protocol. Without it, logins take many seconds each and the app feels broken while working correctly.",
                ),
                GuideStep(
                    "Register the well-known file before anything else",
                    "For other servers to find you, /.well-known/matrix/server must be served at your domain root, not under a path. This is the single most common Matrix setup failure.",
                ),
                GuideStep(
                    "Create the first user with the registration command",
                    "Sharing a registration_shared_secret with the client is convenient and dangerous. Creating accounts server-side is dull and correct.",
                ),
                GuideStep(
                    "Set up a TURN server if you expect calls",
                    "Calls need a TURN server for anything behind a symmetric NAT. Without one, text works perfectly and video never connects, with no useful error message.",
                ),
                GuideStep(
                    "Turn on the shared secret for automated account creation",
                    "The admin web interface can create accounts using a shared secret. This is how you get new users without exposing public registration.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Postgres is required, SQLite is not supported",
                    "Synapse dropped SQLite support. The compose file includes Postgres because there is no alternative.",
                ),
                GuideStep(
                    "Federation is a DoS surface",
                    "A publicly federating server can be flooded by other servers. Rate limits and a reverse proxy with sensible limits are not optional if you let it federate.",
                ),
                GuideStep(
                    "The media repository grows without bound",
                    "Uploaded images and files accumulate. Matrix has a retention configuration and most people should turn it on before the disk fills.",
                ),
                GuideStep(
                    "Element Web is a static site, not a server",
                    "The element-web container is nginx serving files. Any configuration change to Matrix itself goes in Synapse, not there.",
                ),
            ),
            alsoConsider = listOf("jitsi-meet", "mattermost", "authentik"),
        ),
        Service(
            id = "jitsi-meet",
            name = "Jitsi Meet",
            tagline = "Video conferencing in the browser, no account needed",
            category = Category.COLLABORATION,
            blurb = "Jitsi Meet is a complete video conferencing system you can run yourself. Drop a link in a chat message and two people can talk, with screen sharing and a chat, and neither needs an account.\n\nThe full stack is four components and the documentation reflects that. It also does UDP hole punching well, so it works across NATs more often than you would expect.",
            bestFor = listOf(
                "Ad-hoc video calls with a link",
                "Conferences that need no account or app",
                "A private alternative to a hosted meeting service",
            ),
            homepage = "https://jitsi.org",
            docsUrl = "https://github.com/jitsi/docker-jitsi-meet",
            license = "Apache-2.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 10, cores = 2.0),
            defaultPort = "8443",
            compose = JITSI_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Understand the four components",
                    "Web is the interface, Prosody is the signalling, Jicofo manages the conference, and JVB is the media router. The JVB ports matter most and are the ones people forget.",
                ),
                GuideStep(
                    "Publish 10000/udp for the media router",
                    "Jingle ICE UDP on 10000 is how media actually flows. If you publish only the web ports, calls connect and carry no audio.",
                ),
                GuideStep(
                    "Set the public URL for every component",
                    "Each component has its own public URL setting and each defaults to localhost. Behind a proxy, all four must point at the real address or the call connects silently without media.",
                ),
                GuideStep(
                    "Generate the config files once, then edit them",
                    "The container writes templates on first start. Editing them in place is wrong; put your changes in the mounted config directory so they survive a restart.",
                ),
                GuideStep(
                    "Add a hostname, not an IP, for a stable address",
                    "Certificates and the guest link in chat both work far better against a real hostname with a certificate.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "UDP on 10000 is the make-or-break port",
                    "This is the most reported Jitsi problem. If the firewall blocks it, participants see a connecting spinner forever. Verify with the port mapping, not the browser.",
                ),
                GuideStep(
                    "JVB is the bandwidth hog, not the web tier",
                    "Each participant's video is relayed through it. A four-person call at 720p is a few megabits; twenty people in a room on a small VPS is not.",
                ),
                GuideStep(
                    "The image is large and updates are disruptive",
                    "Pulling a new tag restarts all four components, which drops live calls. Update on a schedule when nobody is in a meeting.",
                ),
                GuideStep(
                    "Prosody needs a real hostname for TLS",
                    "Internal certificates for prosody and jicofo work for the first connection and then cause a certificate error on the second device to join.",
                ),
            ),
            alsoConsider = listOf("element", "traefik", "pihole"),
        ),
        Service(
            id = "espocrm",
            name = "EspoCRM",
            tagline = "Customer relationship management that stays small",
            category = Category.COLLABORATION,
            blurb = "EspoCRM is a CRM that does contacts, companies, opportunities, and email sync without becoming a platform. It installs on a Pi, it has a good API, and it will not ask you for a consultant.\n\nFor a small business that wants its customer list to be a database it owns, this is the right size.",
            bestFor = listOf(
                "Contacts, deals, and a sales pipeline",
                "A small business CRM on your own server",
                "Syncing mail and calendars into one place",
            ),
            homepage = "https://www.espocrm.com",
            docsUrl = "https://docs.espocrm.com/administration/installation/docker/installation/",
            license = "GPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 20, cores = 1.0),
            defaultPort = "8080",
            compose = ESPOCRM_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the site URL in .env first",
                    "EspoCRM writes the URL into its database on first start. Changing it later needs a rebuild command, so get it right up front.",
                ),
                GuideStep(
                    "Point it at a fresh database",
                    "Using a database that already has an EspoCRM schema from another install causes a very confusing first login failure.",
                ),
                GuideStep(
                    "Complete the installer",
                    "The web installer creates the admin and writes the config. It is one page and it must be done before the app is reachable to anyone else.",
                ),
                GuideStep(
                    "Add a reverse proxy and a certificate",
                    "The mail sync features and the calendar integration both need a trusted HTTPS origin, or the browser will refuse the connection.",
                ),
                GuideStep(
                    "Keep the data volume on fast storage",
                    "Search and list views are database queries over a growing dataset. Slow disk is felt immediately in the interface.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Rebuild after upgrading",
                    "Every upgrade needs a rebuild:clear action. Skipping it leaves the old cached metadata and produces odd behaviour that looks like corruption.",
                ),
                GuideStep(
                    "The free edition covers most small businesses",
                    "Advanced pack features are paid. Check which ones you need: for a handful of users, the core is usually enough.",
                ),
                GuideStep(
                    "Email sync is the feature people underrate",
                    "Connecting IMAP turns EspoCRM into the front end for the mailbox you already have. It is the reason many teams keep it.",
                ),
            ),
            alsoConsider = listOf("twenty", "baserow", "zammad"),
        ),
        Service(
            id = "twenty",
            name = "Twenty CRM",
            tagline = "A modern, fast CRM with a clean interface",
            category = Category.COLLABORATION,
            blurb = "Twenty is a CRM built with a modern front end, and it shows: it is quick, the interface is pleasant, and it does the fundamentals well. Companies, people, opportunities, and a pipeline view.\n\nIt is younger and has fewer integrations than the incumbents. If the look and speed matter to your team, that trade can be worth it.",
            bestFor = listOf(
                "A pipeline and contacts for a small sales team",
                "A CRM your team will actually use",
                "A faster-feeling alternative to older CRMs",
            ),
            homepage = "https://twenty.com",
            docsUrl = "https://docs.twenty.com/developers/self-hosting/installation",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "3000",
            compose = TWENTY_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "The server image needs a front-end origin too",
                    "Twenty is a Next.js application and a server. In production mode it expects a proxy that serves the front end and forwards API calls.",
                ),
                GuideStep(
                    "Use the server image, not the all-in-one one",
                    "The all-in-one image is for local development. It is explicitly not recommended for production and it holds development defaults.",
                ),
                GuideStep(
                    "Generate the secret key and the app secret properly",
                    "Both are long hex strings. Generate them rather than typing them; a short key fails the startup validation with a message that does not name the variable.",
                ),
                GuideStep(
                    "Run the migrations before the first sign-in",
                    "A fresh database needs the migration pass. Doing it in the entrypoint is fine, but watch the logs rather than assuming it happened.",
                ),
                GuideStep(
                    "Create the first workspace, then the first user",
                    "There is no default account. The setup screen asks you to create a workspace, and the user you create in it is the owner.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Pre-built releases exist and are easier than the image",
                    "The project publishes tarball releases with pre-built frontend assets. They are the documented path for production and save a Node build stage.",
                ),
                GuideStep(
                    "Version upgrades have been schema changes",
                    "Check the changelog for migrations. Taking a Postgres dump before every upgrade is not paranoia here, it is the recommendation.",
                ),
                GuideStep(
                    "Attachments and email need object storage",
                    "Files go to S3 or MinIO. The compose file includes a MinIO service for that, and it is not optional once you use attachments.",
                ),
            ),
            alsoConsider = listOf("espocrm", "baserow", "postgres"),
        ),
        Service(
            id = "zammad",
            name = "Zammad",
            tagline = "Helpdesk and ticketing with email at its centre",
            category = Category.COLLABORATION,
            blurb = "Zammad is a helpdesk where email is the primary interface. Customers write in, tickets are created from the message thread, and your team replies from a web interface that also works as a live chat widget.\n\nThat design is deliberate and it is why Zammad feels different from other ticketing systems. It suits organisations that live in their inbox.",
            bestFor = listOf(
                "Support ticket queues driven by email",
                "A helpdesk your team can live in",
                "Embedding a chat widget on a customer site",
            ),
            homepage = "https://zammad.org",
            docsUrl = "https://admin-docs.zammad.org/en/latest/getting-started/installation/docker.html",
            license = "GPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 4096, ramTypicalMb = 8192, diskGb = 30, cores = 4.0),
            defaultPort = "8080",
            compose = ZAMMAD_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the four secret and password variables",
                    "Zammad needs a Rails secret, a application password, and database credentials. The initial admin account is created with the application password, so set it deliberately.",
                ),
                GuideStep(
                    "Redis is required, not optional",
                    "Sessions, background jobs, and the search index all use Redis. The container will start without it and fail on the first login.",
                ),
                GuideStep(
                    "Give Postgres its own volume from the start",
                    "The seed data, the user table, and the ticket history all live there. Losing it loses the helpdesk, not just the config.",
                ),
                GuideStep(
                    "Configure the email account after first login",
                    "The initial credentials are admin@example.com with the application password. Change the email address before connecting a mail account, or replies will come from a placeholder.",
                ),
                GuideStep(
                    "Set the trusted proxies if you are behind a proxy",
                    "Without this, the IP addresses in ticket history are all the proxy's, which makes auditing and spam filtering useless.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Password reset emails are an SMTP test",
                    "A helpdesk that cannot send email is a helpdesk that cannot work. Set up SMTP and send yourself a ticket before going live.",
                ),
                GuideStep(
                    "Elasticsearch is optional and heavy",
                    "The built-in search is fine for tens of thousands of tickets. Elasticsearch is for very large installations and roughly doubles the memory footprint.",
                ),
                GuideStep(
                    "Storage is the image volume and the database",
                    "Attachments and avatars are files; everything else is Postgres. A complete backup needs both, consistently.",
                ),
            ),
            alsoConsider = listOf("espocrm", "mattermost", "twenty"),
        ),
    )
}

private val MATTERMOST_COMPOSE = """
services:
  db:
    container_name: mattermost_db
    image: docker.io/library/postgres:16-alpine
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=mmuser
      - POSTGRES_PASSWORD=${'$'}{MM_SQLSETTINGS_DATASOURCE_PASSWORD}
      - POSTGRES_DB=mattermost_test
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U mmuser']
      interval: 10s
      retries: 5

  redis:
    container_name: mattermost_redis
    image: redis:7-alpine
    restart: always
    volumes:
      - ./redis:/data

  app:
    container_name: mattermost
    image: mattermost/mattermost-team-edition:${'$'}{MATTERMOST_VERSION:-latest}
    restart: always
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_started
    ports:
      - '8065:8065'
    environment:
      - MM_SERVICESETTINGS_SITEURL=${'$'}{MM_SERVICESETTINGS_SITEURL}
      - MM_SERVICESETTINGS_LISTENADDRESS=:8065
      - MM_SQLSETTINGS_DRIVERNAME=postgres
      - MM_SQLSETTINGS_DATASOURCE=postgres://mmuser:${'$'}{MM_SQLSETTINGS_DATASOURCE_PASSWORD}@db:5432/mattermost_test?sslmode=disable&connect_timeout=10
      - MM_SERVICESETTINGS_ENABLELOCALMODE=true
    volumes:
      - ./config:/mattermost/config
      - ./data:/mattermost/data
      - ./logs:/mattermost/logs
      - ./plugins:/mattermost/plugins
      - ./client/plugins:/mattermost/client/plugins
"""

private val ROCKETCHAT_COMPOSE = """
services:
  mongodb:
    container_name: rocketchat_mongo
    image: mongo:6.0
    restart: always
    command: --wiredTigerCacheSizeGB 1 --replSet rs0 --bind_ip_all
    ports:
      - '27017:27017'
    volumes:
      - ./data/db:/data/db
    networks:
      - rocketchat

  rocketchat:
    container_name: rocketchat
    image: rocket.chat:6.5.3
    restart: unless-stopped
    command: >
      bash -c "for i in $$(seq 1 30); do mongosh --host mongodb --eval 'rs.initiate()' && break || sleep 1; done;
      mongodump --archive | mongorestore --archive;
      sleep 10 && node /app/rocket.chat/dist/server/main.js"
    ports:
      - '3000:3000'
    environment:
      - MONGO_URL=mongodb://mongodb:27017/rocketchat?replicaSet=rs0&directConnection=true
      - ROOT_URL=${'$'}{ROOT_URL}
      - WEBFINGER_URL=${'$'}{ROOT_URL}
      - ADMIN_EMAIL=${'$'}{ADMIN_EMAIL}
      - ADMIN_PASSWORD=${'$'}{ADMIN_PASSWORD}
      - ADMIN_USERNAME=${'$'}{ADMIN_USERNAME:-Rocket.cat}
    volumes:
      - ./data/uploads:/app/uploads
    depends_on:
      - mongodb
    networks:
      - rocketchat

networks:
  rocketchat:
    name: rocketchat
"""

private val MATRIX_COMPOSE = """
services:
  synapse:
    container_name: synapse
    image: matrixdotorg/synapse:${'$'}{SYNAPSE_TAG:-latest}
    restart: unless-stopped
    volumes:
      - ./synapse/data:/data
    ports:
      - '8008:8008'
      # Sliding sync. Without this, Element X is painfully slow.
      - '8009:8009'
    environment:
      - SYNAPSE_REPORT_STATS=no
    depends_on:
      db:
        condition: service_healthy

  db:
    container_name: synapse_db
    image: docker.io/library/postgres:16
    restart: unless-stopped
    volumes:
      - ./synapse/postgres:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER:-synapse}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=synapse
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U ${'$'}{POSTGRES_USER:-synapse}']
      interval: 5s
      retries: 10

  element-web:
    container_name: element-web
    image: vectorim/element-web:${'$'}{ELEMENT_TAG:-latest}
    restart: unless-stopped
    ports:
      - '8080:80'
    volumes:
      - ./element/config.json:/app/config.json
    depends_on:
      - synapse
"""

private val JITSI_COMPOSE = """
services:
  # The interface. This is the only component most people visit directly.
  web:
    container_name: jitsi-web
    image: ghcr.io/jitsi/web:${'$'}{JITSI_TAG:-stable}
    restart: unless-stopped
    ports:
      - '8443:443'
    environment:
      - ENABLE_AUTH=0
      - ENABLE_LETSENCRYPT=0
      - PUBLIC_URL=${'$'}{PUBLIC_URL}

  # Signalling: browsers talk to this to join a call.
  prosody:
    container_name: jitsi-prosody
    image: ghcr.io/jitsi/prosody:${'$'}{JITSI_TAG:-stable}
    restart: unless-stopped
    ports:
      - '5222:5222'
      - '5349:5349'
    volumes:
      - ./prosody/config:/config
    environment:
      - XMPP_DOMAIN=${'$'}{XMPP_DOMAIN}
      - XMPP_AUTH_DOMAIN=auth.jitsi
      - XMPP_MUC_DOMAIN=muc.jitsi
      - XMPP_INTERNAL_MUC_DOMAIN=internal-muc.jitsi
      - XMPP_BOSH_URL=https://${'$'}{XMPP_DOMAIN}/http-bind
      - XMPP_GUEST_DOMAIN=guest.jitsi

  # Conference focus: decides who talks to whom.
  jicofo:
    container_name: jitsi-jicofo
    image: ghcr.io/jitsi/jicofo:${'$'}{JITSI_TAG:-stable}
    restart: unless-stopped
    ports:
      - '8888:8888'
    environment:
      - XMPP_DOMAIN=${'$'}{XMPP_DOMAIN}
      - XMPP_AUTH_DOMAIN=auth.jitsi
      - XMPP_MUC_DOMAIN=muc.jitsi
      - XMPP_INTERNAL_MUC_DOMAIN=internal-muc.jitsi

  # The video bridge. This is what carries the actual media.
  jvb:
    container_name: jitsi-jvb
    image: ghcr.io/jitsi/jvb:${'$'}{JITSI_TAG:-stable}
    restart: unless-stopped
    ports:
      - '10000:10000/udp'
      - '8443:8443'
    environment:
      - XMPP_SERVER=prosody
      - XMPP_DOMAIN=${'$'}{XMPP_DOMAIN}
      - XMPP_BOSH_URL=https://${'$'}{XMPP_DOMAIN}/http-bind
      - XMPP_AUTH_DOMAIN=auth.jitsi
      - PUBLIC_URL=${'$'}{PUBLIC_URL}
"""

private val ESPOCRM_COMPOSE = """
services:
  espocrm:
    container_name: espocrm
    image: espocrm/espocrm:${'$'}{ESPOCRM_TAG:-latest}
    restart: always
    ports:
      - '8080:80'
    environment:
      - ESPOCRM_DATABASE_DRIVER=mysql
      - ESPOCRM_DATABASE_HOST=db
      - ESPOCRM_DATABASE_USER=${'$'}{ESPOCRM_DATABASE_USER}
      - ESPOCRM_DATABASE_PASSWORD=${'$'}{ESPOCRM_DATABASE_PASSWORD}
      - ESPOCRM_DATABASE_NAME=${'$'}{ESPOCRM_DATABASE_NAME}
      - ESPOCRM_SITE_URL=${'$'}{ESPOCRM_SITE_URL}
    volumes:
      - ./data:/var/www/html/data
    depends_on:
      - db

  db:
    container_name: espocrm_db
    image: mariadb:10.11
    restart: always
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    volumes:
      - ./dbdata:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${'$'}{MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=${'$'}{ESPOCRM_DATABASE_NAME}
      - MYSQL_USER=${'$'}{ESPOCRM_DATABASE_USER}
      - MYSQL_PASSWORD=${'$'}{ESPOCRM_DATABASE_PASSWORD}
"""

private val TWENTY_COMPOSE = """
services:
  server:
    container_name: twenty_server
    image: twentycrm/twenty:${'$'}{TWENTY_TAG:-latest}
    restart: always
    command: server
    ports:
      - '3000:3000'
    environment:
      - SERVER_URL=${'$'}{SERVER_URL}
      - APP_SECRET=${'$'}{APP_SECRET}
      - DATABASE_URL=postgres://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@db:5432/${'$'}{POSTGRES_DB}
      - STORAGE_TYPE=local
      - SERVER_PORT=3000
      - NODE_ENV=production
    volumes:
      - ./server_data:/app/.local/share/twenty

  front:
    container_name: twenty_front
    image: twentycrm/twenty:${'$'}{TWENTY_TAG:-latest}
    restart: always
    command: frontend
    ports:
      - '8080:8080'
    environment:
      - SERVER_URL=${'$'}{SERVER_URL}
      - APP_SECRET=${'$'}{APP_SECRET}

  db:
    container_name: twenty_db
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
"""

private val ZAMMAD_COMPOSE = """
services:
  zammad:
    container_name: zammad
    image: zammad/zammad:${'$'}{ZAMMAD_TAG:-latest}
    restart: always
    ports:
      - '8080:80'
    environment:
      - REDIS_URL=redis://zammad_redis:6379/1
      - POSTGRES_HOST=zammad_postgres
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - ZAMMAD_RAILS_SECRET=${'$'}{ZAMMAD_SECRET}
      - ZAMMAD_APPLICATION_PASSWORD=${'$'}{ZAMMAD_APPLICATION_PASSWORD}
      - ZAMMAD_ADMIN_EMAIL=admin@example.com
      - ZAMMAD_ADMIN_PASSWORD=${'$'}{ZAMMAD_APPLICATION_PASSWORD}
    volumes:
      - ./data:/opt/zammad/data
      - ./config:/opt/zammad/etc
      - ./logs:/opt/zammad/log
    depends_on:
      - zammad_postgres
      - zammad_redis

  zammad_postgres:
    container_name: zammad_postgres
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}

  zammad_redis:
    container_name: zammad_redis
    image: docker.io/library/redis:7
    restart: always
    volumes:
      - ./redis:/data
"""
