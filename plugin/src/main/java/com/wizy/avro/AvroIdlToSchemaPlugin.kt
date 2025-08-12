package com.wizy.avro

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.named
import java.io.File

/**
 * Дирижёр: добавляет Extension, создает по таске на каждый SourceSet, подключает выход в ресурсы и ставит зависимости задач.
 *
 */
class AvroIdlToSchemaPlugin : Plugin<Project>
{

    override fun apply(project: Project)
    {
        val ext = project.extensions.create(
            "avroIdl",
            AvroIdlExtension::class.java,
            project.objects,
            project.layout
        )

        project.plugins.withId("java") { configureForJava(project, ext) }
        project.plugins.withId("java-library") { configureForJava(project, ext) }
    }

    private fun configureForJava(project: Project, ext: AvroIdlExtension)
    {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

        sourceSets.configureEach {
            val sourceSetName = name
            val defaultIdlDir: File = project.layout.projectDirectory
                .dir("src/$sourceSetName/avro/idl").asFile

            val idlDirs: List<File> =
                ext.sourceDirs.orNull?.takeIf { it.isNotEmpty() } ?: listOf(defaultIdlDir)

            val baseForSet = ext.outputBaseDir.map { base -> base.dir(sourceSetName) }

            val genTask = project.tasks.register(
                "avroIdlToSchema${sourceSetName.replaceFirstChar { it.uppercase() }}",
                AvroIdlToSchemaTask::class.java
            ) {
                outputDir.set(baseForSet)
                writeProtocolJson.set(ext.writeProtocolJson)

                val trees: List<FileTree> = idlDirs.map { dir ->
                    project.fileTree(dir).matching { include("**/*.avdl") }
                }
                idlFiles.setFrom(trees)
            }


            resources.srcDir(baseForSet)


            project.tasks.named<Copy>(processResourcesTaskName).configure {
                dependsOn(genTask)
            }
        }
    }
}
