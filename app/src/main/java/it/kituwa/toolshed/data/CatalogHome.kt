package it.kituwa.toolshed.data

internal object Home {
    val services = listOf(
        Service(
            id = "home-assistant",
            name = "Home Assistant",
            tagline = "Local home automation that keeps working offline",
            category = Category.HOME,
            blurb = "Home Assistant is the most capable home automation hub that is also local-first. Your automations run on your hardware, without a cloud round trip, and they keep working when your internet does not.\n\nIt supports more device integrations than anything comparable, and that breadth is both its strength and its main source of frustration. The container is the easy part; the YAML is where the time goes.",
            bestFor = listOf(
                "Automations that work without an internet connection",
                "Sensors, lights, and switches from many brands",
                "Replacing a cloud-dependent automation hub",
            ),
            homepage = "https://www.home-assistant.io",
            docsUrl = "https://www.home-assistant.io/installation/linux#docker-compose",
            license = "Apache-2.0",
            difficulty = Difficulty.FOCUSSED,
            resources = Resources(ramMinMb = 2048, ramTypicalMb = 4096, diskGb = 30, cores = 2.0),
            defaultPort = "8123",
            compose = HOMEASSISTANT_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Use the full image, not the slim one",
                    "The full image includes the integrations and the tooling needed to install more. The slim image is a fraction of the size and a fraction of the usefulness.",
                ),
                GuideStep(
                    "Use a host network for device discovery",
                    "Zigbee, Z-Wave, Chromecast, and Bluetooth all rely on broadcasts that do not cross Docker's bridge network. host: true is not a convenience here, it is required.",
                ),
                GuideStep(
                    "Add USB passthrough for a radio, if you use one",
                    "A Zigbee or Z-Wave stick needs the device passed in. Note the container path, and remember that the path changes if you unplug and replug it.",
                ),
                GuideStep(
                    "Set the timezone and the location before adding anything",
                    "Sunrise and sunset, presence, and schedules all depend on it. Getting it wrong produces automations that fire at odd hours and are hard to debug.",
                ),
                GuideStep(
                    "Create a long-lived access token for the mobile app",
                    "In your profile, long-lived access tokens. The app needs one to connect without a browser login, and it is also what you would use for an API.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Back up the configuration directory, not the database",
                    "Your automations, scripts, and secrets are all in the config directory. The database is mostly history. A nightly copy of that one directory is the whole backup.",
                ),
                GuideStep(
                    "Automations that depend on the cloud stop working",
                    "Many integrations call out to a vendor API. If your internet drops, those automations fail rather than degrade. Prefer local protocols such as Zigbee, Z-Wave, and MQTT for anything important.",
                ),
                GuideStep(
                    "Secrets belong in secrets.yaml, never in configuration.yaml",
                    "Keep the file readable only by the user running Home Assistant, and back it up separately and securely. Configuration files get shared when people ask for help.",
                ),
                GuideStep(
                    "Zigbee is a mesh, and mesh design matters",
                    "Battery-powered devices should not be routers. A few mains-powered devices spread through the house make the network reliable in a way no amount of software fixes.",
                ),
            ),
            alsoConsider = listOf("ntfy", "adguard-home", "syncthing"),
        ),
        Service(
            id = "ntfy",
            name = "ntfy",
            tagline = "Push notifications from a script to your phone",
            category = Category.HOME,
            blurb = "ntfy turns a URL into a notification. You can send one with curl, and it arrives on your phone with full notification behaviour: it makes a sound, it shows on the lock screen, and it works without any third party.\n\nIt is the smallest useful service here. Once it is running, every script you write can talk to you, and that changes what you automate.",
            bestFor = listOf(
                "Alerting a script without a mail server",
                "Notifications on your own infrastructure",
                "A cron job telling you it finished",
            ),
            homepage = "https://ntfy.sh",
            docsUrl = "https://docs.ntfy.sh/install/#docker",
            license = "Apache-2.0",
            difficulty = Difficulty.EASY,
            resources = Resources(ramMinMb = 64, ramTypicalMb = 128, diskGb = 1, cores = 0.5),
            defaultPort = "80",
            compose = NTFY_COMPOSE,
            installSteps = listOf(
                GuideStep(
                    "Set the cache and attachment sizes to zero for a pure relay",
                    "The defaults cache messages and accept attachments. If you only send and do not store, this keeps the disk tiny.",
                ),
                GuideStep(
                    "Add basic auth to the listen endpoint",
                    "An open notification server is a spam relay and an information leak. Auth is one environment variable and should be on from the start.",
                ),
                GuideStep(
                    "Install the app and subscribe to your topics",
                    "The Android app is the reason to self-host. Subscribe to a topic, allow notifications, and that is the setup.",
                ),
                GuideStep(
                    "Send your first test from a terminal",
                    "curl -d 'hello' ntfy.sh/yourtopic. Once the app receives it, every other use case is obvious.",
                ),
                GuideStep(
                    "Use a random topic name for anything private",
                    "Anyone who guesses a topic can post to it, and if the topic is guessable they can also read it unless auth is on. Treat topic names as secrets when they matter.",
                ),
            ),
            notes = listOf(
                GuideStep(
                    "Topic names are the only thing protecting unencrypted messages",
                    "With auth on, a topic name stops mattering. With auth off, anyone who guesses it can read and post. There is no in-transit encryption by default.",
                ),
                GuideStep(
                    "Use access tokens for anything that posts from a script",
                    "Basic auth in a script is awkward and ends up in shell history. Tokens are scoped, revocable, and designed for this.",
                ),
                GuideStep(
                    "Put a reverse proxy in front for the app connections",
                    "The app uses a streaming connection for the web interface. The default cache limits are tuned for the public service and can be wrong for a self-hosted one with many subscribers.",
                ),
                GuideStep(
                    "Actions can call back into your network",
                    "An action URL lets a notification trigger a webhook on click. That is a genuinely useful feature and also a way for anyone who can post to call your internal services.",
                ),
            ),
            alsoConsider = listOf("uptime-kuma", "home-assistant", "dockge"),
        ),
    )
}

private val HOMEASSISTANT_COMPOSE = """
services:
  homeassistant:
    container_name: homeassistant
    image: ghcr.io/home-assistant/home-assistant:${'$'}{HA_VERSION:-stable}
    restart: unless-stopped
    # Required: device discovery uses broadcasts that do not cross a bridge.
    network_mode: host
    volumes:
      - ./config:/config
      - /etc/localtime:/etc/localtime:ro
      - ./media:/media
    environment:
      - TZ=${'$'}{TZ:-UTC}
    # Uncomment for a Zigbee or Z-Wave radio. The path can change when the
    # device is replugged, so prefer a stable udev symlink.
    # devices:
    #   - /dev/serial/by-id/usb-Zigbee: /dev/ttyUSB0
"""

private val NTFY_COMPOSE = """
services:
  ntfy:
    container_name: ntfy
    image: binwiederhier/ntfy:${'$'}{NTFY_VERSION:-latest}
    restart: always
    ports:
      - '80:80'
    environment:
      # Auth first. An open ntfy is a spam relay.
      - NTFY_BASIC_AUTH_USER=${'$'}{NTFY_USER:-ntfy}
      - NTFY_BASIC_AUTH_PASSWORD=${'$'}{NTFY_PASSWORD}
      # Relay only: no message cache, no attachments.
      - NTFY_CACHE_FILE=-1
      - NTFY_ATTACHMENT_CACHE_DIR=
      - NTFY_AUTH_FILE=/var/lib/ntfy/user.db
      - NTFY_BASE_URL=${'$'}{NTFY_BASE_URL}
    volumes:
      - ./data:/var/lib/ntfy
"""
