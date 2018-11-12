pipeline {
    agent {
        docker {
            image 'clojure:lein-alpine'
            args '-u root:root'
        }

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
                // sh 'lein run resources/cord-geographies-wikidata.csv out/cord-foi.nq'
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
                        drafter.deleteGraph(PMD, credentials, draft.id, "http://gss-data.org.uk/graph/gdp233")
                        drafter.deleteGraph(PMD, credentials, draft.id, "http://gss-data.org.uk/graph/cord-geography-foi")
                        drafter.addData(PMD, credentials, draft.id, readFile("out/cord-foi.nq"), "application/n-quads;charset=UTF-8")
                        drafter.addData(PMD, credentials, draft.id, readFile("resources/foi.trig"), "application/trig;charset=UTF-8")
                        drafter.addData(PMD, credentials, draft.id, readFile("resources/world.nq"), "application/n-quads;charset=UTF-8")
                        // traverse within ancestor chain and add to draft set
                        ancestorChain = drafter.queryDraftset(PMD, credentials, draft.id, readFile("resources/construct-within.sparql"), "application/n-triples;charset=UTF-8")
                        drafter.addData(PMD, credentials, draft.id, ancestorChain, "application/n-triples;charset=UTF-8", "http://gss-data.org.uk/graph/cord-geography-foi")
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
