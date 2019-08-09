#!groovy

dockerRegistryUri = 'docker.fgtsa.com'
dockerImage = docker.image("${dockerRegistryUri}/ubuntu16-java8-egm:latest")

node('docker') {
    docker.withRegistry("https://${dockerRegistryUri}/", "263a98bf-7918-40ba-930a-077edfc76f82") {
        dockerImage.pull()
    }
    setBuildProperties()
    deleteDir()
    checkout scm

    if (env.BRANCH_NAME == 'master') {
        runReleaseFlow()
    } else {
        runSideBranchFlow()
    }

    cleanWs cleanWhenFailure: false, cleanWhenUnstable: false
}

void runReleaseFlow()
{
    stage("Setup") {
        def version = getReleaseVersion()
        currentBuild.displayName = version
    }

    stage("Build") {
        runInDocker(dockerImage) {
            setVersionNumber(version)
            mvn "package"
        }
    }

    if (!version.endsWith('-SNAPSHOT')) {
        stage("Tag SCM") {
            tagScm("v$version", "Nonsnapshot Maven Plugin release $version")
        }
    }
}

void runSideBranchFlow()
{
    stage("Build") {
        runInDocker(dockerImage) {
            mvn "deploy"
        }
    }
}

void setBuildProperties()
{
    def buildProperties = [disableConcurrentBuilds(), durabilityHint('PERFORMANCE_OPTIMIZED')]
    def buildParameters = []

    if (env.BRANCH_NAME == 'master') {
        buildParameters.add(string(defaultValue: '', description: '', name: 'version', trim: true))

        buildProperties.add(buildDiscarder(logRotator(artifactDaysToKeepStr: '30',
                                                      artifactNumToKeepStr: '1',
                                                      daysToKeepStr: '100',
                                                      numToKeepStr: '10')))
    } else {
        buildProperties.add(buildDiscarder(logRotator(artifactDaysToKeepStr: '2',
                                                      artifactNumToKeepStr: '1',
                                                      daysToKeepStr: '10',
                                                      numToKeepStr: '3')))
    }

    buildProperties.add(parameters(buildParameters))
    properties(buildProperties)
}

String getReleaseVersion()
{
    if (!params.version.trim())
        error("Release version not set")

    return params.version
}

int setVersionNumber(String version)
{
    mvn "versions:set -DnewVersion=${version} -DgenerateBackupPoms=false"
}

void mvn(String args)
{
    withMaven(mavenSettingsConfig: 'novoprime-development') {
        withEnv(["PATH+=$MVN_CMD_DIR"]) {
            sh "$MVN_CMD $args"
        }
    }
}

void tagScm(String tag, String message="")
{
    String args = ""
    if (force)
        args = "--force"

    sshagent(['e782ce43-db87-4d1e-abcb-84df640b882b']) {
        sh("git tag -a $tag -m \'$message\' $args")
        sh("git push $args origin $tag")
    }
}

void runInDocker(def image, Closure closure)
{
    String args = "-v /var/lib/jenkins/.grails/wrapper:/home/jenkins/.grails/wrapper"
    args += " -v /var/lib/jenkins/.m2/repository/${EXECUTOR_NUMBER}:/home/jenkins/.m2/repository"
    image.inside(args, closure);
}
