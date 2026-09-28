package it.kituwa.toolshed.data

internal object FilesDocs {
    val services = listOf(
        Service(
            id = "nextcloud",
            name = "Nextcloud",
            tagline = "The big one: files, calendar, contacts, and office",
            category = Category.FILES_DOCS,
            blurb = "Nextcloud is a very large project and it is the right answer to 'I want my own Dropbox, calendar, and contacts' when you have the patience for an admin. It runs well, it is genuinely useful, and it is also the single most common source of self-hosted disappointment.\n\nAlmost all of that disappointment comes from two choices made in the first ten minutes: storage that is not local, and a reverse proxy that is not HTTPS. Get those right and it is a joy.",
            bestFor = listOf(
                "Replacing Dropbox, Google Drive, and Google Calendar at once",
                "Self-hosted file sync across your own devices",
                "Keeping a calendar and contacts on your own server",
            ),
            homepage = "https://nextcloud.com",
            docsUrl = "https://docs.nextcloud.com/server/latest/admin_manual/installation/docker.html",
            license = "AGPL-3.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 50, cores = 2.0),
            defaultPort = "8080",
            compose = NEXTCLOUD_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Use local storage for the data directory",
                    "Nextcloud's file locking on NFS and SMB is unreliable in ways that only appear under concurrent use. If your data must live on a network share, put the database on the same share and accept the caveats in the docs.",
                ),
                GuideStep(
                    "Give it a hostname before you start",
                    "Nextcloud records the URL it thinks it is served from, and warns loudly if it does not match. Set trusted_domains in the config before the first request, not after.",
                ),
                GuideStep(
                    "Mount the data directory writable, and nothing else",
                    "The app directory should be read-only in production. Most installation guides for other software say the same and it is worth doing here too, where the application surface is large.",
                ),
                GuideStep(
                    "Serve it over HTTPS from the start",
                    "The desktop and mobile clients refuse to sync against a plain HTTP server, and Nextcloud's own security checks will complain on every page load.",
                ),
                GuideStep(
                    "Run the background job cron, do not skip it",
                    "Without a cron entry every five minutes, you get no notifications, no previews, and eventually a warning banner. This is the most commonly missed step in the whole guide.",
                ),
                GuideStep(
                    "Install only what you will use",
                    "Nextcloud ships with a great deal enabled. Turning off Talk, Assistant, and the apps you do not need reduces both the update surface and the memory footprint considerably.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The Apache image is the documented default",
                    "There are Apache, PHP-FPM, and Alpine variants. They are not interchangeable: some guides assume you have copied rewrite rules into the container's configuration, and the FPM image will not have them.",
                ),
                GuideStep(
                    "Redis is only for file locking, and it is optional",
                    "Without Redis, Nextcloud falls back to database locking, which is slower but correct. With Redis misconfigured, you get intermittent 'file is locked' errors that look like disk problems.",
                ),
                GuideStep(
                    "Maintenance window before every major version",
                    "Major versions need an update of the data directory as well as the image, and the two must match. Take a database dump and a file backup first, and read the upgrade notes for the version you are jumping to.",
                ),
                GuideStep(
                    "occ commands are the admin interface",
                    "occ status, occ maintenance:install, occ user:list. Learning a handful of these will save you from the web UI for most tasks, and it is the only way to do some of them.",
                ),
            ),
            alsoConsider = listOf("seafile", "paperless-ng", "onlyoffice", "syncthing", "traefik"),
        ),
        Service(
            id = "seafile",
            name = "Seafile",
            tagline = "Faster file sync for very large libraries, with built-in encryption",
            category = Category.FILES_DOCS,
            blurb = "Seafile does one job better than Nextcloud: syncing a very large number of files quickly. It stores data in its own block format with a per-library encryption key, so the server never sees file contents in plaintext.\n\nThe trade-off is real. You get files and versioned sync, not a calendar, contacts, or an office suite. If your problem is 'my cloud provider is expensive and I have 200,000 files', Seafile is the answer.",
            bestFor = listOf(
                "Syncing very large file libraries quickly",
                "End-to-end encrypted storage on your own server",
                "Replacing a cloud storage subscription",
            ),
            homepage = "https://www.seafile.com",
            docsUrl = "https://manual.seafile.com/setup/setup_ce/",
            license = "GPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 50, cores = 2.0),
            defaultPort = "8000",
            compose = SEAFILE_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Understand the two pieces",
                    "Seafile Server and Seafile Web are separate containers. The web interface talks to the server; neither works properly on its own.",
                ),
                GuideStep(
                    "Use the CE docker image, not the Pro one",
                    "The community edition image is free and complete. Make sure you do not accidentally reference a Pro image, which requires a licence.",
                ),
                GuideStep(
                    "Create the shared folder volume first",
                    "The server writes to a specific path for shared folders, and it expects that path to exist. A missing volume is the most common first-run failure.",
                ),
                GuideStep(
                    "Create your admin account on first request",
                    "The setup page at the site root creates the administrator. Once created it cannot be changed without editing the database.",
                ),
                GuideStep(
                    "Set the site URL correctly",
                    "The clients download links from this setting, and getting it wrong produces download URLs that point at an internal address. It is an environment variable and must be right from the start.",
                ),
                GuideStep(
                    "Put a certificate in front of it",
                    "The desktop and mobile clients do not trust plain HTTP. Use a reverse proxy with Let's Encrypt, and enable HSTS after you are confident the certificate chain is right.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Library encryption keys are the crown jewels",
                    "Each library has an encryption password, and the server stores only a hash. Lose it and the library is unreadable forever, with no reset. Back these up separately from your data.",
                ),
                GuideStep(
                    "seaf-fuse is the alternative client, and it is better",
                    "The built-in client is fine, but seaf-fuse mounts the library as a normal filesystem, which is what you want for scripted access and backups.",
                ),
                GuideStep(
                    "Two databases, so two backups",
                    "MySQL or MariaDB holds the metadata and object store mapping. Backing up only the file volume gives you files with no way to map them back to libraries.",
                ),
                GuideStep(
                    "Office preview needs its own service",
                    "Editing Office documents in the browser runs a separate preview container. It is optional, and skipping it saves a lot of memory.",
                ),
            ),
            alsoConsider = listOf("nextcloud", "onlyoffice", "syncthing"),
        ),
        Service(
            id = "paperless-ng",
            name = "Paperless-ngx",
            tagline = "Scan documents into a searchable archive",
            category = Category.FILES_DOCS,
            blurb = "Paperless-ngx turns a pile of paper into a searchable archive. Drop a scan in a watched folder and it does OCR, extracts the text, and files it by correspondent, date, and document type. Search actually finds things, which is the entire point of digitising paperwork.\n\nIt also handles incoming email, so bills can be attached automatically rather than filed by hand.",
            bestFor = listOf(
                "Digitising and searching household paperwork",
                "Automatic filing of incoming invoices by email",
                "Cutting a filing cabinet down to nothing",
            ),
            homepage = "https://docs.paperless-ngx.com",
            docsUrl = "https://docs.paperless-ngx.com/configuration/",
            license = "GPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 20, cores = 2.0),
            defaultPort = "8000",
            compose = PAPERLESS_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate the secret key before the first start",
                    "The web front end needs a Django secret key. Generate one with python3 -c 'import secrets; print(secrets.token_urlsafe(50))' and put it in .env. The container will not come up cleanly without it.",
                ),
                GuideStep(
                    "Be deliberate about the OCR languages",
                    "Set PAPERLESS_OCR_LANGUAGE to what your documents actually are. The default is English, and mis-set OCR produces confidently wrong text, which is worse than no text.",
                ),
                GuideStep(
                    "Mount the consume and document directories as volumes",
                    "Without a shared consume directory, there is nowhere for the scanner to drop files and the whole point of the automation is lost.",
                ),
                GuideStep(
                    "Add a superuser on the command line",
                    "The first account is not an administrator unless you say so. Run createuperuser in the container, then use it to make a normal admin account.",
                ),
                GuideStep(
                    "Connect your mail account once the basics work",
                    "Mail fetching wants an IMAP password, and Gmail in particular needs an app password rather than your account password. Get the scanning working first, then add mail.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "OCR quality is set by the scan, not the software",
                    "300 dpi, greyscale, no shadows. A 1200 dpi colour scan of a creased document OCRs worse than a clean 300 dpi greyscale one, and takes far longer.",
                ),
                GuideStep(
                    "The workflow triggers and correspondent rules are where the value is",
                    "Set a rule that tags anything from a particular sender as a bill and assigns it to a tag. Doing this by hand afterwards is most of the work the software was meant to save.",
                ),
                GuideStep(
                    "Back up the database and the media directory together",
                    "A backup with one but not the other is not a backup: the database holds the document index, the media directory holds the files, and the pair is meaningless apart.",
                ),
                GuideStep(
                    "Gotenberg and Tika are optional but worth it",
                    "Gotenberg converts Office documents to PDF before OCR, which is what lets Paperless read a .docx that a scanner sent you. Tika extracts text from more formats. Both cost under a gigabyte between them.",
                ),
            ),
            alsoConsider = listOf("dawarich", "nextcloud", "stirling-pdf"),
        ),
        Service(
            id = "syncthing",
            name = "Syncthing",
            tagline = "Peer-to-peer file sync, no server in the middle",
            category = Category.FILES_DOCS,
            blurb = "Syncthing keeps folders in sync directly between your machines. There is no central server, no account, and no cloud provider. Your laptop, your phone, and your NAS all hold the files and agree on changes.\n\nIt is the right answer when you want your devices to talk to each other and nothing else. The web interface is famously plain and does not matter at all.",
            bestFor = listOf(
                "Keeping a folder identical across your own devices",
                "No cloud provider holding your files",
                "Syncing a NAS to laptops over a tailnet",
            ),
            homepage = "https://syncthing.net",
            docsUrl = "https://docs.syncthing.net/users/index.html",
            license = "MPL-2.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 128, ramTypicalMb = 256, diskGb = 5, cores = 0.5),
            defaultPort = "8384",
            compose = SYNCTHING_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Run it with a config volume and nothing else",
                    "Syncthing keeps everything in one directory. Mount it and you are done; the first launch generates the config and a device ID.",
                ),
                GuideStep(
                    "Copy the device ID, not the API key",
                    "Two devices share files by exchanging device IDs. The API key is for the API, and pasting it into another device does nothing useful.",
                ),
                GuideStep(
                    "Think about folder types before you add devices",
                    "Receive Only is the safe default: it will never delete or overwrite. Send and Receive trusts the device to propagate deletions, which is powerful and occasionally unforgiving.",
                ),
                GuideStep(
                    "If it is on a tailnet, keep the discovery relay",
                    "You can disable global discovery and use only your own devices' addresses. It is a small privacy win and a good habit, but global discovery still works if you leave it on.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Sync is not backup",
                    "Syncthing propagates deletions. A file deleted on one device is deleted on all of them, and that deletion syncs too. You still need a real backup with history.",
                ),
                GuideStep(
                    "The first sync of a huge folder needs patience",
                    "A million small files over the block protocol takes time and produces reindexed databases. Let it finish before judging the setup.",
                ),
                GuideStep(
                    "Set a GUI address behind the reverse proxy",
                    "Behind HTTPS, the default address confuses the UI into connecting insecurely. Set the GUI address explicitly to the https URL.",
                ),
            ),
            alsoConsider = listOf("seafile", "nextcloud", "pihole"),
        ),
        Service(
            id = "stirling-pdf",
            name = "Stirling PDF",
            tagline = "An online PDF toolbox you host yourself",
            category = Category.FILES_DOCS,
            blurb = "Stirling PDF is a self-hosted web app for the PDF jobs you would otherwise upload to a website: merge, split, rotate, watermark, convert, redact, and OCR. It is genuinely useful and it is a good demonstration of running something purely for yourself.\n\nIt handles documents, so treat its access control and its backups as seriously as the rest of your paperwork.",
            bestFor = listOf(
                "PDF merging, splitting, and conversion without uploading anything",
                "Redacting a document before sharing it",
                "A private alternative to online PDF tools",
            ),
            homepage = "https://stirlingpdf.com",
            docsUrl = "https://docs.stirlingpdf.com/",
            license = "GPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 10, cores = 1.0),
            defaultPort = "8080",
            compose = STIRLING_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Run the single-container image",
                    "For most people the all-in-one image is the right choice. The split images exist for scaling, and scaling is not the problem you have yet.",
                ),
                GuideStep(
                    "Enable authentication before anyone else can reach it",
                    "It ships with login disabled so that a first run is painless. On a shared network, an unauthenticated PDF tool is an open file server.",
                ),
                GuideStep(
                    "Keep the temp directory on real disk",
                    "Large conversions are written to the data directory. Putting it on tmpfs means a big job can exhaust RAM and take the container down.",
                ),
                GuideStep(
                    "Back up the data directory if you use pipelines",
                    "If you have built custom pipelines, they live in the data directory. Without a backup, a container rebuild loses them.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Files are transient but not necessarily private",
                    "Processed files land in the data directory and are cleaned up on a schedule. If you are handling tax returns or medical paperwork, shorten the cleanup interval.",
                ),
                GuideStep(
                    "Java and LibreOffice together are heavy",
                    "The Office conversion path pulls in LibreOffice, which roughly doubles the memory use and the image size. It is worth it only if you convert Office documents regularly.",
                ),
                GuideStep(
                    "Redaction removes content, not just the overlay",
                    "Use the real redaction tool and then open the result to confirm. A black box drawn over text leaves the text selectable underneath.",
                ),
            ),
            alsoConsider = listOf("paperless-ng", "nextcloud", "homarr"),
        ),
    )
}

private val NEXTCLOUD_COMPOSE = """
services:
  nextcloud:
    container_name: nextcloud
    image: nextcloud:${'$'}{NEXTCLOUD_VERSION:-29-apache}
    restart: always
    ports:
      - '8080:80'
    volumes:
      - nextcloud:/var/www/html
      - ./apps:/var/www/html/custom_apps
      - ./config:/var/www/html/config
      - ./data:/var/www/html/data
      - /etc/localtime:/etc/localtime:ro
    environment:
      - MYSQL_HOST=db
      - MYSQL_DATABASE=${'$'}{NEXTCLOUD_DB_NAME}
      - MYSQL_USER=${'$'}{NEXTCLOUD_DB_USER}
      - MYSQL_PASSWORD=${'$'}{NEXTCLOUD_DB_PASSWORD}
      - NEXTCLOUD_TRUSTED_DOMAINS=${'$'}{NEXTCLOUD_TRUSTED_DOMAINS}
      - REDIS_HOST=redis
      - PHP_MEMORY_LIMIT=512M
    depends_on:
      - db
      - redis

  db:
    container_name: nextcloud_db
    image: mariadb:11.4
    restart: always
    command: --transaction-isolation=READ-COMMITTED --log-bin=binlog --binlog-format=ROW
    volumes:
      - db:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${'$'}{MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=${'$'}{NEXTCLOUD_DB_NAME}
      - MYSQL_USER=${'$'}{NEXTCLOUD_DB_USER}
      - MYSQL_PASSWORD=${'$'}{NEXTCLOUD_DB_PASSWORD}

  redis:
    container_name: nextcloud_redis
    image: redis:7-alpine
    restart: always

volumes:
  nextcloud:
  db:
"""

private val SEAFILE_COMPOSE = """
services:
  db:
    container_name: seafile_db
    image: mariadb:10.11
    restart: always
    volumes:
      - ./seafile-db:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${'$'}{SEAFILE_MYSQL_ROOT_PASSWORD}
      - MYSQL_LOG_CONSOLE=true
      - MYSQL_DATABASE=seafile_db
      - MYSQL_USER=seafile
      - MYSQL_PASSWORD=${'$'}{SEAFILE_MYSQL_PASSWORD}

  memcached:
    container_name: seafile_memcached
    image: memcached:1.6
    restart: always
    command: memcached -m 256

  seafile:
    container_name: seafile
    image: seafileltd/seafile-mc:${'$'}{SEAFILE_IMAGE_TAG:-latest}
    restart: always
    ports:
      - '8082:80'
    volumes:
      - ./seafile-data:/shared
    environment:
      - SEAFILE_MYSQL_DB_HOST=db
      - SEAFILE_MYSQL_DB_USER=seafile
      - SEAFILE_MYSQL_DB_PASSWORD=${'$'}{SEAFILE_MYSQL_PASSWORD}
      - SEAFILE_SERVER_HOSTNAME=${'$'}{SEAFILE_SERVER_HOSTNAME}
    depends_on:
      - db
      - memcached
    mem_limit: 4g

  seafile-web:
    container_name: seafile-web
    image: seafileltd/seafile-web:${'$'}{SEAFILE_IMAGE_TAG:-latest}
    restart: always
    ports:
      - '8000:80'
    volumes:
      - ./seafile-data:/shared
    environment:
      - SEAFILE_SERVER_HOSTNAME=${'$'}{SEAFILE_SERVER_HOSTNAME}
    depends_on:
      - seafile
"""

private val PAPERLESS_COMPOSE = """
services:
  webserver:
    container_name: paperless-web
    image: ghcr.io/paperless-ngx/paperless-ngx:${'$'}{PAPERLESS_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '8000:8000'
    volumes:
      - ./data:/usr/src/paperless/data
      - ./media:/usr/src/paperless/media
      - ./export:/usr/src/paperless/export
      - ./consume:/usr/src/paperless/consume
    environment:
      - PAPERLESS_REDIS=redis://broker:6379
      - PAPERLESS_DBHOST=db
      - PAPERLESS_DBPORT=5432
      - PAPERLESS_DBNAME=${'$'}{PAPERLESS_DBNAME:-paperless}
      - PAPERLESS_DBUSER=${'$'}{PAPERLESS_DBUSER:-paperless}
      - PAPERLESS_DBPASS=${'$'}{PAPERLESS_DBPASS}
      - PAPERLESS_SECRET_KEY=${'$'}{PAPERLESS_SECRET_KEY}
      - PAPERLESS_OCR_LANGUAGE=${'$'}{PAPERLESS_OCR_LANGUAGE:-eng}
      - PAPERLESS_URL=${'$'}{PAPERLESS_URL}
      - PAPERLESS_TIME_ZONE=${'$'}{TZ:-UTC}
    depends_on:
      - db
      - broker

  db:
    container_name: paperless_db
    image: docker.io/library/postgres:16
    restart: unless-stopped
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=${'$'}{PAPERLESS_DBNAME:-paperless}
      - POSTGRES_USER=${'$'}{PAPERLESS_DBUSER:-paperless}
      - POSTGRES_PASSWORD=${'$'}{PAPERLESS_DBPASS}

  broker:
    container_name: paperless_broker
    image: docker.io/valkey/valkey:8-alpine
    restart: unless-stopped
    volumes:
      - ./redisdata:/data

  # Optional: lets Paperless read .docx and similar directly.
  gotenberg:
    container_name: paperless_gotenberg
    image: docker.io/gotenberg/gotenberg:8
    restart: unless-stopped
    command:
      - 'gotenberg'
      - '--chromium-disable-javascript=true'
      - '--chromium-allow-list=file:///tmp/.*'
    expose:
      - '3000'
"""

private val SYNCTHING_COMPOSE = """
services:
  syncthing:
    container_name: syncthing
    image: syncthing/syncthing:${'$'}{SYNCTHING_VERSION:-latest}
    user: ${'$'}{PUID}:${'$'}{PGID}
    volumes:
      - ./config:/var/syncthing/config
      - /srv/sync-data:/data
    ports:
      - '8384:8384'
      - '22000:22000/tcp'
      - '21027:21027/udp'
    restart: unless-stopped
"""

private val STIRLING_COMPOSE = """
services:
  stirling:
    container_name: stirling-pdf
    image: docker.io/stirlingtools/stirling-pdf:${'$'}{STIRLING_VERSION:-latest}
    restart: unless-stopped
    ports:
      - '8080:8080'
    volumes:
      - ./config:/config
      - ./data:/usr/src/pdf-data
      - ./custom-templates:/custom-templates
    environment:
      - DISABLE_ADDITIONAL_FEATURES=true
      - SECURITY_ENABLELOGIN=true
      - TZ=${'$'}{TZ:-UTC}
"""
