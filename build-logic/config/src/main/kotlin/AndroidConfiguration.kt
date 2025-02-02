object AndroidConfiguration {

    object Sdk {
        const val COMPILATION: Int = 35
        const val MIN: Int = 23
        const val TARGET: Int = COMPILATION
    }

    object Application {
        const val CODE: Int = 1
        val version: Version = Version(major = 12, minor = 17, patch = 5)
    }
}

data class Version(
    private val major: Int,
    private val minor: Int,
    private val patch: Int
) {
    override fun toString(): String = "$major.$minor.$patch"
}