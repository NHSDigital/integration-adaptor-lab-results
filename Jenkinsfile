pipeline {
    agent{
        label 'jenkins-workers'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: "10")) // keep only last 10 builds
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        BUILD_TAG = sh label: 'Generating build tag', returnStdout: true, script: 'python3 pipeline/scripts/tag.py ${GIT_BRANCH} ${BUILD_NUMBER} ${GIT_COMMIT}'
        BUILD_TAG_LOWER = sh label: 'Lowercase build tag', returnStdout: true, script: "echo -n ${BUILD_TAG} | tr '[:upper:]' '[:lower:]'"
        ENVIRONMENT_ID = "lab-results-build"
        ECR_REPO_DIR = "lab-results"
        DOCKER_IMAGE = "${DOCKER_REGISTRY}/${ECR_REPO_DIR}:${BUILD_TAG}"
    }

    stages {
        stage('Build and Test Locally') {
            stages {
                stage('Build') {
                    steps {
                        script {
                            sh label: 'Create logs directory', script: 'mkdir -p logs build'
                            if (sh(label: 'Build lab-results-static-check', script: 'docker build -t local/lab-results-static-check:${BUILD_TAG} -f Dockerfile.tests .', returnStatus: true) != 0) {error("Failed to build docker image for static check")}
                            if (sh(label: 'Running static checks', script: 'docker run -v /var/run/docker.sock:/var/run/docker.sock --name lab-results-static-check local/lab-results-static-check:${BUILD_TAG} gradle check -x test -x integrationTest --continue -i', returnStatus: true) != 0) {error("Some static checks failed, check the logs")}
                        }
                    }
                    post {
                        always {
                            sh "docker cp lab-results-static-check:/home/gradle/src/build ."
                            recordIssues(
                                    enabledForFailure: true,
                                    tools: [
                                            checkStyle(pattern: 'build/reports/checkstyle/*.xml'),
                                            spotBugs(pattern: 'build/reports/spotbugs/*.xml')
                                    ]
                            )
                            sh "rm -rf build"
                            sh "docker stop lab-results-static-check"
                        }
                    }
                }
                stage('Tests') {
                    steps {
                        script {
                            sh label: 'Create logs directory', script: 'mkdir -p logs build'
                            if (sh(label: 'Build lab-results', script: 'docker build -t local/lab-results-tests:${BUILD_TAG} -f Dockerfile.tests .', returnStatus: true) != 0) {error("Failed to build docker image for tests")}
                            if (sh(label: 'Running tests', script: 'docker run -v /var/run/docker.sock:/var/run/docker.sock --name lab-results-tests local/lab-results-tests:${BUILD_TAG} gradle check -x checkstyleInTest -x checkstyleMain -x checkstyleTest -x spotbugsIntTest -x spotbugsMain -x spotbugsTest -i', returnStatus: true) != 0) {error("Some tests failed, check the logs")}
                        }
                    }
                    post {
                        always {
                            sh "docker cp lab-results-tests:/home/gradle/src/build ."
                            junit '**/build/test-results/**/*.xml'
                            step([
                                $class : 'JacocoPublisher',
                                execPattern : '**/build/jacoco/*.exec',
                                classPattern : '**/build/classes/java',
                                sourcePattern : 'src/main/java',
                                exclusionPattern : '**/*Test.class'
                            ])
                            sh "rm -rf build"
                            sh "docker stop lab-results-tests"

                        }
                    }
                }
                stage('Build Docker Images') {
                    steps {
                        script {
                            sh label: 'Running docker build', script: 'docker build -t ${DOCKER_IMAGE} .'
                        }
                    }
                }
                stage('Push image') {
                    when {
                        expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
                    }
                    steps {
                        script {
                            if (ecrLogin(TF_STATE_BUCKET_REGION) != 0 )  { error("Docker login to ECR failed") }
                            String dockerPushCommand = "docker push ${DOCKER_IMAGE}"
                            if (sh (label: "Pushing image", script: dockerPushCommand, returnStatus: true) !=0) { error("Docker push image failed") }
                        }
                    }
                }
            }
            post {
                always {
                    sh label: 'Copy lab-results container logs', script: 'docker-compose logs lab-results > logs/lab-results.log'
                    sh label: 'Copy activemq logs', script: 'docker-compose logs activemq > logs/inbound.log'
                    archiveArtifacts artifacts: 'logs/*.log', fingerprint: true
                    sh label: 'Stopping containers', script: 'docker-compose down -v'
                }
            }
        }


    }
    post {
        always {
            sh label: 'Stopping containers', script: 'docker-compose down -v'
            sh label: 'Remove all unused images not just dangling ones', script:'docker system prune --force'
            sh 'docker image rm -f $(docker images "*/*:*${BUILD_TAG}" -q) $(docker images "*/*/*:*${BUILD_TAG}" -q) || true'
        }
    }
}

String tfEnv(String tfEnvRepo="https://github.com/tfutils/tfenv.git", String tfEnvPath="~/.tfenv") {
  sh(label: "Get tfenv" ,  script: "git clone ${tfEnvRepo} ${tfEnvPath}", returnStatus: true)
  sh(label: "Install TF",  script: "${tfEnvPath}/bin/tfenv install"     , returnStatus: true)
  return "${tfEnvPath}/bin/terraform"
}

int terraformInit(String tfStateBucket, String project, String environment, String component, String region) {
  String terraformBinPath = tfEnv()
  println("Terraform Init for Environment: ${environment} Component: ${component} in region: ${region} using bucket: ${tfStateBucket}")
  String command = "${terraformBinPath} init -backend-config='bucket=${tfStateBucket}' -backend-config='region=${region}' -backend-config='key=${project}-${environment}-${component}.tfstate' -input=false -no-color"
  dir("components/${component}") {
    return( sh( label: "Terraform Init", script: command, returnStatus: true))
  } // dir
} // int TerraformInit

int terraform(String action, String tfStateBucket, String project, String environment, String component, String region, Map<String, String> variables=[:], List<String> parameters=[]) {
    println("Running Terraform ${action} in region ${region} with: \n Project: ${project} \n Environment: ${environment} \n Component: ${component}")
    variablesMap = variables
    variablesMap.put('region',region)
    variablesMap.put('project', project)
    variablesMap.put('environment', environment)
    variablesMap.put('tf_state_bucket',tfStateBucket)
    parametersList = parameters
    parametersList.add("-no-color")

    // Get the secret variables for global
    String secretsFile = "etc/secrets.tfvars"
    writeVariablesToFile(secretsFile,getAllSecretsForEnvironment(environment,"nia",region))
    String terraformBinPath = tfEnv()
    List<String> variableFilesList = [
      "-var-file=../../etc/global.tfvars",
      "-var-file=../../etc/${region}_${environment}.tfvars",
      "-var-file=../../${secretsFile}"
    ]
    if (action == "apply"|| action == "destroy") {parametersList.add("-auto-approve")}
    List<String> variablesList=variablesMap.collect { key, value -> "-var ${key}=${value}" }
    String command = "${terraformBinPath} ${action} ${variableFilesList.join(" ")} ${parametersList.join(" ")} ${variablesList.join(" ")} "
    dir("components/${component}") {
      return sh(label:"Terraform: "+action, script: command, returnStatus: true)
    } // dir
} // int Terraform

Map<String,String> collectTfOutputs(String component) {
  Map<String,String> returnMap = [:]
  dir("components/${component}") {
    String terraformBinPath = tfEnv()
    List<String> outputsList = sh (label: "Listing TF outputs", script: "${terraformBinPath} output", returnStdout: true).split("\n")
    outputsList.each {
      returnMap.put(it.split("=")[0].trim(),it.split("=")[1].trim())
    }
  } // dir
  return returnMap
}

int ecrLogin(String aws_region) {
    String dockerLogin = "aws ecr get-login-password --region ${aws_region} | docker login -u AWS --password-stdin \"https://\$(aws sts get-caller-identity --query 'Account' --output text).dkr.ecr.${aws_region}.amazonaws.com\""
    return sh(label: "Logging in with Docker", script: dockerLogin, returnStatus: true)
}

// Retrieving Secrets from AWS Secrets
String getSecretValue(String secretName, String region) {
  String awsCommand = "aws secretsmanager get-secret-value --region ${region} --secret-id ${secretName} --query SecretString --output text"
  return sh(script: awsCommand, returnStdout: true).trim()
}

Map<String,Object> decodeSecretKeyValue(String rawSecret) {
  List<String> secretsSplit = rawSecret.replace("{","").replace("}","").split(",")
  Map<String,Object> secretsDecoded = [:]
  secretsSplit.each {
    String key = it.split(":")[0].trim().replace("\"","")
    Object value = it.split(":")[1]
    secretsDecoded.put(key,value)
  }
  return secretsDecoded
}

List<String> getSecretsByPrefix(String prefix, String region) {
  String awsCommand = "aws secretsmanager list-secrets --region ${region} --query SecretList[].Name --output text"
  List<String> awsReturnValue = sh(script: awsCommand, returnStdout: true).split()
  return awsReturnValue.findAll { it.startsWith(prefix) }
}

Map<String,Object> getAllSecretsForEnvironment(String environment, String secretsPrefix, String region) {
  List<String> globalSecrets = getSecretsByPrefix("${secretsPrefix}-global",region)
  println "global secrets:" + globalSecrets
  List<String> environmentSecrets = getSecretsByPrefix("${secretsPrefix}-${environment}",region)
  println "env secrets:" + environmentSecrets
  Map<String,Object> secretsMerged = [:]
  globalSecrets.each {
    String rawSecret = getSecretValue(it,region)
    if (it.contains("-kvp")) {
      secretsMerged << decodeSecretKeyValue(rawSecret)
    } else {
      secretsMerged.put(it.replace("${secretsPrefix}-global-",""),rawSecret)
    }
  }
  environmentSecrets.each {
    String rawSecret = getSecretValue(it,region)
    if (it.contains("-kvp")) {
      secretsMerged << decodeSecretKeyValue(rawSecret)
    } else {
      secretsMerged.put(it.replace("${secretsPrefix}-${environment}-",""),rawSecret)
    }
  }
  return secretsMerged
}

void writeVariablesToFile(String fileName, Map<String,Object> variablesMap) {
  List<String> variablesList=variablesMap.collect { key, value -> "${key} = ${value}" }
  sh (script: "touch ${fileName} && echo '\n' > ${fileName}")
  variablesList.each {
    sh (script: "echo '${it}' >> ${fileName}")
  }
}
