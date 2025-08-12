package com.wizy.avro

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

/**
 * Пользовательская конфигурация плагина (куда складывать .avdl, куда генерировать .avsc).
 *  */
abstract class AvroIdlExtension @Inject constructor(
    layout: ProjectLayout
)
{

    abstract val sourceDirs: ListProperty<File>

    abstract val outputBaseDir: DirectoryProperty

    abstract val writeProtocolJson: Property<Boolean>

    init
    {
        sourceDirs.convention(emptyList())
        outputBaseDir.convention(layout.buildDirectory.dir("generated/avro"))
        writeProtocolJson.convention(true)
    }
}
