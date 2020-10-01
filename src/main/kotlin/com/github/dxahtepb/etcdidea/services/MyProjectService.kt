package com.github.dxahtepb.etcdidea.services

import com.intellij.openapi.project.Project
import com.github.dxahtepb.etcdidea.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
