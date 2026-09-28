package it.kituwa.toolshed.data

internal object Productivity {
    val services = listOf(
        Service(
            id = "joplin",
            name = "Joplin Server",
            tagline = "Markdown notes with end-to-end encrypted sync",
            category = Category.PRODUCTIVITY,
            blurb = "Joplin is a note-taking app that treats your notes as your own: Markdown files you can read without the app, and a server you control. The server is the boring middle part: the desktop and mobile clients do the encryption, so the server sees ciphertext.\n\nThe clients are the reason to use it. The server alone is a Synology-like target for attachments with a web interface, and that is a perfectly good reason too.",
            bestFor = listOf(
                "Notes you can read without any app",
                "Syncing notes across phone, desktop, and web",
                "Replacing a proprietary note service",
            ),
            homepage = "https://joplinapp.org",
            docsUrl = "https://joplinapp.org/help/apps/server/",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 20, cores = 1.0),
            defaultPort = "41184",
            compose = JOPLIN_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate the master key first",
                    "Joplin needs a 32-byte master key in .env. Without it the server starts and then rejects every client, which is a confusing first hour. Generate one with openssl rand -hex 32.",
                ),
                GuideStep(
                    "Decide about email before you start syncing",
                    "Users can be created by anyone who signs up, which on a public hostname means strangers. Set the registration to disabled, or put it behind a proxy that requires authentication.",
                ),
                GuideStep(
                    "Start the database and wait for it to be healthy",
                    "Postgres takes a few seconds longer than the server expects. Depending on a database that is not ready produces an error on first sync and a support thread in your head.",
                ),
                GuideStep(
                    "Point one client at it and sync once",
                    "Get a single device working before adding five. The setup flow asks for the server URL and the encryption master key, and both are easy to mistype.",
                ),
                GuideStep(
                    "Back up the Postgres volume",
                    "Notes live in the database, not on disk anywhere else. The volume is the backup target and there is nothing else to copy.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "E2EE means the server cannot search your notes",
                    "Encryption happens on the client. The server stores blobs it cannot read, which is the point, and it also means full-text search happens locally. Do not expect server-side search.",
                ),
                GuideStep(
                    "Attachments are the real storage cost",
                    "Photos pasted into notes are uploaded separately and are not encrypted the same way. A notes database that grows unexpectedly is almost always attachments.",
                ),
                GuideStep(
                    "Joplin Cloud is a different product",
                    "The vendor's paid service is not this. The server is self-hosted and free, and the paid apps simply let you point them at your own URL.",
                ),
            ),
            alsoConsider = listOf("paperless-ng", "nextcloud", "appflowy"),
        ),
        Service(
            id = "vikunja",
            name = "Vikunja",
            tagline = "Tasks and projects that stay readable",
            category = Category.PRODUCTIVITY,
            blurb = "Vikunja is a task manager for people who find most task managers bloated. Lists, kanban boards, and gantt views, with a clean web interface and a real API, and it runs happily on modest hardware.\n\nIt pairs well with the rest of a self-hosted setup: it can authenticate against Authentik or LDAP, so it does not need its own user management.",
            bestFor = listOf(
                "Household and work task lists",
                "A self-hosted to-do you own",
                "Kanban and gantt for small projects",
            ),
            homepage = "https://vikunja.io",
            docsUrl = "https://vikunja.io/docs/setup/",
            license = "AGPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 5, cores = 1.0),
            defaultPort = "3456",
            compose = VIKUNJA_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Run the single binary container",
                    "Vikunja needs a database but not a separate cache. For most installs the all-in-one image with SQLite is the sane starting point.",
                ),
                GuideStep(
                    "Set a public URL",
                    "The URL setting is what makes links in emails and calendar entries work. Getting it wrong produces broken task links that are annoying to fix later.",
                ),
                GuideStep(
                    "Create the admin account on the registration page",
                    "The first account registered becomes the admin. Do it yourself, before the hostname is public.",
                ),
                GuideStep(
                    "Add Authentik once the basic flow works",
                    "Single sign-on removes a second set of passwords and gives you central logout. Do it after you are happy with the app, not before.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The database migration runs automatically on start",
                    "Starting a new version against an old database migrates it. Take a copy first, because there is no undo.",
                ),
                GuideStep(
                    "CalDAV support is for dates, not tasks",
                    "Vikunja exposes due dates over CalDAV so they appear in your calendar. It does not sync the task text itself, which surprises people.",
                ),
                GuideStep(
                    "A small team beats a large one on this tool",
                    "It has no permissions model to speak of and no notion of an organisation. For a family or a small team it is ideal; for a company it is not the answer.",
                ),
            ),
            alsoConsider = listOf("openproject", "baserow", "n8n"),
        ),
        Service(
            id = "openproject",
            name = "OpenProject",
            tagline = "Project management for people who have done it before",
            category = Category.PRODUCTIVITY,
            blurb = "OpenProject is a serious project management tool: work packages, gantt charts, time tracking, budgets, and a permission model that will not get in the way. It is what a small engineering team would choose rather than a spreadsheet.\n\nIt is also large. The community edition is free, and the performance tuning guide exists because the defaults assume a real server.",
            bestFor = listOf(
                "Small team project management with gantt and time tracking",
                "Structure beyond a flat to-do list",
                "A self-hosted replacement for Jira",
            ),
            homepage = "https://www.openproject.org",
            docsUrl = "https://www.openproject.org/docs/user-guide/installation-and-configuration/docker/",
            license = "GPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 4096, ramTypicalMb = 8192, diskGb = 30, cores = 4.0),
            defaultPort = "8080",
            compose = OPENPROJECT_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Give it 4 GB to start with",
                    "The Ruby application plus PostgreSQL plus memcached will be killed by the OOM killer on a 2 GB box, and the error messages do not say that.",
                ),
                GuideStep(
                    "Use the seeded database image for a first run",
                    "The seeded variant pre-creates the initial admin account, which saves a genuinely confusing first-login experience.",
                ),
                GuideStep(
                    "Set the host name before the first visit",
                    "OpenProject builds absolute URLs from this value, including the links in notification emails. Set it as an environment variable and do not change it later.",
                ),
                GuideStep(
                    "Turn off the seed data once you are set up",
                    "The demo project is useful for one look and confusing afterwards. Remove it when you create your real first project.",
                ),
                GuideStep(
                    "Attach storage separately if you plan to use attachments",
                    "The default nginx configuration serves files from the app container. For anything beyond a trial, put object or file storage behind it.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The upgrade path is the hard part",
                    "Each major version has a defined path, and skipping versions is not supported. Read the upgrade documentation for the specific jump and take a database backup before starting.",
                ),
                GuideStep(
                    "Session and cache tuning are the performance story",
                    "Memcached and the Rails session store are both in the compose file for a reason. Removing memcached does not break it, it just makes it feel slow enough that people assume it is broken.",
                ),
                GuideStep(
                    "Heavy for a personal to-do list",
                    "If you want a list of things to do, use Vikunja. OpenProject earns its weight when several people need to see the same structure.",
                ),
            ),
            alsoConsider = listOf("vikunja", "twenty", "baserow"),
        ),
        Service(
            id = "onlyoffice",
            name = "ONLYOFFICE Docs",
            tagline = "Microsoft Office documents in the browser",
            category = Category.PRODUCTIVITY,
            blurb = "When a document arrives as a .docx and the person who sent it expects it to look right, ONLYOFFICE is the answer. It is the closest browser-based implementation of Word, Excel, and PowerPoint, with real layout fidelity and co-editing.\n\nThe catch is that it is a heavy Java application with a conversion service behind it, and it is worth it only if you genuinely need Office formats rather than Markdown.",
            bestFor = listOf(
                "Editing .docx and .xlsx in a browser",
                "Office documents on a self-hosted Nextcloud or Seafile",
                "Co-editing with a shared cursor",
            ),
            homepage = "https://www.onlyoffice.com",
            docsUrl = "https://helpcenter.onlyoffice.com/installation/docker",
            license = "AGPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 4096, ramTypicalMb = 6144, diskGb = 20, cores = 4.0),
            defaultPort = "8080",
            compose = ONLYOFFICE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Give it at least 4 GB",
                    "This is the single most common failure. At 2 GB the conversion service is killed under load and documents silently fail to open.",
                ),
                GuideStep(
                    "Know which edition you are running",
                    "There is a Community Edition and a commercial Document Server with different feature sets and licences. The image name tells you which one you have.",
                ),
                GuideStep(
                    "Use the JWT secret if you connect it to storage",
                    "When integrating with Nextcloud, Seafile, or SharePoint, set a JWT secret. Without it the integration is refused for security reasons that are not explained in the error.",
                ),
                GuideStep(
                    "Set the trusted domains or it will refuse to load",
                    "The document server only serves its interface to domains you list. Getting this wrong produces a blank page with a message in the console.",
                ),
                GuideStep(
                    "Put it behind the same hostname as storage",
                    "The simplest integration is one hostname for the document server and another for the file store, with both reachable from the browser. Splitting them across machines works but complicates the cookies.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "First conversion of a document is slow",
                    "The conversion service starts cold and the first document can take 30 seconds. This is normal and gets better, but people assume it is broken.",
                ),
                GuideStep(
                    "It caches converted documents",
                    "The cache directory can grow to several gigabytes and is safe to clear when the disk is tight. Documents are re-converted on the next request.",
                ),
                GuideStep(
                    "Not a general replacement for Office",
                    "Complex macros, some Excel functions, and some fonts will not survive. If fidelity is critical, this is a good tool; if it is critical, you may need the commercial edition.",
                ),
            ),
            alsoConsider = listOf("nextcloud", "seafile"),
        ),
        Service(
            id = "appflowy",
            name = "AppFlowy",
            tagline = "A local-first workspace with Notion's ideas",
            category = Category.PRODUCTIVITY,
            blurb = "AppFlowy is a Notion-shaped workspace where the data lives on your disk first. Pages, boards, calendars, and documents, with a self-hosted backend so several people can share a workspace.\n\nIf you have outgrown a folder of Markdown and want something more structured without handing your notes to a company, this is the closest thing available.",
            bestFor = listOf(
                "A self-hosted structured notes workspace",
                "Local-first so it works with no network",
                "Replacing a collaborative document workspace",
            ),
            homepage = "https://appflowy.com",
            docsUrl = "https://docs.appflowy.io/self-host",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 10, cores = 1.0),
            defaultPort = "8000",
            compose = APPFLOWY_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Understand that the client is the source of truth",
                    "The apps hold local copies and sync to the server. Running only the server gives you a web interface with none of the local-first benefits.",
                ),
                GuideStep(
                    "Use a PostgreSQL database, not the default in-memory one",
                    "The image runs without a database for testing. Anything you care about needs a real Postgres instance behind it.",
                ),
                GuideStep(
                    "Set the base URL correctly",
                    "Clients are told where to sync from by the URL they connect to. Behind a reverse proxy, this must be the public address or every client fails to connect.",
                ),
                GuideStep(
                    "Create the first user through the web app",
                    "There is no default account. The first account to register is the administrator.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Attaching files works differently than you expect",
                    "Uploads go through the server and are not local-first in the same way as text. Large attachment-heavy workspaces are the weak point of this architecture.",
                ),
                GuideStep(
                    "The project is young and moves fast",
                    "Expect breaking changes between versions and read the release notes before upgrading a workspace with real content in it.",
                ),
                GuideStep(
                    "Collab sync needs the server; solo use does not",
                    "If you only need your own notes, the desktop app works entirely offline and the server becomes optional. That is a perfectly good deployment.",
                ),
            ),
            alsoConsider = listOf("joplin", "outline", "baserow"),
        ),
        Service(
            id = "outline",
            name = "Outline",
            tagline = "A wiki that people actually write in",
            category = Category.PRODUCTIVITY,
            blurb = "Outline is a knowledge base with a typing experience that makes writing pleasant, which is the whole difficulty with wikis. It has collections, permissions, and search, and it stores everything in Postgres with an S3-compatible object store for attachments.\n\nIt authenticates against your own identity provider rather than holding passwords, which makes it a natural companion to Authentik.",
            bestFor = listOf(
                "A team wiki or internal documentation",
                "Runbooks and handover notes",
                "A documentation site you host yourself",
            ),
            homepage = "https://www.getoutline.com",
            docsUrl = "https://docs.getoutline.com/s/hosting/doc/self-host",
            license = "BSL-1.1",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "3010",
            compose = OUTLINE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate all four secrets properly",
                    "Outline needs SECRET_KEY, UTILS_SECRET, and a public URL, and each has a required length. The startup script refuses to run with weak values, which is helpful, but the error names them one at a time.",
                ),
                GuideStep(
                    "Point it at S3 or MinIO for attachments",
                    "The default is local disk, which works for a trial. MinIO or any S3-compatible store is what makes the backup story simple and the data portable.",
                ),
                GuideStep(
                    "Connect Authentik or another OIDC provider",
                    "This is the intended way in. Set up the OIDC application first, including the exact redirect URL, because a mismatch fails silently at the login button.",
                ),
                GuideStep(
                    "Run migrations to completion",
                    "On a fresh install the first start creates the schema, which takes a minute. Interrupting it leaves a half-built database and the fix is to let it finish.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The licence is not GPL",
                    "Outline uses the Business Source License, which is source-available and not open source. F-Droid requires a free and open licence, so check this before submitting an app that bundles or depends on it. Use it as a service you host, not as a dependency you ship.",
                ),
                GuideStep(
                    "OIDC with Authentik takes three exact values",
                    "Application type of OpenID Connect, the redirect URL, and the client ID and secret. All three must match exactly or the login flow returns you to a blank page.",
                ),
                GuideStep(
                    "Two-factor is per user and easy to lose",
                    "The recovery codes are shown once. Users who skip that step will need an administrator to reset their second factor, and there is a documented command for it.",
                ),
            ),
            alsoConsider = listOf("authentik", "minio", "joplin"),
        ),
        Service(
            id = "baserow",
            name = "Baserow",
            tagline = "A database with a grid, like a spreadsheet that behaves",
            category = Category.PRODUCTIVITY,
            blurb = "Baserow is a no-code database with a spreadsheet grid, form builder, and API. It fills the gap between a spreadsheet that has stopped scaling and a database that needs an engineer to use.\n\nInstalls are small and fast, and the row-level permissions are good enough for internal tools. The premium self-hosted features are the part to watch.",
            bestFor = listOf(
                "A content calendar or asset tracker",
                "Internal tools with a form and a table",
                "A front end for a Postgres database",
            ),
            homepage = "https://baserow.io",
            docsUrl = "https://baserow.io/docs/installation%2Fdocker",
            license = "MIT",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 10, cores = 1.0),
            defaultPort = "8000",
            compose = BASEROW_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Use the all-in-one image and a real database",
                    "For anything beyond a trial, point it at Postgres. The bundled SQLite default is not intended for concurrent use.",
                ),
                GuideStep(
                    "Create the superuser on first request",
                    "The setup screen creates the first account. It is separate from any account you later create through the admin interface.",
                ),
                GuideStep(
                    "Set the public URL before building anything",
                    "The URL is embedded in webhooks, form responses, and API calls. Changing it afterwards breaks integrations that were built against the old one.",
                ),
                GuideStep(
                    "Use forms rather than sharing the grid",
                    "A shared grid requires an account. A published form does not, and for anything a customer or visitor touches, that is the right approach.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Some features are premium-only in self-hosted mode",
                    "Row-level permissions on shared links, field-level permissions, and the audit log are commercial features. Check the feature list against your needs before committing to it.",
                ),
                GuideStep(
                    "It is a Postgres application, so back up Postgres",
                    "There is no separate file store. The database is the whole state, and pg_dump is the backup.",
                ),
                GuideStep(
                    "The API rate limit is per user by default",
                    "Automations that hammer the API get throttled. For scheduled work, use a service account and a sensible interval.",
                ),
            ),
            alsoConsider = listOf("nocodb", "vikunja", "postgres"),
        ),
    )
}

private val JOPLIN_COMPOSE = """
services:
  db:
    container_name: joplin_db
    image: docker.io/library/postgres:16
    restart: unless-stopped
    volumes:
      - ./dbdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=joplin
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=joplin

  server:
    container_name: joplin_server
    image: joplin/server:${'$'}{JOPLIN_VERSION:-latest}
    restart: unless-stopped
    depends_on:
      - db
    ports:
      - '41184:41184'
    volumes:
      - ./data:/app/data
      - ./config:/app/config
    environment:
      - APP_BASE_URL=${'$'}{APP_BASE_URL}
      - APP_PORT=41184
      - POSTGRES_DB=joplin
      - POSTGRES_USER=joplin
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_HOST=db
      - DB_CLIENT=pg
"""

private val VIKUNJA_COMPOSE = """
services:
  vikunja:
    container_name: vikunja
    image: vikunja/vikunja:${'$'}{VIKUNJA_VERSION:-latest}
    restart: always
    user: ${'$'}{PUID}:${'$'}{PGID}
    ports:
      - '3456:3456'
    volumes:
      - ./data:/var/lib/vikunja
      - ./config:/etc/vikunja
    environment:
      - VIKUNJA_SERVICE_URL=${'$'}{VIKUNJA_SERVICE_URL}
      - VIKUNJA_DATABASE_DRIVER=sqlite
      - VIKUNJA_DATABASE_PATH=/var/lib/vikunja/vikunja.db
    depends_on:
      - db

  db:
    container_name: vikunja_db
    image: docker.io/library/postgres:16-alpine
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=vikunja
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=vikunja
"""

private val OPENPROJECT_COMPOSE = """
services:
  db:
    container_name: openproject_db
    image: postgres:17
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=openproject
      - POSTGRES_USER=openproject
      - POSTGRES_PASSWORD=${'$'}{OPENPROJECT_DB_PASSWORD}
    command: |
      postgres
        -c 'max_connections=200'

  memcached:
    container_name: openproject_memcached
    image: memcached:1.6
    restart: always
    command: memcached -m 512

  web:
    container_name: openproject_web
    image: openproject/openproject:${'$'}{OPENPROJECT_VERSION:-16}
    restart: always
    depends_on:
      - db
      - memcached
    ports:
      - '8080:80'
    environment:
      - OPENPROJECT_HTTPS=false
      - OPENPROJECT_HOST__NAME=${'$'}{OPENPROJECT_HOST}
      - OPENPROJECT_RAILS__CACHE__STORE=memcache
      - OPENPROJECT_RAILS__RELATIVE__URL__ROOT=
      - OPENPROJECT_IMAP__ADDRESS=imap.example.com
      - IMAP__AUTH=login
      - IMAP__PORT=993
      - IMAP__SSL=true
      - IMAP__USERNAME=${'$'}{IMAP_USERNAME}
      - IMAP__PASSWORD=${'$'}{IMAP_PASSWORD}
"""

private val ONLYOFFICE_COMPOSE = """
services:
  onlyoffice:
    container_name: onlyoffice
    image: onlyoffice/documentserver:${'$'}{ONLYOFFICE_VERSION:-9.0}
    restart: always
    ports:
      - '8080:80'
    volumes:
      - ./data:/var/www/onlyoffice/Data
      - ./logs:/var/log/onlyoffice
      - ./cache:/var/lib/onlyoffice
      - ./db:/var/lib/postgresql
    environment:
      - JWT_ENABLED=true
      - JWT_SECRET=${'$'}{JWT_SECRET}
      - JWT_HEADER=Authorization
      - ALLOW_PRIVATE_IP_ADDRESS=true
      - USE_UNAUTHORIZED_STORAGE=false
    # At least 4 GB. The conversion service is killed below this.
    mem_limit: 6g
    ulimits:
      nofile:
        soft: 65536
        hard: 65536
"""

private val APPFLOWY_COMPOSE = """
services:
  appflowy:
    container_name: appflowy
    image: ghcr.io/mayber2001/appflowy-self-host:${'$'}{APPFLOWY_VERSION:-latest}
    restart: unless-stopped
    environment:
      - APPFLOWY_URL=${'$'}{APPFLOWY_URL}
      - APPFLOWY_DB_URL=postgres://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@postgres:5432/${'$'}{POSTGRES_DB}
      - APPFLOWY_PORT=8000
    volumes:
      - ./appflowy_data:/root/.local/share/appflowy
    depends_on:
      - postgres
    ports:
      - '8000:8000'

  postgres:
    container_name: appflowy_postgres
    image: docker.io/library/postgres:16
    restart: unless-stopped
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
"""

private val OUTLINE_COMPOSE = """
services:
  outline:
    container_name: outline
    image: outlinewiki/outline:${'$'}{OUTLINE_VERSION:-latest}
    restart: always
    depends_on:
      - postgres
      - redis
      - s3
    ports:
      - '3010:3010'
    environment:
      - URL=${'$'}{PUBLIC_URL}
      - PORT=3010
      - SECRET_KEY=${'$'}{SECRET_KEY}
      - UTILS_SECRET=${'$'}{UTILS_SECRET}
      - DATABASE_URL=postgres://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@postgres:5432/${'$'}{POSTGRES_DB}
      - DATABASE_URL_TEST=postgres://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@postgres:5432/${'$'}{POSTGRES_DB}
      - REDIS_URL=redis://redis:6379
      - PGSSLMODE=disable
      - FILE_STORAGE=local
      - FILE_STORAGE_LOCAL_ROOT_DIR=/app/files
    volumes:
      - ./data:/app/files

  postgres:
    container_name: outline_postgres
    image: docker.io/library/postgres:16
    restart: always
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
    volumes:
      - ./pgdata:/var/lib/postgresql/data

  redis:
    container_name: outline_redis
    image: redis:7-alpine
    restart: always

  s3:
    container_name: outline_s3
    image: minio/minio:latest
    restart: always
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=${'$'}{S3_ACCESS_KEY_ID}
      - MINIO_ROOT_PASSWORD=${'$'}{S3_SECRET_ACCESS_KEY}
    volumes:
      - ./s3:/data
    ports:
      - '9000:9000'
      - '9001:9001'
"""

private val BASEROW_COMPOSE = """
services:
  baserow:
    container_name: baserow
    image: baserow/baserow:${'$'}{BASEROW_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '8000:8000'
      - '80:80'
    volumes:
      - ./data:/baserow/data
    environment:
      - BASEROW_PUBLIC_URL=${'$'}{BASEROW_PUBLIC_URL}
    depends_on:
      - postgres

  postgres:
    container_name: baserow_postgres
    image: docker.io/library/postgres:16
    restart: unless-stopped
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=baserow
"""
