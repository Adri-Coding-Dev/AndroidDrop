/**
 * Precompiled [androiddrop.core.gradle.kts][Androiddrop_core_gradle] script plugin.
 *
 * @see Androiddrop_core_gradle
 */
public
class Androiddrop_corePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Androiddrop_core_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
