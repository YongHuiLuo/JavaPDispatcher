package com.permission.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class JavaAssistPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def log = project.getLogger()
        log.error "---------------start plugin-----------------"
        log.error "-----------------666666---------------------------"
        log.error "----------------end plugin--------------"
    }
}