package com.wizy.avro

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

/**
 * Пользовательская конфигурация плагина:
 *  - sourceDirs: откуда брать .avdl
 *  - outputBaseDir: куда генерить .avsc/.pr
 *  - writeProtocolJson: генерировать ли .pr (protocol JSON)
 */
open class AvroIdlExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
)
{

    /** Список директорий с .avdl */
    val sourceDirs: ListProperty<File> =
        objects.listProperty(File::class.java).convention(emptyList())

    /** Базовая директория генерации .avsc/.pr */
    val outputBaseDir: DirectoryProperty =
        objects.directoryProperty()
            .convention(layout.buildDirectory.dir("generated/avro"))

    /** Генерировать protocol JSON (.pr) */
    val writeProtocolJson: Property<Boolean> =
        objects.property(Boolean::class.java).convention(true)
}
