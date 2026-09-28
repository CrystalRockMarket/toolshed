package it.kituwa.toolshed.data

internal object Business {
    val services = listOf(
        Service(
            id = "akaunting",
            name = "Akaunting",
            tagline = "Small-business accounting and invoicing",
            category = Category.BUSINESS,
            blurb = "Akaunting is bookkeeping for a small business: invoices, expenses, bills, and accounts receivable, double-entry underneath. It is the self-hosted option where the paid alternatives want a subscription and an accountant's workflow.\n\nRun it for a sole trader and it does everything. Run it for a company with several entities and you will be aware of its limits.",
            bestFor = listOf(
                "Invoices and expense tracking for a small business",
                "Double-entry bookkeeping you control",
                "A self-hosted alternative to accounting subscriptions",
            ),
            homepage = "https://akaunting.org",
            docsUrl = "https://docs.akaunting.org/installation/docker/",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 512, ramTypicalMb = 1024, diskGb = 10, cores = 1.0),
            defaultPort = "8080",
            compose = AKAUNTING_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Choose the edition deliberately",
                    "The open source edition covers invoicing, expenses, and accounts. The paid editions add multi-company and some reports. Start with open source and see whether you hit a wall.",
                ),
                GuideStep(
                    "Set the app URL and the app key",
                    "Both are in the environment and both are needed before the first request. The app key signs internal links; a wrong one produces blank pages on some routes.",
                ),
                GuideStep(
                    "Give the container a writable storage path",
                    "Attachments, invoice PDFs, and logos are written to disk. A container filesystem is lost on rebuild unless you mount it.",
                ),
                GuideStep(
                    "Create the first company and set the currency and tax",
                    "These are per-company settings and they appear on every invoice. Getting the currency wrong after sending invoices is an awkward correction to make.",
                ),
                GuideStep(
                    "Set up the first bank or category structure early",
                    "Categorising everything afterwards by hand is the tedious part of accounting software. Ten minutes of structure up front saves hours.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Back up the database and the storage path together",
                    "Invoice PDFs and uploaded receipts are files. The database references them. A backup of the database alone restores an app that shows invoices with no PDFs attached.",
                ),
                GuideStep(
                    "Custom fields are powerful and hard to undo",
                    "Adding them to a transaction is easy. Removing one afterwards, once a hundred records depend on it, is a database exercise. Think before you add.",
                ),
                GuideStep(
                    "Multi-currency and multi-company are the edition boundary",
                    "If you invoice in more than one currency, check which edition supports it. The free one has a single company and a single currency by default.",
                ),
            ),
            alsoConsider = listOf("invoice-ninja", "documenso", "ntfy"),
        ),
        Service(
            id = "invoice-ninja",
            name = "Invoice Ninja",
            tagline = "Client invoicing with payment links built in",
            category = Category.BUSINESS,
            blurb = "Invoice Ninja is built around the invoice: templates, line items, recurring invoices, and hosted payment pages. It is more focused than Akaunting and considerably more pleasant for a freelancer or small consultancy.\n\nIf invoicing is the job, this does the job with less configuration. If you need full double-entry bookkeeping, it is the wrong tool.",
            bestFor = listOf(
                "Sending professional invoices quickly",
                "Recurring invoices and payment links",
                "Tracking what has been paid and what has not",
            ),
            homepage = "https://invoiceninja.com",
            docsUrl = "https://invoiceninja.com/docs/self-hosting-docker/",
            license = "Elastic-2.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 20, cores = 1.0),
            defaultPort = "8000",
            compose = INVOICENINJA_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate a mail encryption key",
                    "The setup script needs a 32-character key. Without it the container starts and email sending fails later, which is a confusing order of events.",
                ),
                GuideStep(
                    "Run the setup once and wait for it to finish",
                    "The first start installs the schema. Interrupting it leaves a half-built database and the recovery is to delete the volume and start again.",
                ),
                GuideStep(
                    "Log in with the generated email and password",
                    "The setup writes an administrator account to the logs. Capture it, because there is no default and no reset by email on a fresh install.",
                ),
                GuideStep(
                    "Set up the mail server before sending anything",
                    "Invoices, quotes, and reminders are all sent by email. Get SMTP working first, then create a client and a template.",
                ),
                GuideStep(
                    "Decide where the workers run",
                    "In development mode scheduling and PDF generation run on a schedule. Set the app to production and use a separate queue worker process for a busy account.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Read the licence before shipping this to clients",
                    "Invoice Ninja uses the Elastic Licence, which is source-available with a restriction on competing services. Running it for your own business is fine; the terms are worth reading if you are considering anything else.",
                ),
                GuideStep(
                    "The PDF templates are the whole product",
                    "The default template is fine and a custom one is where the value is. Editing a template in the admin and previewing it is a five-minute job with a large payoff.",
                ),
                GuideStep(
                    "Recurring invoices need the scheduler to be running",
                    "A stopped scheduler means recurring invoices quietly stop being created. Check the log for the scheduler line when one is missing.",
                ),
            ),
            alsoConsider = listOf("akaunting", "documenso", "twenty"),
        ),
        Service(
            id = "documenso",
            name = "Documenso",
            tagline = "E-signatures without sending documents to a third party",
            category = Category.BUSINESS,
            blurb = "Documenso is an electronic signature app. You send a document, the recipient signs it in a browser, and you get a signed PDF back, with an audit trail.\n\nThe difference from the commercial services is where the document goes: nowhere, because you host the thing. For contracts, NDAs, and anything with a confidentiality obligation, that is the entire argument.",
            bestFor = listOf(
                "Contracts and NDAs that need signing",
                "Keeping signed documents on your own infrastructure",
                "A self-hosted alternative to a signature SaaS",
            ),
            homepage = "https://documenso.com",
            docsUrl = "https://docs.documenso.com/self-host",
            license = "AGPL-3.0",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 20, cores = 2.0),
            defaultPort = "3000",
            compose = DOCUMENSO_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set NEXTAUTH_URL and the four auth variables",
                    "Documenso is a Next.js application and it validates these at startup. A missing or wrong value produces a redirect loop on the login page with no useful message.",
                ),
                GuideStep(
                    "Give it Postgres and Redis from the start",
                    "The compose file includes both and the migrations need Redis. The in-memory development defaults are not for anything you care about.",
                ),
                GuideStep(
                    "Let the migrations finish before the first sign-in",
                    "The first start runs the schema migrations, which takes a while. The login page can appear before the database is ready and the first attempt fails.",
                ),
                GuideStep(
                    "Upload a logo and set the company details",
                    "These appear on every document. Setting them once at the start means every envelope looks professional without editing it each time.",
                ),
                GuideStep(
                    "Test a full signing round trip before you send a real one",
                    "Send yourself an envelope, sign it, download it, and open the PDF. That whole path has several moving parts and a failure halfway is awkward.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The legal weight depends on the jurisdiction",
                    "E-signatures are legally recognised in many places under an electronic transactions act, but not uniformly, and some document types are excluded. For anything high-value, check locally.",
                ),
                GuideStep(
                    "Signed PDFs and the audit trail are both in Postgres",
                    "One backup covers the whole application state. The document storage is the only thing to check separately if you have enabled external object storage.",
                ),
                GuideStep(
                    "Templates are the time saver",
                    "Upload a contract as a template with signature fields already placed, and every new envelope is two minutes of work instead of twenty.",
                ),
            ),
            alsoConsider = listOf("stirling-pdf", "akaunting", "authentik"),
        ),
        Service(
            id = "nocodb",
            name = "NocoDB",
            tagline = "A spreadsheet interface over a real database",
            category = Category.BUSINESS,
            blurb = "NocoDB turns a Postgres or MySQL database into a spreadsheet-style grid, with forms, kanban boards, and an API. It is a thin layer over a database you control rather than a database of its own, which is both its strength and its constraint.\n\nBecause the schema is yours, NocoDB cannot offer the flexibility of a purpose-built app. What it does offer is putting a usable interface over existing data very cheaply.",
            bestFor = listOf(
                "A grid over a database you already have",
                "Internal tools with forms and shared views",
                "Small CRUD apps without writing a frontend",
            ),
            homepage = "https://nocodb.com",
            docsUrl = "https://nocodb.com/docs/product-docs/overview/installation/docker",
            license = "Sustainable Use License",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 10, cores = 1.0),
            defaultPort = "8080",
            compose = NOCODB_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set NC_DB to point at your existing database",
                    "This is a metadata store, not an application database. It needs a real Postgres instance, and the container will not start without one configured.",
                ),
                GuideStep(
                    "Create the first admin before opening the port",
                    "The setup screen creates the superuser on first request. Before that it is reachable by anyone who knows the URL.",
                ),
                GuideStep(
                    "Connect a table from your own database",
                    "Point NocoDB at a Postgres table and it becomes a grid immediately. This is much faster than modelling from scratch and it is the intended use.",
                ),
                GuideStep(
                    "Create a view for other people rather than sharing the base",
                    "Views have their own permissions and filters. Sharing a base gives access to the schema, which is rarely what a colleague needs.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "You can rename and drop fields, and that reaches the real table",
                    "NocoDB is a view over a live schema, and a destructive operation in the interface is destructive in Postgres. Back up the database before large schema changes.",
                ),
                GuideStep(
                    "Many roles and views is where it starts to strain",
                    "A grid over a wide table with a lot of rows will feel slow. Paginated views and field limits help; a different tool helps more.",
                ),
                GuideStep(
                    "The licence restricts hosted commercial use",
                    "The Sustainable Use License permits self-hosting for your own use and bars selling it as a service. Read it if that is a possibility.",
                ),
            ),
            alsoConsider = listOf("baserow", "twenty", "postgres"),
        ),
        Service(
            id = "listmonk",
            name = "listmonk",
            tagline = "A mailing list and newsletter you own",
            category = Category.BUSINESS,
            blurb = "listmonk is a newsletter tool: subscribers, templates, campaigns, and analytics. It is a single Go binary and it is fast, which is unusual in a category full of heavy marketing platforms.\n\nCombined with Mailu or an SMTP provider, it is a complete mailing list. It also handles double opt-in and unsubscribe correctly, which is the part that keeps you out of trouble.",
            bestFor = listOf(
                "A newsletter with no per-subscriber pricing",
                "Announcements for a community or family",
                "A mailing list on your own server",
            ),
            homepage = "https://listmonk.com",
            docsUrl = "https://listmonk.org/docs/installation/",
            license = "AGPL-3.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 256, ramTypicalMb = 512, diskGb = 5, cores = 1.0),
            defaultPort = "9000",
            compose = LISTMONK_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Create the database and the first user on the CLI",
                    "The install command prompts for both. Doing it on the command line means you never have a running instance with a default password.",
                ),
                GuideStep(
                    "Set the site URL before sending anything",
                    "Unsubscribe links and confirmation pages are built from it. A wrong URL means every unsubscribe link is broken, and that is a legal problem, not a cosmetic one.",
                ),
                GuideStep(
                    "Connect an SMTP server",
                    "The included queue is a sender, not a mail server. Point it at Mailu on the same host, or a provider, and set the correct port and TLS settings.",
                ),
                GuideStep(
                    "Enable double opt-in before the first import",
                    "Importing a list and sending immediately is how you end up with a deliverability problem. Double opt-in cleans the list and keeps you out of spam folders.",
                ),
                GuideStep(
                    "Send your first campaign to yourself",
                    "A test email does not show you what the campaign looks like in a real inbox. Send a real one to your own address and look at it.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Unsubscribe and consent are the features that matter",
                    "One-click unsubscribe and a documented consent record are the difference between a mailing list and a spam generator. Configure both before you import anyone.",
                ),
                GuideStep(
                    "The bounce handler needs a cron entry",
                    "Marking bounces as unsubscribed automatically keeps the list clean. It runs on a schedule and without the entry, nothing happens.",
                ),
                GuideStep(
                    "Templates with custom code need the merge tags",
                    "The editor is restrictive by design to keep campaigns deliverable. Pushing a full HTML layout through the custom-code path is powerful and easy to break.",
                ),
            ),
            alsoConsider = listOf("n8n", "traefik", "authentik"),
        ),
    )
}

private val AKAUNTING_COMPOSE = """
services:
  akaunting:
    container_name: akaunting
    image: akaunting/akaunting:${'$'}{AKAUNTING_TAG:-3}
    restart: always
    ports:
      - '8080:80'
    environment:
      - APP_URL=${'$'}{APP_URL}
      - APP_KEY=${'$'}{APP_KEY}
      - DB_HOST=db
      - DB_DATABASE=${'$'}{DB_DATABASE:-akaunting}
      - DB_USERNAME=${'$'}{DB_USERNAME:-akaunting}
      - DB_PASSWORD=${'$'}{DB_PASSWORD}
    volumes:
      - ./storage:/var/www/html/storage
    depends_on:
      - db

  db:
    container_name: akaunting_db
    image: mariadb:10.11
    restart: always
    volumes:
      - ./dbdata:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${'$'}{MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=${'$'}{DB_DATABASE:-akaunting}
      - MYSQL_USER=${'$'}{DB_USERNAME:-akaunting}
      - MYSQL_PASSWORD=${'$'}{DB_PASSWORD}
"""

private val INVOICENINJA_COMPOSE = """
services:
  invoice-ninja:
    container_name: invoice-ninja
    image: invoiceninja/invoiceninja:${'$'}{INVOICENINJA_VERSION:-5}
    restart: always
    ports:
      - '8000:8000'
    environment:
      - APP_KEY=${'$'}{APP_KEY}
      - APP_DEBUG=false
      - APP_URL=${'$'}{APP_URL}
      - DB_HOST=db
      - DB_DATABASE=${'$'}{DB_DATABASE:-ninja}
      - DB_USERNAME=${'$'}{DB_USERNAME:-ninja}
      - DB_PASSWORD=${'$'}{DB_PASSWORD}
      - MAIL_MAILER=smtp
      - MAIL_HOST=${'$'}{MAIL_HOST}
      - MAIL_PORT=${'$'}{MAIL_PORT:-587}
      - MAIL_USERNAME=${'$'}{MAIL_USERNAME}
      - MAIL_PASSWORD=${'$'}{MAIL_PASSWORD}
      - MAIL_ENCRYPTION=${'$'}{MAIL_ENCRYPTION:-tls}
      # 32 characters. Required for mail to be sent at all.
      - MAIL_MAILER_ARGUMENTS=--force
    volumes:
      - ./storage:/var/www/html/storage
      - ./public:/var/www/html/public
    depends_on:
      - db

  db:
    container_name: invoice_ninja_db
    image: mariadb:10.11
    restart: always
    volumes:
      - ./dbdata:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${'$'}{MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=${'$'}{DB_DATABASE:-ninja}
      - MYSQL_USER=${'$'}{DB_USERNAME:-ninja}
      - MYSQL_PASSWORD=${'$'}{DB_PASSWORD}

  nginx:
    container_name: invoice_ninja_nginx
    image: nginx:alpine
    restart: always
    depends_on:
      - invoice-ninja
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
"""

private val DOCUMENSO_COMPOSE = """
services:
  documenso:
    container_name: documenso
    image: documenso/documenso:${'$'}{DOCUMENSO_VERSION:-latest}
    restart: always
    ports:
      - '3000:3000'
    environment:
      - NEXTAUTH_URL=${'$'}{NEXTAUTH_URL}
      - NEXTAUTH_SECRET=${'$'}{NEXTAUTH_SECRET}
      - AUTH_SECRET=${'$'}{AUTH_SECRET}
      - AUTH_TRUST_HOST=true
      - DATABASE_URL=postgresql://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@db:5432/${'$'}{POSTGRES_DB}
      - DATABASE_URL_NEXT=postgresql://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@db:5432/${'$'}{POSTGRES_DB}
      - REDIS_URL=redis://redis:6379
      - NEXT_PUBLIC_WEBAPP_URL=${'$'}{NEXTAUTH_URL}
    depends_on:
      - db
      - redis

  db:
    container_name: documenso_db
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB}

  redis:
    container_name: documenso_redis
    image: redis:7
    restart: always
    volumes:
      - ./redisdata:/data
"""

private val NOCODB_COMPOSE = """
services:
  nocodb:
    container_name: nocodb
    image: nocodb/nocodb:${'$'}{NOCODB_TAG:-latest}
    restart: always
    ports:
      - '8080:8080'
    environment:
      # This is NocoDB's own metadata store, not your application data.
      - NC_DB=postgres://${'$'}{POSTGRES_USER}:${'$'}{POSTGRES_PASSWORD}@db:5432/${'$'}{NOCODB_DB:-nocodb}?sslmode=disable
    volumes:
      - ./nocodb-data:/usr/app/data
    depends_on:
      db:
        condition: service_healthy

  db:
    container_name: nocodb_db
    image: docker.io/library/postgres:17
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{NOCODB_DB:-nocodb}
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U ${'$'}{POSTGRES_USER}']
      interval: 10s
      retries: 5
"""

private val LISTMONK_COMPOSE = """
services:
  listmonk:
    container_name: listmonk
    image: listmonk/listmonk:${'$'}{LISTMONK_VERSION:-latest}
    restart: always
    ports:
      - '9000:9000'
    environment:
      - LISTMONK_app__address=0.0.0.0:9000
      - LISTMONK_app__admin_user=${'$'}{LISTMONK_ADMIN_USER:-listmonk}
      - LISTMONK_app__admin_password=${'$'}{LISTMONK_ADMIN_PASSWORD}
      - LISTMONK_db__host=db
      - LISTMONK_db__user=${'$'}{POSTGRES_USER}
      - LISTMONK_db__password=${'$'}{POSTGRES_PASSWORD}
      - LISTMONK_db__database=${'$'}{POSTGRES_DB:-listmonk}
      - LISTMONK_db__ssl_mode=disable
      # listmonk sends mail. Point this at Mailu or a provider.
      - LISTMONK_app__smtp__host=${'$'}{SMTP_HOST}
      - LISTMONK_app__smtp__port=${'$'}{SMTP_PORT:-587}
      - LISTMONK_app__smtp__username=${'$'}{SMTP_USERNAME}
      - LISTMONK_app__smtp__password=${'$'}{SMTP_PASSWORD}
      - LISTMONK_app__smtp__from_email=${'$'}{SMTP_FROM}
    depends_on:
      - db

  db:
    container_name: listmonk_db
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - POSTGRES_DB=${'$'}{POSTGRES_DB:-listmonk}
"""
