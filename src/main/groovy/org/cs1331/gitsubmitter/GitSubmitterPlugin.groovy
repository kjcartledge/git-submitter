package org.cs1331.gitsubmitter

import org.gradle.api.Project
import org.gradle.api.Plugin

class GitSubmitterPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("submission", GitSubmitterPluginExtension)
        project.task('submit') << {
            List<String> args = []
            if (project.submission.collabs.size() > 0) {
                args << p(Application.ADD_COLLABS_OPT)
                args.addAll(project.submission.collabs)
            }

            if (project.submission.files.size() > 0) {
                args << p(Application.OVERRIDE_FILES_OPT)
                args.addAll(project.submission.files)
            }

            args << p(Application.SHOW_SUBMISSION_OPT)
            args << p(Application.FORCE_SUBMIT_OPT)

            args << p(Application.REPO_NAME_OPT)
            args << project.submission.repo_name

            Application.main((String[])args)
        }
    }

    String p(String s) {
        return "-" + s
    }
}
