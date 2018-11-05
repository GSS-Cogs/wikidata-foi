pipeline {
    agent {
        docker { image 'clojure:lein' }
    }
    stages {
        stage('Create or replace Draft') {
            steps {
                script {
                    jobDraft.replace()
                }
            }
        }
        stage('Build FOI data') {
            steps {
                sh 'lein run test/resources/eg-wikidata.csv out/cord-foi.nq'
            }
        }
        stage('Upload FOI data') {
            steps {
                script {
                    def draft = jobDraft.find()
                    configFileProvider([configFile(fileId: 'pmd', variable: 'configfile')]) {
                        def config = readJSON(text: readFile(file: configfile))
                        String PMD = config['pmd_api']
                        String credentials = config['credentials']
                        drafter.addData(PMD, credentials, draft.id, readFile("out/cord-foi.nq"), "application/n-quads;charset=UTF-8")
                    }
                }
            }
        }
        stage('Publish Draft') {
            steps {
                script {
                    jobDraft.publish()
                }
            }
        }
    }
}