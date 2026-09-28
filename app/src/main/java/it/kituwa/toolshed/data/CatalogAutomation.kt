package it.kituwa.toolshed.data

internal object Automation {
    val services = listOf(
        Service(
            id = "n8n",
            name = "n8n",
            tagline = "Workflow automation with a visual editor",
            category = Category.AUTOMATION,
            blurb = "n8n connects things. It has a visual editor for building workflows across APIs, databases, files, and shell commands, and it is the most approachable automation tool in the self-hosted world.\n\nWhat makes it dangerous is also what makes it useful: it can run arbitrary code, read your files, and reach your whole network. A workflow you did not write has the same power as one you did. Treat the instance like a web application with a database, because that is what it is.",
            bestFor = listOf(
                "Gluing services together without writing code",
                "Scheduled jobs that talk to APIs",
                "Automating the boring parts of administration",
            ),
            homepage = "https://n8n.io",
            docsUrl = "https://docs.n8n.io/hosting/installation/docker/",
            license = "Sustainable Use License",
            difficulty = Difficulty.MODERATE,
            resources = Resources(ramMinMb = 1024, ramTypicalMb = 2048, diskGb = 10, cores = 1.0),
            defaultPort = "5678",
            compose = N8N_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Generate an encryption key with real entropy",
                    "N8N encrypts credentials in its database with a key you supply. Generate a 32-byte hex string. If you lose it, every stored credential becomes unreadable and must be re-entered by hand.",
                ),
                GuideStep(
                    "Turn on user management on the first run",
                    "By default the first account can be created by anyone who reaches the setup page. Set N8N_USER_MANAGEMENT_ENABLED before you expose the instance, and make the owner account yourself.",
                ),
                GuideStep(
                    "Mount the database, not SQLite, for anything real",
                    "SQLite is fine for a trial. Concurrent workflow execution against SQLite produces locking errors, so move to Postgres before you rely on it.",
                ),
                GuideStep(
                    "Understand what the task runner does",
                    "Since version 1.0, Python and JavaScript code nodes run in a separate task runner rather than inside the main process. A runner in internal mode is fine for trusted workflows; external mode is what you want for anything a user can submit.",
                ),
                GuideStep(
                    "Give it a URL that matches reality",
                    "Webhook URLs in workflows are built from N8N_HOST and the editor URL. Getting these wrong produces webhooks that fire at an address nobody can reach.",
                ),
                GuideStep(
                    "Back up the database on a schedule",
                    "Workflows, credentials, and execution history all live in the database. This is the backup that matters; the n8n folder is mostly logs and encryption key material.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "The licence is source-available, not open source",
                    "n8n uses the Sustainable Use License and fair-code model. Embedding it in a product you sell is restricted, and hosting it for your own use is fine. F-Droid requires free and open licences, so do not bundle it; this entry documents it as a service you run.",
                ),
                GuideStep(
                    "A workflow can do anything the container user can",
                    "Code nodes run JavaScript, shell nodes run commands, and the HTTP request node reaches anything the network allows. Review workflows the same way you would review a shell script, and keep the instance off the public internet.",
                ),
                GuideStep(
                    "Never mount the host Docker socket unless you must",
                    "Doing so gives every workflow root on the host. If a workflow needs to manage containers, use a restricted socket proxy with an allow-list instead.",
                ),
                GuideStep(
                    "Execution history is what makes the database grow",
                    "Prune old executions on a schedule, or the database grows without bound and the instance gets slow for reasons unrelated to your workflows.",
                ),
            ),
            alsoConsider = listOf("postgres", "traefik", "homarr"),
        ),
    )
}

private val N8N_COMPOSE = """
services:
  n8n:
    container_name: n8n
    image: docker.n8n.io/n8nio/n8n:${'$'}{N8N_VERSION:-latest}
    restart: always
    ports:
      - '5678:5678'
    environment:
      # Generate with: openssl rand -hex 32
      - N8N_ENCRYPTION_KEY=${'$'}{N8N_ENCRYPTION_KEY}
      - DB_TYPE=postgresdb
      - DB_POSTGRESDB_HOST=db
      - DB_POSTGRESDB_PORT=5432
      - DB_POSTGRESDB_DATABASE=${'$'}{POSTGRES_DB}
      - DB_POSTGRESDB_USER=${'$'}{POSTGRES_USER}
      - DB_POSTGRESDB_PASSWORD=${'$'}{POSTGRES_PASSWORD}
      - N8N_HOST=${'$'}{N8N_HOST}
      - N8N_PORT=5678
      - N8N_PROTOCOL=https
      - WEBHOOK_URL=${'$'}{N8N_WEBHOOK_URL}
      - GENERIC_TIMEZONE=${'$'}{TZ:-UTC}
      - N8N_USER_MANAGEMENT_ENABLED=true
      - N8N_RUNNERS_ENABLED=true
      # Internal mode is fine while only you write workflows.
      # Switch to 'external' once other people can build them.
      - N8N_RUNNERS_MODE=internal
    volumes:
      - ./n8n_data:/home/node/.n8n
      - ./files:/files
    depends_on:
      - db

  db:
    container_name: n8n_db
    image: docker.io/library/postgres:16
    restart: always
    volumes:
      - ./pgdata:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=${'$'}{POSTGRES_DB}
      - POSTGRES_USER=${'$'}{POSTGRES_USER}
      - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
"""
