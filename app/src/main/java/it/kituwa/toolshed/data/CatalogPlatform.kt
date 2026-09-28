package it.kituwa.toolshed.data

internal object Platform {
    val services = listOf(
        Service(
            id = "postgres",
            name = "PostgreSQL",
            tagline = "The database behind most of this list",
            category = Category.PLATFORM,
            blurb = "PostgreSQL is the database for a large share of these services, and it is a genuinely good database rather than a dependency you tolerate. Extensible types, proper transactions, and a query planner that does not embarrass itself.\n\nRun one instance and share it between services with a separate database and user per service. One Postgres serving ten applications is normal, sensible, and much cheaper than ten servers.",
            bestFor = listOf(
                "A database for most of the apps on this list",
                "A shared database with one user per service",
                "Structured data with real constraints and transactions",
            ),
            homepage = "https://www.postgresql.org",
            docsUrl = "https://www.postgresql.org/docs/current/docker.html",
            license = "PostgreSQL",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 2048, diskGb = 20, cores = 1.0),
            defaultPort = "5432",
            compose = POSTGRES_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the password in the environment, not by hand",
                    "POSTGRES_PASSWORD applies only on first initialisation of an empty data directory. Changing the variable later does nothing, which is a common and confusing discovery.",
                ),
                GuideStep(
                    "Create a database and user per service",
                    "Share the instance but not the credentials. Each service gets its own database, its own user, and a password that appears only in that service's compose file.",
                ),
                GuideStep(
                    "Do not publish port 5432 to the host",
                    "Other containers reach it over the Docker network by service name. Publishing it to the world is the least secure thing in this file and is never necessary.",
                ),
                GuideStep(
                    "Set sane connection limits",
                    "The default max_connections of 100 is generous for one service and a problem for ten. Raise it only when something actually needs it.",
                ),
                GuideStep(
                    "Turn on a backup before you have data you care about",
                    "pg_dump on a schedule, or a base backup for a large database. Know that restoring one has been tested before you need it.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "A major version upgrade needs a dump and restore",
                    "You cannot jump the data directory between major versions. For 16 to 17, that means pg_dumpall, upgrade the image, pg_restore. Doing it on a Friday is how people learn this the hard way.",
                ),
                GuideStep(
                    "Tune shared_buffers before anything else",
                    "The default is 128 MB, which is conservative. For a dedicated small server, a quarter of RAM is a reasonable starting point and usually the biggest single win.",
                ),
                GuideStep(
                    "WAL files will fill the disk if archiving is broken",
                    "If a replica or an archive command fails and nothing notices, WAL segments accumulate until the disk is full and the database refuses all writes. Alert on free disk space.",
                ),
                GuideStep(
                    "Extensions need to exist in the image",
                    "pgvector, PostGIS, and TimescaleDB are not in the standard image. Use a purpose-built image, and check the version of the extension matches the server.",
                ),
            ),
            alsoConsider = listOf("minio", "dockge", "baserow", "prometheus"),
        ),
        Service(
            id = "redis",
            name = "Redis",
            tagline = "Caching, queues, and sessions",
            category = Category.PLATFORM,
            blurb = "Redis is an in-memory store, and almost every service on this list uses it for one of three things: a cache, a job queue, or session storage. It is small, fast, and has no opinion about your data.\n\nBecause it holds sessions and queues, it is also the service that will lose work quietly if it is not persisted properly. Decide what it is for before you configure it.",
            bestFor = listOf(
                "Session storage and caching for several services",
                "Background job queues",
                "Rate limiting and counters",
            ),
            homepage = "https://redis.io",
            docsUrl = "https://redis.io/docs/latest/operate/oss_and_stack/install/install-docker/",
            license = "RSALv2-SPL / SSPL",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 1024, diskGb = 5, cores = 1.0),
            defaultPort = "6379",
            compose = REDIS_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Decide whether it is a cache or a queue",
                    "A cache can be thrown away and rebuilt, so persistence is optional. A job queue cannot, so append-only persistence is required. Configuring it for the wrong role is how jobs get lost.",
                ),
                GuideStep(
                    "Set a password, even on a private network",
                    "Redis has no authentication by default and every container on the network can reach it. A password is one environment variable.",
                ),
                GuideStep(
                    "Set a memory limit and an eviction policy",
                    "With maxmemory set and allkeys-lru, a cache under pressure degrades rather than failing. Without a limit, Redis will use all available memory and get OOM-killed.",
                ),
                GuideStep(
                    "Name the database index per service",
                    "Redis databases are numbered and isolated but not secured against each other. Using a distinct index per service stops one from flushing another's data.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "FLUSHALL from one service wipes every other one",
                    "There is no per-database protection in Redis. This happens more often than anyone would like, and the volume backup is the only recovery.",
                ),
                GuideStep(
                    "The licence is not open source",
                    "Redis moved to RSALv2/SSPL. Valkey is the community fork under a permissive licence and is a drop-in replacement for almost every use. For anything you intend to build on, prefer Valkey.",
                ),
                GuideStep(
                    "RDB snapshots are not the same as append-only log",
                    "A snapshot is periodic and can lose the interval since the last one. If a job queue matters, use AOF with everysec, and know the difference you are making.",
                ),
            ),
            alsoConsider = listOf("postgres", "dockge", "authentik"),
        ),
        Service(
            id = "minio",
            name = "MinIO",
            tagline = "S3-compatible object storage on your own disk",
            category = Category.PLATFORM,
            blurb = "MinIO speaks the S3 API, which means it drops into anything written against S3 without modification. Backups, attachments, media, and static files all go in buckets, and anything that supports S3 works with it unchanged.\n\nIt is the standard answer to 'I need an object store but I do not want to pay for one', and it is also a good answer to 'I want backups that leave the building'.",
            bestFor = listOf(
                "S3-compatible storage without a vendor",
                "Backups that go somewhere other than the server",
                "Attachments for services that expect S3",
            ),
            homepage = "https://min.io",
            docsUrl = "https://min.io/docs/minio/container/index.html",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 2048, diskGb = 100, cores = 1.0),
            defaultPort = "9000",
            compose = MINIO_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the root credentials as very long as you can",
                    "The root user has everything. The console lets you use a service account per application instead, and that is the right pattern once two things are connected.",
                ),
                GuideStep(
                    "Decide single-disk or multi-disk before starting",
                    "Erasure coding needs multiple drives and cannot be enabled later. Single-drive mode is simpler; distributed mode is what you want if this holds your backups.",
                ),
                GuideStep(
                    "Create a bucket and a service account per application",
                    "Never let an application use the root credentials. A bucket plus a scoped account is a few minutes of work and bounds the damage of a leak.",
                ),
                GuideStep(
                    "Mount the data on a real filesystem, not a network share",
                    "Object storage wants local or dedicated storage. It works on many network filesystems and the failure modes are not worth the risk for a backup target.",
                ),
                GuideStep(
                    "Point a backup tool at it",
                    "This is the point. A tool that speaks S3, such as restic, pointed at MinIO, gives you deduplicated encrypted backups on storage you control.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The console is on a separate port and has its own login",
                    "Port 9000 is the API and 9001 is the console. Forgetting which one you are on when it says access denied is a rite of passage.",
                ),
                GuideStep(
                    "A single disk is not erasure coded",
                    "Single-drive single-node mode stores one copy. It is a perfectly good object store, but it does not survive the loss of that disk, which is the entire reason to run one.",
                ),
                GuideStep(
                    "Versioning and object lock cost storage",
                    "Both are excellent for backups and both make the volume grow faster than you planned. Turn them on for the backup bucket and leave them off for scratch buckets.",
                ),
            ),
            alsoConsider = listOf("outline", "postgres", "dockge"),
        ),
        Service(
            id = "forgejo",
            name = "Forgejo",
            tagline = "A self-hosted Git forge you can actually maintain",
            category = Category.PLATFORM,
            blurb = "Forgejo is a Git service in the Gitea mould: repositories, issues, pull requests, CI, packages, and a wiki. It is a small Go binary and it runs happily on a modest VPS.\n\nIf you write code at all, having somewhere to push it that you control removes a whole category of risk, and the built-in Actions-compatible runner can build and deploy it.",
            bestFor = listOf(
                "Your own Git hosting with issues and pull requests",
                "A private package registry for your own projects",
                "Self-hosted CI for a small stack",
            ),
            homepage = "https://forgejo.org",
            docsUrl = "https://forgejo.org/docs/latest/admin/installation-with-docker/",
            license = "MIT",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 20, cores = 1.0),
            defaultPort = "3000",
            compose = FORGEJO_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Install the git and the forge together",
                    "The documented image includes Gitea or Forgejo plus the git binary. The split images exist, and using the combined one avoids a class of problem where git pushes work but hooks do not.",
                ),
                GuideStep(
                    "Set the install lock after the first run",
                    "Until you set INSTALL_LOCK, the setup wizard is reachable by anyone. Do that immediately after the first visit.",
                ),
                GuideStep(
                    "Set the domain and the SSH port before the first start",
                    "Both are baked into the repository clone URLs and the SSH configuration. Changing them afterwards means fixing every remote on every machine.",
                ),
                GuideStep(
                    "Create a repository from a template for a new project",
                    "A README, a licence, a CI file, and an ignore file as a starting point saves the boring part of starting a project.",
                ),
                GuideStep(
                    "Run the Actions runner in a container",
                    "The runner image runs as an unprivileged user and mounts the Docker socket. Mounting the socket means the runner can do anything on the host, so only run workflows you wrote.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The runner's Docker socket is host root",
                    "That is how it runs docker build. It also means a compromised workflow means a compromised host. Use it for your own repositories and nothing else.",
                ),
                GuideStep(
                    "Back up the repositories and the database together",
                    "Repositories are files, the users and issues are Postgres. A backup of one without the other produces a working Git host with no history of anything.",
                ),
                GuideStep(
                    "SSH clones need the host key published",
                    "Users cloning over SSH will get a host key warning unless you add the container's SSH keys to the forge. It is a one-line fix in the admin panel and it confuses everyone the first time.",
                ),
            ),
            alsoConsider = listOf("dockge", "traefik", "authentik"),
        ),
        Service(
            id = "dockge",
            name = "Dockge",
            tagline = "Edit compose files in a browser",
            category = Category.PLATFORM,
            blurb = "Dockge turns a folder of compose files into something you can edit, start, stop, and update from a web interface, with a live log view. It exists for the moment when you need to change one environment variable and do not want to remember the exact command.\n\nIt writes your files back to disk, so every change is a real edit to a real file that survives the container being replaced.",
            bestFor = listOf(
                "Editing compose files without a terminal",
                "A place to see and follow container logs",
                "Rollbacks by keeping previous versions of a stack",
            ),
            homepage = "https://github.com/louislam/dockge",
            docsUrl = "https://dockge.io/",
            license = "MIT",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 2, cores = 0.5),
            defaultPort = "5001",
            compose = DOCKGE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the host path to where your compose files live",
                    "DOCKGE_STACKS_DIR is the directory it manages. Set it to something real and writable, or the interface has nothing to show on first run.",
                ),
                GuideStep(
                    "Create the first user straight away",
                    "The registration is open until an admin exists. This is a short window and it is on a port you are about to expose.",
                ),
                GuideStep(
                    "Put it behind a proxy and behind a password",
                    "It is an interface that can start and stop everything on the host. Treat it exactly like Portainer.",
                ),
                GuideStep(
                    "Adopt one folder at a time",
                    "Pointing it at a directory of live stacks is useful and slightly alarming. Start with the stack you understand best.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "It edits files, so keep them in version control",
                    "The stacks directory is plain text on disk. Having it in git means a change made in the browser is reviewable and revertable, which is worth more than the convenience.",
                ),
                GuideStep(
                    "Updates are one click and not always wise",
                    "Pulling a new image tag for a service with a database migration behind it is how a working stack breaks. Check the release notes, as you would anywhere else.",
                ),
                GuideStep(
                    "The Docker socket is required",
                    "There is no way around it. If the security model of that bothers you, use the Portainer socket proxy in front of it.",
                ),
            ),
            alsoConsider = listOf("portainer", "homarr", "traefik"),
        ),
    )
}

private val POSTGRES_COMPOSE = """
services:
  db:
    container_name: postgres
    image: docker.io/library/postgres:${'$'}{POSTGRES_VERSION:-16}
    restart: always
    # Not published. Other containers reach this by service name.
    expose:
      - '5432'
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_ADMIN_USER:-postgres}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_ADMIN_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_ADMIN_DB:-postgres}
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    command:
      - postgres
      # 100 is generous for one service and tight for ten.
      - -c
      - max_connections=200
      # Start with a quarter of RAM, tuned later from real measurements.
      - -c
      - shared_buffers=512MB
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U ${'$'}{POSTGRES_ADMIN_USER:-postgres}']
      interval: 10s
      retries: 5

  # One-off helper: create a database and a user for a new service.
  # docker compose run --rm db sh -c \\
  #   'psql -U postgres -c "CREATE DATABASE myapp;"' && \\
  # docker compose run --rm db sh -c \\
  #   'psql -U postgres -c "CREATE USER myapp WITH PASSWORD '\\''secret'\\'';"'
"""

private val REDIS_COMPOSE = """
services:
  redis:
    container_name: redis
    image: docker.io/library/redis:7-alpine
    restart: always
    expose:
      - '6379'
    environment:
      # Not published, and authenticated anyway.
      - REDIS_PASSWORD=${'$'}{REDIS_PASSWORD}
    volumes:
      - ./redisdata:/data
    command: >
      redis-server
        --requirepass ${'$'}{REDIS_PASSWORD}
        --maxmemory 512mb
        --maxmemory-policy allkeys-lru
        --appendonly yes
        --appendfsync everysec
"""

private val MINIO_COMPOSE = """
services:
  minio:
    container_name: minio
    image: minio/minio:${'$'}{MINIO_RELEASE:-latest}
    restart: always
    ports:
      - '9000:9000'
      - '9001:9001'
    environment:
      - MINIO_ROOT_USER=${'$'}{MINIO_ROOT_USER}
      - MINIO_ROOT_PASSWORD=${'$'}{MINIO_ROOT_PASSWORD}
    volumes:
      - ./data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ['CMD', 'curl', '-f', 'http://localhost:9000/minio/health/live']
      interval: 30s
      timeout: 20s
      retries: 3

  # Create a bucket once, then use a scoped service account for each app.
  #   docker compose run --rm minio mc alias set local http://minio:9000 \\
  #     ${'$'}{MINIO_ROOT_USER} ${'$'}{MINIO_ROOT_PASSWORD}
  #   docker compose run --rm minio mc mb local/backups
"""

private val FORGEJO_COMPOSE = """
services:
  server:
    container_name: forgejo
    image: codeberg/forgejo:${'$'}{FORGEJO_VERSION:-latest}
    restart: always
    environment:
      - USER_UID=1000
      - USER_GID=1000
      - FORGEJO__database__DB_TYPE=sqlite3
      - FORGEJO__server__DOMAIN=${'$'}{FORGEJO_DOMAIN}
      - FORGEJO__server__SSH_DOMAIN=${'$'}{FORGEJO_SSH_DOMAIN}
      - FORGEJO__server__ROOT_URL=${'$'}{FORGEJO_ROOT_URL}
      # Set to true after the setup wizard, not before.
      - FORGEJO__security__INSTALL_LOCK=${'$'}{FORGEJO_INSTALL_LOCK:-false}
    volumes:
      - forgejo-data:/data
      - /etc/timezone:/etc/timezone:ro
      - /etc/localtime:/etc/localtime:ro
    ports:
      - '3000:3000'
      - '2222:22'

  runner:
    container_name: forgejo_runner
    image: code.forgejo.org/forgejo/runner:${'$'}{FORGEJO_RUNNER_VERSION:-latest}
    restart: always
    depends_on:
      - server
    environment:
      - FORGEJO_INSTANCE_URL=${'$'}{FORGEJO_ROOT_URL}
      - FORGEJO_RUNNER_REGISTRATION_TOKEN=${'$'}{FORGEJO_RUNNER_TOKEN}
    volumes:
      - ./runner-data:/data
      # Required for docker-based jobs. Host root access: use only for
      # workflows you wrote yourself.
      - /var/run/docker.sock:/var/run/docker.sock

volumes:
  forgejo-data:
"""

private val DOCKGE_COMPOSE = """
services:
  dockge:
    container_name: dockge
    image: louislam/dockge:${'$'}{DOCKGE_VERSION:-1}
    restart: always
    ports:
      - '5001:5001'
    environment:
      - DOCKGE_STACKS_DIR=${'$'}{DOCKGE_STACKS_DIR:-/opt/stacks}
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - ${'$'}{DOCKGE_STACKS_DIR:-/opt/stacks}:/opt/stacks
"""
