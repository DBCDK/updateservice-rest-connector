#!groovy

def workerNode = "devel11"

void notifyOfBuildStatus(final String buildStatus) {
	final String subject = "${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
	final String details = """<p> Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""
	emailext(
			subject: "$subject",
			body: "$details", attachLog: true, compressLog: false,
			mimeType: "text/html",
			recipientProviders: [[$class: "CulpritsRecipientProvider"]]
	)
}

pipeline {
	agent { label workerNode }

	tools {
		jdk 'jdk11'
		maven 'Maven 3'
	}

	triggers {
		pollSCM("H/03 * * * *")
		upstream(upstreamProjects: "Docker-payara6-bump-trigger",
				threshold: hudson.model.Result.SUCCESS)
	}

	options {
		timestamps()
	}

	stages {
		stage("clear workspace") {
			steps {
				deleteDir()
				checkout scm
			}
		}

		stage("Maven build") {
			steps {
				sh "mvn verify pmd:pmd pmd:cpd spotbugs:spotbugs"

				junit testResults: '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'

				script {
					def java = scanForIssues tool: [$class: 'Java']
					publishIssues issues: [java], unstableTotalAll:1

					def pmd = scanForIssues tool: [$class: 'Pmd']
					publishIssues issues: [pmd], unstableTotalAll:1

					// spotbugs still has some outstanding issues with regard
					// to analyzing Java 11 bytecode.
					// def spotbugs = scanForIssues tool: [$class: 'SpotBugs']
					// publishIssues issues:[spotbugs], unstableTotalAll:1
				}
			}
		}

		stage("deploy") {
			when {
				branch "master"
			}
			steps {
				withMaven(maven: 'Maven 3') {
					sh "mvn jar:jar deploy:deploy"
				}
			}
		}
	}

	post {
		unstable {
			notifyOfBuildStatus("build became unstable")
		}
		failure {
			notifyOfBuildStatus("build failed")
		}
	}
}
