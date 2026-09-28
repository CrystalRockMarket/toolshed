package it.kituwa.toolshed.data

internal object PhotosMedia {
    val services = listOf(
        Service(
            id = "immich",
            name = "Immich",
            tagline = "Self-hosted photo and video library with face recognition",
            category = Category.PHOTOS_MEDIA,
            blurb = "Immich is the closest thing to a private Google Photos that you own outright. It backs up photos and videos from your phones automatically, does face and object recognition on your own hardware, and serves a timeline app that behaves the way you expect. The search is genuinely good, which is what separates it from a folder of files with a web UI on top.\n\nIt is also the most demanding thing most people install. The machine learning container alone will happily use several gigabytes of RAM, and a first backup of a phone with 40,000 photos will run for a long time. Plan for it rather than discovering it on a Sunday evening.",
            bestFor = listOf(
                "Automatic phone backup with a real timeline app",
                "On-device face and object search",
                "Sharing albums with family without an account",
            ),
            homepage = "https://immich.app",
            docsUrl = "https://immich.app/docs/install/docker-compose",
            license = "AGPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 100, cores = 2.0),
            defaultPort = "2283",
            compose = IMMICH_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Give it real hardware",
                    "Immich wants at least 2 GB of RAM free and 2 CPU threads for the machine learning container. A Pi 5 works if you enable swap, but expect the first backup to take days rather than hours.",
                ),
                GuideStep(
                    "Set a storage path that will not move",
                    "Create the media directory and the upload location before you start. The path is baked into the database, so changing it later means a migration, not an edit.",
                ),
                GuideStep(
                    "Pin the Postgres image",
                    "Immich needs a specific Postgres build with the vector extensions. The published compose file pins it by digest for a reason: swapping in a plain postgres:latest will fail in a way that is hard to read.",
                ),
                GuideStep(
                    "Bring it up and create the admin account",
                    "Start the stack, then open the URL and register the first account. That first account is the administrator, so do it yourself before anyone else gets the URL.",
                ),
                GuideStep(
                    "Install the phone app and pick a backup policy",
                    "Set backup to Wi-Fi only if you are on a metered connection, and check the setting that limits background uploads. Then let it run overnight for the first pass.",
                ),
                GuideStep(
                    "Put it behind a reverse proxy with a real certificate",
                    "Immich expects HTTPS for the mobile app to behave. Tailscale Serve or a Let's Encrypt proxy both work, and both stop the app complaining about cleartext traffic.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Hardware transcoding is where the time goes",
                    "Without hardware acceleration, a video library is transcoded on the CPU and it will keep the machine busy for days. On Intel, use the QSV image variant; on Nvidia, the CUDA one. The difference is roughly an order of magnitude.",
                ),
                GuideStep(
                    "Back up the database separately",
                    "The photo files are just files, and any file-level backup covers them. The database holds the album structure, face data, and your search index, and a bare file backup will not capture it. Use pg_dump on a schedule.",
                ),
                GuideStep(
                    "Do not put the upload directory on a network mount",
                    "NFS and SMB will work and will also make Immich unhappy in ways that show up months later as failed imports. Local storage, backed up to elsewhere.",
                ),
                GuideStep(
                    "Pin a version, not 'release'",
                    "The compose file's default of 'release' moves under you. When a release changes its database schema, an automatic upgrade on a boot is how a library gets lost. Read the release notes, then set an explicit version.",
                ),
            ),
            alsoConsider = listOf("dawarich", "paperless-ng", "minio"),
        ),
        Service(
            id = "dawarich",
            name = "Dawarich",
            tagline = "GPS-tagged photo library with heatmaps and self-hosted tracking",
            category = Category.PHOTOS_MEDIA,
            blurb = "Where Immich is about the photos themselves, Dawarich is about where they were taken. It ingests location data, builds map timelines, and can reconstruct tracked routes, which is a different and occasionally unsettling pleasure.\n\nIt is a young project with a fast-moving schema. The database migrations are aggressive, so treat upgrades as deliberate events rather than a pull-and-restart.",
            bestFor = listOf(
                "Location history and route heatmaps",
                "Feeding a private location archive",
                "Importing tracks from your phone",
            ),
            homepage = "https://dawarich.app",
            docsUrl = "https://dawarich.app/docs",
            license = "AGPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 50, cores = 2.0),
            defaultPort = "3000",
            compose = DAWARICH_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "PostGIS is not optional",
                    "Dawarich needs a PostGIS-enabled database. A standard postgres image will start and then fail every map query.",
                ),
                GuideStep(
                    "Keep the image version current but not bleeding edge",
                    "Check the release notes before upgrading. Several versions have needed manual intervention on the PostGIS data.",
                ),
                GuideStep(
                    "Register, then connect a source",
                    "Create the admin account, then add a phone or a folder import as a source. The import is incremental, so re-running it is safe.",
                ),
                GuideStep(
                    "Expect a slow first import",
                    "Building the timeline and the map tiles for a large library is a background job that runs for hours. Watch the container logs rather than assuming it has hung.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Redis is a dependency, not an optional extra",
                    "The compose file includes Redis for job queuing. Removing it produces errors that look like network timeouts rather than a missing service.",
                ),
                GuideStep(
                    "Location data is sensitive in a way photos are not",
                    "A photo library leaks your surroundings. A location history leaks your habits, your commute, and the times you are not at home. Treat the export as sensitive and encrypt the backups.",
                ),
            ),
            alsoConsider = listOf("immich", "minio"),
        ),
        Service(
            id = "jellyfin",
            name = "Jellyfin",
            tagline = "Free-software media server for your own films and shows",
            category = Category.PHOTOS_MEDIA,
            blurb = "Jellyfin serves your own media library to your own devices. It is free software, which is the whole point: no paid edition, no account, no phoning home.\n\nThe transcoding story is what separates it from a file share. If your family has devices that cannot play what you have, Jellyfin re-encodes on the fly, and that re-encoding is CPU-bound. Hardware acceleration turns a buffering spinner into a smooth stream.",
            bestFor = listOf(
                "Streaming your own film and TV library",
                "Replacing a subscription streaming service",
                "Watching on a TV, phone, or tablet",
            ),
            homepage = "https://jellyfin.org",
            docsUrl = "https://jellyfin.org/docs/general/installation/container/",
            license = "GPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 500, cores = 2.0),
            defaultPort = "8096",
            compose = JELLYFIN_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Decide where the media lives first",
                    "Read-only mounts of your media directories are best. Jellyfin only needs write access to its own config and cache directories, not to the films.",
                ),
                GuideStep(
                    "Mount the libraries read-only",
                    "Appending :ro to a media volume means a bug in Jellyfin cannot damage the files it is serving. It costs nothing.",
                ),
                GuideStep(
                    "Set the server name and finish the wizard",
                    "The first-run wizard asks for a server name. This is what devices will see on the local network, so name it something your household recognises.",
                ),
                GuideStep(
                    "Add libraries in separate folders",
                    "One library per genre means each gets its own metadata provider, artwork, and scraping rules. A single giant library rarely scrapes well.",
                ),
                GuideStep(
                    "Decide on hardware transcoding now",
                    "On Intel machines, pass the VAAPI device into the container and set Jellyfin to use QuickSync. Without it, direct play failures turn into buffering.",
                ),
                GuideStep(
                    "Configure remote access deliberately",
                    "A reverse proxy for outside access, or a VPN if you would rather not expose anything. Jellyfin will happily serve over plain HTTP if you let it.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Direct play versus transcoding is the whole game",
                    "If a client can play the file as-is, Jellyfin streams it untouched and uses almost no CPU. If it cannot, the server does the work. Most playback problems are really client codec support problems.",
                ),
                GuideStep(
                    "Metadata scraping needs a decent connection, once",
                    "Providers rate-limit aggressively. Give each library a separate scraping delay, or a large collection will hit rate limits and end up with placeholder artwork.",
                ),
                GuideStep(
                    "Never mount the media read-write for convenience",
                    "Jellyfin will not normally damage files, but a misconfigured library pointing at the wrong folder is exactly the kind of mistake that a read-only mount prevents entirely.",
                ),
                GuideStep(
                    "Back up the config directory, not the media",
                    "The media is your own files and your own backup policy covers them. What is irreplaceable is the config: watch history, play positions, users, and library definitions.",
                ),
            ),
            alsoConsider = listOf("navidrome", "portainer", "traefik"),
        ),
        Service(
            id = "navidrome",
            name = "Navidrome",
            tagline = "Lightweight music server with a very good mobile app",
            category = Category.PHOTOS_MEDIA,
            blurb = "Navidrome is a music server in the truest sense: a small binary, an SQLite database, and a folder of files. It streams to any browser or to its own Subsonic-compatible clients, and it costs almost nothing to run.\n\nBecause it uses SQLite rather than a database server, it is a good first self-hosted service. There is no database to back up, only one file.",
            bestFor = listOf(
                "Streaming a personal music library",
                "A self-hosted alternative to streaming subscriptions",
                "Very small servers and Raspberry Pi",
            ),
            homepage = "https://www.navidrome.org",
            docsUrl = "https://www.navidrome.org/docs/installation/docker/",
            license = "GPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 20, cores = 1.0),
            defaultPort = "4533",
            compose = NAVIDROME_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Point it at a folder of music",
                    "One library directory, mounted read-only. Navidrome reads the tags and builds its index; it never needs to write to your music.",
                ),
                GuideStep(
                    "Turn on transcoding only if you need it",
                    "Navidrome can transcode on the fly for formats browsers cannot play. On a low-powered box, leave it off and stick to MP3 and AAC.",
                ),
                GuideStep(
                    "Create the first user before opening it up",
                    "The setup page creates an admin account. Do that first, then start sharing links.",
                ),
                GuideStep(
                    "Use a Subsonic client on your phone",
                    "DSub, Ultrasonic, or Symfonium. They are the reason this service is pleasant to use daily rather than a novelty.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The index is a single SQLite file",
                    "It lives in the data directory. Copy that directory and you have a complete backup, which is rarer in self-hosting than it should be.",
                ),
                GuideStep(
                    "Scan is manual unless you ask for it",
                    "Navidrome scans on start and on demand. For a large library on slow storage, a periodic rescan from a cron job or a restart is simpler and more predictable.",
                ),
                GuideStep(
                    "Multi-user is genuinely supported",
                    "Each user gets their own play counts, playlists, and library visibility. For a household, create a separate account per person rather than sharing one.",
                ),
            ),
            alsoConsider = listOf("jellyfin", "homarr"),
        ),
    )
}

private val IMMICH_COMPOSE = """
services:
  immich-server:
    container_name: immich_server
    image: ghcr.io/immich-app/immich-server:${'$'}{IMMICH_VERSION:-release}
    volumes:
      - ${'$'}{UPLOAD_LOCATION}:/data
      - /etc/localtime:/etc/localtime:ro
    ports:
      - '2283:2283'
    environment:
      - DB_DATA_LOCATION=/var/lib/postgresql/data
    restart: always

  immich-machine-learning:
    container_name: immich_machine_learning
    image: ghcr.io/immich-app/immich-machine-learning:${'$'}{IMMICH_VERSION:-release}
    volumes:
      - ml-cache:/cache
    env_file:
      - .env
    restart: always

  redis:
    container_name: immich_redis
    image: docker.io/valkey/valkey:9
    healthcheck:
      test: redis-cli ping || exit 1
    restart: always

  database:
    container_name: immich_postgres
    image: ghcr.io/immich-app/postgres:14-vectorchord0.4.3-pgvectors0.2.0
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
    volumes:
      - ${'$'}{DB_DATA_LOCATION}:/var/lib/postgresql/data
    shm_size: 128mb
    restart: always

volumes:
  ml-cache:
"""

private val DAWARICH_COMPOSE = """
services:
  dawarich:
    container_name: dawarich
    image: freikin/dawarich:${'$'}{DAWARICH_VERSION:-latest}
    depends_on:
      - postgres
      - redis
    ports:
      - '3000:3000'
    environment:
      - DATABASE_HOST=postgres
      - DATABASE_PORT=5432
      - DATABASE_NAME=${'$'}{POSTGRES_DB}
      - DATABASE_USERNAME=${'$'}{POSTGRES_USER}
      - DATABASE_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - REDIS_HOST=redis
      - SECRET_KEY_BASE=${'$'}{SECRET_KEY_BASE}
      - RAILS_ENV=production
    volumes:
      - ./storage:/var/dawarich/storage
    restart: unless-stopped

  postgres:
    container_name: dawarich_postgres
    image: postgis/postgis:17-3.5-alpine
    environment:
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    restart: unless-stopped

  redis:
    container_name: dawarich_redis
    image: redis:7.4-alpine
    restart: unless-stopped
"""

private val JELLYFIN_COMPOSE = """
services:
  jellyfin:
    container_name: jellyfin
    image: jellyfin/jellyfin:${'$'}{JELLYFIN_VERSION:-latest}
    user: uid=${'$'}{PUID}:gid=${'$'}{PGID}
    network_mode: host
    volumes:
      - ./config:/config
      - ./cache:/cache
      # Media is mounted read-only. Jellyfin never needs write access to it.
      - /srv/media/movies:/media/movies:ro
      - /srv/media/tv:/media/tv:ro
      - /srv/media/music:/media/music:ro
    # Uncomment for Intel hardware transcoding:
    # devices:
    #   - /dev/dri:/dev/dri
    restart: unless-stopped
"""

private val NAVIDROME_COMPOSE = """
services:
  navidrome:
    container_name: navidrome
    image: deluan/navidrome:${'$'}{NAVIDROME_VERSION:-latest}
    user: ${'$'}{PUID}:${'$'}{PGID}
    ports:
      - '4533:4533'
    environment:
      - ND_MUSICFOLDER=/music
      - ND_LOGLEVEL=info
      - ND_SESSIONTIMEOUT=24h
      - ND_TRANSCODINGCACHESIZE=128MB
    volumes:
      - ./data:/data
      - /srv/media/music:/music:ro
    restart: unless-stopped
"""
