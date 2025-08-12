package com.wizy.avro

import org.apache.avro.Protocol
import org.apache.avro.Schema
import org.apache.avro.SchemaFormatter
import org.apache.avro.idl.IdlFile
import org.apache.avro.idl.IdlReader
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class AvroIdlToSchemaTask : DefaultTask()
{

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val idlFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val writeProtocolJson: Property<Boolean>

    init
    {
        description = "Преобразует Avro IDL (.avdl) в схемы Avro .avsc и .avpr"
        group = "build"
    }

    @TaskAction
    fun generate()
    {
        val outRoot = outputDir.get().asFile
        val schemaOut = File(outRoot, "schema").apply { mkdirs() }
        val protocolOut = File(outRoot, "protocol").apply { mkdirs() }
        val reader = IdlReader()

        idlFiles.files.filter { it.isFile && it.extension.equals("avdl", ignoreCase = true) }
            .forEach { idl ->
                val parsed: IdlFile = reader.parse(idl.toPath())
                parsed.namedSchemas.values.forEach { schema ->
                    writeSchema(schema, schemaOut)
                }
                if (writeProtocolJson.get())
                {
                    parsed.protocol?.let { protocol ->
                        writeProtocol(protocol, protocolOut)
                    }
                }
            }
    }

    private fun writeSchema(schema: Schema, root: File)
    {
        val ns = schema.namespace ?: ""
        val dir = checkAndMakeDirs(ns, root)
        File(dir, "${schema.name}.avsc").writeText(SchemaFormatter.format("json/pretty", schema))
    }

    private fun writeProtocol(protocol: Protocol, root: File)
    {
        val ns = protocol.namespace ?: ""
        val dir = checkAndMakeDirs(ns, root)
        File(dir, "${protocol.name}.avpr").writeText(protocol.toString(true))
    }

    private fun checkAndMakeDirs(ns: String, root: File): File
    {
        val dir = if (ns.isEmpty()) root else File(root, ns.replace('.', File.separatorChar))

        if (!dir.exists()) dir.mkdirs()

        return dir
    }
}
