/**
 * Precompiled [androiddrop.feature.gradle.kts][Androiddrop_feature_gradle] script plugin.
 *
 * @see Androiddrop_feature_gradle
 */
public
class Androiddrop_featurePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Androiddrop_feature_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
