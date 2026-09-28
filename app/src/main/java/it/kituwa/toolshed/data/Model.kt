package it.kituwa.toolshed.data

enum class Category(val label: String, val blurb: String) {
    PHOTOS_MEDIA("Photos & media", "Libraries, streaming, and photo vaults"),
    FILES_DOCS("Files & documents", "Storage, sync, and paperwork"),
    PRODUCTIVITY("Productivity", "Notes, tasks, and office suites"),
    COLLABORATION("Chat & collaboration", "Messaging, calls, and forums"),
    AUTOMATION("Automation", "Workflows and scheduled jobs"),
    MONITORING("Monitoring", "Uptime, metrics, and alerting"),
    SECURITY("Identity & security", "Single sign-on, VPN, and secrets"),
    NETWORKING("Networking", "Reverse proxies, DNS, and VPN"),
    PLATFORM("Building blocks", "Databases, caches, and storage backends"),
    BUSINESS("Business & admin", "Finance, CRM, and helpdesk"),
    HOME("Home & family", "Automation and shared family services"),
    ;

    companion object {
        val ALL: List<Category> = entries.toList()
    }
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MODERATE("Moderate"),
    FOCUSSED("Fiddly"),
}

/**
 * A rough footprint for planning, not a promise. [ramMinMb] is what the service
 * will start on, [ramTypicalMb] is where it settles once warmed up and busy.
 * [diskGb] is application data only and excludes the Docker image itself.
 */
data class Resources(
    val ramMinMb: Int,
    val ramTypicalMb: Int,
    val diskGb: Int,
    val cores: Double,
)

data class GuideStep(
    val title: String,
    val body: String,
)

data class Service(
    val id: String,
    val name: String,
    val tagline: String,
    val category: Category,
    val blurb: String,
    val bestFor: List<String>,
    val homepage: String,
    val docsUrl: String,
    val license: String,
    val difficulty: Difficulty,
    val resources: Resources,
    val defaultPort: String,
    val compose: String,
    val installSteps: List<GuideStep>,
    val notes: List<GuideStep>,
    val alsoConsider: List<String> = emptyList(),
)

/**
 * A bundle of services that are installed together, either because they
 * genuinely depend on each other or because a first install of one without
 * the other tends to go wrong.
 */
data class StarterStack(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val serviceIds: List<String>,
    val targetHardware: String,
    val difficulty: Difficulty,
    val compose: String,
    val installSteps: List<GuideStep>,
)

/** A box the planner can size a selection against. */
data class Machine(
    val id: String,
    val label: String,
    val ramMb: Int,
    val diskGb: Int,
    val cores: Double,
    val note: String,
) {
    companion object {
        val ALL: List<Machine> = listOf(
            Machine("pi5", "Raspberry Pi 5 8GB", 8192, 256, 4.0, "Arm, and one very small disk. Good for a few services, not for media transcoding."),
            Machine("minipc", "Mini PC 16GB", 16384, 512, 8.0, "The sensible default for a handful of services with room to spare."),
            Machine("mini32", "Mini PC 32GB", 32768, 1024, 8.0, "Comfortable for a dozen services, photo libraries, and databases."),
            Machine("nas", "NAS or workstation 64GB", 65536, 4096, 12.0, "For big libraries, local models, and several stacks at once."),
            Machine("vps1", "VPS 1GB", 1024, 25, 1.0, "One small service, nothing else. Swap will save you, but enjoy the disk."),
            Machine("vps2", "VPS 2GB", 2048, 50, 2.0, "A proxy and one or two apps."),
            Machine("vps4", "VPS 4GB", 4096, 80, 4.0, "A comfortable small VPS with room for a stack."),
            Machine("vps8", "VPS 8GB", 8192, 160, 4.0, "Several services and a database without worrying."),
        )

        fun byId(id: String): Machine = ALL.firstOrNull { it.id == id } ?: ALL[1]
    }
}

/** Totals for a set of services, used by the planner. */
data class ResourceTotal(
    val services: List<Service>,
    val ramMinMb: Int,
    val ramTypicalMb: Int,
    val diskGb: Int,
    val cores: Double,
) {
    val isEmpty: Boolean get() = services.isEmpty()

    companion object {
        val EMPTY = ResourceTotal(emptyList(), 0, 0, 0, 0.0)
    }
}

fun totalOf(services: List<Service>): ResourceTotal = ResourceTotal(
    services = services,
    ramMinMb = services.sumOf { it.resources.ramMinMb },
    ramTypicalMb = services.sumOf { it.resources.ramTypicalMb },
    diskGb = services.sumOf { it.resources.diskGb },
    cores = services.sumOf { it.resources.cores },
)
